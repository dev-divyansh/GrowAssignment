package com.divyansh.growassignment.data.repository

import com.divyansh.growassignment.data.api.AlphaVantageApi
import com.divyansh.growassignment.data.dao.CompanyDao
import com.divyansh.growassignment.data.dao.WatchlistDao
import com.divyansh.growassignment.data.dao.TopMoverDao
import com.divyansh.growassignment.data.dao.QuoteDao
import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.mappers.toEntity
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistStockCrossRef
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import com.divyansh.growassignment.data.models.TopMoverEntity
import com.divyansh.growassignment.data.models.TopMoverDto
import com.divyansh.growassignment.data.models.QuoteEntity
import com.divyansh.growassignment.data.models.TickerMatchDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: AlphaVantageApi,
    private val companyDao: CompanyDao,
    private val watchlistDao: WatchlistDao,
    private val topMoverDao: TopMoverDao,
    private val quoteDao: QuoteDao
) : StockRepository {

    private val apiKey =  "8R44YO46OAMPZEG1"  // "E717D20RKD1G1H94"

    // 6-hour cache expiration
    private val cacheExpiry = 6 * 60 * 60 * 1000

    init {
        Log.d("StockRepositoryImpl", "Initialized with API key: ${apiKey.take(8)}...")
    }

    // Test API connection
    override suspend fun testApiConnection(): Boolean {
        return try {
            Log.d("StockRepositoryImpl", "Testing API connection...")
            val response = api.getRealTimeQuote(symbol = "AAPL", apiKey = apiKey)
            Log.d("StockRepositoryImpl", "Test API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                // Check if response contains error message (rate limit, invalid key, etc.)
                if (body?.hasError() == false && body?.quote?.symbol != null) {
                    Log.d("StockRepositoryImpl", "API connection successful")
                    true
                } else {
                    val errorMsg = body?.getErrorMessage() ?: "Invalid response format"
                    Log.e("StockRepositoryImpl", "API test failed: $errorMsg")
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("StockRepositoryImpl", "API test failed: ${response.code()} - $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("StockRepositoryImpl", "API test exception", e)
            false
        }
    }

    override suspend fun getCompanyOverview(symbol: String): CompanyEntity? = withContext(Dispatchers.IO) {
        val cached = companyDao.getCompany(symbol)

        val isExpired =
            cached == null || (System.currentTimeMillis() - cached.lastUpdated > cacheExpiry)

        if (isExpired) {
            Log.d("StockRepositoryImpl", "Fetching fresh company overview for $symbol")
            val response = api.getCompanyOverview(symbol = symbol, apiKey = apiKey)
            Log.d("StockRepositoryImpl", "Company overview API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("StockRepositoryImpl", "Company overview response: $body")
                
                // Check for API errors
                if (body?.hasError() == true) {
                    val errorMsg = body.getErrorMessage()
                    Log.e("StockRepositoryImpl", "Company overview API error: $errorMsg")
                    
                    // Handle rate limit specifically
                    if (errorMsg.contains("rate limit", ignoreCase = true) || 
                        errorMsg.contains("25 requests per day", ignoreCase = true)) {
                        Log.w("StockRepositoryImpl", "Rate limit exceeded, using fallback company data")
                        return@withContext getFallbackCompanyData(symbol)
                    } else {
                        return@withContext cached
                    }
                }
                
                // Check if the response is valid
                if (body?.isValid() == true) {
                    val entity = body.toEntity()
                    companyDao.insertCompany(entity)
                    Log.d("StockRepositoryImpl", "Saved company overview to database")
                    entity
                } else {
                    Log.w("StockRepositoryImpl", "Company overview response is invalid")
                    cached ?: getFallbackCompanyData(symbol)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("StockRepositoryImpl", "Company overview API error: ${response.code()} - $errorBody")
                cached ?: getFallbackCompanyData(symbol)
            }
        } else {
            Log.d("StockRepositoryImpl", "Returning cached company overview for $symbol")
            cached
        }
    }

    override suspend fun getRealTimeQuote(symbol: String): QuoteEntity? = withContext(Dispatchers.IO) {
        val cached = quoteDao.getQuote(symbol)
        val isExpired = cached == null || (System.currentTimeMillis() - cached.lastUpdated > cacheExpiry)
        if (isExpired) {
            val response = api.getRealTimeQuote(symbol = symbol, apiKey = apiKey)
            if (response.isSuccessful) {
                val body = response.body()
                
                // Check for API errors
                if (body?.hasError() == true) {
                    Log.e("StockRepositoryImpl", "Quote API error: ${body.getErrorMessage()}")
                    return@withContext cached
                }
                
                body?.quote?.let { quote ->
                    val entity = QuoteEntity(
                        symbol = quote.symbol,
                        price = quote.price,
                        changePercent = quote.changePercent,
                        lastUpdated = System.currentTimeMillis()
                    )
                    quoteDao.insertQuote(entity)
                    entity
                } ?: cached
            } else {
                Log.e("StockRepositoryImpl", "Quote API HTTP error: ${response.code()}")
                cached
            }
        } else {
            cached
        }
    }

    override suspend fun createWatchlist(name: String): Long =
        watchlistDao.insertWatchlist(WatchlistEntity(name = name))

    override suspend fun deleteWatchlist(watchlist: WatchlistEntity) =
        watchlistDao.deleteWatchlist(watchlist)

    override fun getAllWatchlists(): Flow<List<WatchlistEntity>> =
        watchlistDao.getAllWatchlists()

    override fun getAllWatchlistsWithStocks(): Flow<List<WatchlistWithStocks>> =
        watchlistDao.getAllWatchlistsWithStocks()

    override fun getWatchlistWithStocks(watchlistId: Long): Flow<WatchlistWithStocks> =
        watchlistDao.getWatchlistWithStocks(watchlistId)

    override suspend fun addStockToWatchlist(watchlistId: Long, symbol: String) =
        watchlistDao.addStockToWatchlist(WatchlistStockCrossRef(watchlistId, symbol))

    override suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String) =
        watchlistDao.removeStockFromWatchlist(watchlistId, symbol)

    override suspend fun getTopMovers(type: String): List<TopMoverEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiry = now - cacheExpiry
        topMoverDao.deleteOlderThan(expiry)
        val cached = topMoverDao.getByType(type)
        if (cached.isNotEmpty()) {
            Log.d("StockRepositoryImpl", "Returning cached data for $type: ${cached.size} items")
            cached
        } else {
            Log.d("StockRepositoryImpl", "Fetching fresh data for $type from API")
            val response = api.getTopGainersLosers(apiKey = apiKey)
            Log.d("StockRepositoryImpl", "API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("StockRepositoryImpl", "API response body: $body")
                
                // Check if the response contains an error message (rate limit, etc.)
                if (body?.hasError() == true) {
                    val errorMsg = body.getErrorMessage()
                    Log.e("StockRepositoryImpl", "API returned error: $errorMsg")
                    
                    // Handle rate limit specifically
                    if (errorMsg.contains("rate limit", ignoreCase = true) || 
                        errorMsg.contains("25 requests per day", ignoreCase = true)) {
                        Log.w("StockRepositoryImpl", "Rate limit exceeded, using fallback data")
                        val fallbackData = getFallbackTopMovers(type, now)
                        Log.d("StockRepositoryImpl", "Fallback data generated: ${fallbackData.size} items")
                        return@withContext fallbackData
                    } else {
                        Log.e("StockRepositoryImpl", "API error: $errorMsg")
                        return@withContext emptyList()
                    }
                }
                
                val movers = when (type) {
                    "gainer" -> body?.top_gainers
                    "loser" -> body?.top_losers
                    else -> null
                } ?: emptyList()
                
                Log.d("StockRepositoryImpl", "Parsed movers for $type: ${movers.size} items")
                
                val entities = movers.map {
                    TopMoverEntity(
                        type = type,
                        symbol = it.ticker,
                        name = it.name,
                        price = it.price,
                        changeAmount = it.change_amount,
                        changePercentage = it.change_percentage,
                        volume = it.volume,
                        lastUpdated = now
                    )
                }
                topMoverDao.insertAll(entities)
                Log.d("StockRepositoryImpl", "Saved ${entities.size} entities to database")
                entities
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("StockRepositoryImpl", "API error for $type: ${response.code()} - $errorBody")
                
                // Handle specific error cases
                when (response.code()) {
                    401 -> Log.e("StockRepositoryImpl", "Invalid API key")
                    429 -> Log.e("StockRepositoryImpl", "Rate limit exceeded")
                    500 -> Log.e("StockRepositoryImpl", "Server error")
                    else -> Log.e("StockRepositoryImpl", "Unknown API error")
                }
                
                // Fallback: Use popular stocks if TOP_GAINERS_LOSERS is not available
                if (response.code() == 404 || response.code() == 400) {
                    Log.d("StockRepositoryImpl", "Using fallback data for $type")
                    getFallbackTopMovers(type, now)
                } else {
                    emptyList()
                }
            }
        }
    }

    private suspend fun getFallbackTopMovers(type: String, timestamp: Long): List<TopMoverEntity> {
        Log.d("StockRepositoryImpl", "Generating fallback data for $type")
        
        // Mock data for when API rate limit is exceeded
        val mockStocks = when (type) {
            "gainer" -> listOf(
                Triple("AAPL", "Apple Inc.", "+2.45"),
                Triple("MSFT", "Microsoft Corporation", "+1.87"),
                Triple("GOOGL", "Alphabet Inc.", "+3.12"),
                Triple("AMZN", "Amazon.com Inc.", "+2.78"),
                Triple("TSLA", "Tesla Inc.", "+4.23")
            )
            "loser" -> listOf(
                Triple("META", "Meta Platforms Inc.", "-1.56"),
                Triple("NFLX", "Netflix Inc.", "-2.34"),
                Triple("AMD", "Advanced Micro Devices", "-1.89"),
                Triple("INTC", "Intel Corporation", "-2.67"),
                Triple("NVDA", "NVIDIA Corporation", "-1.23")
            )
            else -> emptyList()
        }
        
        val entities = mockStocks.map { (symbol, name, changePercent) ->
            val basePrice = when (type) {
                "gainer" -> (150.0 + (0..50).random()).toFloat()
                "loser" -> (100.0 + (0..30).random()).toFloat()
                else -> 100.0f
            }
            
            val changeAmount = changePercent.replace("+", "").replace("-", "").toFloatOrNull() ?: 1.0f
            val price = if (changePercent.startsWith("+")) basePrice + changeAmount else basePrice - changeAmount
            
            TopMoverEntity(
                type = type,
                symbol = symbol,
                name = name,
                price = String.format("%.2f", price),
                changeAmount = String.format("%.2f", changeAmount),
                changePercentage = changePercent,
                volume = (1000000..5000000).random().toString(),
                lastUpdated = timestamp
            )
        }
        
        if (entities.isNotEmpty()) {
            topMoverDao.insertAll(entities)
            Log.d("StockRepositoryImpl", "Saved ${entities.size} fallback entities to database")
        }
        
        return entities
    }

    override suspend fun searchTickers(query: String): List<TickerMatchDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("StockRepositoryImpl", "Searching tickers for query: $query")
            val response = api.searchTicker(keywords = query, apiKey = apiKey)
            Log.d("StockRepositoryImpl", "Search API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("StockRepositoryImpl", "Search response body: $body")
                val results = body?.bestMatches ?: emptyList()
                Log.d("StockRepositoryImpl", "Search results: ${results.size} items")
                results
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("StockRepositoryImpl", "Search API error: ${response.code()} - $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("StockRepositoryImpl", "Exception in searchTickers", e)
            emptyList()
        }
    }

    override suspend fun getPriceHistory(symbol: String): List<Pair<Long, Float>> = withContext(Dispatchers.IO) {
        try {
            Log.d("StockRepositoryImpl", "Fetching price history for $symbol")
            val response = api.getDailyTimeSeriesAdjusted(symbol = symbol, apiKey = apiKey)
            Log.d("StockRepositoryImpl", "Price history API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("StockRepositoryImpl", "Price history response: $body")
                
                // Check for API errors
                if (body?.hasError() == true) {
                    val errorMsg = body.getErrorMessage()
                    Log.e("StockRepositoryImpl", "Price history API error: $errorMsg")
                    
                    // Handle rate limit specifically
                    if (errorMsg.contains("rate limit", ignoreCase = true) || 
                        errorMsg.contains("25 requests per day", ignoreCase = true)) {
                        Log.w("StockRepositoryImpl", "Rate limit exceeded, using fallback price data")
                        return@withContext getFallbackPriceHistory(symbol)
                    } else {
                        return@withContext emptyList()
                    }
                }
                
                val timeSeries = body?.timeSeries ?: emptyMap()
                if (timeSeries.isNotEmpty()) {
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
                    Log.d("StockRepositoryImpl", "Parsed price history: ${data.size} data points")
                    data
                } else {
                    Log.w("StockRepositoryImpl", "No price history data available, using fallback")
                    getFallbackPriceHistory(symbol)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("StockRepositoryImpl", "Price history API error: ${response.code()} - $errorBody")
                getFallbackPriceHistory(symbol)
            }
        } catch (e: Exception) {
            Log.e("StockRepositoryImpl", "Exception in getPriceHistory", e)
            getFallbackPriceHistory(symbol)
        }
    }

    private suspend fun getFallbackPriceHistory(symbol: String): List<Pair<Long, Float>> {
        Log.d("StockRepositoryImpl", "Generating fallback price history for $symbol")
        
        // Mock data for when API rate limit is exceeded
        val mockData = when (symbol.uppercase()) {
            "AAPL" -> listOf(
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 6), 150.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 5), 152.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 4), 155.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 3), 158.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 2), 160.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 1), 162.0f)
            )
            "MSFT" -> listOf(
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 6), 200.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 5), 202.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 4), 205.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 3), 208.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 2), 210.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 1), 212.0f)
            )
            "GOOGL" -> listOf(
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 6), 250.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 5), 252.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 4), 255.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 3), 258.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 2), 260.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 1), 262.0f)
            )
            "AMZN" -> listOf(
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 6), 300.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 5), 302.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 4), 305.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 3), 308.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 2), 310.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 1), 312.0f)
            )
            "TSLA" -> listOf(
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 6), 350.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 5), 352.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 4), 355.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 3), 358.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 2), 360.0f),
                Pair(System.currentTimeMillis() - (1000 * 60 * 60 * 1), 362.0f)
            )
            else -> emptyList()
        }
        
        return mockData
    }

    private suspend fun getFallbackCompanyData(symbol: String): CompanyEntity {
        Log.d("StockRepositoryImpl", "Generating fallback company data for $symbol")
        
        val companyData = when (symbol.uppercase()) {
            "AAPL" -> CompanyEntity(
                symbol = symbol,
                name = "Apple Inc.",
                sector = "Technology",
                description = "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide.",
                marketCap = "2,500,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
            "MSFT" -> CompanyEntity(
                symbol = symbol,
                name = "Microsoft Corporation",
                sector = "Technology",
                description = "Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide.",
                marketCap = "2,800,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
            "GOOGL" -> CompanyEntity(
                symbol = symbol,
                name = "Alphabet Inc.",
                sector = "Technology",
                description = "Alphabet Inc. provides online advertising services in the United States, Europe, the Middle East, Africa, the Asia-Pacific, Canada, and Latin America.",
                marketCap = "1,800,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
            "AMZN" -> CompanyEntity(
                symbol = symbol,
                name = "Amazon.com Inc.",
                sector = "Consumer Cyclical",
                description = "Amazon.com Inc. engages in the retail sale of consumer products and subscriptions in North America and internationally.",
                marketCap = "1,600,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
            "TSLA" -> CompanyEntity(
                symbol = symbol,
                name = "Tesla Inc.",
                sector = "Consumer Cyclical",
                description = "Tesla Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems.",
                marketCap = "800,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
            else -> CompanyEntity(
                symbol = symbol,
                name = "$symbol Corporation",
                sector = "Technology",
                description = "A technology company focused on innovation and growth in the digital economy.",
                marketCap = "100,000,000,000",
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        companyDao.insertCompany(companyData)
        Log.d("StockRepositoryImpl", "Saved fallback company data to database")
        return companyData
    }
}
