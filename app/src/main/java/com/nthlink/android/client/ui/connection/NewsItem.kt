package com.nthlink.android.client.ui.connection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.ViewHolderHeadlineBinding
import com.nthlink.android.client.databinding.ViewHolderNotificationBinding

/**
 * Model
 */
sealed interface NewsModel {

    fun getViewType(): Int

    fun getNewsTitle(): String

    fun getNewsUrl(): String

    data class Notification(val title: String, val url: String) : NewsModel {
        override fun getViewType(): Int = R.layout.view_holder_notification
        override fun getNewsTitle(): String = title
        override fun getNewsUrl(): String = url
    }

    data class Headline(
        val title: String,
        val excerpt: String,
        val image: String,
        val url: String
    ) : NewsModel {
        override fun getViewType(): Int = R.layout.view_holder_headline
        override fun getNewsTitle(): String = title
        override fun getNewsUrl(): String = url
    }
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

            if (item is NewsModel.Notification) {
                binding.title.text = item.title
            }
        }
    }

    class Headline(private val binding: ViewHolderHeadlineBinding) :
        NewsViewHolder(binding.root) {
        override fun <T : NewsModel> bind(
            item: T,
            position: Int,
            onNewsItemClick: ((NewsModel) -> Unit)?
        ) {
            itemView.setOnClickListener { onNewsItemClick?.invoke(item) }

            if (item is NewsModel.Headline) {
                with(binding) {
                    val color = if (position % 2 == 0) R.color.white else R.color.eggshell_white_2
                    binding.root.setBackgroundResource(color)
                    title.text = item.title
                    subtitle.text = item.excerpt
                }
            }
        }
    }
}

fun getNewsViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val view = layoutInflater.inflate(viewType, parent, false)

    return when (viewType) {
        R.layout.view_holder_notification -> {
            NewsViewHolder.Notification(ViewHolderNotificationBinding.bind(view))
        }

        R.layout.view_holder_headline -> {
            NewsViewHolder.Headline(ViewHolderHeadlineBinding.bind(view))
        }

        else -> throw IllegalArgumentException("Unknown view type!")
    }
}