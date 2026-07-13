package io.github.yyagi.tinyplayer.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.yyagi.tinyplayer.data.db.PlaylistItem
import io.github.yyagi.tinyplayer.data.db.PlaylistRepository
import io.github.yyagi.tinyplayer.data.db.PlaylistSongCrossRef
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    val items: StateFlow<List<PlaylistItem>> = playlistRepository.observePlaylistItems(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeSong(songId: Long) {
        viewModelScope.launch { playlistRepository.removeSong(playlistId, songId) }
    }

    fun moveUp(index: Int) = swap(index, index - 1)

    fun moveDown(index: Int) = swap(index, index + 1)

    private fun swap(indexA: Int, indexB: Int) {
        val current = items.value
        if (indexA !in current.indices || indexB !in current.indices) return
        val a = current[indexA]
        val b = current[indexB]
        viewModelScope.launch {
            playlistRepository.reorder(
                listOf(
                    PlaylistSongCrossRef(id = a.crossRefId, playlistId = playlistId, songId = a.song.id, position = b.position),
                    PlaylistSongCrossRef(id = b.crossRefId, playlistId = playlistId, songId = b.song.id, position = a.position),
                ),
            )
        }
    }

    companion object {
        fun factory(playlistId: Long, playlistRepository: PlaylistRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaylistDetailViewModel(playlistId, playlistRepository) as T
                }
            }
    }
}
