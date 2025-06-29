package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class IntradayResponse(
    @SerializedName("Meta Data")
    val metaData: MetaData?,
    @SerializedName("Time Series (1min)")
    val timeSeries: Map<String, IntradayData>?
)

data class IntradayData(
    @SerializedName("1. open")
    val open: String?,
    @SerializedName("2. high")
    val high: String?,
    @SerializedName("3. low")
    val low: String?,
    @SerializedName("4. close")
    val close: String?,
    @SerializedName("5. volume")
    val volume: String?
)