package com.divyansh.growassignment.presentation.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.divyansh.growassignment.databinding.FragmentWatchlistDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchlistDetailFragment : Fragment() {
    private var _binding: FragmentWatchlistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WatchlistViewModel by viewModels()
    private lateinit var adapter: WatchlistStockAdapter
    private var watchlistId: Long = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWatchlistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        watchlistId = arguments?.getLong("watchlistId") ?: -1
        adapter = WatchlistStockAdapter()
        binding.recyclerStocks.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerStocks.adapter = adapter

        lifecycleScope.launch {
            viewModel.getWatchlistWithStocks(watchlistId).collectLatest { watchlist ->
                if (watchlist.stocks.isEmpty()) {
                    binding.recyclerStocks.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.recyclerStocks.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    adapter.submitList(watchlist.stocks)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 