package io.github.yyagi.tinyplayer.ui.util

fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

/**
 * Second line of a song list row, e.g. "3:45 アーティスト名". MediaStore does not always report a
 * duration, so fall back to the subtitle alone rather than showing a misleading "0:00".
 */
fun subtitleWithDuration(durationMs: Long, subtitle: String): String =
    if (durationMs > 0) "${formatDurationMs(durationMs)} $subtitle" else subtitle
