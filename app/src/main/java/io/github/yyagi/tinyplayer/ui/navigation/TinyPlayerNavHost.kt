package io.github.yyagi.tinyplayer.ui.navigation

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
import io.github.yyagi.tinyplayer.data.AppContainer
import io.github.yyagi.tinyplayer.ui.artists.ArtistDetailScreen
import io.github.yyagi.tinyplayer.ui.artists.ArtistsScreen
import io.github.yyagi.tinyplayer.ui.library.LibraryScreen
import io.github.yyagi.tinyplayer.ui.library.LibraryViewModel
import io.github.yyagi.tinyplayer.ui.nowplaying.NowPlayingScreen
import io.github.yyagi.tinyplayer.ui.nowplaying.NowPlayingViewModel
import io.github.yyagi.tinyplayer.ui.playlists.PlaylistDetailScreen
import io.github.yyagi.tinyplayer.ui.playlists.PlaylistDetailViewModel
import io.github.yyagi.tinyplayer.ui.playlists.PlaylistsScreen
import io.github.yyagi.tinyplayer.ui.playlists.PlaylistsViewModel

@Composable
fun TinyPlayerNavHost(
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

        composable(Destinations.ARTISTS) {
            val viewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.factory(context, container.songRepository, container.playlistRepository),
            )
            ArtistsScreen(
                viewModel = viewModel,
                onArtistClick = { artist ->
                    navController.navigate(Destinations.artistDetail(artist))
                },
            )
        }

        composable(
            route = Destinations.ARTIST_DETAIL,
            arguments = listOf(navArgument("artist") { type = NavType.StringType }),
        ) { backStackEntry ->
            val artist = backStackEntry.arguments?.getString("artist").orEmpty()
            val viewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.factory(context, container.songRepository, container.playlistRepository),
            )
            ArtistDetailScreen(
                artist = artist,
                viewModel = viewModel,
                onSongClick = { songs, index ->
                    container.playerController.playQueue(songs, index)
                    navController.navigate(Destinations.NOW_PLAYING)
                },
                onBack = { navController.popBackStack() },
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
