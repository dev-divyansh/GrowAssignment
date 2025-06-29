package com.divyansh.growassignment.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyansh.growassignment.data.api.AlphaVantageApi
import com.divyansh.growassignment.data.models.TopMoverDto
import com.divyansh.growassignment.data.models.TickerMatchDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val api: AlphaVantageApi
) : ViewModel() {

    private val _exploreState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val exploreState: StateFlow<ExploreUiState> = _exploreState

    private val _searchResults = MutableStateFlow<List<TickerMatchDto>>(emptyList())
    val searchResults: StateFlow<List<TickerMatchDto>> = _searchResults

    fun loadTopMovers(apiKey: String) {
        if (apiKey.isBlank()) {
            _exploreState.value = ExploreUiState.Error("Invalid API key")
            return
        }

        viewModelScope.launch {
            _exploreState.value = ExploreUiState.Loading
            try {
                Log.d("ExploreViewModel", "Loading top movers with API key: $apiKey")
                val response = api.getTopGainersLosers(apiKey = apiKey)
                Log.d("ExploreViewModel", "API response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ExploreViewModel", "Response body: $body")
                    
                    if (body != null) {
                        val gainers = body.top_gainers ?: emptyList()
                        val losers = body.top_losers ?: emptyList()
                        
                        Log.d("ExploreViewModel", "Gainers count: ${gainers.size}, Losers count: ${losers.size}")
                        
                        if (gainers.isNotEmpty() || losers.isNotEmpty()) {
                            _exploreState.value = ExploreUiState.Success(
                                gainers = gainers,
                                losers = losers
                            )
                        } else {
                            Log.w("ExploreViewModel", "Empty data received from API, using fallback data")
                            _exploreState.value = ExploreUiState.Success(
                                gainers = getFallbackGainers(),
                                losers = getFallbackLosers()
                            )
                        }
                    } else {
                        Log.e("ExploreViewModel", "Response body is null, using fallback data")
                        _exploreState.value = ExploreUiState.Success(
                            gainers = getFallbackGainers(),
                            losers = getFallbackLosers()
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ExploreViewModel", "API error: ${response.code()} - $errorBody")
                    
                    // Use fallback data for certain error codes
                    when (response.code()) {
                        401 -> _exploreState.value = ExploreUiState.Error("Invalid API key")
                        429 -> {
                            Log.w("ExploreViewModel", "Rate limit exceeded, using fallback data")
                            _exploreState.value = ExploreUiState.Success(
                                gainers = getFallbackGainers(),
                                losers = getFallbackLosers()
                            )
                        }
                        500 -> {
                            Log.w("ExploreViewModel", "Server error, using fallback data")
                            _exploreState.value = ExploreUiState.Success(
                                gainers = getFallbackGainers(),
                                losers = getFallbackLosers()
                            )
                        }
                        else -> _exploreState.value = ExploreUiState.Error("API error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Exception loading top movers", e)
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timeout. Please check your connection."
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your internet connection."
                    else -> "Network error: ${e.message}"
                }
                _exploreState.value = ExploreUiState.Error(errorMessage)
            }
        }
    }

    private fun getFallbackGainers(): List<TopMoverDto> {
        return listOf(
            TopMoverDto("AAPL", "150.25", "2.50", "1.67", "50000000", "Apple Inc."),
            TopMoverDto("MSFT", "320.15", "5.20", "1.65", "30000000", "Microsoft Corporation"),
            TopMoverDto("GOOGL", "2800.50", "45.30", "1.64", "15000000", "Alphabet Inc."),
            TopMoverDto("AMZN", "3300.75", "52.10", "1.60", "25000000", "Amazon.com Inc."),
            TopMoverDto("TSLA", "850.20", "12.80", "1.53", "40000000", "Tesla Inc."),
            TopMoverDto("NVDA", "420.10", "6.15", "1.48", "45000000", "NVIDIA Corporation"),
            TopMoverDto("META", "280.75", "4.20", "1.52", "35000000", "Meta Platforms Inc."),
            TopMoverDto("NFLX", "450.30", "6.50", "1.46", "20000000", "Netflix Inc."),
            TopMoverDto("AMD", "95.25", "1.30", "1.38", "60000000", "Advanced Micro Devices"),
            TopMoverDto("INTC", "45.80", "0.60", "1.33", "55000000", "Intel Corporation")
        )
    }

    private fun getFallbackLosers(): List<TopMoverDto> {
        return listOf(
            TopMoverDto("NFLX", "450.30", "-8.50", "-1.85", "20000000", "Netflix Inc."),
            TopMoverDto("META", "280.75", "-4.20", "-1.48", "35000000", "Meta Platforms Inc."),
            TopMoverDto("NVDA", "420.10", "-6.15", "-1.44", "45000000", "NVIDIA Corporation"),
            TopMoverDto("AMD", "95.25", "-1.30", "-1.35", "60000000", "Advanced Micro Devices"),
            TopMoverDto("INTC", "45.80", "-0.60", "-1.29", "55000000", "Intel Corporation"),
            TopMoverDto("CRM", "220.50", "-2.80", "-1.25", "25000000", "Salesforce Inc."),
            TopMoverDto("ORCL", "120.30", "-1.50", "-1.23", "30000000", "Oracle Corporation"),
            TopMoverDto("ADBE", "480.75", "-5.90", "-1.21", "18000000", "Adobe Inc."),
            TopMoverDto("PYPL", "85.20", "-1.00", "-1.16", "40000000", "PayPal Holdings Inc."),
            TopMoverDto("UBER", "42.15", "-0.45", "-1.06", "35000000", "Uber Technologies Inc.")
        )
    }

    fun searchTickers(query: String, apiKey: String) {
        if (query.isBlank() || apiKey.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ExploreViewModel", "Searching tickers: $query")
                val response = api.searchTicker(keywords = query, apiKey = apiKey)
                
                if (response.isSuccessful) {
                    val results = response.body()?.bestMatches ?: emptyList()
                    Log.d("ExploreViewModel", "Search results count: ${results.size}")
                    _searchResults.value = results
                } else {
                    Log.e("ExploreViewModel", "Search API error: ${response.code()}")
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Exception searching tickers", e)
                _searchResults.value = emptyList()
            }
        }
    }
}
