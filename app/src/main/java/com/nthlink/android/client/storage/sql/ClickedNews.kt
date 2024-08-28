package com.nthlink.android.client.storage.sql

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clicked_news")
data class ClickedNews(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
