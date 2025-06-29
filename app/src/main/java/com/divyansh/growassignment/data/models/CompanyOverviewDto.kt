package com.divyansh.growassignment.data.models

import com.google.gson.annotations.SerializedName

data class CompanyOverviewDto(
    @SerializedName("Symbol") val symbol: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Sector") val sector: String,
    @SerializedName("Description") val description: String,
    @SerializedName("MarketCapitalization") val marketCap: String
)

