package io.github.yyagi.tinyplayer.ui.navigation

import android.net.Uri

object Destinations {
    const val LIBRARY = "library"
    const val ARTISTS = "artists"
    const val ARTIST_DETAIL = "artist_detail/{artist}"
    const val ALBUMS = "albums"
    const val ALBUM_DETAIL = "album_detail/{album}"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val NOW_PLAYING = "now_playing"

    fun playlistDetail(playlistId: Long) = "playlist_detail/$playlistId"
    fun artistDetail(artist: String) = "artist_detail/${Uri.encode(artist)}"
    fun albumDetail(album: String) = "album_detail/${Uri.encode(album)}"
}
