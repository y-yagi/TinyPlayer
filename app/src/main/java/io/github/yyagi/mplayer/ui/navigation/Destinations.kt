package io.github.yyagi.mplayer.ui.navigation

import android.net.Uri

object Destinations {
    const val LIBRARY = "library"
    const val ARTISTS = "artists"
    const val ARTIST_DETAIL = "artist_detail/{artist}"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val NOW_PLAYING = "now_playing"

    fun playlistDetail(playlistId: Long) = "playlist_detail/$playlistId"
    fun artistDetail(artist: String) = "artist_detail/${Uri.encode(artist)}"
}
