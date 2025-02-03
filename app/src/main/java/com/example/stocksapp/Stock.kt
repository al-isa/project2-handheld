package com.example.stocksapp

data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val candleData: List<CandleData> // For chart data
)

data class CandleData(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val date: String
)
