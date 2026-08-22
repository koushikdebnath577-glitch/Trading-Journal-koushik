package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.TradeDao
import com.example.data.db.TradingRuleDao
import com.example.data.db.TradingSetupDao
import com.example.data.model.Trade
import com.example.data.model.TradingRule
import com.example.data.model.TradingSetup
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TradingRepository(
    private val tradeDao: TradeDao,
    private val ruleDao: TradingRuleDao,
    private val setupDao: TradingSetupDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("trading_journal_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow = _settingsFlow.asStateFlow()

    val allTrades: Flow<List<Trade>> = tradeDao.getAllTrades()
    val allTradesChronological: Flow<List<Trade>> = tradeDao.getAllTradesChronological()
    val allRules: Flow<List<TradingRule>> = ruleDao.getAllRules()
    val allSetups: Flow<List<TradingSetup>> = setupDao.getAllSetups()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            startingCapital = prefs.getFloat("starting_capital", 100000.0f).toDouble(),
            currencySymbol = prefs.getString("currency_symbol", "₹") ?: "₹",
            defaultRiskAmount = prefs.getFloat("default_risk", 1000.0f).toDouble(),
            maxDailyLoss = prefs.getFloat("max_daily_loss", 3000.0f).toDouble(),
            maxTradesPerDay = prefs.getInt("max_trades_day", 3),
            defaultChargesRate = prefs.getFloat("charges_rate", 40.0f).toDouble(),
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        )
    }

    fun updateSettings(settings: UserSettings) {
        prefs.edit().apply {
            putFloat("starting_capital", settings.startingCapital.toFloat())
            putString("currency_symbol", settings.currencySymbol)
            putFloat("default_risk", settings.defaultRiskAmount.toFloat())
            putFloat("max_daily_loss", settings.maxDailyLoss.toFloat())
            putInt("max_trades_day", settings.maxTradesPerDay)
            putFloat("charges_rate", settings.defaultChargesRate.toFloat())
            putString("theme_mode", settings.themeMode)
            apply()
        }
        _settingsFlow.value = settings
    }

    suspend fun insertTrade(trade: Trade): Long = tradeDao.insertTrade(trade)
    suspend fun updateTrade(trade: Trade) = tradeDao.updateTrade(trade)
    suspend fun deleteTrade(trade: Trade) = tradeDao.deleteTrade(trade)
    suspend fun deleteTradeById(id: Long) = tradeDao.deleteTradeById(id)
    suspend fun deleteAllTrades() = tradeDao.deleteAllTrades()

    suspend fun insertRule(rule: TradingRule) = ruleDao.insertRule(rule)
    suspend fun updateRule(rule: TradingRule) = ruleDao.updateRule(rule)
    suspend fun deleteRule(rule: TradingRule) = ruleDao.deleteRule(rule)

    suspend fun insertSetup(setup: TradingSetup) = setupDao.insertSetup(setup)
    suspend fun deleteSetup(setup: TradingSetup) = setupDao.deleteSetup(setup)

    suspend fun resetSampleData() {
        tradeDao.deleteAllTrades()
        val sampleTrades = listOf(
            Trade(
                date = "2026-08-10",
                stockName = "RELIANCE",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 2850.0,
                exitPrice = 2885.0,
                quantity = 50,
                stopLoss = 2835.0,
                target = 2880.0,
                grossPnl = 1750.0,
                charges = 45.0,
                netPnl = 1705.0,
                riskAmount = 750.0,
                rMultiple = 2.27,
                result = "WIN",
                entryTime = "09:25 AM",
                exitTime = "10:10 AM",
                notes = "Clean breakout of morning range with heavy volume.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-11",
                stockName = "HDFCBANK",
                setup = "Resistance Rejection",
                direction = "SELL",
                entryPrice = 1620.0,
                exitPrice = 1605.0,
                quantity = 100,
                stopLoss = 1630.0,
                target = 1600.0,
                grossPnl = 1500.0,
                charges = 50.0,
                netPnl = 1450.0,
                riskAmount = 1000.0,
                rMultiple = 1.45,
                result = "WIN",
                entryTime = "10:15 AM",
                exitTime = "11:05 AM",
                notes = "Shooting star on 15m chart at resistance zone.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-12",
                stockName = "TATASTEEL",
                setup = "Support Bounce",
                direction = "BUY",
                entryPrice = 154.0,
                exitPrice = 151.5,
                quantity = 400,
                stopLoss = 151.5,
                target = 159.0,
                grossPnl = -1000.0,
                charges = 40.0,
                netPnl = -1040.0,
                riskAmount = 1000.0,
                rMultiple = -1.04,
                result = "LOSS",
                entryTime = "09:40 AM",
                exitTime = "10:05 AM",
                notes = "Sector weakness triggered SL strictly.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-13",
                stockName = "INFY",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 1820.0,
                exitPrice = 1852.0,
                quantity = 60,
                stopLoss = 1805.0,
                target = 1850.0,
                grossPnl = 1920.0,
                charges = 48.0,
                netPnl = 1872.0,
                riskAmount = 900.0,
                rMultiple = 2.08,
                result = "WIN",
                entryTime = "09:30 AM",
                exitTime = "10:45 AM",
                notes = "IT index opened gap-up and held morning high.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-14",
                stockName = "NIFTY",
                setup = "Resistance Rejection",
                direction = "SELL",
                entryPrice = 24850.0,
                exitPrice = 24890.0,
                quantity = 25,
                stopLoss = 24890.0,
                target = 24750.0,
                grossPnl = -1000.0,
                charges = 42.0,
                netPnl = -1042.0,
                riskAmount = 1000.0,
                rMultiple = -1.04,
                result = "LOSS",
                entryTime = "11:20 AM",
                exitTime = "11:50 AM",
                notes = "Entered without candle close confirmation.",
                mistakes = "Late Entry,FOMO"
            ),
            Trade(
                date = "2026-08-17",
                stockName = "TATAMOTORS",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 980.0,
                exitPrice = 1004.0,
                quantity = 100,
                stopLoss = 970.0,
                target = 1000.0,
                grossPnl = 2400.0,
                charges = 52.0,
                netPnl = 2348.0,
                riskAmount = 1000.0,
                rMultiple = 2.35,
                result = "WIN",
                entryTime = "09:20 AM",
                exitTime = "10:30 AM",
                notes = "Auto index rally leader, huge volume.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-18",
                stockName = "ICICIBANK",
                setup = "Resistance Rejection",
                direction = "SELL",
                entryPrice = 1240.0,
                exitPrice = 1222.0,
                quantity = 80,
                stopLoss = 1250.0,
                target = 1220.0,
                grossPnl = 1440.0,
                charges = 46.0,
                netPnl = 1394.0,
                riskAmount = 800.0,
                rMultiple = 1.74,
                result = "WIN",
                entryTime = "01:15 PM",
                exitTime = "02:25 PM",
                notes = "Triple rejection at 1240 level.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-19",
                stockName = "SBIN",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 820.0,
                exitPrice = 812.0,
                quantity = 150,
                stopLoss = 812.0,
                target = 836.0,
                grossPnl = -1200.0,
                charges = 45.0,
                netPnl = -1245.0,
                riskAmount = 1200.0,
                rMultiple = -1.04,
                result = "LOSS",
                entryTime = "09:35 AM",
                exitTime = "10:00 AM",
                notes = "Whipsaw breakdown, SL saved capital.",
                mistakes = "Early Entry"
            ),
            Trade(
                date = "2026-08-20",
                stockName = "BAJFINANCE",
                setup = "Support Bounce",
                direction = "BUY",
                entryPrice = 7150.0,
                exitPrice = 7260.0,
                quantity = 20,
                stopLoss = 7100.0,
                target = 7250.0,
                grossPnl = 2200.0,
                charges = 55.0,
                netPnl = 2145.0,
                riskAmount = 1000.0,
                rMultiple = 2.15,
                result = "WIN",
                entryTime = "10:00 AM",
                exitTime = "11:40 AM",
                notes = "Triple bottom on intraday VWAP support.",
                mistakes = ""
            ),
            Trade(
                date = "2026-08-21",
                stockName = "TCS",
                setup = "VWAP Pullback",
                direction = "BUY",
                entryPrice = 4220.0,
                exitPrice = 4268.0,
                quantity = 30,
                stopLoss = 4195.0,
                target = 4270.0,
                grossPnl = 1440.0,
                charges = 48.0,
                netPnl = 1392.0,
                riskAmount = 750.0,
                rMultiple = 1.86,
                result = "WIN",
                entryTime = "11:10 AM",
                exitTime = "12:15 PM",
                notes = "Healthy consolidation near VWAP with breakout on volume.",
                mistakes = ""
            )
        )
        tradeDao.insertTrades(sampleTrades)
    }

    fun exportToCsv(trades: List<Trade>): String {
        val sb = StringBuilder()
        sb.append("Date,Stock,Setup,Direction,Entry Price,Exit Price,Quantity,Stop Loss,Target,Gross PnL,Charges,Net PnL,Risk Amount,R Multiple,Result,Entry Time,Exit Time,Mistakes,Notes\n")
        trades.forEach { t ->
            sb.append("\"${t.date}\",")
            sb.append("\"${t.stockName}\",")
            sb.append("\"${t.setup}\",")
            sb.append("\"${t.direction}\",")
            sb.append("${t.entryPrice},")
            sb.append("${t.exitPrice},")
            sb.append("${t.quantity},")
            sb.append("${t.stopLoss},")
            sb.append("${t.target},")
            sb.append("${t.grossPnl},")
            sb.append("${t.charges},")
            sb.append("${t.netPnl},")
            sb.append("${t.riskAmount},")
            sb.append("${t.rMultiple},")
            sb.append("\"${t.result}\",")
            sb.append("\"${t.entryTime}\",")
            sb.append("\"${t.exitTime}\",")
            sb.append("\"${t.mistakes}\",")
            sb.append("\"${t.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}
