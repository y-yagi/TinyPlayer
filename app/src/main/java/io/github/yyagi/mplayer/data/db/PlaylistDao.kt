package io.github.yyagi.mplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_song_cross_ref WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeCrossRefs(playlistId: Long): Flow<List<PlaylistSongCrossRef>>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Insert
    suspend fun insertCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteCrossRef(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_song_cross_ref WHERE songId = :songId")
    suspend fun deleteCrossRefsForSong(songId: Long)

    @Update
    suspend fun updateCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Transaction
    suspend fun addSong(playlistId: Long, songId: Long) {
        val nextPosition = maxPosition(playlistId) + 1
        insertCrossRef(PlaylistSongCrossRef(playlistId = playlistId, songId = songId, position = nextPosition))
    }

    @Transaction
    suspend fun reorder(crossRefs: List<PlaylistSongCrossRef>) {
        updateCrossRefs(crossRefs)
    }
}
