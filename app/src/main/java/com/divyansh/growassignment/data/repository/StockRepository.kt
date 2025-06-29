package com.divyansh.growassignment.data.repository

import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.models.QuoteEntity
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import com.divyansh.growassignment.data.models.TopMoverEntity
import com.divyansh.growassignment.data.models.TickerMatchDto
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun testApiConnection(): Boolean
    suspend fun getCompanyOverview(symbol: String): CompanyEntity?
    suspend fun getRealTimeQuote(symbol: String): QuoteEntity?
    suspend fun getTopMovers(type: String): List<TopMoverEntity>
    suspend fun searchTickers(query: String): List<TickerMatchDto>
    suspend fun getPriceHistory(symbol: String): List<Pair<Long, Float>>

    suspend fun createWatchlist(name: String): Long
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>
    fun getAllWatchlistsWithStocks(): Flow<List<WatchlistWithStocks>>
    fun getWatchlistWithStocks(watchlistId: Long): Flow<WatchlistWithStocks>
    suspend fun addStockToWatchlist(watchlistId: Long, symbol: String)
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)
}
