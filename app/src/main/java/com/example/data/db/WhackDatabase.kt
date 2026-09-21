package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HighScoreEntity::class], version = 1, exportSchema = false)
abstract class WhackDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        @Volatile
        private var INSTANCE: WhackDatabase? = null

        fun getDatabase(context: Context): WhackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WhackDatabase::class.java,
                    "whack_a_mole_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
