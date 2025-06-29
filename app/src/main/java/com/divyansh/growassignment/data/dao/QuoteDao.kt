package com.divyansh.growassignment.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.divyansh.growassignment.data.models.QuoteEntity

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quote WHERE symbol = :symbol")
    suspend fun getQuote(symbol: String): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)
}
