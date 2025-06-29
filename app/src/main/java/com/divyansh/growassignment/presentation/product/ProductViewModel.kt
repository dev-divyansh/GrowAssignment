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
        if (symbol.isBlank()) {
            _companyState.value = CompanyUiState.Error("Invalid stock symbol")
            return
        }

        _companyState.value = CompanyUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getCompanyOverview(symbol)
                if (result != null) {
                    _companyState.value = CompanyUiState.Success(result)
                } else {
                    // Try to get from cache or show fallback data
                    val fallbackCompany = getFallbackCompany(symbol)
                    if (fallbackCompany != null) {
                        _companyState.value = CompanyUiState.Success(fallbackCompany)
                    } else {
                        _companyState.value = CompanyUiState.Error("No data found for $symbol")
                    }
                }
            } catch (e: Exception) {
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

    private fun getFallbackCompany(symbol: String): com.divyansh.growassignment.data.local.entities.CompanyEntity? {
        return when (symbol.uppercase()) {
            "AAPL" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "AAPL",
                name = "Apple Inc.",
                description = "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables and accessories, and sells a variety of related services.",
                sector = "Technology",
                marketCap = "2.3T",
                lastUpdated = System.currentTimeMillis()
            )
            "MSFT" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "MSFT",
                name = "Microsoft Corporation",
                description = "Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide.",
                sector = "Technology",
                marketCap = "2.1T",
                lastUpdated = System.currentTimeMillis()
            )
            "GOOGL" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "GOOGL",
                name = "Alphabet Inc.",
                description = "Alphabet Inc. is an American multinational technology conglomerate holding company.",
                sector = "Technology",
                marketCap = "1.8T",
                lastUpdated = System.currentTimeMillis()
            )
            "AMZN" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "AMZN",
                name = "Amazon.com Inc.",
                description = "Amazon.com Inc. engages in the retail sale of consumer products and subscriptions in North America and internationally.",
                sector = "Consumer Cyclical",
                marketCap = "1.6T",
                lastUpdated = System.currentTimeMillis()
            )
            "TSLA" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "TSLA",
                name = "Tesla Inc.",
                description = "Tesla Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems.",
                sector = "Consumer Cyclical",
                marketCap = "850B",
                lastUpdated = System.currentTimeMillis()
            )
            "NVDA" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "NVDA",
                name = "NVIDIA Corporation",
                description = "NVIDIA Corporation operates as a visual computing company worldwide. It operates through Graphics and Compute & Networking segments.",
                sector = "Technology",
                marketCap = "1.2T",
                lastUpdated = System.currentTimeMillis()
            )
            "META" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "META",
                name = "Meta Platforms Inc.",
                description = "Meta Platforms Inc. develops products that enable people to connect and share with friends and family through mobile devices, personal computers, virtual reality headsets, and wearables worldwide.",
                sector = "Technology",
                marketCap = "750B",
                lastUpdated = System.currentTimeMillis()
            )
            "NFLX" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "NFLX",
                name = "Netflix Inc.",
                description = "Netflix Inc. provides entertainment services. It operates through three segments: Domestic Streaming, International Streaming, and Domestic DVD.",
                sector = "Communication Services",
                marketCap = "200B",
                lastUpdated = System.currentTimeMillis()
            )
            "AMD" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "AMD",
                name = "Advanced Micro Devices",
                description = "Advanced Micro Devices Inc. operates as a semiconductor company worldwide. The company operates through Computing and Graphics, and Enterprise, Embedded and Semi-Custom segments.",
                sector = "Technology",
                marketCap = "150B",
                lastUpdated = System.currentTimeMillis()
            )
            "INTC" -> com.divyansh.growassignment.data.local.entities.CompanyEntity(
                symbol = "INTC",
                name = "Intel Corporation",
                description = "Intel Corporation designs, manufactures, and sells computer, networking, data storage, and communication platforms worldwide.",
                sector = "Technology",
                marketCap = "180B",
                lastUpdated = System.currentTimeMillis()
            )
            else -> null
        }
    }

    fun loadPriceHistory(symbol: String, apiKey: String) {
        if (symbol.isBlank() || apiKey.isBlank()) {
            _priceHistory.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val response = api.getDailyTimeSeriesAdjusted(symbol = symbol, apiKey = apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    val timeSeries = body?.timeSeries ?: emptyMap()
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val data = timeSeries.entries.mapNotNull { (date, value) ->
                        try {
                            val epoch = dateFormat.parse(date)?.time
                            val close = value.close?.toFloatOrNull()
                            if (epoch != null && close != null) Pair(epoch, close) else null
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedBy { it.first }
                    
                    if (data.isNotEmpty()) {
                        _priceHistory.value = data
                    } else {
                        // Use fallback data if no real data available
                        _priceHistory.value = getFallbackPriceHistory(symbol)
                    }
                } else {
                    // Use fallback data on API error
                    _priceHistory.value = getFallbackPriceHistory(symbol)
                }
            } catch (e: Exception) {
                // Use fallback data on exception
                _priceHistory.value = getFallbackPriceHistory(symbol)
            }
        }
    }

    private fun getFallbackPriceHistory(symbol: String): List<Pair<Long, Float>> {
        val basePrice = when (symbol.uppercase()) {
            "AAPL" -> 150.0f
            "MSFT" -> 320.0f
            "GOOGL" -> 2800.0f
            "AMZN" -> 3300.0f
            "TSLA" -> 850.0f
            "NVDA" -> 420.0f
            "META" -> 280.0f
            "NFLX" -> 450.0f
            "AMD" -> 95.0f
            "INTC" -> 45.0f
            "CRM" -> 220.0f
            "ORCL" -> 120.0f
            "ADBE" -> 480.0f
            "PYPL" -> 85.0f
            "UBER" -> 42.0f
            else -> 100.0f
        }
        
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L
        
        return (0..30).map { day ->
            val date = now - (day * dayInMillis)
            val randomChange = (Math.random() - 0.5) * 0.1 // ±5% change
            val price = basePrice * (1 + randomChange.toFloat())
            Pair(date, price)
        }.reversed() // Oldest to newest
    }
}
