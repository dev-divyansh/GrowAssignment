package com.divyansh.growassignment.presentation.watchlist

sealed class WatchlistUiState {
    object Idle : WatchlistUiState()
    object Success : WatchlistUiState()
    data class Error(val message: String) : WatchlistUiState()
} 