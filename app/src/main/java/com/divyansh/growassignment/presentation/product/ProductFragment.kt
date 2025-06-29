package com.divyansh.growassignment.presentation.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.databinding.FragmentProductBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.divyansh.growassignment.presentation.watchlist.WatchlistPickerDialogFragment
import com.divyansh.growassignment.presentation.watchlist.WatchlistViewModel
import androidx.fragment.app.viewModels as viewModels2
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@AndroidEntryPoint
class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private val watchlistViewModel: WatchlistViewModel by viewModels2()
    private var isInWatchlist: Boolean = false
    private val apiKey = "E717D20RKD1G1H94" // Updated API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val symbol = arguments?.getString("symbol")
        if (symbol.isNullOrEmpty()) {
            showError("Invalid stock symbol")
            return
        }
        
        viewModel.loadCompany(symbol)

        // Observe company state
        lifecycleScope.launch {
            viewModel.companyState.collect { state ->
                if (_binding != null) { // Check if binding is still valid
                    when (state) {
                        is CompanyUiState.Loading -> showLoading()
                        is CompanyUiState.Success -> showCompany(state.data)
                        is CompanyUiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Watchlist icon state
        lifecycleScope.launch {
            try {
                watchlistViewModel.watchlists.collect { lists ->
                    if (_binding != null) { // Check if binding is still valid
                        isInWatchlist = lists.any { wl -> wl.stocks.any { it.symbol == symbol } }
                        binding.btnWatchlist.isSelected = isInWatchlist
                    }
                }
            } catch (e: Exception) {
                // Handle watchlist state error silently
            }
        }

        binding.btnWatchlist.setOnClickListener {
            try {
                if (isInWatchlist) {
                    // Remove from all watchlists
                    watchlistViewModel.watchlists.value.forEach { wl ->
                        if (wl.stocks.any { it.symbol == symbol }) {
                            watchlistViewModel.removeStockFromWatchlist(wl.id, symbol)
                        }
                    }
                    Toast.makeText(requireContext(), "Removed from Watchlist", Toast.LENGTH_SHORT).show()
                } else {
                    WatchlistPickerDialogFragment { watchlistId ->
                        watchlistViewModel.addStockToWatchlist(watchlistId, symbol)
                        Toast.makeText(requireContext(), "Added to Watchlist", Toast.LENGTH_SHORT).show()
                    }.show(parentFragmentManager, "watchlist_picker")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating watchlist", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe price history for chart
        lifecycleScope.launch {
            try {
                viewModel.priceHistory.collect { data ->
                    if (_binding != null) { // Check if binding is still valid
                        if (data.isNotEmpty()) {
                            val entries = data.mapIndexed { idx, pair -> Entry(idx.toFloat(), pair.second) }
                            val dataSet = LineDataSet(entries, "Price")
                            dataSet.setDrawValues(false)
                            dataSet.setDrawCircles(false)
                            binding.lineChart.data = LineData(dataSet)
                            binding.lineChart.invalidate()
                            binding.lineChart.visibility = View.VISIBLE
                        } else {
                            binding.lineChart.clear()
                            binding.lineChart.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle chart error silently
                if (_binding != null) {
                    binding.lineChart.visibility = View.GONE
                }
            }
        }
    }

    private fun showLoading() {
        if (_binding != null) {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentScrollView.visibility = View.GONE
                binding.errorState.visibility = View.GONE
            } catch (e: Exception) {
                // Handle binding error
            }
        }
    }

    private fun showCompany(company: CompanyEntity) {
        if (_binding != null) {
            try {
                binding.progressBar.visibility = View.GONE
                binding.contentScrollView.visibility = View.VISIBLE
                binding.errorState.visibility = View.GONE

                binding.tvSymbol.text = company.symbol
                binding.tvName.text = company.name
                binding.tvSector.text = company.sector
                binding.tvMarketCap.text = "Market Cap: $${company.marketCap}"
                binding.tvDescription.text = company.description

                viewModel.loadPriceHistory(company.symbol, apiKey)
            } catch (e: Exception) {
                showError("Error displaying company data")
            }
        }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            try {
                binding.progressBar.visibility = View.GONE
                binding.contentScrollView.visibility = View.GONE
                binding.errorState.visibility = View.VISIBLE
                binding.tvErrorMessage.text = message
            } catch (e: Exception) {
                // Handle error display failure
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
