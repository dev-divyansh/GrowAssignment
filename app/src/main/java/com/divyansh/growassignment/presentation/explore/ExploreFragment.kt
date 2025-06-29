package com.divyansh.growassignment.presentation.explore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.divyansh.growassignment.R
import com.divyansh.growassignment.databinding.FragmentExploreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.divyansh.growassignment.data.models.TopMoverDto
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import android.text.Editable
import android.text.TextWatcher
import android.util.Log

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var gainersAdapter: TopMoverAdapter
    private lateinit var losersAdapter: TopMoverAdapter
    private lateinit var searchAdapter: StockAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAdapters()
        setupRecyclerViews()
        setupSearchListener()
        setupClickListeners()
        observeViewModel()
        
        // Add a small delay to ensure the view is properly initialized
        view.post {
            viewModel.loadTopMovers()
        }
    }

    private fun setupAdapters() {
        gainersAdapter = TopMoverAdapter { symbol ->
            findNavController().navigate(
                R.id.action_exploreFragment_to_productFragment,
                bundleOf("symbol" to symbol)
            )
        }
        losersAdapter = TopMoverAdapter { symbol ->
            findNavController().navigate(
                R.id.action_exploreFragment_to_productFragment,
                bundleOf("symbol" to symbol)
            )
        }
        searchAdapter = StockAdapter { symbol ->
            findNavController().navigate(
                R.id.action_exploreFragment_to_productFragment,
                bundleOf("symbol" to symbol)
            )
        }
    }

    private fun setupRecyclerViews() {
        // Use GridLayoutManager for vertical grid layout (2 columns)
        binding.recyclerGainers.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerGainers.adapter = gainersAdapter
        
        binding.recyclerLosers.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerLosers.adapter = losersAdapter
        
        binding.recyclerSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearch.adapter = searchAdapter
        binding.recyclerSearch.visibility = View.GONE
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    viewModel.searchTickers(query)
                    binding.recyclerSearch.visibility = View.VISIBLE
                    binding.recyclerGainers.visibility = View.GONE
                    binding.recyclerLosers.visibility = View.GONE
                } else {
                    binding.recyclerSearch.visibility = View.GONE
                    binding.recyclerGainers.visibility = View.VISIBLE
                    binding.recyclerLosers.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadTopMovers()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.exploreState.collect { state ->
                when (state) {
                    is ExploreUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.errorState.visibility = View.GONE
                        binding.recyclerGainers.visibility = View.VISIBLE
                        binding.recyclerLosers.visibility = View.VISIBLE
                        Log.d("ExploreFragment", "Loading state")
                    }
                    is ExploreUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorState.visibility = View.GONE
                        binding.recyclerGainers.visibility = View.VISIBLE
                        binding.recyclerLosers.visibility = View.VISIBLE
                        Log.d("ExploreFragment", "Success state - Gainers: ${state.gainers.size}, Losers: ${state.losers.size}")
                        gainersAdapter.submitList(state.gainers)
                        losersAdapter.submitList(state.losers)
                    }
                    is ExploreUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorState.visibility = View.VISIBLE
                        binding.recyclerGainers.visibility = View.GONE
                        binding.recyclerLosers.visibility = View.GONE
                        binding.tvErrorMessage.text = state.message
                        Log.e("ExploreFragment", "Error state: ${state.message}")
                        Toast.makeText(requireContext(), "Failed to load data: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                Log.d("ExploreFragment", "Search results updated: ${results.size} items")
                searchAdapter.submitList(results)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
