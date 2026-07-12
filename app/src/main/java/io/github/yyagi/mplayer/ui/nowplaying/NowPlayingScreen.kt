package io.github.yyagi.mplayer.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import io.github.yyagi.mplayer.ui.components.AlbumArtThumbnail

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

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
                value = state.positionMs.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.seekBy(-30_000) }) {
                    Icon(Icons.Filled.FastRewind, contentDescription = "30秒戻る")
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
                IconButton(onClick = { viewModel.seekBy(30_000) }) {
                    Icon(Icons.Filled.FastForward, contentDescription = "30秒進む")
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
