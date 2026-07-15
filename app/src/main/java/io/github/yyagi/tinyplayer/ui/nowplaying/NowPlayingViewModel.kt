package io.github.yyagi.tinyplayer.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.yyagi.tinyplayer.media.PlaybackUiState
import io.github.yyagi.tinyplayer.media.PlayerController
import kotlinx.coroutines.flow.StateFlow

class NowPlayingViewModel(
    private val playerController: PlayerController,
) : ViewModel() {
    val uiState: StateFlow<PlaybackUiState> = playerController.uiState

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekToNext() = playerController.seekToNext()
    fun seekToPrevious() = playerController.seekToPrevious()
    fun seekBy(deltaMs: Long) = playerController.seekBy(deltaMs)
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)
    fun cycleRepeatMode() = playerController.cycleRepeatMode()
    fun setSleepTimer(durationMs: Long) = playerController.setSleepTimer(durationMs)
    fun cancelSleepTimer() = playerController.cancelSleepTimer()

    companion object {
        fun factory(playerController: PlayerController) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NowPlayingViewModel(playerController) as T
                }
            }
    }
}
