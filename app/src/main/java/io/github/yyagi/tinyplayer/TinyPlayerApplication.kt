package io.github.yyagi.tinyplayer

import android.app.Application
import io.github.yyagi.tinyplayer.data.AppContainer

class TinyPlayerApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
