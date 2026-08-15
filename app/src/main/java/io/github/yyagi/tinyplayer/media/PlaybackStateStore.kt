package io.github.yyagi.tinyplayer.media

import android.content.Context

data class SavedPlaybackState(val songId: Long, val positionMs: Long)

class PlaybackStateStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(songId: Long, positionMs: Long) {
        prefs.edit().putLong(KEY_SONG_ID, songId).putLong(KEY_POSITION_MS, positionMs).apply()
    }

    fun load(): SavedPlaybackState? {
        val songId = prefs.getLong(KEY_SONG_ID, -1L)
        if (songId == -1L) return null
        return SavedPlaybackState(songId, prefs.getLong(KEY_POSITION_MS, 0L))
    }

    fun saveRecentPositions(positions: Map<Long, Long>) {
        val encoded = positions.entries.joinToString(",") { (songId, positionMs) -> "$songId:$positionMs" }
        prefs.edit().putString(KEY_RECENT_POSITIONS, encoded).apply()
    }

    fun loadRecentPositions(): LinkedHashMap<Long, Long> {
        val encoded = prefs.getString(KEY_RECENT_POSITIONS, null).orEmpty()
        val result = LinkedHashMap<Long, Long>()
        encoded.split(",").forEach { entry ->
            val (songIdPart, positionPart) = entry.split(":").takeIf { it.size == 2 } ?: return@forEach
            val songId = songIdPart.toLongOrNull() ?: return@forEach
            val positionMs = positionPart.toLongOrNull() ?: return@forEach
            result[songId] = positionMs
        }
        return result
    }

    companion object {
        private const val PREFS_NAME = "playback_state"
        private const val KEY_SONG_ID = "last_song_id"
        private const val KEY_POSITION_MS = "last_position_ms"
        private const val KEY_RECENT_POSITIONS = "recent_positions"
    }
}
