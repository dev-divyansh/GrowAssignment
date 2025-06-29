package com.divyansh.growassignment.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.divyansh.growassignment.R
import com.divyansh.growassignment.data.models.TopMoverDto
import com.divyansh.growassignment.databinding.ItemStockBinding

class TopMoverAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<TopMoverDto, TopMoverAdapter.TopMoverViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopMoverViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopMoverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopMoverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TopMoverViewHolder(private val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: TopMoverDto) {
            binding.apply {
                tvStockSymbol.text = stock.ticker
                tvStockName.text = stock.name ?: "Unknown Company"
                tvStockPrice.text = "$${stock.price}"
                
                // Handle change amount and percentage
                val changeAmount = stock.change_amount.toFloatOrNull() ?: 0f
                val changePercent = stock.change_percentage.replace("%", "").toFloatOrNull() ?: 0f
                
                if (changeAmount >= 0) {
                    tvStockChange.text = "+$${String.format("%.2f", changeAmount)}"
                    tvStockChange.setBackgroundResource(R.drawable.change_background_positive)
                    tvStockChange.setTextColor(itemView.context.getColor(R.color.positive_green))
                    tvStockChangePercent.text = "+${String.format("%.2f", changePercent)}%"
                    tvStockChangePercent.setTextColor(itemView.context.getColor(R.color.positive_green))
                } else {
                    tvStockChange.text = "$${String.format("%.2f", changeAmount)}"
                    tvStockChange.setBackgroundResource(R.drawable.change_background_negative)
                    tvStockChange.setTextColor(itemView.context.getColor(R.color.negative_red))
                    tvStockChangePercent.text = "${String.format("%.2f", changePercent)}%"
                    tvStockChangePercent.setTextColor(itemView.context.getColor(R.color.negative_red))
                }
                
                // Load stock image/logo if available
                // For now, we'll use a placeholder. In a real app, you'd get the logo URL from the API
                val logoUrl = "https://logo.clearbit.com/${stock.ticker.lowercase()}.com"
                ivStockImage.load(logoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_stock_default)
                    error(R.drawable.ic_stock_default)
                }
                
                root.setOnClickListener {
                    onItemClick(stock.ticker)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TopMoverDto>() {
        override fun areItemsTheSame(oldItem: TopMoverDto, newItem: TopMoverDto) =
            oldItem.ticker == newItem.ticker

        override fun areContentsTheSame(oldItem: TopMoverDto, newItem: TopMoverDto) =
            oldItem == newItem
    }
} 