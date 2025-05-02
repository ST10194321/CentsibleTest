package com.st10194321.centsibletest

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//RoomDB
@Database(entities = [users::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): userDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "centsibledb"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}