package com.nthlink.android.client.storage.sql

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClickedNews::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nthlink_db"
        ).build()
    }

    abstract fun clickedNewsDao(): ClickedNewsDao
}