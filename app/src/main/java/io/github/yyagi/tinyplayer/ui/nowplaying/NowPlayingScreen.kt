package io.github.yyagi.tinyplayer.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import io.github.yyagi.tinyplayer.ui.components.AlbumArtThumbnail
import io.github.yyagi.tinyplayer.ui.components.SleepTimerDialog

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.background),
            ),
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showSleepTimerDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                    ) {
                        Icon(
                            Icons.Filled.Bedtime,
                            contentDescription = "スリープタイマー",
                            tint = if (state.sleepTimerEndTimeMs != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    state.sleepTimerEndTimeMs?.let { endTime ->
                        Text(
                            formatRemainingMinutes(endTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AlbumArtThumbnail(
                    uri = state.albumArtUri,
                    size = 280.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .shadow(
                            elevation = 24.dp,
                            shape = MaterialTheme.shapes.large,
                            ambientColor = MaterialTheme.colorScheme.primary,
                            spotColor = MaterialTheme.colorScheme.primary,
                        )
                        .padding(bottom = 24.dp),
                )

                Text(
                    state.title.ifEmpty { "再生していません" },
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    state.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

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
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        formatDurationMs(if (isDragging) dragPositionMs.toLong() else state.positionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatDurationMs(state.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { viewModel.seekBy(-60_000) }) {
                        SeekIcon(Icons.Filled.Replay, seconds = 60, contentDescription = "1分戻る")
                    }
                    IconButton(onClick = { viewModel.seekBy(-10_000) }) {
                        Icon(
                            Icons.Filled.Replay10,
                            contentDescription = "10秒戻る",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.seekToPrevious() }) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "前へ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "一時停止" else "再生",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    IconButton(onClick = { viewModel.seekToNext() }) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "次へ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.seekBy(10_000) }) {
                        Icon(
                            Icons.Filled.Forward10,
                            contentDescription = "10秒進む",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.seekBy(60_000) }) {
                        SeekIcon(Icons.Filled.Replay, seconds = 60, contentDescription = "1分進む", mirror = true)
                    }
                }

                IconToggleButton(
                    checked = state.repeatMode != Player.REPEAT_MODE_OFF,
                    onCheckedChange = { viewModel.cycleRepeatMode() },
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "リピート",
                    )
                }
            }
        }

        if (showSleepTimerDialog) {
            SleepTimerDialog(
                isActive = state.sleepTimerEndTimeMs != null,
                onDismiss = { showSleepTimerDialog = false },
                onSelect = { durationMs -> viewModel.setSleepTimer(durationMs) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
            )
        }
    }
}

private fun formatRemainingMinutes(endTimeMs: Long): String {
    val remainingMs = (endTimeMs - System.currentTimeMillis()).coerceAtLeast(0)
    val minutes = (remainingMs + 59_999) / 60_000
    return "残り${minutes}分"
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = if (mirror) Modifier.scale(scaleX = -1f, scaleY = 1f) else Modifier,
        )
        Text(text = seconds.toString(), style = MaterialTheme.typography.labelSmall)
    }
}
