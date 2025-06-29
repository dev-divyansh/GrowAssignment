package com.divyansh.growassignment.presentation.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.divyansh.growassignment.databinding.FragmentWatchlistBinding
import com.divyansh.growassignment.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchlistFragment : Fragment() {
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WatchlistViewModel by viewModels()
    private lateinit var adapter: WatchlistListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = WatchlistListAdapter { watchlistId ->
            findNavController().navigate(
                R.id.action_watchlistFragment_to_watchlistDetailFragment,
                Bundle().apply { putLong("watchlistId", watchlistId) }
            )
        }
        binding.recyclerWatchlists.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWatchlists.adapter = adapter

        lifecycleScope.launch {
            viewModel.watchlists.collectLatest { watchlists ->
                if (watchlists.isEmpty()) {
                    binding.recyclerWatchlists.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.recyclerWatchlists.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    adapter.submitList(watchlists)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 