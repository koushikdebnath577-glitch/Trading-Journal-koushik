package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_rules")
data class TradingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isEnabled: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trading_setups")
data class TradingSetup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val isDefault: Boolean = false
)

data class UserSettings(
    val startingCapital: Double = 100000.0,
    val currencySymbol: String = "₹",
    val defaultRiskAmount: Double = 1000.0,
    val maxDailyLoss: Double = 3000.0,
    val maxTradesPerDay: Int = 3,
    val defaultChargesRate: Double = 40.0, // standard approx ₹40 per trade (buy+sell)
    val themeMode: String = "SYSTEM" // "DARK", "LIGHT", "SYSTEM"
)
