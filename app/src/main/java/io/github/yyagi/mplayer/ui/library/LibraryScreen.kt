package io.github.yyagi.mplayer.ui.library

import android.app.Activity
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
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
    var deleteCandidate by remember { mutableStateOf<Song?>(null) }
    val context = LocalContext.current

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            deleteCandidate?.let { viewModel.onSongDeleted(it.id) }
        }
        deleteCandidate = null
    }

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
                                        DropdownMenuItem(
                                            text = { Text("削除") },
                                            onClick = {
                                                menuExpanded = false
                                                deleteCandidate = song
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Delete, contentDescription = null)
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

    deleteCandidate?.let { song ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("曲を削除") },
            text = { Text("「${song.title}」を端末から削除しますか？この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = {
                    val deleteRequest = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        listOf(song.contentUri),
                    )
                    deleteLauncher.launch(IntentSenderRequest.Builder(deleteRequest.intentSender).build())
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("キャンセル")
                }
            },
        )
    }
}
