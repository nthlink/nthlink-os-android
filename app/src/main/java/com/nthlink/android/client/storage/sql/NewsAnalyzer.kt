package com.nthlink.android.client.storage.sql

import com.nthlink.android.client.ui.connection.NewsModel
import com.nthlink.android.core.model.Config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NewsAnalyzer(private val dao: ClickedNewsDao, private val scope: CoroutineScope) {
    private val oneMonthInMillis: Long = TimeUnit.DAYS.toMillis(30)

    private var notifications: List<NewsModel.Notification> = emptyList()
    private var newsTitles: List<NewsModel.HeadlineNews> = emptyList()

    fun addClickedNews(categories: List<String>) {
        if (categories.isEmpty()) return

        scope.launch(IO) {
            val timestamp = System.currentTimeMillis()
            val records = categories.map { category ->
                ClickedNews(category = category, timestamp = timestamp)
            }

            dao.insertAll(records)
        }
    }

    suspend fun loadNews(config: Config) {
        // notifications
        notifications = config.notifications.map {
            NewsModel.Notification(it.title, it.url)
        }

        // sort news
        val recommendedNewsList = sortByUserPreference(config.headlineNews)

        // news
        newsTitles = recommendedNewsList.map {
            NewsModel.HeadlineNews(
                NewsModel.NEWS_TITLE,
                it.title,
                it.excerpt,
                it.image,
                it.url,
                it.categories
            )
        }
    }

    fun getRecommendedNews(): List<NewsModel> {
        val newsList = ArrayList<NewsModel>().apply {
            addAll(notifications)
            addAll(newsTitles)
        }

        return newsList
    }

    private suspend fun sortByUserPreference(newsList: List<Config.HeadlineNews>): List<Config.HeadlineNews> {
        return withContext(IO) {
            // get user's preference
            val categoryCounts = dao.getCategoryCountsIn(periodTimestamp = oneMonthInMillis)
            if (categoryCounts.isEmpty()) return@withContext newsList
            val userPreference = categoryCounts.associate { it.category to it.count }

            // score the news
            val scoredNewsMap = mutableMapOf<Config.HeadlineNews, Int>()
            for (news in newsList) {
                var totalScore = 0
                for (category in news.categories) {
                    val score = userPreference[category] ?: 0
                    totalScore += score
                }
                scoredNewsMap[news] = totalScore
            }

            scoredNewsMap.toList().sortedByDescending { it.second }.map { it.first }
        }
    }

    fun removeExpiredClickedNews() {
        scope.launch(IO) {
            dao.deleteIn(periodTimestamp = oneMonthInMillis)
        }
    }
}