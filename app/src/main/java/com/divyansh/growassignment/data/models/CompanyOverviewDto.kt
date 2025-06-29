package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class CompanyOverviewDto(
    @SerializedName("Symbol") val symbol: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("Sector") val sector: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("MarketCapitalization") val marketCap: String? = null,
    @SerializedName("Information") val information: String? = null,
    @SerializedName("Note") val note: String? = null,
    @SerializedName("Error Message") val apiErrorMessage: String? = null
) {
    fun hasError(): Boolean {
        return !information.isNullOrBlank() || !note.isNullOrBlank() || !apiErrorMessage.isNullOrBlank() ||
               symbol.isNullOrBlank() || name.isNullOrBlank()
    }
    
    fun getErrorMessage(): String {
        return information ?: note ?: apiErrorMessage ?: "Invalid company data"
    }
    
    fun isValid(): Boolean {
        return !hasError() && !symbol.isNullOrBlank() && !name.isNullOrBlank()
    }
}

