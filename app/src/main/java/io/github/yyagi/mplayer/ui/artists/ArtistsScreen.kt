package io.github.yyagi.mplayer.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.yyagi.mplayer.ui.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    viewModel: LibraryViewModel,
    onArtistClick: (String) -> Unit = {},
) {
    val songs by viewModel.songs.collectAsState()
    val artists = songs.groupBy { it.artist }
        .mapValues { it.value.size }
        .toSortedMap()

    Scaffold(
        topBar = { TopAppBar(title = { Text("アーティスト") }) },
    ) { innerPadding ->
        if (artists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text(
                    "曲が見つかりません",
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(artists.entries.toList(), key = { it.key }) { (artist, songCount) ->
                    ListItem(
                        headlineContent = { Text(artist) },
                        supportingContent = { Text("$songCount 曲") },
                        modifier = Modifier.clickable { onArtistClick(artist) },
                    )
                }
            }
        }
    }
}
