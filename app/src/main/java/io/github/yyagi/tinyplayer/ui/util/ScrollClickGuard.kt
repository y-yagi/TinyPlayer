package io.github.yyagi.tinyplayer.ui.util

import android.os.SystemClock
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

/**
 * Guards against clicks that land immediately after a fling was stopped by the same touch.
 * [LazyListState.isScrollInProgress] already reads false by the time such a click fires, so this
 * instead tracks when scrolling last stopped and rejects clicks within [graceMs] of that moment.
 */
@Composable
fun rememberScrollClickGuard(listState: LazyListState, graceMs: Long = 200L): () -> Boolean {
    var lastScrollStopAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { inProgress ->
                if (!inProgress) lastScrollStopAt = SystemClock.elapsedRealtime()
            }
    }
    return { SystemClock.elapsedRealtime() - lastScrollStopAt >= graceMs }
}
