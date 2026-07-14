package io.github.yyagi.tinyplayer.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yyagi.tinyplayer.data.db.PlaylistItem
import io.github.yyagi.tinyplayer.data.song.Song
import io.github.yyagi.tinyplayer.ui.components.AlbumArtThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    viewModel: PlaylistDetailViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val items by viewModel.items.collectAsState()
    var removeTarget by remember { mutableStateOf<PlaylistItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlistName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(bottom = 12.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        "曲がありません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            val songs = items.map { it.song }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.crossRefId }) { index, item ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.clickable { onSongClick(songs, index) },
                    ) {
                        ListItem(
                            headlineContent = { Text(item.song.title) },
                            supportingContent = { Text(item.song.artist) },
                            leadingContent = {
                                AlbumArtThumbnail(
                                    uri = item.song.albumArtUri,
                                    size = 56.dp,
                                    shape = MaterialTheme.shapes.small,
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { viewModel.moveUp(index) }, enabled = index > 0) {
                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "上へ")
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveDown(index) },
                                        enabled = index < items.lastIndex,
                                    ) {
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "下へ")
                                    }
                                    IconButton(onClick = { removeTarget = item }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "削除")
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        )
                    }
                }
            }
        }
    }

    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("曲を削除") },
            text = { Text("「${target.song.title}」をプレイリストから削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeSong(target.song.id)
                    removeTarget = null
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) {
                    Text("キャンセル")
                }
            },
        )
    }
}
