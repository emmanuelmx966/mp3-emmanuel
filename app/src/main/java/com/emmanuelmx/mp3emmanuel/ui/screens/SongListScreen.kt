package com.emmanuelmx.mp3emmanuel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    onOpenPlaylists: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Canciones") },
                actions = {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Default.Settings, contentDescription = null)
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
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        label = { Text("Canciones") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onOpenPlaylists,
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Listas") }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(songs) { song ->
                SongItem(
                    song = song,
                    onClick = { viewModel.onSongClick(song) }
                )
            }
        }
    }
}

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text("${song.artist} • ${song.album}") },
        leadingContent = {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}
