package com.divyansh.growassignment.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "top_mover")
data class TopMoverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "gainer" or "loser"
    val symbol: String,
    val name: String?,
    val price: String,
    val changeAmount: String,
    val changePercentage: String,
    val volume: String,
    val lastUpdated: Long = System.currentTimeMillis()
) 