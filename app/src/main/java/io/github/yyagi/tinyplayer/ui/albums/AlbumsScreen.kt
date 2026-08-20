package io.github.yyagi.tinyplayer.ui.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.yyagi.tinyplayer.ui.components.AlbumArtThumbnail
import io.github.yyagi.tinyplayer.ui.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (String) -> Unit = {},
) {
    val songs by viewModel.songs.collectAsState()
    val albumGroups = songs.groupBy { it.album }.toSortedMap()
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("アルバム") }) },
    ) { innerPadding ->
        if (albumGroups.isEmpty()) {
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
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(albumGroups.entries.toList(), key = { it.key }) { (album, albumSongs) ->
                    ListItem(
                        headlineContent = {
                            Text(
                                album,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        supportingContent = {
                            Text(
                                "${albumSongs.size} 曲",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingContent = {
                            AlbumArtThumbnail(
                                uri = albumSongs.first().albumArtUri,
                                size = 48.dp,
                                shape = MaterialTheme.shapes.small,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth().clickable { onAlbumClick(album) },
                    )
                }
            }
        }
    }
}
