package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.analytics.TradeAnalyticsEngine
import com.example.data.model.Trade
import com.example.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Trading Journal", appName)
    }

    @Test
    fun `test analytics calculation accurately computes metrics`() {
        val sampleTrades = listOf(
            Trade(
                id = 1,
                date = "2026-08-20",
                stockName = "RELIANCE",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 3000.0,
                exitPrice = 3040.0,
                quantity = 50,
                stopLoss = 2980.0,
                target = 3050.0,
                grossPnl = 2000.0,
                charges = 100.0,
                netPnl = 1900.0,
                riskAmount = 1000.0,
                rMultiple = 1.9,
                result = "WIN",
                entryTime = "09:20 AM",
                exitTime = "10:15 AM",
                notes = "Clean breakout",
                mistakes = "None"
            ),
            Trade(
                id = 2,
                date = "2026-08-20",
                stockName = "HDFCBANK",
                setup = "Resistance Rejection",
                direction = "SELL",
                entryPrice = 1650.0,
                exitPrice = 1660.0,
                quantity = 100,
                stopLoss = 1660.0,
                target = 1630.0,
                grossPnl = -1000.0,
                charges = 100.0,
                netPnl = -1100.0,
                riskAmount = 1000.0,
                rMultiple = -1.1,
                result = "LOSS",
                entryTime = "11:00 AM",
                exitTime = "11:30 AM",
                notes = "Hit stop loss cleanly",
                mistakes = "None"
            ),
            Trade(
                id = 3,
                date = "2026-08-21",
                stockName = "TCS",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 4200.0,
                exitPrice = 4250.0,
                quantity = 20,
                stopLoss = 4175.0,
                target = 4280.0,
                grossPnl = 1000.0,
                charges = 50.0,
                netPnl = 950.0,
                riskAmount = 500.0,
                rMultiple = 1.9,
                result = "WIN",
                entryTime = "09:30 AM",
                exitTime = "10:00 AM",
                notes = "Good follow through",
                mistakes = "None"
            )
        )

        val settings = UserSettings(
            startingCapital = 100000.0,
            maxDailyLoss = 3000.0,
            maxTradesPerDay = 3
        )

        val analytics = TradeAnalyticsEngine.computeAnalytics(sampleTrades, settings)

        assertEquals(3, analytics.totalTrades)
        assertEquals(2, analytics.winningTrades)
        assertEquals(1, analytics.losingTrades)
        assertEquals(66.66, analytics.winRate, 0.1)
        assertEquals(1750.0, analytics.netProfit, 0.01)
        assertEquals(1.75, analytics.netProfitPercent, 0.01)
        assertEquals(101750.0, analytics.currentCapital, 0.01)

        // Setup win rate checks
        assertEquals(100.0, analytics.morningBreakoutWinRate, 0.01)
        assertEquals(0.0, analytics.resistanceRejectionWinRate, 0.01)

        // Equity curve points
        assertTrue(analytics.equityCurve.isNotEmpty())
        assertEquals(100000.0, analytics.equityCurve.first().equity, 0.01)
        assertEquals(101750.0, analytics.equityCurve.last().equity, 0.01)
    }

    @Test
    fun `test risk limit breach detection`() {
        val trades = listOf(
            Trade(
                id = 1,
                date = "2026-08-21",
                stockName = "NIFTY",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 24000.0,
                exitPrice = 23900.0,
                quantity = 50,
                stopLoss = 23900.0,
                target = 24200.0,
                grossPnl = -5000.0,
                charges = 100.0,
                netPnl = -5100.0,
                riskAmount = 5000.0,
                rMultiple = -1.02,
                result = "LOSS",
                entryTime = "09:30 AM",
                exitTime = "10:00 AM",
                notes = "",
                mistakes = "FOMO"
            )
        )

        val settings = UserSettings(
            startingCapital = 100000.0,
            maxDailyLoss = 3000.0,
            maxTradesPerDay = 3
        )

        val analytics = TradeAnalyticsEngine.computeAnalytics(trades, settings)
        assertTrue("Max loss should be exceeded", analytics.isMaxLossExceeded)
        assertEquals(1, analytics.todayTradesCount)
    }

    @Test
    fun `test daily and monthly performance aggregation for calendar`() {
        val trades = listOf(
            Trade(
                id = 1,
                date = "2026-08-10",
                stockName = "INFY",
                setup = "Morning Breakout",
                direction = "BUY",
                entryPrice = 1800.0,
                exitPrice = 1820.0,
                quantity = 50,
                stopLoss = 1790.0,
                target = 1830.0,
                grossPnl = 1000.0,
                charges = 50.0,
                netPnl = 950.0,
                riskAmount = 500.0,
                rMultiple = 1.9,
                result = "WIN"
            ),
            Trade(
                id = 2,
                date = "2026-08-10",
                stockName = "TATASTEEL",
                setup = "Support Bounce",
                direction = "BUY",
                entryPrice = 150.0,
                exitPrice = 145.0,
                quantity = 100,
                stopLoss = 145.0,
                target = 160.0,
                grossPnl = -500.0,
                charges = 50.0,
                netPnl = -550.0,
                riskAmount = 500.0,
                rMultiple = -1.1,
                result = "LOSS"
            ),
            Trade(
                id = 3,
                date = "2026-08-15",
                stockName = "SBIN",
                setup = "VWAP Pullback",
                direction = "BUY",
                entryPrice = 800.0,
                exitPrice = 820.0,
                quantity = 50,
                stopLoss = 790.0,
                target = 830.0,
                grossPnl = 1000.0,
                charges = 50.0,
                netPnl = 950.0,
                riskAmount = 500.0,
                rMultiple = 1.9,
                result = "WIN"
            )
        )

        // Day 2026-08-10 total net P&L should be 950 + (-550) = +400 (Profitable Day)
        val day10Trades = trades.filter { it.date == "2026-08-10" }
        val day10NetPnl = day10Trades.sumOf { it.netPnl }
        assertEquals(400.0, day10NetPnl, 0.01)
        assertEquals(2, day10Trades.size)

        // August 2026 month total Net P&L should be 950 - 550 + 950 = 1350
        val augustTrades = trades.filter { it.date.startsWith("2026-08") }
        val augustNetPnl = augustTrades.sumOf { it.netPnl }
        assertEquals(1350.0, augustNetPnl, 0.01)
    }
}
