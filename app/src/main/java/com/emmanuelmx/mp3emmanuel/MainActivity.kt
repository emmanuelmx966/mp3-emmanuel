package com.emmanuelmx.mp3emmanuel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emmanuelmx.mp3emmanuel.ui.MusicViewModel
import com.emmanuelmx.mp3emmanuel.ui.screens.EqualizerScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.FavoritesScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.PlayerScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.PlaylistDetailScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.PlaylistScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.SongListScreen
import com.emmanuelmx.mp3emmanuel.ui.theme.MP3EMMANUELTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as MP3App
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(app.songRepository, app.musicController) as T
            }
        }
    }

    private val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) viewModel.onPermissionResult(true)
        else permissionLauncher.launch(audioPermission)

        setContent {
            MP3EMMANUELTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "songs") {
                    composable("songs") {
                        SongListScreen(
                            viewModel = viewModel,
                            onOpenPlayer = { navController.navigate("player") },
                            onOpenEqualizer = { navController.navigate("equalizer") },
                            onOpenPlaylists = { navController.navigate("playlists") },
                            onOpenFavorites = { navController.navigate("favorites") }
                        )
                    }
                    composable("favorites") {
                        FavoritesScreen(
                            viewModel = viewModel,
                            onOpenPlayer = { navController.navigate("player") },
                            onOpenSongs = {
                                navController.navigate("songs") {
                                    popUpTo("songs") { inclusive = true }
                                }
                            },
                            onOpenPlaylists = { navController.navigate("playlists") }
                        )
                    }
                    composable("player") {
                        PlayerScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("equalizer") {
                        EqualizerScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("playlists") {
                        PlaylistScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onOpenPlaylist = { id -> navController.navigate("playlist/$id") }
                        )
                    }
                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                        PlaylistDetailScreen(
                            viewModel = viewModel,
                            playlistId = id,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}