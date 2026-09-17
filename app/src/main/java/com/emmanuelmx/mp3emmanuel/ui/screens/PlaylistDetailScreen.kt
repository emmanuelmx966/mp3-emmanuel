package com.emmanuelmx.mp3emmanuel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.Song
import com.emmanuelmx.mp3emmanuel.ui.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: MusicViewModel,
    playlistId: Long,
    onBackClick: () -> Unit
) {
    val songs by viewModel.getSongsInPlaylist(playlistId).collectAsState(initial = emptyList())
    val allSongs by viewModel.allSongs.collectAsState()

    var playlist by remember { mutableStateOf<Playlist?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(playlistId) {
        viewModel.getPlaylistById(playlistId) { playlist = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Lista") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (songs.isNotEmpty()) viewModel.onPlayPlaylist(songs, 0)
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir lista")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Eliminar lista") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deletePlaylist(playlistId)
                                    onBackClick()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir canción")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (songs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Lista vacía",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pulsa el botón + para añadir canciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                        PlaylistSongItem(
                            song = song,
                            onClick = { viewModel.onPlayPlaylist(songs, index) },
                            onDeleteClick = { songToDelete = song }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para añadir canción
    if (showAddDialog) {
        AddSongDialog(
            allSongs = allSongs,
            existingIds = songs.map { it.id }.toSet(),
            onDismiss = { showAddDialog = false },
            onSelect = { song ->
                viewModel.addSongToPlaylist(playlistId, song.id)
                showAddDialog = false
            }
        )
    }

    // Confirmación de eliminar canción
    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Quitar canción") },
            text = { Text("¿Quitar \"${song.title}\" de esta lista?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeSongFromPlaylist(playlistId, song.id)
                    songToDelete = null
                }) {
                    Text("Quitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PlaylistSongItem(
    song: Song,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar de la lista")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongDialog(
    allSongs: List<Song>,
    existingIds: Set<Long>,
    onDismiss: () -> Unit,
    onSelect: (Song) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allSongs) {
        if (query.isBlank()) allSongs
        else allSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir canción") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (filtered.isEmpty()) {
                    Text("No hay canciones")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(filtered.size, key = { filtered[it].id }) { i ->
                            val song = filtered[i]
                            val alreadyAdded = existingIds.contains(song.id)
                            ListItem(
                                headlineContent = { Text(song.title) },
                                supportingContent = { Text(song.artist) },
                                trailingContent = {
                                    if (alreadyAdded) {
                                        Text("Añadida", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                modifier = Modifier.clickable(enabled = !alreadyAdded) {
                                    if (!alreadyAdded) onSelect(song)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}