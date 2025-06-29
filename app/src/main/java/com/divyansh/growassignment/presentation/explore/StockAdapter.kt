package com.divyansh.growassignment.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.divyansh.growassignment.data.models.TickerMatchDto
import com.divyansh.growassignment.databinding.ItemStockBinding

class StockAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<TickerMatchDto, StockAdapter.StockViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StockViewHolder(private val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: TickerMatchDto) {
            binding.tvStockSymbol.text = stock.symbol
            binding.tvStockName.text = stock.name
            binding.root.setOnClickListener {
                onItemClick(stock.symbol)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TickerMatchDto>() {
        override fun areItemsTheSame(oldItem: TickerMatchDto, newItem: TickerMatchDto) =
            oldItem.symbol == newItem.symbol

        override fun areContentsTheSame(oldItem: TickerMatchDto, newItem: TickerMatchDto) =
            oldItem == newItem
    }
}
