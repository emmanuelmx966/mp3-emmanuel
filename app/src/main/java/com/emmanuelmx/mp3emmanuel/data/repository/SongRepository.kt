package com.emmanuelmx.mp3emmanuel.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.emmanuelmx.mp3emmanuel.data.local.SongDao
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.PlaylistSong
import com.emmanuelmx.mp3emmanuel.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(
    private val contentResolver: ContentResolver,
    private val songDao: SongDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()

    fun getAllPlaylists(): Flow<List<Playlist>> = songDao.getAllPlaylists()

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        songDao.getSongsInPlaylist(playlistId)

    fun getSongCountInPlaylist(playlistId: Long): Flow<Int> =
        songDao.getSongCountInPlaylist(playlistId)

    suspend fun getPlaylistById(playlistId: Long): Playlist? =
        songDao.getPlaylistById(playlistId)

    suspend fun updateSong(song: Song) = songDao.updateSong(song)

    suspend fun createPlaylist(playlist: Playlist): Long = songDao.createPlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = songDao.deletePlaylist(playlist)

    suspend fun deletePlaylistById(playlistId: Long) = songDao.deletePlaylistById(playlistId)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) =
        songDao.addSongToPlaylist(PlaylistSong(playlistId, songId))

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        songDao.removeSongFromPlaylist(playlistId, songId)

    suspend fun refreshSongs() = withContext(Dispatchers.IO) {
        val favoriteIds = songDao.getFavoriteIds().toSet()
        val previousDates = songDao.getAllIdsWithDate().associate { it.id to it.dateAdded }

        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri.toString(),
                        albumArtUri = albumArtUri.toString(),
                        isFavorite = favoriteIds.contains(id),
                        dateAdded = previousDates[id] ?: System.currentTimeMillis()
                    )
                )
            }
        }
        songDao.insertSongs(songs)
    }
}