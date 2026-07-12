package io.github.yyagi.mplayer.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.yyagi.mplayer.data.db.PlaylistEntity
import io.github.yyagi.mplayer.data.db.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    val playlists: StateFlow<List<PlaylistEntity>> = playlistRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }

    fun renamePlaylist(playlist: PlaylistEntity, newName: String) {
        viewModelScope.launch { playlistRepository.renamePlaylist(playlist, newName) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }

    companion object {
        fun factory(playlistRepository: PlaylistRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaylistsViewModel(playlistRepository) as T
                }
            }
    }
}
