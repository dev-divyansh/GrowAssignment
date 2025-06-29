package com.divyansh.growassignment.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import com.divyansh.growassignment.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    val watchlists = repository.getAllWatchlistsWithStocks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Idle)
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    fun createWatchlist(name: String) {
        viewModelScope.launch {
            try {
                repository.createWatchlist(name)
                _uiState.value = WatchlistUiState.Success
            } catch (e: Exception) {
                _uiState.value = WatchlistUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteWatchlist(watchlist: WatchlistEntity) {
        viewModelScope.launch {
            try {
                repository.deleteWatchlist(watchlist)
                _uiState.value = WatchlistUiState.Success
            } catch (e: Exception) {
                _uiState.value = WatchlistUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addStockToWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                repository.addStockToWatchlist(watchlistId, symbol)
                _uiState.value = WatchlistUiState.Success
            } catch (e: Exception) {
                _uiState.value = WatchlistUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun removeStockFromWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                repository.removeStockFromWatchlist(watchlistId, symbol)
                _uiState.value = WatchlistUiState.Success
            } catch (e: Exception) {
                _uiState.value = WatchlistUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = WatchlistUiState.Idle
    }

    fun getWatchlistWithStocks(watchlistId: Long) = repository.getWatchlistWithStocks(watchlistId)
} 