package io.github.yyagi.tinyplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.yyagi.tinyplayer.ui.components.MiniPlayerBar
import io.github.yyagi.tinyplayer.ui.navigation.Destinations
import io.github.yyagi.tinyplayer.ui.navigation.TinyPlayerNavHost
import io.github.yyagi.tinyplayer.ui.theme.TinyPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TinyPlayerApplication).container
        setContent {
            TinyPlayerTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val showBottomChrome = currentRoute == Destinations.LIBRARY ||
                    currentRoute == Destinations.ARTISTS ||
                    currentRoute == Destinations.PLAYLISTS
                val playbackState by container.playerController.uiState.collectAsState()

                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        if (showBottomChrome) {
                            item(
                                selected = currentRoute == Destinations.LIBRARY,
                                onClick = { navigateToTab(navController, Destinations.LIBRARY) },
                                icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = null) },
                                label = { Text("ライブラリ") },
                            )
                            item(
                                selected = currentRoute == Destinations.ARTISTS,
                                onClick = { navigateToTab(navController, Destinations.ARTISTS) },
                                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                label = { Text("アーティスト") },
                            )
                            item(
                                selected = currentRoute == Destinations.PLAYLISTS,
                                onClick = { navigateToTab(navController, Destinations.PLAYLISTS) },
                                icon = { Icon(Icons.Filled.QueueMusic, contentDescription = null) },
                                label = { Text("プレイリスト") },
                            )
                        }
                    },
                ) {
                    Column {
                        TinyPlayerNavHost(
                            navController = navController,
                            container = container,
                            modifier = Modifier.weight(1f),
                        )
                        if (showBottomChrome && playbackState.title.isNotEmpty()) {
                            MiniPlayerBar(
                                state = playbackState,
                                onClick = { navController.navigate(Destinations.NOW_PLAYING) },
                                onTogglePlayPause = { container.playerController.togglePlayPause() },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
