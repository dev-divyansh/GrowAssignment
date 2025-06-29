package com.divyansh.growassignment.data.dao

import androidx.room.*
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistStockCrossRef
import com.divyansh.growassignment.data.models.WatchlistWithStocks
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(watchlist: WatchlistEntity): Long

    @Delete
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)

    @Query("SELECT * FROM watchlist")
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>

    @Transaction
    @Query("SELECT * FROM watchlist WHERE id = :watchlistId")
    fun getWatchlistWithStocks(watchlistId: Long): Flow<WatchlistWithStocks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStockToWatchlist(crossRef: WatchlistStockCrossRef)

    @Query("DELETE FROM watchlist_stock_cross_ref WHERE watchlistId = :watchlistId AND symbol = :symbol")
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)

    @Transaction
    @Query("SELECT * FROM watchlist")
    fun getAllWatchlistsWithStocks(): Flow<List<WatchlistWithStocks>>
} 