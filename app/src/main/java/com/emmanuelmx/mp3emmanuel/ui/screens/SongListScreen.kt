package com.emmanuelmx.mp3emmanuel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.emmanuelmx.mp3emmanuel.data.model.Song
import com.emmanuelmx.mp3emmanuel.ui.MusicViewModel
import com.emmanuelmx.mp3emmanuel.ui.components.MiniPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    viewModel: MusicViewModel,
    onOpenPlayer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenFavorites: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Canciones") },
                actions = {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Default.Settings, contentDescription = "Ecualizador")
                    }
                }
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
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = "Canciones") },
                        label = { Text("Canciones") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenFavorites,
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
            when {
                hasPermission == false -> PermissionDeniedMessage()
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                songs.isEmpty() -> EmptyLibraryMessage()
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(songs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onClick = { viewModel.onSongClick(song) },
                                onFavoriteClick = { viewModel.toggleFavorite(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sin acceso a la música",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Concede el permiso de acceso a archivos de audio para ver tu biblioteca.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyLibraryMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay canciones",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Añade archivos MP3 a tu dispositivo para verlos aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text("${song.artist} • ${song.album}") },
        leadingContent = {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = "Portada de ${song.title}",
                modifier = Modifier.size(50.dp)
            )
        },
        trailingContent = {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (song.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (song.isFavorite) {
                        "Quitar de favoritos"
                    } else {
                        "Añadir a favoritos"
                    },
                    tint = if (song.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}