package io.github.yyagi.mplayer.data.db

import io.github.yyagi.mplayer.data.song.Song
import io.github.yyagi.mplayer.data.song.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PlaylistItem(
    val crossRefId: Long,
    val position: Int,
    val song: Song,
)

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val songRepository: SongRepository,
) {
    val playlists: Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()

    suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun renamePlaylist(playlist: PlaylistEntity, newName: String) {
        playlistDao.updatePlaylist(playlist.copy(name = newName))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSong(playlistId: Long, songId: Long) {
        playlistDao.addSong(playlistId, songId)
    }

    suspend fun removeSong(playlistId: Long, songId: Long) {
        playlistDao.deleteCrossRef(playlistId, songId)
    }

    suspend fun reorder(crossRefs: List<PlaylistSongCrossRef>) {
        playlistDao.reorder(crossRefs)
    }

    fun observeSongs(playlistId: Long): Flow<List<Song>> =
        combine(playlistDao.observeCrossRefs(playlistId), songRepository.songs) { crossRefs, songs ->
            val songsById = songs.associateBy { it.id }
            crossRefs.sortedBy { it.position }.mapNotNull { songsById[it.songId] }
        }

    fun observePlaylistItems(playlistId: Long): Flow<List<PlaylistItem>> =
        combine(playlistDao.observeCrossRefs(playlistId), songRepository.songs) { crossRefs, songs ->
            val songsById = songs.associateBy { it.id }
            crossRefs.sortedBy { it.position }.mapNotNull { ref ->
                songsById[ref.songId]?.let { song -> PlaylistItem(ref.id, ref.position, song) }
            }
        }
}
