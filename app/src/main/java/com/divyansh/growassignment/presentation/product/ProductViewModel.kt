package com.divyansh.growassignment.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyansh.growassignment.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _companyState = MutableStateFlow<CompanyUiState>(CompanyUiState.Loading)
    val companyState: StateFlow<CompanyUiState> = _companyState.asStateFlow()

    private val _priceHistory = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val priceHistory: StateFlow<List<Pair<Long, Float>>> = _priceHistory.asStateFlow()

    fun loadCompany(symbol: String) {
        if (symbol.isBlank()) {
            _companyState.value = CompanyUiState.Error("Invalid stock symbol")
            return
        }

        Log.d("ProductViewModel", "Loading company data for symbol: $symbol")
        _companyState.value = CompanyUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getCompanyOverview(symbol)
                if (result != null) {
                    Log.d("ProductViewModel", "Company data loaded successfully: ${result.name}")
                    _companyState.value = CompanyUiState.Success(result)
                } else {
                    Log.e("ProductViewModel", "No company data found for $symbol")
                    _companyState.value = CompanyUiState.Error("No data found for $symbol")
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Exception loading company data", e)
                val errorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection."
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timeout. Please try again."
                    else -> "Unable to load stock information: ${e.message ?: "Unknown error"}"
                }
                _companyState.value = CompanyUiState.Error(errorMessage)
            }
        }
    }

    fun loadPriceHistory(symbol: String) {
        if (symbol.isBlank()) {
            _priceHistory.value = emptyList()
            return
        }

        Log.d("ProductViewModel", "Loading price history for symbol: $symbol")
        viewModelScope.launch {
            try {
                val data = repository.getPriceHistory(symbol)
                Log.d("ProductViewModel", "Price history loaded: ${data.size} data points")
                _priceHistory.value = data
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Exception loading price history", e)
                _priceHistory.value = emptyList()
            }
        }
    }
}
