package com.divyansh.growassignment.presentation.explore

import com.divyansh.growassignment.data.models.TopMoverDto

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val gainers: List<TopMoverDto>, val losers: List<TopMoverDto>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}
