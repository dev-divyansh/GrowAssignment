package com.divyansh.growassignment.data.mappers

import com.divyansh.growassignment.data.local.entities.CompanyEntity
import com.divyansh.growassignment.data.models.CompanyOverviewDto

fun CompanyOverviewDto.toEntity(): CompanyEntity {
    return CompanyEntity(
        symbol = symbol,
        name = name,
        sector = sector,
        description = description,
        marketCap = marketCap,
        lastUpdated = System.currentTimeMillis()
    )
}

