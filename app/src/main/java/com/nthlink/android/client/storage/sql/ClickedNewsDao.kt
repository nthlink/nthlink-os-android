package com.nthlink.android.client.storage.sql

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClickedNewsDao {
    @Insert
    suspend fun insertAll(records: List<ClickedNews>)

    @Query("SELECT category, COUNT(*) as count FROM clicked_news WHERE :currentTimestamp - timestamp <= :periodTimestamp  GROUP BY category")
    suspend fun getCategoryCountsIn(
        currentTimestamp: Long = System.currentTimeMillis(),
        periodTimestamp: Long
    ): List<CategoryCount>

    @Query("DELETE FROM clicked_news WHERE :currentTimestamp - timestamp > :periodTimestamp")
    suspend fun deleteIn(currentTimestamp: Long = System.currentTimeMillis(), periodTimestamp: Long)
}

data class CategoryCount(val category: String, val count: Int)