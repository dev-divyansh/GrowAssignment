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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: AlphaVantageApi,
    private val companyDao: CompanyDao,
    private val watchlistDao: WatchlistDao,
    private val topMoverDao: TopMoverDao,
    private val quoteDao: QuoteDao
) : StockRepository {

    private val apiKey = "E717D20RKD1G1H94"

    // 6-hour cache expiration
    private val cacheExpiry = 6 * 60 * 60 * 1000

    override suspend fun getCompanyOverview(symbol: String): CompanyEntity? = withContext(Dispatchers.IO) {
        val cached = companyDao.getCompany(symbol)

        val isExpired =
            cached == null || (System.currentTimeMillis() - cached.lastUpdated > cacheExpiry)

        if (isExpired) {
            val response = api.getCompanyOverview(symbol = symbol, apiKey = apiKey)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val entity = dto.toEntity()
                    companyDao.insertCompany(entity)
                    entity
                } ?: cached
            } else {
                cached
            }
        } else {
            cached
        }
    }

    override suspend fun getRealTimeQuote(symbol: String): QuoteEntity? = withContext(Dispatchers.IO) {
        val cached = quoteDao.getQuote(symbol)
        val isExpired = cached == null || (System.currentTimeMillis() - cached.lastUpdated > cacheExpiry)
        if (isExpired) {
            val response = api.getRealTimeQuote(symbol = symbol, apiKey = apiKey)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val entity = QuoteEntity(
                        symbol = dto.quote.symbol,
                        price = dto.quote.price,
                        changePercent = dto.quote.changePercent,
                        lastUpdated = System.currentTimeMillis()
                    )
                    quoteDao.insertQuote(entity)
                    entity
                } ?: cached
            } else {
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

    suspend fun getTopMovers(type: String): List<TopMoverEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiry = now - cacheExpiry
        topMoverDao.deleteOlderThan(expiry)
        val cached = topMoverDao.getByType(type)
        if (cached.isNotEmpty()) {
            cached
        } else {
            val response = api.getTopGainersLosers(apiKey = apiKey)
            if (response.isSuccessful) {
                val body = response.body()
                val movers = when (type) {
                    "gainer" -> body?.top_gainers
                    "loser" -> body?.top_losers
                    else -> null
                } ?: emptyList()
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
                entities
            } else {
                emptyList()
            }
        }
    }
}
