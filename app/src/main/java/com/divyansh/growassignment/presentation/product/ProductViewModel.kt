package com.divyansh.growassignment.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyansh.growassignment.data.repository.StockRepositoryImpl
import com.divyansh.growassignment.data.api.AlphaVantageApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: StockRepositoryImpl,
    private val api: AlphaVantageApi
) : ViewModel() {

    private val _companyState = MutableStateFlow<CompanyUiState>(CompanyUiState.Loading)
    val companyState: StateFlow<CompanyUiState> = _companyState.asStateFlow()

    private val _priceHistory = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val priceHistory: StateFlow<List<Pair<Long, Float>>> = _priceHistory.asStateFlow()

    fun loadCompany(symbol: String) {
        _companyState.value = CompanyUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getCompanyOverview(symbol)
                if (result != null) {
                    _companyState.value = CompanyUiState.Success(result)
                } else {
                    _companyState.value = CompanyUiState.Error("No data found")
                }
            } catch (e: Exception) {
                _companyState.value = CompanyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadPriceHistory(symbol: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = api.getDailyTimeSeriesAdjusted(symbol = symbol, apiKey = apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    val timeSeries = body?.timeSeries ?: emptyMap()
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val data = timeSeries.entries.mapNotNull { (date, value) ->
                        val epoch = runCatching { 
                            dateFormat.parse(date)?.time
                        }.getOrNull()
                        val close = value.close?.toFloatOrNull()
                        if (epoch != null && close != null) Pair(epoch, close) else null
                    }.sortedBy { it.first }
                    _priceHistory.value = data
                } else {
                    _priceHistory.value = emptyList()
                }
            } catch (e: Exception) {
                _priceHistory.value = emptyList()
            }
        }
    }
}
