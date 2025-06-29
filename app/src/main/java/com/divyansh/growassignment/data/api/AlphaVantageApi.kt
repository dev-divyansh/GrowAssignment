package com.divyansh.growassignment.data.api

import com.divyansh.growassignment.data.models.CompanyOverviewDto
import com.divyansh.growassignment.data.models.RealTimeQuoteDto
import com.divyansh.growassignment.data.models.TickerSearchResponse
import com.divyansh.growassignment.data.models.TopGainersLosersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import  com.divyansh.growassignment.data.models.DailyAdjustedResponse
import com.divyansh.growassignment.data.models.IntradayResponse

interface AlphaVantageApi {

    // Ticker Search
    @GET("query")
    suspend fun searchTicker(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): Response<TickerSearchResponse>

    // Company Overview
    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<CompanyOverviewDto>

    // Real-Time Quote
    @GET("query")
    suspend fun getRealTimeQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<RealTimeQuoteDto>

    // Intraday Time Series (for short-term chart)
    @GET("query")
    suspend fun getIntradayTimeSeries(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "15min",
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String
    ): Response<IntradayResponse>

    // Daily Time Series Adjusted (for long-term chart)
    @GET("query")
    suspend fun getDailyTimeSeriesAdjusted(
        @Query("function") function: String = "TIME_SERIES_DAILY_ADJUSTED",
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String
    ): Response<DailyAdjustedResponse>

    // Top Gainers and Losers
    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String
    ): Response<TopGainersLosersResponse>
}
