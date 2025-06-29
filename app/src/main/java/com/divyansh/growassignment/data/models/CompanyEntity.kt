package com.divyansh.growassignment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_overview")
data class CompanyEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val sector: String,
    val description: String,
    val marketCap: String,
    val lastUpdated: Long
)

