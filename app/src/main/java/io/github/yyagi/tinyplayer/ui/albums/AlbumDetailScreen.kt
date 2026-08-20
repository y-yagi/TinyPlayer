package io.github.yyagi.tinyplayer.ui.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yyagi.tinyplayer.data.song.Song
import io.github.yyagi.tinyplayer.ui.components.SongListItem
import io.github.yyagi.tinyplayer.ui.library.LibraryViewModel
import io.github.yyagi.tinyplayer.ui.util.rememberScrollClickGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: String,
    viewModel: LibraryViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    currentSongId: Long? = null,
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val albumSongs = songs.filter { it.album == album }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (albumSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(bottom = 12.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        "曲が見つかりません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            val allowClick = rememberScrollClickGuard(listState)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(albumSongs, key = { _, song -> song.id }) { index, song ->
                    SongListItem(
                        song = song,
                        subtitle = song.artist,
                        playlists = playlists,
                        onClick = { if (allowClick()) onSongClick(albumSongs, index) },
                        onAddToPlaylist = { playlist -> viewModel.addSongToPlaylist(song.id, playlist) },
                        onCreatePlaylist = { name -> viewModel.createPlaylistAndAddSong(name, song.id) },
                        onDeleted = { viewModel.onSongDeleted(song.id) },
                        isCurrentlyPlaying = song.id == currentSongId,
                    )
                }
            }
        }
    }
}
