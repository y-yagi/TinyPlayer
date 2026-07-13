package io.github.yyagi.tinyplayer.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import io.github.yyagi.tinyplayer.ui.components.AlbumArtThumbnail

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlbumArtThumbnail(
                uri = state.albumArtUri,
                size = 240.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Text(
                state.title.ifEmpty { "再生していません" },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(state.artist, style = MaterialTheme.typography.bodyMedium)

            Slider(
                value = if (isDragging) dragPositionMs else state.positionMs.toFloat(),
                onValueChange = {
                    isDragging = true
                    dragPositionMs = it
                },
                onValueChangeFinished = {
                    viewModel.seekTo(dragPositionMs.toLong())
                    isDragging = false
                },
                valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatDurationMs(if (isDragging) dragPositionMs.toLong() else state.positionMs),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(formatDurationMs(state.durationMs), style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.seekBy(-60_000) }) {
                    SeekIcon(Icons.Filled.Replay, seconds = 60, contentDescription = "1分戻る")
                }
                IconButton(onClick = { viewModel.seekBy(-10_000) }) {
                    Icon(Icons.Filled.Replay10, contentDescription = "10秒戻る")
                }
                IconButton(onClick = { viewModel.seekToPrevious() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "前へ")
                }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) "一時停止" else "再生",
                    )
                }
                IconButton(onClick = { viewModel.seekToNext() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "次へ")
                }
                IconButton(onClick = { viewModel.seekBy(10_000) }) {
                    Icon(Icons.Filled.Forward10, contentDescription = "10秒進む")
                }
                IconButton(onClick = { viewModel.seekBy(60_000) }) {
                    SeekIcon(Icons.Filled.Replay, seconds = 60, contentDescription = "1分進む", mirror = true)
                }
            }

            IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                val tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
                Icon(
                    if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "リピート",
                    tint = tint,
                )
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun SeekIcon(base: ImageVector, seconds: Int, contentDescription: String, mirror: Boolean = false) {
    Box(
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            base,
            contentDescription = null,
            modifier = if (mirror) Modifier.scale(scaleX = -1f, scaleY = 1f) else Modifier,
        )
        Text(text = seconds.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
