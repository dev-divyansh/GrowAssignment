package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class DailyAdjustedResponse(
    @SerializedName("Meta Data")
    val metaData: MetaData?,
    @SerializedName("Time Series (Daily Adjusted)")
    val timeSeries: Map<String, DailyAdjustedData>?
)

data class MetaData(
    @SerializedName("1. Information")
    val information: String?,
    @SerializedName("2. Symbol")
    val symbol: String?,
    @SerializedName("3. Last Refreshed")
    val lastRefreshed: String?,
    @SerializedName("4. Output Size")
    val outputSize: String?,
    @SerializedName("5. Time Zone")
    val timeZone: String?
)

data class DailyAdjustedData(
    @SerializedName("1. open")
    val open: String?,
    @SerializedName("2. high")
    val high: String?,
    @SerializedName("3. low")
    val low: String?,
    @SerializedName("4. close")
    val close: String?,
    @SerializedName("5. adjusted close")
    val adjustedClose: String?,
    @SerializedName("6. volume")
    val volume: String?,
    @SerializedName("7. dividend amount")
    val dividendAmount: String?,
    @SerializedName("8. split coefficient")
    val splitCoefficient: String?
) 