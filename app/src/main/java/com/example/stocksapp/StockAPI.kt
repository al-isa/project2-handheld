package com.example.stockapp

import android.util.Log
import com.example.stocksapp.CandleData
import com.example.stocksapp.Stock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import org.json.JSONObject
import java.io.IOException

object StockAPI {
    private const val BASE_URL = "https://query1.finance.yahoo.com/v7/finance"
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    fun fetchForexData(pair: String, callback: (List<CandleData>?) -> Unit) {
        val url = "https://query1.finance.yahoo.com/v7/finance/chart/$pair?range=1mo&interval=1d"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("StockAPI", "Failed to fetch Forex data", e)
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        try {
                            val candleDataList = parseForexCandleData(body)
                            callback(candleDataList)
                        } catch (e: Exception) {
                            Log.e("StockAPI", "Error parsing Forex data", e)
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("StockAPI", "Unsuccessful response for Forex data: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    private fun parseForexCandleData(response: String): List<CandleData> {
        val candleDataList = mutableListOf<CandleData>()
        val jsonObject = JSONObject(response)
        val result = jsonObject.getJSONObject("chart").getJSONArray("result").getJSONObject(0)

        val timestamps = result.optJSONArray("timestamp")
        val indicators = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

        val opens = indicators.optJSONArray("open")
        val highs = indicators.optJSONArray("high")
        val lows = indicators.optJSONArray("low")
        val closes = indicators.optJSONArray("close")

        if (timestamps != null && opens != null && highs != null && lows != null && closes != null) {
            for (i in 0 until timestamps.length()) {
                val timestamp = timestamps.optLong(i, -1L) * 1000 // Convert to milliseconds
                if (timestamp != -1L) {
                    candleDataList.add(
                        CandleData(
                            open = String.format("%.2f", opens.optDouble(i, 0.0)).toDouble(),
                            high = String.format("%.2f", highs.optDouble(i, 0.0)).toDouble(),
                            low = String.format("%.2f", lows.optDouble(i, 0.0)).toDouble(),
                            close = String.format("%.2f", closes.optDouble(i, 0.0)).toDouble(),
                            date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(timestamp))
                        )
                    )
                }
            }
        }
        return candleDataList
    }

    fun fetchOptionsData(symbol: String, callback: (List<CandleData>?) -> Unit) {
        val url = "https://query1.finance.yahoo.com/v7/finance/options/$symbol"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("StockAPI", "Failed to fetch options data", e)
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        try {
                            val candleDataList = parseOptionsCandleData(body)
                            callback(candleDataList)
                        } catch (e: Exception) {
                            Log.e("StockAPI", "Error parsing options data", e)
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("StockAPI", "Unsuccessful response for options data: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    private fun parseOptionsCandleData(response: String): List<CandleData> {
        val candleDataList = mutableListOf<CandleData>()
        val jsonObject = JSONObject(response)
        val options = jsonObject.getJSONObject("optionChain").getJSONArray("result").getJSONObject(0)
        val quote = options.getJSONObject("quote")
        val timestamps = listOf(quote.optLong("regularMarketTime", 0L)) // Use a single timestamp for simplicity
        val open = quote.optDouble("regularMarketOpen", 0.0)
        val high = quote.optDouble("regularMarketDayHigh", 0.0)
        val low = quote.optDouble("regularMarketDayLow", 0.0)
        val close = quote.optDouble("regularMarketPrice", 0.0)

        for (timestamp in timestamps) {
            if (timestamp > 0) {
                candleDataList.add(
                    CandleData(
                        open = String.format("%.2f", open).toDouble(),
                        high = String.format("%.2f", high).toDouble(),
                        low = String.format("%.2f", low).toDouble(),
                        close = String.format("%.2f", close).toDouble(),
                        date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(timestamp * 1000))
                    )
                )
            }
        }
        return candleDataList
    }


    fun fetchCryptoData(symbol: String, callback: (List<CandleData>?) -> Unit) {
        val url = "https://query1.finance.yahoo.com/v7/finance/chart/$symbol-USD?range=1mo&interval=1h"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("StockAPI", "Failed to fetch cryptocurrency data", e)
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        try {
                            val candleDataList = parseCryptoCandleData(body)
                            callback(candleDataList)
                        } catch (e: Exception) {
                            Log.e("StockAPI", "Error parsing cryptocurrency data", e)
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("StockAPI", "Unsuccessful response for cryptocurrency data: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    private fun parseCryptoCandleData(response: String): List<CandleData> {
        val candleDataList = mutableListOf<CandleData>()
        val jsonObject = JSONObject(response)
        val result = jsonObject.getJSONObject("chart").getJSONArray("result").getJSONObject(0)
        val timestamps = result.getJSONArray("timestamp")
        val indicators = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

        val opens = indicators.getJSONArray("open")
        val highs = indicators.getJSONArray("high")
        val lows = indicators.getJSONArray("low")
        val closes = indicators.getJSONArray("close")

        for (i in 0 until timestamps.length()) {
            candleDataList.add(
                CandleData(
                    open = String.format("%.2f", opens.optDouble(i, 0.0)).toDouble(),
                    high = String.format("%.2f", highs.optDouble(i, 0.0)).toDouble(),
                    low = String.format("%.2f", lows.optDouble(i, 0.0)).toDouble(),
                    close = String.format("%.2f", closes.optDouble(i, 0.0)).toDouble(),
                    date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(timestamps.getLong(i) * 1000))
                )
            )
        }
        return candleDataList
    }


    fun fetchCandleData(symbol: String, callback: (List<CandleData>?) -> Unit) {
        val url = "$BASE_URL/chart/$symbol?range=3mo&interval=1d"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("StockAPI", "Failed to fetch candle data", e)
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body.isNullOrEmpty()) {
                        Log.e("StockAPI", "Empty response body for candle data")
                        callback(null)
                        return
                    }

                    try {
                        // Parse the response
                        val jsonObject = JSONObject(body)
                        val result = jsonObject.getJSONObject("chart").getJSONArray("result").getJSONObject(0)

                        // Extract available price data (quotes)
                        val timestamps = result.optJSONArray("timestamp")
                        val indicators = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

                        if (timestamps == null || indicators == null) {
                            Log.e("StockAPI", "Missing expected fields in response: $body")
                            callback(null)
                            return
                        }

                        val opens = indicators.optJSONArray("open")
                        val highs = indicators.optJSONArray("high")
                        val lows = indicators.optJSONArray("low")
                        val closes = indicators.optJSONArray("close")

                        val candleDataList = mutableListOf<CandleData>()
                        for (i in 0 until timestamps.length()) {
                            val timestamp = timestamps.optLong(i, -1L) * 1000 // Convert to milliseconds
                            if (timestamp != -1L) {
                                candleDataList.add(
                                    CandleData(
                                        open = opens.optDouble(i, 0.0),
                                        high = highs.optDouble(i, 0.0),
                                        low = lows.optDouble(i, 0.0),
                                        close = closes.optDouble(i, 0.0),
                                        date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(timestamp))
                                    )
                                )
                            }
                        }
                        callback(candleDataList)
                    } catch (e: Exception) {
                        Log.e("StockAPI", "Error parsing candle data response", e)
                        callback(null)
                    }
                } else {
                    Log.e("StockAPI", "Unsuccessful response: ${response.code}")
                    callback(null)
                }
            }
        })
    }



    private fun parseYahooCandleData(response: String): List<CandleData> {
        val candleDataList = mutableListOf<CandleData>()
        val jsonObject = JSONObject(response)
        val result = jsonObject.getJSONObject("chart").getJSONArray("result").getJSONObject(0)
        val timestamps = result.getJSONArray("timestamp")
        val indicators = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

        val opens = indicators.getJSONArray("open")
        val highs = indicators.getJSONArray("high")
        val lows = indicators.getJSONArray("low")
        val closes = indicators.getJSONArray("close")

        for (i in 0 until timestamps.length()) {
            candleDataList.add(
                CandleData(
                    open = String.format("%.2f", opens.optDouble(i, 0.0)).toDouble(),
                    high = String.format("%.2f", highs.optDouble(i, 0.0)).toDouble(),
                    low = String.format("%.2f", lows.optDouble(i, 0.0)).toDouble(),
                    close = String.format("%.2f", closes.optDouble(i, 0.0)).toDouble(),
                    date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(timestamps.getLong(i) * 1000))
                )
            )
        }
        return candleDataList
    }

    // Fetch Metadata from Yahoo Finance
    fun fetchStockMetadata(symbol: String, callback: (String?, String?, String?, String?, String?, String?) -> Unit) {
        val url = "$BASE_URL/chart/$symbol?range=1mo&interval=1d"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null, null, null, null, null, null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()


                    if (!responseBody.isNullOrEmpty()) {
                        Log.d("StockAPI", "Raw Yahoo Response: $responseBody")
                        try {
                            val jsonObject = JSONObject(responseBody)
                                .getJSONObject("chart")
                                .getJSONArray("result")
                                .getJSONObject(0)

                            val price = jsonObject.getJSONObject("meta")
                            val summaryDetail = jsonObject.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

                            val companyName = price.optString("longName", "Unknown Company")
                            val peRatio = summaryDetail.optDouble("trailingPE", Double.NaN).toString()
                            val eps = summaryDetail.optDouble("trailingEps", Double.NaN).toString()
                            val dividendYield = summaryDetail.optDouble("dividendYield", Double.NaN).toString()
                            val volume = summaryDetail.optJSONArray("volume")?.optInt(0)?.toString() ?: "N/A"
                            val changePercent = price.optDouble("regularMarketChangePercent", Double.NaN).toString()


                            callback(companyName, peRatio, dividendYield, eps, volume, changePercent)
                        } catch (e: Exception) {
                            Log.e("StockAPI", "Error parsing Yahoo Finance metadata response", e)
                            callback(null, null, null, null, null, null)
                        }
                    } else {
                        Log.e("StockAPI", "Error parsing Yahoo Finance metadata response")
                        callback(null, null, null, null, null, null)
                    }
                } else {
                    Log.e("StockAPI", "Error parsing Yahoo Finance metadata response")
                    callback(null, null, null, null, null, null)
                }
            }
        })
    }

}

