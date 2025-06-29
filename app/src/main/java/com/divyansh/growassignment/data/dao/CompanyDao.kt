package com.divyansh.growassignment.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.divyansh.growassignment.data.local.entities.CompanyEntity

@Dao
interface CompanyDao {

    @Query("SELECT * FROM company_overview WHERE symbol = :symbol")
    suspend fun getCompany(symbol: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)
}
