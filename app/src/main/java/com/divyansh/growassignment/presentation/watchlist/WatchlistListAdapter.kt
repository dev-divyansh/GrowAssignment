package com.divyansh.growassignment.presentation.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import com.divyansh.growassignment.databinding.ItemWatchlistBinding

class WatchlistListAdapter(
    private val onClick: (Long) -> Unit
) : ListAdapter<WatchlistWithStocks, WatchlistListAdapter.WatchlistViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val binding = ItemWatchlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WatchlistViewHolder(binding)
    }
    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class WatchlistViewHolder(private val binding: ItemWatchlistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(watchlist: WatchlistWithStocks) {
            binding.tvWatchlistName.text = watchlist.name
            binding.root.setOnClickListener { onClick(watchlist.id) }
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<WatchlistWithStocks>() {
        override fun areItemsTheSame(oldItem: WatchlistWithStocks, newItem: WatchlistWithStocks) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WatchlistWithStocks, newItem: WatchlistWithStocks) = oldItem == newItem
    }
} 