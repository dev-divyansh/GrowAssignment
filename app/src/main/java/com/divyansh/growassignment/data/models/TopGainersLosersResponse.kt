package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class TopGainersLosersResponse(
    @SerializedName("top_gainers")
    val top_gainers: List<TopMoverDto>? = emptyList(),
    @SerializedName("top_losers")
    val top_losers: List<TopMoverDto>? = emptyList(),
    @SerializedName("Information")
    val information: String? = null,
    @SerializedName("Note")
    val note: String? = null,
    @SerializedName("Error Message")
    val apiErrorMessage: String? = null
) {
    fun hasError(): Boolean {
        return !information.isNullOrBlank() || !note.isNullOrBlank() || !apiErrorMessage.isNullOrBlank()
    }
    
    fun getErrorMessage(): String {
        return information ?: note ?: apiErrorMessage ?: "Unknown error"
    }
}

data class TopMoverDto(
    @SerializedName("ticker")
    val ticker: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("change_amount")
    val change_amount: String,
    @SerializedName("change_percentage")
    val change_percentage: String,
    @SerializedName("volume")
    val volume: String,
    @SerializedName("name")
    val name: String? = null
) 