package com.emmanuelmx.mp3emmanuel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuelmx.mp3emmanuel.data.model.Song
import com.emmanuelmx.mp3emmanuel.data.repository.SongRepository
import com.emmanuelmx.mp3emmanuel.service.MusicController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: SongRepository,
    private val musicController: MusicController
) : ViewModel() {
    val allSongs: StateFlow<List<Song>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<com.emmanuelmx.mp3emmanuel.data.model.Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying

    init {
        viewModelScope.launch {
            repository.refreshSongs()
        }
    }

    fun onSongClick(song: Song) {
        musicController.playSong(song)
    }

    fun onPlayPauseClick() {
        musicController.togglePlayPause()
    }

    fun onNextClick() {
        musicController.skipToNext()
    }

    fun onPreviousClick() {
        musicController.skipToPrevious()
    }

    fun onSeek(position: Float) {
        // TODO: Handle seek correctly with duration
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.updateSong(song.copy(isFavorite = !song.isFavorite))
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(com.emmanuelmx.mp3emmanuel.data.model.Playlist(name = name))
        }
    }
}
