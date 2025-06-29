package com.divyansh.growassignment.data.models

data class TopGainersLosersResponse(
    val top_gainers: List<TopMoverDto>,
    val top_losers: List<TopMoverDto>
)

data class TopMoverDto(
    val ticker: String,
    val price: String,
    val change_amount: String,
    val change_percentage: String,
    val volume: String,
    val name: String?
) 