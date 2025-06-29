package com.divyansh.growassignment.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import com.divyansh.growassignment.data.local.entities.CompanyEntity

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist_stock_cross_ref",
    primaryKeys = ["watchlistId", "symbol"],
    foreignKeys = [
        ForeignKey(
            entity = WatchlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["watchlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = com.divyansh.growassignment.data.local.entities.CompanyEntity::class,
            parentColumns = ["symbol"],
            childColumns = ["symbol"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["symbol"])]
)
data class WatchlistStockCrossRef(
    val watchlistId: Long,
    val symbol: String
)

data class WatchlistWithStocks(
    val id: Long,
    val name: String,
    val createdAt: Long,
    @Relation(
        parentColumn = "id",
        entityColumn = "symbol",
        associateBy = androidx.room.Junction(
            value = WatchlistStockCrossRef::class,
            parentColumn = "watchlistId",
            entityColumn = "symbol"
        )
    )
    val stocks: List<CompanyEntity>
) 