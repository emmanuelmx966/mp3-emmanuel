package com.emmanuelmx.mp3emmanuel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.Song
import com.emmanuelmx.mp3emmanuel.data.repository.SongRepository
import com.emmanuelmx.mp3emmanuel.service.MusicController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: SongRepository,
    private val musicController: MusicController
) : ViewModel() {

    private val _hasPermission = MutableStateFlow<Boolean?>(null)
    val hasPermission: StateFlow<Boolean?> = _hasPermission.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val allSongs: StateFlow<List<Song>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying
    val duration = musicController.duration
    val position = musicController.position
    val repeatMode = musicController.repeatMode
    val shuffleEnabled = musicController.shuffleEnabled

    fun onPermissionResult(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) refreshSongs()
    }

    private fun refreshSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshSongs()
            } catch (e: SecurityException) {
                _hasPermission.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSongClick(song: Song) {
        musicController.playSong(song, allSongs.value)
    }

    /** Reproduce una canción dentro de una lista arbitraria (favoritos, playlist, etc.) */
    fun onSongClickInList(song: Song, list: List<Song>) {
        musicController.playSong(song, list)
    }

    fun onPlayPlaylist(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        musicController.playSong(songs[startIndex], songs)
    }

    fun onPlayPauseClick() = musicController.togglePlayPause()
    fun onNextClick() = musicController.skipToNext()
    fun onPreviousClick() = musicController.skipToPrevious()
    fun onSeek(positionMs: Long) = musicController.seekTo(positionMs)
    fun onToggleRepeat() = musicController.toggleRepeat()
    fun onToggleShuffle() = musicController.toggleShuffle()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.updateSong(song.copy(isFavorite = !song.isFavorite))
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(Playlist(name = name))
        }
    }

    // ---- Playlist detail ----

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        repository.getSongsInPlaylist(playlistId)

    fun getSongCountInPlaylist(playlistId: Long): Flow<Int> =
        repository.getSongCountInPlaylist(playlistId)

    fun getPlaylistById(playlistId: Long, onResult: (Playlist?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getPlaylistById(playlistId))
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylistById(playlistId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}