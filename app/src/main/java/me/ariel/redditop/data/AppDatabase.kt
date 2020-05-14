package me.ariel.redditop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Entry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        fun generateDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "redditop.db"
        ).build()
    }
}