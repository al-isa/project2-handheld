package com.example.stocksapp

import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.StockAPI
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry

class menu : AppCompatActivity() {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Find buttons
        val btnStocks: Button = findViewById(R.id.btn_stocks)
        val btnCrypto: Button = findViewById(R.id.btn_crypto)
        val btnOptions: Button = findViewById(R.id.btn_options)
        val btnForex: Button = findViewById(R.id.btn_forex)

        // Set click listeners
        btnStocks.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }

        btnCrypto.setOnClickListener {
            val intent = Intent(this, crypto::class.java)
            startActivity(intent)
        }

        btnOptions.setOnClickListener {
            val intent = Intent(this, options::class.java)
            startActivity(intent)
        }

        btnForex.setOnClickListener {
            val intent = Intent(this, forex::class.java)
            startActivity(intent)
        }
    }
}