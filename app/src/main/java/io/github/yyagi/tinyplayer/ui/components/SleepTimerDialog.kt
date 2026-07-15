package io.github.yyagi.tinyplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private val SLEEP_TIMER_OPTIONS_MINUTES = listOf(15, 30, 45, 60, 90)

@Composable
fun SleepTimerDialog(
    isActive: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    onCancelTimer: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("スリープタイマー", style = MaterialTheme.typography.titleMedium)

                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    SLEEP_TIMER_OPTIONS_MINUTES.forEach { minutes ->
                        ListItem(
                            headlineContent = { Text("${minutes}分") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(minutes * 60_000L)
                                    onDismiss()
                                },
                        )
                    }

                    if (isActive) {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("タイマーを解除") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCancelTimer()
                                    onDismiss()
                                },
                        )
                    }
                }
            }
        }
    }
}
