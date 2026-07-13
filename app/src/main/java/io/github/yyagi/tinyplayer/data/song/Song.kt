package io.github.yyagi.tinyplayer.data.song

import android.net.Uri

data class Song(
    val id: Long,
    val contentUri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArtUri: Uri?,
    val fileName: String,
)
