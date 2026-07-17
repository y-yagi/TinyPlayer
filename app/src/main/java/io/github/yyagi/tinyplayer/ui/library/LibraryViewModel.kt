package io.github.yyagi.tinyplayer.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.yyagi.tinyplayer.data.db.PlaylistEntity
import io.github.yyagi.tinyplayer.data.db.PlaylistRepository
import io.github.yyagi.tinyplayer.data.song.Song
import io.github.yyagi.tinyplayer.data.song.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val appContext: Context,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    val songs: StateFlow<List<Song>> = songRepository.songs
    val isLoading: StateFlow<Boolean> = songRepository.isLoading
    val playlists: StateFlow<List<PlaylistEntity>> = playlistRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            songRepository.refreshLibrary(appContext)
        }
    }

    fun addSongToPlaylist(songId: Long, playlist: PlaylistEntity) {
        viewModelScope.launch { playlistRepository.addSong(playlist.playlistId, songId) }
    }

    fun createPlaylistAndAddSong(name: String, songId: Long) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            playlistRepository.addSong(playlistId, songId)
        }
    }

    fun onSongDeleted(songId: Long) {
        songRepository.removeFromCache(songId)
        viewModelScope.launch { playlistRepository.removeSongEverywhere(songId) }
    }

    companion object {
        fun factory(context: Context, songRepository: SongRepository, playlistRepository: PlaylistRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(context.applicationContext, songRepository, playlistRepository) as T
                }
            }
    }
}
