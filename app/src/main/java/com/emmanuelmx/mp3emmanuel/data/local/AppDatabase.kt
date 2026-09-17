package com.emmanuelmx.mp3emmanuel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.emmanuelmx.mp3emmanuel.data.model.Playlist
import com.emmanuelmx.mp3emmanuel.data.model.PlaylistSong
import com.emmanuelmx.mp3emmanuel.data.model.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSong::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        /**
         * Migración 1 → 2:
         * Añade Foreign Keys con ON DELETE CASCADE a playlist_songs
         * y los índices correspondientes.
         *
         * SQLite no soporta ALTER TABLE ADD CONSTRAINT, así que hay que
         * recrear la tabla completa.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Eliminar filas huérfanas ANTES de crear las FKs
                db.execSQL(
                    """
                    DELETE FROM playlist_songs
                    WHERE playlistId NOT IN (SELECT id FROM playlists)
                       OR songId NOT IN (SELECT id FROM songs)
                    """.trimIndent()
                )

                // 2. Crear tabla nueva con FKs
                db.execSQL(
                    """
                    CREATE TABLE playlist_songs_new (
                        playlistId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        PRIMARY KEY(playlistId, songId),
                        FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE,
                        FOREIGN KEY(songId) REFERENCES songs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // 3. Copiar datos (ya saneados)
                db.execSQL(
                    """
                    INSERT INTO playlist_songs_new (playlistId, songId)
                    SELECT playlistId, songId FROM playlist_songs
                    """.trimIndent()
                )

                // 4. Eliminar tabla antigua
                db.execSQL("DROP TABLE playlist_songs")

                // 5. Renombrar la nueva
                db.execSQL("ALTER TABLE playlist_songs_new RENAME TO playlist_songs")

                // 6. Crear índices (Room los exige para las FKs)
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_playlist_songs_playlistId " +
                        "ON playlist_songs(playlistId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_playlist_songs_songId " +
                        "ON playlist_songs(songId)"
                )
            }
        }
    }
}