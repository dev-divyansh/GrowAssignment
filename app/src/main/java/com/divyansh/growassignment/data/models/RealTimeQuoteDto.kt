package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class RealTimeQuoteDto(
    @SerializedName("Global Quote") val quote: QuoteData
)

data class QuoteData(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("10. change percent") val changePercent: String
)
