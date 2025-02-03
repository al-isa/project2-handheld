package com.example.stocksapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.StockAPI
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry

class forex : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var fetchButton: Button
    private lateinit var cryptoDataTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private lateinit var cryptoChart: CandleStickChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crypto)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchEditText = findViewById(R.id.searchEditText)
        fetchButton = findViewById(R.id.fetchButton)
        cryptoChart = findViewById(R.id.stockChart)

        val cryptoData = listOf(
            Stock(
                "N/A", "N/A", 0.0,
                listOf(
                    CandleData(30000.0, 32000.0, 29000.0, 31000.0, date = "2024-12-06"),
                    CandleData(31000.0, 33000.0, 30000.0, 32000.0, date = "2024-12-07")
                )
            )
        )
        adapter = StockAdapter(cryptoData) { crypto ->
            displayCandleChart(crypto)
        }
        recyclerView.adapter = adapter

        fetchButton.setOnClickListener {
            val symbol = searchEditText.text.toString().trim()
            if (symbol.isNotEmpty()) {
                fetchCryptoData(symbol)
            }
        }
    }

    private fun displayCandleChart(crypto: Stock) {
        val entries = crypto.candleData.mapIndexed { index, candle ->
            CandleEntry(
                index.toFloat(),
                candle.high.toFloat(),
                candle.low.toFloat(),
                candle.open.toFloat(),
                candle.close.toFloat()
            )
        }

        val dataSet = CandleDataSet(entries, "${crypto.symbol} Prices").apply {
            color = resources.getColor(R.color.purple_500, theme)
            shadowColor = resources.getColor(R.color.black, theme)
            decreasingColor = resources.getColor(R.color.red, theme)
            increasingColor = resources.getColor(R.color.green, theme)
            neutralColor = resources.getColor(R.color.purple_700, theme)
            setDrawValues(false)
        }

        val candleData = com.github.mikephil.charting.data.CandleData(dataSet)
        cryptoChart.data = candleData

        cryptoChart.xAxis.apply {
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in crypto.candleData.indices) {
                        crypto.candleData[index].date
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

        cryptoChart.axisLeft.apply {
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$${value}"
                }
            }
            setDrawGridLines(true)
            axisMinimum = crypto.candleData.minOf { it.low.toFloat() } * 0.99f
            axisMaximum = crypto.candleData.maxOf { it.high.toFloat() } * 1.01f
        }

        cryptoChart.axisRight.isEnabled = false
        cryptoChart.apply {
            setBackgroundColor(resources.getColor(R.color.white, theme))
            description.isEnabled = false
            setPinchZoom(true)
            isHighlightPerDragEnabled = true
            setDrawGridBackground(false)
        }

        cryptoChart.invalidate()
    }

    private fun fetchCryptoData(symbol: String) {
        StockAPI.fetchForexData(symbol) { candleData ->
            if (candleData != null) {
                runOnUiThread {
                    val crypto = Stock(
                        symbol = symbol,
                        companyName = "$symbol Cryptocurrency",
                        currentPrice = String.format("%.2f", candleData.last().close).toDouble(),
                        candleData = candleData
                    )
                    adapter.updateData(listOf(crypto))
                    displayCandleChart(crypto)
                }
            }
        }
    }
}