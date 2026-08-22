package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val stockName: String, // e.g. RELIANCE, NIFTY, TATASTEEL
    val setup: String, // e.g. "Morning Breakout", "Resistance Rejection"
    val direction: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Int,
    val stopLoss: Double,
    val target: Double,
    val grossPnl: Double,
    val charges: Double,
    val netPnl: Double,
    val riskAmount: Double,
    val rMultiple: Double,
    val result: String, // "WIN", "LOSS", "BREAKEVEN"
    val entryTime: String = "", // "09:20 AM"
    val exitTime: String = "", // "09:55 AM"
    val notes: String = "",
    val mistakes: String = "", // Comma-separated list e.g. "FOMO,Early Entry"
    val screenshotUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
