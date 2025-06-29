package com.divyansh.growassignment.data.repository

import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.models.QuoteEntity
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getCompanyOverview(symbol: String): CompanyEntity?
    suspend fun getRealTimeQuote(symbol: String): QuoteEntity?

    suspend fun createWatchlist(name: String): Long
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>
    fun getAllWatchlistsWithStocks(): Flow<List<WatchlistWithStocks>>
    fun getWatchlistWithStocks(watchlistId: Long): Flow<WatchlistWithStocks>
    suspend fun addStockToWatchlist(watchlistId: Long, symbol: String)
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)
}
