package io.github.yyagi.mplayer.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.yyagi.mplayer.data.song.Song
import io.github.yyagi.mplayer.ui.components.AddToPlaylistDialog
import io.github.yyagi.mplayer.ui.permission.PermissionGate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var addToPlaylistSong by remember { mutableStateOf<Song?>(null) }

    PermissionGate(onGranted = { viewModel.refresh() }) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("ライブラリ") }) },
        ) { innerPadding ->
            if (songs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    Text(
                        "曲が見つかりません",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artist) },
                            modifier = Modifier.clickable { onSongClick(songs, index) },
                            trailingContent = {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "メニュー")
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("プレイリストに追加") },
                                            onClick = {
                                                menuExpanded = false
                                                addToPlaylistSong = song
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    addToPlaylistSong?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { addToPlaylistSong = null },
            onSelect = { playlist -> viewModel.addSongToPlaylist(song.id, playlist) },
            onCreateNew = { name -> viewModel.createPlaylistAndAddSong(name, song.id) },
        )
    }
}
