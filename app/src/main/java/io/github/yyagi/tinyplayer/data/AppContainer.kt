package io.github.yyagi.tinyplayer.data

import android.content.Context
import io.github.yyagi.tinyplayer.data.db.AppDatabase
import io.github.yyagi.tinyplayer.data.db.PlaylistRepository
import io.github.yyagi.tinyplayer.data.song.SongRepository
import io.github.yyagi.tinyplayer.media.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val songRepository: SongRepository by lazy { SongRepository() }
    val playerController: PlayerController by lazy { PlayerController(appContext, appScope, songRepository) }
    val playlistRepository: PlaylistRepository by lazy { PlaylistRepository(database.playlistDao(), songRepository) }
}
