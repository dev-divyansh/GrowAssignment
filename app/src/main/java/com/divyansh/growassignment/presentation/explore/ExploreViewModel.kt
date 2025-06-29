package com.divyansh.growassignment.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyansh.growassignment.data.repository.StockRepository
import com.divyansh.growassignment.data.models.TickerMatchDto
import com.divyansh.growassignment.data.models.TopMoverEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _exploreState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val exploreState: StateFlow<ExploreUiState> = _exploreState

    private val _searchResults = MutableStateFlow<List<TickerMatchDto>>(emptyList())
    val searchResults: StateFlow<List<TickerMatchDto>> = _searchResults

    fun loadTopMovers() {
        viewModelScope.launch {
            _exploreState.value = ExploreUiState.Loading
            try {
                Log.d("ExploreViewModel", "Loading top movers from repository")
                
                // Get gainers and losers from repository directly
                // The repository will handle API errors and use fallback data if needed
                val gainers = repository.getTopMovers("gainer")
                val losers = repository.getTopMovers("loser")
                
                Log.d("ExploreViewModel", "Gainers count: ${gainers.size}, Losers count: ${losers.size}")
                
                if (gainers.isNotEmpty() || losers.isNotEmpty()) {
                    // Convert TopMoverEntity to TopMoverDto for UI
                    val gainerDtos = gainers.map { entity ->
                        com.divyansh.growassignment.data.models.TopMoverDto(
                            ticker = entity.symbol,
                            price = entity.price,
                            change_amount = entity.changeAmount,
                            change_percentage = entity.changePercentage,
                            volume = entity.volume,
                            name = entity.name
                        )
                    }
                    
                    val loserDtos = losers.map { entity ->
                        com.divyansh.growassignment.data.models.TopMoverDto(
                            ticker = entity.symbol,
                            price = entity.price,
                            change_amount = entity.changeAmount,
                            change_percentage = entity.changePercentage,
                            volume = entity.volume,
                            name = entity.name
                        )
                    }
                    
                    Log.d("ExploreViewModel", "Successfully loaded data - Gainers: ${gainerDtos.size}, Losers: ${loserDtos.size}")
                    _exploreState.value = ExploreUiState.Success(
                        gainers = gainerDtos,
                        losers = loserDtos
                    )
                } else {
                    // Check if this might be due to rate limiting
                    val errorMessage = if (gainers.isEmpty() && losers.isEmpty()) {
                        "No market data available. This might be due to API rate limits. Please try again later or upgrade to a premium API plan."
                    } else {
                        "No data available"
                    }
                    Log.e("ExploreViewModel", "No data available: $errorMessage")
                    _exploreState.value = ExploreUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Exception loading top movers", e)
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timeout. Please check your connection."
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your internet connection."
                    e.message?.contains("rate limit", ignoreCase = true) == true ->
                        "API rate limit exceeded. Please try again later or upgrade to a premium plan."
                    else -> "Network error: ${e.message}"
                }
                _exploreState.value = ExploreUiState.Error(errorMessage)
            }
        }
    }

    fun searchTickers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ExploreViewModel", "Searching tickers: $query")
                val results = repository.searchTickers(query)
                Log.d("ExploreViewModel", "Search results count: ${results.size}")
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Exception searching tickers", e)
                _searchResults.value = emptyList()
            }
        }
    }
}
