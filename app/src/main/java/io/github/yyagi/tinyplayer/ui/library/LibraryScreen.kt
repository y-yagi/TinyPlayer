package io.github.yyagi.tinyplayer.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yyagi.tinyplayer.data.song.Song
import io.github.yyagi.tinyplayer.ui.components.SongListItem
import io.github.yyagi.tinyplayer.ui.permission.PermissionGate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    PermissionGate(onGranted = { viewModel.refresh() }) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("ライブラリ") }) },
        ) { innerPadding ->
            if (songs.isEmpty()) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongListItem(
                            song = song,
                            playlists = playlists,
                            onClick = { onSongClick(songs, index) },
                            onAddToPlaylist = { playlist -> viewModel.addSongToPlaylist(song.id, playlist) },
                            onCreatePlaylist = { name -> viewModel.createPlaylistAndAddSong(name, song.id) },
                            onDeleted = { viewModel.onSongDeleted(song.id) },
                        )
                    }
                }
            }
        }
    }
}
