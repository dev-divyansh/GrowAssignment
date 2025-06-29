package com.divyansh.growassignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.divyansh.growassignment.data.dao.CompanyDao
import com.divyansh.growassignment.data.dao.QuoteDao
import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.models.QuoteEntity
import com.divyansh.growassignment.data.models.WatchlistEntity
import com.divyansh.growassignment.data.models.WatchlistStockCrossRef
import com.divyansh.growassignment.data.dao.WatchlistDao
import com.divyansh.growassignment.data.models.TopMoverEntity
import com.divyansh.growassignment.data.dao.TopMoverDao

@Database(
    entities = [CompanyEntity::class, QuoteEntity::class, WatchlistEntity::class, WatchlistStockCrossRef::class, TopMoverEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun quoteDao(): QuoteDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun topMoverDao(): TopMoverDao
}
