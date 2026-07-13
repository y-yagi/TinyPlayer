package io.github.yyagi.tinyplayer.ui.components

import android.app.Activity
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.yyagi.tinyplayer.data.db.PlaylistEntity
import io.github.yyagi.tinyplayer.data.song.Song

@Composable
fun SongListItem(
    song: Song,
    playlists: List<PlaylistEntity>,
    onClick: () -> Unit,
    onAddToPlaylist: (PlaylistEntity) -> Unit,
    onCreatePlaylist: (name: String) -> Unit,
    onDeleted: () -> Unit,
    subtitle: String = song.artist,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onDeleted()
        }
    }

    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { AlbumArtThumbnail(uri = song.albumArtUri) },
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
                            showAddToPlaylistDialog = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("削除") },
                        onClick = {
                            menuExpanded = false
                            showDeleteConfirm = true
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        },
                    )
                }
            }
        },
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onSelect = onAddToPlaylist,
            onCreateNew = onCreatePlaylist,
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("曲を削除") },
            text = { Text("「${song.title}」を端末から削除しますか？この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
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
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("キャンセル")
                }
            },
        )
    }
}
