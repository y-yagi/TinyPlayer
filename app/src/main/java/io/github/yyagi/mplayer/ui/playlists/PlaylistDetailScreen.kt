package io.github.yyagi.mplayer.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yyagi.mplayer.data.song.Song
import io.github.yyagi.mplayer.ui.components.AlbumArtThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    viewModel: PlaylistDetailViewModel,
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val items by viewModel.items.collectAsState()

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
                Text("曲がありません", modifier = Modifier.align(Alignment.Center))
            }
        } else {
            val songs = items.map { it.song }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                itemsIndexed(items, key = { _, item -> item.crossRefId }) { index, item ->
                    ListItem(
                        headlineContent = { Text(item.song.title) },
                        supportingContent = { Text(item.song.artist) },
                        leadingContent = { AlbumArtThumbnail(uri = item.song.albumArtUri, size = 48.dp) },
                        modifier = Modifier.clickable { onSongClick(songs, index) },
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
                                IconButton(onClick = { viewModel.removeSong(item.song.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "削除")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
