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
    private val apiKey = "YOUR_API_KEY" // Replace with your actual API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val symbol = arguments?.getString("symbol") ?: return
        viewModel.loadCompany(symbol)

        lifecycleScope.launch {
            viewModel.companyState.collect { state ->
                when (state) {
                    is CompanyUiState.Loading -> showLoading()
                    is CompanyUiState.Success -> showCompany(state.data)
                    is CompanyUiState.Error -> showError(state.message)
                }
            }
        }

        // Watchlist icon state
        lifecycleScope.launch {
            watchlistViewModel.watchlists.collect { lists ->
                isInWatchlist = lists.any { wl -> wl.stocks.any { it.symbol == symbol } }
                binding.btnWatchlist.isSelected = isInWatchlist
            }
        }

        binding.btnWatchlist.setOnClickListener {
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
        }

        // Observe price history for chart
        lifecycleScope.launch {
            viewModel.priceHistory.collect { data ->
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
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentScrollView.visibility = View.GONE
    }

    private fun showCompany(company: CompanyEntity) {
        binding.progressBar.visibility = View.GONE
        binding.contentScrollView.visibility = View.VISIBLE

        binding.tvSymbol.text = company.symbol
        binding.tvName.text = company.name
        binding.tvSector.text = company.sector
        binding.tvMarketCap.text = "Market Cap: $${company.marketCap}"
        binding.tvDescription.text = company.description

        viewModel.loadPriceHistory(company.symbol, apiKey)
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentScrollView.visibility = View.GONE
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
