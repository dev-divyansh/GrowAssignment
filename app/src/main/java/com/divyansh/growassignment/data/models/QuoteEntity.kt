package com.divyansh.growassignment.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote")
data class QuoteEntity(
    @PrimaryKey val symbol: String,
    val price: String,
    val changePercent: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
