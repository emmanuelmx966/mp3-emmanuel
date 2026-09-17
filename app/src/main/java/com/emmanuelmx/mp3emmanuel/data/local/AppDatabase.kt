package com.emmanuelmx.mp3emmanuel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.PlaylistSong
import com.emmanuelmx.mp3emmanuel.data.model.Song

@Database(entities = [Song::class, Playlist::class, PlaylistSong::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}
