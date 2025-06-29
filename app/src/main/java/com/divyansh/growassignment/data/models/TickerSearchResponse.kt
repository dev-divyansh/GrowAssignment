package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class TickerSearchResponse(
    @SerializedName("bestMatches")
    val bestMatches: List<TickerMatchDto>
)

data class TickerMatchDto(
    @SerializedName("1. symbol") val symbol: String,
    @SerializedName("2. name") val name: String
)
