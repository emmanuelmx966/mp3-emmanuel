package com.emmanuelmx.mp3emmanuel.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.net.Uri

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumArtUri: String?,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
