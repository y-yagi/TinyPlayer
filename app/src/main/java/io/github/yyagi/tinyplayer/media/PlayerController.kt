package io.github.yyagi.tinyplayer.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.yyagi.tinyplayer.data.song.Song
import io.github.yyagi.tinyplayer.data.song.SongRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentSongId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val albumArtUri: Uri? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val sleepTimerEndTimeMs: Long? = null,
)

private const val MAX_REMEMBERED_POSITIONS = 10
private const val POSITION_SAVE_INTERVAL_TICKS = 10
private const val RECONNECT_DELAY_MS = 1_000L
private const val RESUME_REWIND_MS = 5_000L
private const val RESTORE_TOLERANCE_MS = 3_000L

class PlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val songRepository: SongRepository,
    private val playbackStateStore: PlaybackStateStore = PlaybackStateStore(context),
) {
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null
    private var pollingJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var pendingRestore: SavedPlaybackState? = null

    private val lastPositions = object : LinkedHashMap<Long, Long>(MAX_REMEMBERED_POSITIONS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Long>) =
            size > MAX_REMEMBERED_POSITIONS
    }

    init {
        lastPositions.putAll(playbackStateStore.loadRecentPositions())
        connect()
    }

    private fun connect() {
        scope.launch {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val mediaController = try {
                MediaController.Builder(context, token)
                    .setListener(object : MediaController.Listener {
                        override fun onDisconnected(controller: MediaController) {
                            handleDisconnected(controller)
                        }
                    })
                    .buildAsync()
                    .await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                delay(RECONNECT_DELAY_MS)
                connect()
                return@launch
            }
            controller = mediaController
            attachListener(mediaController)
            restoreLastPlayback(mediaController)
        }
    }

    /**
     * A disconnected MediaController reports position 0 and empty metadata, so keeping it around
     * would overwrite the saved position with 0 and blank out the Now Playing screen. Drop it and
     * reconnect; the restore step then reloads the saved song if the service lost its queue.
     */
    private fun handleDisconnected(disconnected: MediaController) {
        if (controller !== disconnected) return
        controller = null
        pollingJob?.cancel()
        pollingJob = null
        disconnected.release()
        _uiState.update { it.copy(isPlaying = false) }
        connect()
    }

    private fun currentSongId(controller: MediaController): Long? =
        controller.currentMediaItem?.mediaId?.toLongOrNull()

    private fun saveCurrentState(controller: MediaController) {
        if (!controller.isConnected) return
        val id = currentSongId(controller) ?: return
        if (!shouldSavePosition(controller, id)) return
        playbackStateStore.save(id, controller.currentPosition)
    }

    /**
     * After restoring from a cold start the player is re-prepared. Until it is ready its position is
     * not trustworthy, and if the source turned out to be unseekable it comes up at 0. Saving that
     * would destroy the remembered position, so hold off and retry the seek once instead.
     */
    private fun shouldSavePosition(controller: MediaController, songId: Long): Boolean {
        val pending = pendingRestore ?: return true
        if (pending.songId != songId) {
            pendingRestore = null
            return true
        }
        if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_BUFFERING) {
            return false
        }
        pendingRestore = null
        if (controller.currentPosition < pending.positionMs - RESTORE_TOLERANCE_MS) {
            controller.seekTo(pending.positionMs)
            return false
        }
        return true
    }

    /** Rebuilds the UI state from the controller's own snapshot so it can never drift from it. */
    private fun syncState(controller: MediaController) {
        if (!controller.isConnected) return
        val songId = currentSongId(controller)
        val metadata = controller.mediaMetadata
        val itemMetadata = controller.currentMediaItem?.mediaMetadata
        val song = songId?.let { songRepository.findById(it) }
        _uiState.update {
            it.copy(
                currentSongId = songId,
                title = (metadata.title ?: itemMetadata?.title)?.toString()?.ifEmpty { null }
                    ?: song?.title.orEmpty(),
                artist = (metadata.artist ?: itemMetadata?.artist)?.toString()?.ifEmpty { null }
                    ?: song?.artist.orEmpty(),
                albumArtUri = metadata.artworkUri ?: itemMetadata?.artworkUri ?: song?.albumArtUri,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition,
                durationMs = controller.duration.takeIf { d -> d != C.TIME_UNSET }
                    ?: if (songId == it.currentSongId) it.durationMs else 0L,
                repeatMode = controller.repeatMode,
            )
        }
    }

    private fun attachListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) saveCurrentState(controller)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                saveCurrentState(controller)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                syncState(controller)
            }
        })
        syncState(controller)

        pollingJob?.cancel()
        pollingJob = scope.launch {
            var tick = 0
            while (true) {
                syncState(controller)
                tick++
                if (tick % POSITION_SAVE_INTERVAL_TICKS == 0) {
                    saveCurrentState(controller)
                }
                delay(500)
            }
        }
    }

    private suspend fun restoreLastPlayback(controller: MediaController) {
        // After a reconnect the service may still hold the queue; never clobber it.
        if (controller.currentMediaItem != null) return
        val saved = playbackStateStore.load() ?: return
        val song = songRepository.findById(saved.songId)
            ?: songRepository.querySong(context, saved.songId)
            // MediaStore can't be read until the permission is granted; fall back to the library scan.
            ?: songRepository.songs.first { it.isNotEmpty() }.firstOrNull { it.id == saved.songId }
            ?: return
        // The user may have started something else while we were waiting.
        if (this.controller !== controller || controller.currentMediaItem != null) return
        pendingRestore = saved.takeIf { it.positionMs > 0L }
        controller.setMediaItem(song.toMediaItem(), saved.positionMs)
        controller.prepare()
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        val controller = controller ?: return
        pendingRestore = null
        currentSongId(controller)?.let { previousId ->
            lastPositions[previousId] = controller.currentPosition
            playbackStateStore.saveRecentPositions(lastPositions)
        }
        val targetSongId = songs.getOrNull(startIndex)?.id
        val resumePositionMs = targetSongId?.let { lastPositions[it] } ?: 0L
        controller.setMediaItems(songs.map { it.toMediaItem() }, startIndex, resumePositionMs)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            // Bluetooth output buffering can leave a short gap between the displayed
            // pause position and what was actually last heard, so rewind slightly on resume.
            controller.seekTo((controller.currentPosition - RESUME_REWIND_MS).coerceAtLeast(0L))
            controller.play()
        }
    }

    fun seekToNext() {
        controller?.seekToNext()
    }

    fun seekToPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        val controller = controller ?: return
        val duration = controller.duration.takeIf { it != C.TIME_UNSET } ?: return
        pendingRestore = null
        val target = positionMs.coerceIn(0L, duration)
        controller.seekTo(target)
        saveCurrentPosition(target)
    }

    fun seekBy(deltaMs: Long) {
        val controller = controller ?: return
        val duration = controller.duration.takeIf { it != C.TIME_UNSET } ?: return
        pendingRestore = null
        val target = (controller.currentPosition + deltaMs).coerceIn(0L, duration)
        controller.seekTo(target)
        saveCurrentPosition(target)
    }

    private fun saveCurrentPosition(positionMs: Long) {
        controller?.let(::currentSongId)?.let { id -> playbackStateStore.save(id, positionMs) }
    }

    fun cycleRepeatMode() {
        val controller = controller ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setSleepTimer(durationMs: Long) {
        if (controller == null) return
        sleepTimerJob?.cancel()
        val endTime = System.currentTimeMillis() + durationMs
        _uiState.update { it.copy(sleepTimerEndTimeMs = endTime) }
        sleepTimerJob = scope.launch {
            delay(durationMs)
            controller?.pause()
            _uiState.update { it.copy(sleepTimerEndTimeMs = null) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.update { it.copy(sleepTimerEndTimeMs = null) }
    }
}

private fun Song.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setArtworkUri(albumArtUri)
        .build()
    return MediaItem.Builder()
        .setUri(contentUri)
        .setMediaId(id.toString())
        .setMediaMetadata(metadata)
        .build()
}
