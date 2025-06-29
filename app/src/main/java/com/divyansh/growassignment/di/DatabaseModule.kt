package com.divyansh.growassignment.di

import android.content.Context
import androidx.room.Room
import com.divyansh.growassignment.data.dao.CompanyDao
import com.divyansh.growassignment.data.dao.QuoteDao
import com.divyansh.growassignment.data.dao.WatchlistDao
import com.divyansh.growassignment.data.dao.TopMoverDao
import com.divyansh.growassignment.data.local.AppDatabase
import com.divyansh.growassignment.data.repository.StockRepository
import com.divyansh.growassignment.data.repository.StockRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "stock_app_db")
                .fallbackToDestructiveMigration(true)
                .build()

        @Provides
        fun provideCompanyDao(db: AppDatabase): CompanyDao = db.companyDao()

        @Provides
        fun provideQuoteDao(db: AppDatabase): QuoteDao = db.quoteDao()

        @Provides
        fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

        @Provides
        fun provideTopMoverDao(db: AppDatabase): TopMoverDao = db.topMoverDao()
    }
}
