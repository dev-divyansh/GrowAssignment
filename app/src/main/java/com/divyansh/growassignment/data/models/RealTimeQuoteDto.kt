package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class RealTimeQuoteDto(
    @SerializedName("Global Quote") val quote: QuoteData? = null,
    @SerializedName("Information") val information: String? = null,
    @SerializedName("Note") val note: String? = null,
    @SerializedName("Error Message") val apiErrorMessage: String? = null
) {
    fun hasError(): Boolean {
        return quote == null || !information.isNullOrBlank() || !note.isNullOrBlank() || !apiErrorMessage.isNullOrBlank()
    }
    
    fun getErrorMessage(): String {
        return information ?: note ?: apiErrorMessage ?: "Invalid quote data"
    }
}

data class QuoteData(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("10. change percent") val changePercent: String
)
