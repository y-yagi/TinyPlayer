package io.github.yyagi.mplayer

import android.app.Application
import io.github.yyagi.mplayer.data.AppContainer

class MPlayerApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
