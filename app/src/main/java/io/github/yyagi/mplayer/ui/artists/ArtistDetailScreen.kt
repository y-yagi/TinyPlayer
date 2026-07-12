package io.github.yyagi.mplayer.ui.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.yyagi.mplayer.data.song.Song
import io.github.yyagi.mplayer.ui.components.SongListItem
import io.github.yyagi.mplayer.ui.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: String,
    viewModel: LibraryViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val artistSongs = songs.filter { it.artist == artist }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (artistSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text("曲が見つかりません", modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                itemsIndexed(artistSongs, key = { _, song -> song.id }) { index, song ->
                    SongListItem(
                        song = song,
                        subtitle = song.album,
                        playlists = playlists,
                        onClick = { onSongClick(artistSongs, index) },
                        onAddToPlaylist = { playlist -> viewModel.addSongToPlaylist(song.id, playlist) },
                        onCreatePlaylist = { name -> viewModel.createPlaylistAndAddSong(name, song.id) },
                        onDeleted = { viewModel.onSongDeleted(song.id) },
                    )
                }
            }
        }
    }
}
