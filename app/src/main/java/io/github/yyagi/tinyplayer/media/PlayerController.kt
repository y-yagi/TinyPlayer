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
import kotlinx.coroutines.withTimeoutOrNull

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

private const val MAX_REMEMBERED_POSITIONS = 5
private const val POSITION_SAVE_INTERVAL_TICKS = 10
private const val RESTORE_TIMEOUT_MS = 10_000L

class PlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val songRepository: SongRepository,
    private val playbackStateStore: PlaybackStateStore = PlaybackStateStore(context),
) {
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null
    private var sleepTimerJob: Job? = null

    private val lastPositions = object : LinkedHashMap<Long, Long>(MAX_REMEMBERED_POSITIONS, 0.75f, false) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Long>) =
            size > MAX_REMEMBERED_POSITIONS
    }

    init {
        scope.launch {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val mediaController = MediaController.Builder(context, token).buildAsync().await()
            controller = mediaController
            attachListener(mediaController)
            restoreLastPlayback(mediaController)
        }
    }

    private fun attachListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (!isPlaying) {
                    _uiState.value.currentSongId?.let { id ->
                        playbackStateStore.save(id, controller.currentPosition)
                    }
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                _uiState.update {
                    it.copy(
                        title = mediaMetadata.title?.toString().orEmpty(),
                        artist = mediaMetadata.artist?.toString().orEmpty(),
                        albumArtUri = mediaMetadata.artworkUri,
                        durationMs = controller.duration.takeIf { d -> d != C.TIME_UNSET } ?: 0L,
                    )
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _uiState.update {
                    it.copy(
                        currentSongId = mediaItem?.mediaId?.toLongOrNull(),
                        positionMs = controller.currentPosition,
                    )
                }
                mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                    playbackStateStore.save(id, controller.currentPosition)
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _uiState.update { it.copy(repeatMode = repeatMode) }
            }
        })
        _uiState.update {
            it.copy(
                currentSongId = controller.currentMediaItem?.mediaId?.toLongOrNull(),
                title = controller.mediaMetadata.title?.toString().orEmpty(),
                artist = controller.mediaMetadata.artist?.toString().orEmpty(),
                albumArtUri = controller.mediaMetadata.artworkUri,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition,
                durationMs = controller.duration.takeIf { d -> d != C.TIME_UNSET } ?: 0L,
                repeatMode = controller.repeatMode,
            )
        }

        scope.launch {
            var tick = 0
            while (true) {
                _uiState.update {
                    it.copy(
                        positionMs = controller.currentPosition,
                        durationMs = controller.duration.takeIf { d -> d != C.TIME_UNSET } ?: it.durationMs,
                    )
                }
                tick++
                if (tick % POSITION_SAVE_INTERVAL_TICKS == 0) {
                    _uiState.value.currentSongId?.let { id ->
                        playbackStateStore.save(id, controller.currentPosition)
                    }
                }
                delay(500)
            }
        }
    }

    private suspend fun restoreLastPlayback(controller: MediaController) {
        val saved = playbackStateStore.load() ?: return
        val songs = withTimeoutOrNull(RESTORE_TIMEOUT_MS) { songRepository.songs.first { it.isNotEmpty() } } ?: return
        val song = songs.firstOrNull { it.id == saved.songId } ?: return
        controller.setMediaItem(song.toMediaItem(), saved.positionMs)
        controller.prepare()
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        val controller = controller ?: return
        _uiState.value.currentSongId?.let { previousId ->
            lastPositions[previousId] = controller.currentPosition
        }
        val targetSongId = songs.getOrNull(startIndex)?.id
        val resumePositionMs = targetSongId?.let { lastPositions[it] } ?: 0L
        controller.setMediaItems(songs.map { it.toMediaItem() }, startIndex, resumePositionMs)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
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
        controller.seekTo(positionMs.coerceIn(0L, duration))
    }

    fun seekBy(deltaMs: Long) {
        val controller = controller ?: return
        val duration = controller.duration.takeIf { it != C.TIME_UNSET } ?: return
        val target = (controller.currentPosition + deltaMs).coerceIn(0L, duration)
        controller.seekTo(target)
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
        val controller = controller ?: return
        sleepTimerJob?.cancel()
        val endTime = System.currentTimeMillis() + durationMs
        _uiState.update { it.copy(sleepTimerEndTimeMs = endTime) }
        sleepTimerJob = scope.launch {
            delay(durationMs)
            controller.pause()
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
