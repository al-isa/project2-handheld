package com.example.stocksapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(
    private var stocks: List<Stock>,
    private val onItemClick: (Stock) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    fun updateData(newStocks: List<Stock>) {
        stocks = newStocks
        notifyDataSetChanged()
    }

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbolTextView: TextView = itemView.findViewById(R.id.symbolTextView)
        val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameTextView)
        val currentPriceTextView: TextView = itemView.findViewById(R.id.currentPriceTextView)

        fun bind(stock: Stock) {
            symbolTextView.text = stock.symbol
            companyNameTextView.text = stock.companyName
            currentPriceTextView.text = "$${stock.currentPrice}"
            itemView.setOnClickListener { onItemClick(stock) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stocks[position])
    }

    override fun getItemCount() = stocks.size
}