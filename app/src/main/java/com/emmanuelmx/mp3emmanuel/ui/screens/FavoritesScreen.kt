package com.emmanuelmx.mp3emmanuel.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emmanuelmx.mp3emmanuel.ui.MusicViewModel
import com.emmanuelmx.mp3emmanuel.ui.components.MiniPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MusicViewModel,
    onOpenPlayer: () -> Unit,
    onOpenSongs: () -> Unit,
    onOpenPlaylists: () -> Unit
) {
    val favorites by viewModel.favoriteSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") }
            )
        },
        bottomBar = {
            Column {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onPlayPauseClick = { viewModel.onPlayPauseClick() },
                    onClick = onOpenPlayer
                )
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenSongs,
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = "Canciones") },
                        label = { Text("Canciones") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                        label = { Text("Favoritos") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenPlaylists,
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Listas") },
                        label = { Text("Listas") }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (favorites.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay favoritos",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca el corazón en cualquier canción para añadirla aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(favorites, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            onClick = { viewModel.onSongClickInList(song, favorites) },
                            onFavoriteClick = { viewModel.toggleFavorite(song) }
                        )
                    }
                }
            }
        }
    }
}