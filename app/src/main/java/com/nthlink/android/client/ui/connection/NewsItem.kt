package com.nthlink.android.client.ui.connection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.ViewHolderNewsTitleBinding
import com.nthlink.android.client.databinding.ViewHolderNotificationBinding

/**
 * Model
 */
sealed class NewsModel(val viewType: Int, val title: String, val url: String) {

    companion object {
        const val NOTIFICATION = 0
        const val NEWS_TITLE = 1
    }

    class Notification(title: String, url: String) : NewsModel(NOTIFICATION, title, url)

    class HeadlineNews(
        viewType: Int,
        title: String,
        val excerpt: String,
        val image: String,
        url: String,
        val categories: List<String>
    ) : NewsModel(viewType, title, url)
}

/**
 * View Holder
 */
sealed class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun <T : NewsModel> bind(
        item: T,
        position: Int,
        onNewsItemClick: ((NewsModel) -> Unit)? = null
    )

    class Notification(private val binding: ViewHolderNotificationBinding) :
        NewsViewHolder(binding.root) {
        override fun <T : NewsModel> bind(
            item: T,
            position: Int,
            onNewsItemClick: ((NewsModel) -> Unit)?
        ) {
            itemView.setOnClickListener { onNewsItemClick?.invoke(item) }
            binding.title.text = item.title
        }
    }

    class NewsTitle(private val binding: ViewHolderNewsTitleBinding) :
        NewsViewHolder(binding.root) {
        override fun <T : NewsModel> bind(
            item: T,
            position: Int,
            onNewsItemClick: ((NewsModel) -> Unit)?
        ) {
            itemView.setOnClickListener { onNewsItemClick?.invoke(item) }

            with(binding) {
                val color = if (position % 2 == 0) R.color.white else R.color.eggshell_white_2
                binding.root.setBackgroundResource(color)
                title.text = item.title
                subtitle.text = (item as NewsModel.HeadlineNews).excerpt
            }
        }
    }
}

fun getNewsViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
    val inflater = LayoutInflater.from(parent.context)

    return when (viewType) {
        NewsModel.NOTIFICATION -> {
            val binding = ViewHolderNotificationBinding.inflate(inflater, parent, false)
            NewsViewHolder.Notification(binding)
        }

        NewsModel.NEWS_TITLE -> {
            val binding = ViewHolderNewsTitleBinding.inflate(inflater, parent, false)
            NewsViewHolder.NewsTitle(binding)
        }

        else -> throw IllegalArgumentException("Unknown view type!")
    }
}