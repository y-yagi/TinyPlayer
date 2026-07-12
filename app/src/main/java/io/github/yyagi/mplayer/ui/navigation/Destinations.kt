package io.github.yyagi.mplayer.ui.navigation

object Destinations {
    const val LIBRARY = "library"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val NOW_PLAYING = "now_playing"

    fun playlistDetail(playlistId: Long) = "playlist_detail/$playlistId"
}
