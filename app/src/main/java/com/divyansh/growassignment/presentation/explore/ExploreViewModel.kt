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

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val api: AlphaVantageApi
) : ViewModel() {

    private val _exploreState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val exploreState: StateFlow<ExploreUiState> = _exploreState

    private val _searchResults = MutableStateFlow<List<TickerMatchDto>>(emptyList())
    val searchResults: StateFlow<List<TickerMatchDto>> = _searchResults

    fun loadTopMovers(apiKey: String) {
        viewModelScope.launch {
            _exploreState.value = ExploreUiState.Loading
            try {
                val response = api.getTopGainersLosers(apiKey = apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _exploreState.value = ExploreUiState.Success(
                            gainers = body.top_gainers,
                            losers = body.top_losers
                        )
                    } else {
                        _exploreState.value = ExploreUiState.Error("No data")
                    }
                } else {
                    _exploreState.value = ExploreUiState.Error("API error")
                }
            } catch (e: Exception) {
                _exploreState.value = ExploreUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchTickers(query: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = api.searchTicker(keywords = query, apiKey = apiKey)
                if (response.isSuccessful) {
                    _searchResults.value = response.body()?.bestMatches ?: emptyList()
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }
}
