package io.github.yyagi.mplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.yyagi.mplayer.data.AppContainer
import io.github.yyagi.mplayer.ui.library.LibraryScreen
import io.github.yyagi.mplayer.ui.library.LibraryViewModel
import io.github.yyagi.mplayer.ui.nowplaying.NowPlayingScreen
import io.github.yyagi.mplayer.ui.nowplaying.NowPlayingViewModel
import io.github.yyagi.mplayer.ui.playlists.PlaylistDetailScreen
import io.github.yyagi.mplayer.ui.playlists.PlaylistDetailViewModel
import io.github.yyagi.mplayer.ui.playlists.PlaylistsScreen
import io.github.yyagi.mplayer.ui.playlists.PlaylistsViewModel

@Composable
fun MPlayerNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Destinations.LIBRARY, modifier = modifier) {
        composable(Destinations.LIBRARY) {
            val viewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.factory(context, container.songRepository, container.playlistRepository),
            )
            LibraryScreen(
                viewModel = viewModel,
                onSongClick = { songs, index ->
                    container.playerController.playQueue(songs, index)
                    navController.navigate(Destinations.NOW_PLAYING)
                },
            )
        }

        composable(Destinations.PLAYLISTS) {
            val viewModel: PlaylistsViewModel = viewModel(
                factory = PlaylistsViewModel.factory(container.playlistRepository),
            )
            PlaylistsScreen(
                viewModel = viewModel,
                onPlaylistClick = { playlist ->
                    navController.navigate(Destinations.playlistDetail(playlist.playlistId))
                },
            )
        }

        composable(
            route = Destinations.PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val playlistsViewModel: PlaylistsViewModel = viewModel(
                factory = PlaylistsViewModel.factory(container.playlistRepository),
            )
            val playlists by playlistsViewModel.playlists.collectAsState()
            val playlistName = playlists.firstOrNull { it.playlistId == playlistId }?.name.orEmpty()
            val viewModel: PlaylistDetailViewModel = viewModel(
                factory = PlaylistDetailViewModel.factory(playlistId, container.playlistRepository),
            )
            PlaylistDetailScreen(
                playlistName = playlistName,
                viewModel = viewModel,
                onSongClick = { songs, index ->
                    container.playerController.playQueue(songs, index)
                    navController.navigate(Destinations.NOW_PLAYING)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.NOW_PLAYING) {
            val viewModel: NowPlayingViewModel = viewModel(
                factory = NowPlayingViewModel.factory(container.playerController),
            )
            NowPlayingScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
