package com.divyansh.growassignment.presentation.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.databinding.ItemStockBinding

class WatchlistStockAdapter : ListAdapter<CompanyEntity, WatchlistStockAdapter.StockViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class StockViewHolder(private val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: CompanyEntity) {
            binding.tvStockSymbol.text = stock.symbol
            binding.tvStockName.text = stock.name
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<CompanyEntity>() {
        override fun areItemsTheSame(oldItem: CompanyEntity, newItem: CompanyEntity) = oldItem.symbol == newItem.symbol
        override fun areContentsTheSame(oldItem: CompanyEntity, newItem: CompanyEntity) = oldItem == newItem
    }
} 