package com.emmanuelmx.mp3emmanuel.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.emmanuelmx.mp3emmanuel.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicController(context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val controller: MediaController?
        get() = try {
            if (controllerFuture?.isDone == true) controllerFuture?.get() else null
        } catch (e: Exception) {
            null
        }

    // Guardamos la cola actual para poder mapear el índice → Song
    private var queue: List<Song> = emptyList()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            setupController()
        }, MoreExecutors.directExecutor())
    }

    private fun setupController() {
        val controller = this.controller ?: return

        controller.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) startPositionUpdates() else stopPositionUpdates()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller.currentMediaItemIndex
                if (index in queue.indices) {
                    _currentSong.value = queue[index]
                }
                _duration.value = controller.duration.coerceAtLeast(0L)
                _position.value = controller.currentPosition.coerceAtLeast(0L)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleEnabled.value = shuffleModeEnabled
            }

            override fun onPlayerError(error: PlaybackException) {
                // TODO: exponer estado de error a la UI si quieres
            }
        })

        // Sincronizar estado inicial
        _repeatMode.value = controller.repeatMode
        _shuffleEnabled.value = controller.shuffleModeEnabled
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                val c = controller ?: break
                _position.value = c.currentPosition.coerceAtLeast(0L)
                _duration.value = c.duration.coerceAtLeast(0L)
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    /**
     * Reproduce una canción dentro de una cola completa.
     * @param song Canción seleccionada.
     * @param queue Lista completa para habilitar siguiente/anterior/shuffle/repeat.
     */
    fun playSong(song: Song, queue: List<Song>) {
        val controller = this.controller ?: return
        this.queue = queue

        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItems = queue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(s.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems, index, 0L)
        controller.prepare()
        controller.play()
        _currentSong.value = song
        _position.value = 0L
    }

    fun togglePlayPause() {
        val controller = this.controller ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun skipToNext() {
        controller?.seekToNext()
    }

    fun skipToPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _position.value = positionMs.coerceAtLeast(0L)
    }

    /** Cicla OFF → ALL → ONE → OFF */
    fun toggleRepeat() {
        val c = controller ?: return
        val next = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        c.repeatMode = next
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun release() {
        stopPositionUpdates()
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}