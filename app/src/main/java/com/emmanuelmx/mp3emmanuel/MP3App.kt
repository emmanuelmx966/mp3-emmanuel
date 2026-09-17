package com.emmanuelmx.mp3emmanuel

import android.app.Application
import androidx.room.Room
import com.emmanuelmx.mp3emmanuel.data.local.AppDatabase
import com.emmanuelmx.mp3emmanuel.data.repository.SongRepository
import com.emmanuelmx.mp3emmanuel.service.MusicController

class MP3App : Application() {
    lateinit var database: AppDatabase
    lateinit var songRepository: SongRepository
    lateinit var musicController: MusicController

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "mp3_emmanuel_db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        songRepository = SongRepository(contentResolver, database.songDao())
        musicController = MusicController(this)
    }
}