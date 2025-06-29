package com.divyansh.growassignment.presentation.product

import com.divyansh.growassignment.data.local.entities.CompanyEntity

sealed class CompanyUiState {
    object Loading : CompanyUiState()
    data class Success(val data: CompanyEntity) : CompanyUiState()
    data class Error(val message: String) : CompanyUiState()
}
