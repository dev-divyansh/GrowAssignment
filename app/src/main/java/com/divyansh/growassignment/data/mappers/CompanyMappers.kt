package com.divyansh.growassignment.data.mappers

import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.models.CompanyOverviewDto

fun CompanyOverviewDto.toEntity(): CompanyEntity {
    return CompanyEntity(
        symbol = symbol ?: "Unknown",
        name = name ?: "Unknown Company",
        sector = sector ?: "Unknown Sector",
        description = description ?: "No description available",
        marketCap = marketCap ?: "0",
        lastUpdated = System.currentTimeMillis()
    )
}

