package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Trade
import com.example.data.model.TradingRule
import com.example.data.model.TradingSetup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Trade::class, TradingRule::class, TradingSetup::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun tradingRuleDao(): TradingRuleDao
    abstract fun tradingSetupDao(): TradingSetupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trading_journal_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val setupDao = database.tradingSetupDao()
                val initialSetups = listOf(
                    TradingSetup(name = "Morning Breakout", description = "High volume breakout in the first 30 mins above pre-market / opening high", isDefault = true),
                    TradingSetup(name = "Resistance Rejection", description = "Rejection wick at key daily/weekly resistance level with bearish confirmation", isDefault = true),
                    TradingSetup(name = "Support Bounce", description = "Long entry on clean bounce from major support or previous day high/low", isDefault = true),
                    TradingSetup(name = "VWAP Pullback", description = "Continuation trade when price tests VWAP and resumes trend", isDefault = true),
                    TradingSetup(name = "Opening Range Breakout (ORB)", description = "15-minute range breakout with strong volume spike", isDefault = true),
                    TradingSetup(name = "EMA 9/21 Cross", description = "Trend following entry on fast exponential moving average crossover", isDefault = true)
                )
                setupDao.insertSetups(initialSetups)

                val ruleDao = database.tradingRuleDao()
                val initialRules = listOf(
                    TradingRule(title = "Maximum 3 trades per day", description = "Stop trading immediately after 3 completed executions to prevent overtrading", isEnabled = true),
                    TradingRule(title = "Strict Stop Loss on every trade", description = "Always calculate position size and never move stop loss wider", isEnabled = true),
                    TradingRule(title = "No Revenge Trading", description = "Take a 30-minute break after any losing trade before taking another setup", isEnabled = true),
                    TradingRule(title = "Minimum 1:2 Risk to Reward", description = "Target must be at least double the risk before entering", isEnabled = true),
                    TradingRule(title = "Trade only confirmed A+ Setups", description = "Wait for candle close and volume confirmation; avoid guessing", isEnabled = true)
                )
                ruleDao.insertRules(initialRules)

                // Populate sample trades so first-time users can see the dashboard, equity curve, metrics, and setup analysis immediately
                val tradeDao = database.tradeDao()
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
                        notes = "Clean breakout of morning range with heavy institutional buying.",
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
                        notes = "Strong shooting star pattern on 15m chart at major resistance zone.",
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
                        notes = "Metal sector witnessed sudden selling pressure, stop loss triggered strictly.",
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
                        notes = "IT index opened gap-up and held above previous day high with rising volume.",
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
                        notes = "Entered slightly late without waiting for breakdown candle close.",
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
                        notes = "Auto index led the market rally. Superb momentum follow-through.",
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
                        notes = "Tested 1240 resistance three times on 5m chart, broke down cleanly.",
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
                        notes = "False breakout with immediate mean reversion. Stop loss hit smoothly.",
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
                        notes = "Triple bottom on intraday VWAP support. Clean risk-reward.",
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
        }
    }
}
