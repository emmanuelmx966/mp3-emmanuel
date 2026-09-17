package com.emmanuelmx.mp3emmanuel.data.local

import androidx.room.*
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.PlaylistSong
import com.emmanuelmx.mp3emmanuel.data.model.Song
import kotlinx.coroutines.flow.Flow

data class SongIdAndDate(
    val id: Long,
    val dateAdded: Long
)

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Update
    suspend fun updateSong(song: Song)

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT id FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Long>

    @Query("SELECT id, dateAdded FROM songs")
    suspend fun getAllIdsWithDate(): List<SongIdAndDate>

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY dateCreated DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY s.title ASC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCountInPlaylist(playlistId: Long): Flow<Int>
}