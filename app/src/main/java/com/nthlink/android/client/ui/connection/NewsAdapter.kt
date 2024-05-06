package com.nthlink.android.client.ui.connection

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class NewsAdapter : ListAdapter<NewsModel, NewsViewHolder>(this) {

    companion object : DiffUtil.ItemCallback<NewsModel>() {
        override fun areItemsTheSame(oldItem: NewsModel, newItem: NewsModel): Boolean {
            return (oldItem.getViewType() == newItem.getViewType())
                    && (oldItem.getNewsTitle() == newItem.getNewsTitle())
        }

        override fun areContentsTheSame(oldItem: NewsModel, newItem: NewsModel): Boolean {
            return oldItem == newItem
        }
    }

    var onNewsItemClick: ((NewsModel) -> Unit)? = null

    override fun getItemViewType(position: Int): Int = getItem(position).getViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return getNewsViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position), position, onNewsItemClick)
    }
}