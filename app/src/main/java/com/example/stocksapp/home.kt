package com.example.stocksapp

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.StockAPI
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class home : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var fetchButton: Button
    private lateinit var stockDataTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private lateinit var stockChart: CandleStickChart
    private lateinit var peRatioTextView: TextView
    private lateinit var dividendYieldTextView: TextView
    private lateinit var volumeTextView: TextView
    private lateinit var changePercentTextView: TextView
    private lateinit var epsTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        // Test fetching stock data
        searchEditText = findViewById(R.id.searchEditText)
        fetchButton = findViewById(R.id.fetchButton)
        stockChart = findViewById(R.id.stockChart)

        // Performance metrics UI
        peRatioTextView = findViewById(R.id.peRatioTextView)
        dividendYieldTextView = findViewById(R.id.dividendYieldTextView)
        volumeTextView = findViewById(R.id.volumeTextView)
        changePercentTextView = findViewById(R.id.changePercentTextView)
        epsTextView = findViewById(R.id.epsTextView)

        val stockData = listOf(
            Stock(
                "Symbol", "Company Name", 0.0,
                listOf(
                    CandleData(148.0, 152.0, 147.0, 151.0, date = "2024-12-06"),
                    CandleData(151.0, 155.0, 149.0, 153.0, date = "2024-12-07"),
                    CandleData(153.0, 157.0, 152.0, 156.0, date = "2024-12-08"),
                    CandleData(156.0, 159.0, 155.0, 157.0, date = "2024-12-09")
                )
            )
        )
        adapter = StockAdapter(stockData) { stock ->
            displayCandleChart(stock)
        }


        recyclerView.adapter = adapter

        fetchButton.setOnClickListener {
            val symbol = searchEditText.text.toString().trim()
            if (symbol.isNotEmpty()) {
                fetchStockData(symbol)
                saveRecentSearch(symbol)
            }
        }


    }

    private fun saveRecentSearch(symbol: String) {
        val userId = auth.currentUser?.uid ?: return

        val searchEntry = mapOf(
            "symbol" to symbol,
        )

        val userSearchesRef = database.child("recentSearches").child(userId)

        userSearchesRef.push().setValue(searchEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Search saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to save search: ${e.message}")
            }
    }

    private fun displayCandleChart(stock: Stock) {
        val entries = stock.candleData.mapIndexed { index, candle ->
            CandleEntry(
                index.toFloat(),
                candle.high.toFloat(),
                candle.low.toFloat(),
                candle.open.toFloat(),
                candle.close.toFloat()
            )
        }

        // Create the CandleDataSet
        val dataSet = CandleDataSet(entries, "${stock.symbol} Prices").apply {
            color = resources.getColor(R.color.purple_500, theme)
            shadowColor = resources.getColor(R.color.black, theme)
            decreasingColor = resources.getColor(R.color.red, theme)
            increasingColor = resources.getColor(R.color.green, theme)
            neutralColor = resources.getColor(R.color.purple_700, theme)
            setDrawValues(false)
        }

        // Apply the dataset to the chart
        val candleData = com.github.mikephil.charting.data.CandleData(dataSet)
        stockChart.data = candleData

        // Configure the X-axis to show dates/hours
        stockChart.xAxis.apply {
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in stock.candleData.indices) {
                        stock.candleData[index].date // Assuming date is a formatted string like "MM/dd" or "HH:mm"
                    } else {
                        ""
                    }
                }
            }
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = true
            labelRotationAngle = -45f

        }

        // Configure the Y-axis to show dollar signs
        stockChart.axisLeft.apply {
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$${String.format("%.2f", value)}"
                }
            }
            setDrawGridLines(true)
            axisMinimum = stock.candleData.minOf { it.low.toFloat() } * 0.99f
            axisMaximum = stock.candleData.maxOf { it.high.toFloat() } * 1.01f
        }
        // Disable the right Y-axis
        stockChart.axisRight.isEnabled = false

        // General chart appearance
        stockChart.apply {
            setBackgroundColor(resources.getColor(R.color.white, theme))
            description.isEnabled = false
            setPinchZoom(true)
            isHighlightPerDragEnabled = true
            setDrawGridBackground(false)
        }

        stockChart.invalidate() // Refresh the chart
    }


    private fun fetchStockData(symbol: String) {
        StockAPI.fetchCandleData(symbol) { candleData ->
            if (candleData != null) {
                StockAPI.fetchStockMetadata(symbol) { companyName, pe, dividendYield, eps, volume, changePercent ->
                    runOnUiThread {
                        val stock = Stock(
                            symbol = symbol,
                            companyName = companyName ?: "Unknown Company",
                            currentPrice = String.format("%.2f", candleData.last().close).toDouble(), // Format price to 2dp
                            candleData = candleData
                        )
                        peRatioTextView.text = "P/E Ratio: ${pe ?: "N/A"}"
                        dividendYieldTextView.text = "Dividend Yield: ${dividendYield ?: "N/A"}"
                        volumeTextView.text = "Volume: ${volume ?: "N/A"}"
                        changePercentTextView.text = "Change Percentage: ${String.format("%.2f", changePercent?.toDoubleOrNull() ?: 0.0)}%"
                        epsTextView.text = "EPS: ${String.format("%.2f", eps?.toDoubleOrNull() ?: 0.0)}"
                        Log.d("StockAPI", "Metadata: P/E: $pe, Dividend Yield: $dividendYield, EPS: $eps, Volume: $volume, Change Percentage: $changePercent")


                        // Update your adapter and chart
                        adapter.updateData(listOf(stock))
                        displayCandleChart(stock)
                    }
                }
            }
        }
    }
}