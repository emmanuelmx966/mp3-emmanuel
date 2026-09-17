package com.emmanuelmx.mp3emmanuel

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emmanuelmx.mp3emmanuel.ui.MusicViewModel
import com.emmanuelmx.mp3emmanuel.ui.screens.EqualizerScreen
import com.emmanuelmx.mp3emmanuel.ui.screens.PlayerScreen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var hasAudioPermission by mutableStateOf(false)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasAudioPermission = isGranted
        }

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        permissionLauncher.launch(permission)

        setContent {
            MP3EMMANUELTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "songs") {
                    composable("songs") {
                        SongListScreen(
                            viewModel = viewModel,
                            onOpenPlayer = { navController.navigate("player") },
                            onOpenEqualizer = { navController.navigate("equalizer") },
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
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}