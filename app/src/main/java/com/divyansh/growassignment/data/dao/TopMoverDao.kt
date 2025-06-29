package com.divyansh.growassignment.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.divyansh.growassignment.data.models.TopMoverEntity

@Dao
interface TopMoverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movers: List<TopMoverEntity>)

    @Query("SELECT * FROM top_mover WHERE type = :type ORDER BY lastUpdated DESC")
    suspend fun getByType(type: String): List<TopMoverEntity>

    @Query("DELETE FROM top_mover WHERE lastUpdated < :expiry")
    suspend fun deleteOlderThan(expiry: Long)

    @Query("DELETE FROM top_mover")
    suspend fun clearAll()
} 