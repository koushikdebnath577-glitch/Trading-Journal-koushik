package com.example.analytics

import com.example.data.model.Trade
import com.example.data.model.UserSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class EquityPoint(
    val tradeIndex: Int,
    val tradeId: Long,
    val date: String,
    val stockName: String,
    val netPnl: Double,
    val cumulativeNetPnl: Double,
    val equity: Double
)

data class SetupStats(
    val setupName: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double,
    val averageR: Double,
    val averageWinR: Double,
    val totalNetPnl: Double,
    val profitFactor: Double,
    val expectancy: Double
)

data class DailyPerformance(
    val date: String,
    val tradeCount: Int,
    val grossPnl: Double,
    val charges: Double,
    val netPnl: Double,
    val totalR: Double,
    val winRate: Double,
    val trades: List<Trade>
)

data class PeriodPerformance(
    val periodName: String, // e.g. "Aug 2026", "Week 33"
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val netPnl: Double,
    val netProfitPercent: Double,
    val winRate: Double,
    val averageR: Double,
    val expectancy: Double,
    val profitFactor: Double,
    val bestSetup: String,
    val worstSetup: String,
    val maxDrawdown: Double
)

data class MistakeAnalysis(
    val mostCommonMistake: String,
    val totalTradesWithMistakes: Int,
    val totalPnlLossFromMistakes: Double,
    val winRateWithMistakes: Double,
    val winRateWithoutMistakes: Double,
    val mistakeCounts: Map<String, Int>
)

data class PerformanceHighlights(
    val bestTrade: Trade?,
    val worstTrade: Trade?,
    val bestDay: DailyPerformance?,
    val worstDay: DailyPerformance?,
    val bestSetup: String,
    val worstSetup: String,
    val highestRTrade: Trade?,
    val largestLossTrade: Trade?
)

data class OverallAnalytics(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val breakevenTrades: Int = 0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val averageR: Double = 0.0,
    val averageWinR: Double = 0.0,
    val averageLossR: Double = 0.0,
    val expectancy: Double = 0.0,
    val profitFactor: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val totalGrossLoss: Double = 0.0,
    val totalCharges: Double = 0.0,
    val netProfit: Double = 0.0,
    val netProfitPercent: Double = 0.0,
    val startingCapital: Double = 100000.0,
    val currentCapital: Double = 100000.0,
    val peakEquity: Double = 100000.0,
    val currentDrawdown: Double = 0.0,
    val currentDrawdownPercent: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val maxDrawdownPercent: Double = 0.0,
    val morningBreakoutWinRate: Double = 0.0,
    val resistanceRejectionWinRate: Double = 0.0,
    val equityCurve: List<EquityPoint> = emptyList(),
    val setupStats: List<SetupStats> = emptyList(),
    val dailyPerformanceList: List<DailyPerformance> = emptyList(),
    val weeklyPerformanceList: List<PeriodPerformance> = emptyList(),
    val monthlyPerformanceList: List<PeriodPerformance> = emptyList(),
    val mistakeAnalysis: MistakeAnalysis = MistakeAnalysis("None", 0, 0.0, 0.0, 0.0, emptyMap()),
    val highlights: PerformanceHighlights = PerformanceHighlights(null, null, null, null, "N/A", "N/A", null, null),
    val rDistribution: Map<String, Int> = emptyMap(),
    // Today's discipline status
    val todayTradesCount: Int = 0,
    val todayNetPnl: Double = 0.0,
    val isMaxTradesExceeded: Boolean = false,
    val isMaxLossExceeded: Boolean = false
)

object TradeAnalyticsEngine {

    fun computeAnalytics(trades: List<Trade>, settings: UserSettings): OverallAnalytics {
        if (trades.isEmpty()) {
            return OverallAnalytics(
                startingCapital = settings.startingCapital,
                currentCapital = settings.startingCapital,
                peakEquity = settings.startingCapital
            )
        }

        // Sort trades chronologically (oldest to newest) for cumulative equity curve and drawdown
        val chronologicalTrades = trades.sortedWith(
            compareBy<Trade> { it.date }.thenBy { it.timestamp }
        )

        val totalTrades = trades.size
        val winningTradesList = trades.filter { it.netPnl > 0 }
        val losingTradesList = trades.filter { it.netPnl < 0 }
        val breakevenTradesList = trades.filter { it.netPnl == 0.0 }

        val winningTrades = winningTradesList.size
        val losingTrades = losingTradesList.size
        val breakevenTrades = breakevenTradesList.size

        val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0
        val lossRate = if (totalTrades > 0) (losingTrades.toDouble() / totalTrades) * 100.0 else 0.0

        val totalR = trades.sumOf { it.rMultiple }
        val averageR = if (totalTrades > 0) totalR / totalTrades else 0.0

        val winningRSum = winningTradesList.sumOf { it.rMultiple }
        val averageWinR = if (winningTrades > 0) winningRSum / winningTrades else 0.0

        val losingRSum = losingTradesList.sumOf { it.rMultiple }
        val averageLossR = if (losingTrades > 0) losingRSum / losingTrades else 0.0

        // Expectancy = Total R / Total Trades or (WinRate/100 * AvgWinR) - (LossRate/100 * AvgLossR)
        val expectancy = if (totalTrades > 0) totalR / totalTrades else 0.0

        val totalGrossProfit = trades.filter { it.grossPnl > 0 }.sumOf { it.grossPnl }
        val totalGrossLoss = trades.filter { it.grossPnl < 0 }.sumOf { it.grossPnl }
        val totalCharges = trades.sumOf { it.charges }
        val netProfit = trades.sumOf { it.netPnl }

        val startingCapital = settings.startingCapital
        val currentCapital = startingCapital + netProfit
        val netProfitPercent = if (startingCapital > 0) (netProfit / startingCapital) * 100.0 else 0.0

        val profitFactor = if (abs(totalGrossLoss) > 0.0001) {
            totalGrossProfit / abs(totalGrossLoss)
        } else if (totalGrossProfit > 0) {
            99.99
        } else {
            0.0
        }

        // Equity curve & Drawdown calculation
        var runningCumulativeNetPnl = 0.0
        var peakCumulativePnl = 0.0
        var peakEquity = startingCapital
        var maxDrawdownAmount = 0.0
        var maxDrawdownPct = 0.0

        val equityCurve = mutableListOf<EquityPoint>()
        // Base starting point at index 0
        equityCurve.add(
            EquityPoint(
                tradeIndex = 0,
                tradeId = 0L,
                date = chronologicalTrades.firstOrNull()?.date ?: "Start",
                stockName = "Start Capital",
                netPnl = 0.0,
                cumulativeNetPnl = 0.0,
                equity = startingCapital
            )
        )

        chronologicalTrades.forEachIndexed { index, trade ->
            runningCumulativeNetPnl += trade.netPnl
            val currentEquity = startingCapital + runningCumulativeNetPnl

            if (currentEquity > peakEquity) {
                peakEquity = currentEquity
            }
            if (runningCumulativeNetPnl > peakCumulativePnl) {
                peakCumulativePnl = runningCumulativeNetPnl
            }

            val drawdownFromPeak = peakEquity - currentEquity
            if (drawdownFromPeak > maxDrawdownAmount) {
                maxDrawdownAmount = drawdownFromPeak
                maxDrawdownPct = if (peakEquity > 0) (maxDrawdownAmount / peakEquity) * 100.0 else 0.0
            }

            equityCurve.add(
                EquityPoint(
                    tradeIndex = index + 1,
                    tradeId = trade.id,
                    date = trade.date,
                    stockName = trade.stockName,
                    netPnl = trade.netPnl,
                    cumulativeNetPnl = runningCumulativeNetPnl,
                    equity = currentEquity
                )
            )
        }

        val currentDrawdown = max(0.0, peakEquity - currentCapital)
        val currentDrawdownPercent = if (peakEquity > 0) (currentDrawdown / peakEquity) * 100.0 else 0.0

        // Setups calculation
        val setupGroups = trades.groupBy { it.setup.ifBlank { "Unassigned" } }
        val setupStats = setupGroups.map { (setupName, sTrades) ->
            val sTotal = sTrades.size
            val sWins = sTrades.count { it.netPnl > 0 }
            val sLosses = sTrades.count { it.netPnl < 0 }
            val sWinRate = if (sTotal > 0) (sWins.toDouble() / sTotal) * 100.0 else 0.0
            val sTotalR = sTrades.sumOf { it.rMultiple }
            val sAvgR = if (sTotal > 0) sTotalR / sTotal else 0.0
            val sWinTrades = sTrades.filter { it.netPnl > 0 }
            val sAvgWinR = if (sWinTrades.isNotEmpty()) sWinTrades.sumOf { it.rMultiple } / sWinTrades.size else 0.0
            val sNetPnl = sTrades.sumOf { it.netPnl }
            val sGrossProfit = sTrades.filter { it.grossPnl > 0 }.sumOf { it.grossPnl }
            val sGrossLoss = sTrades.filter { it.grossPnl < 0 }.sumOf { it.grossPnl }
            val sPf = if (abs(sGrossLoss) > 0.0001) sGrossProfit / abs(sGrossLoss) else if (sGrossProfit > 0) 99.9 else 0.0
            val sExpectancy = if (sTotal > 0) sTotalR / sTotal else 0.0

            SetupStats(
                setupName = setupName,
                totalTrades = sTotal,
                winningTrades = sWins,
                losingTrades = sLosses,
                winRate = sWinRate,
                averageR = sAvgR,
                averageWinR = sAvgWinR,
                totalNetPnl = sNetPnl,
                profitFactor = sPf,
                expectancy = sExpectancy
            )
        }.sortedByDescending { it.totalNetPnl }

        val morningBreakoutWinRate = setupStats.find { it.setupName.equals("Morning Breakout", ignoreCase = true) }?.winRate ?: 0.0
        val resistanceRejectionWinRate = setupStats.find { it.setupName.equals("Resistance Rejection", ignoreCase = true) }?.winRate ?: 0.0

        // Daily Performance
        val dailyGroups = trades.groupBy { it.date }.toSortedMap(reverseOrder())
        val dailyPerformanceList = dailyGroups.map { (date, dTrades) ->
            val dTotal = dTrades.size
            val dGross = dTrades.sumOf { it.grossPnl }
            val dCharges = dTrades.sumOf { it.charges }
            val dNet = dTrades.sumOf { it.netPnl }
            val dR = dTrades.sumOf { it.rMultiple }
            val dWins = dTrades.count { it.netPnl > 0 }
            val dWinRate = if (dTotal > 0) (dWins.toDouble() / dTotal) * 100.0 else 0.0
            DailyPerformance(
                date = date,
                tradeCount = dTotal,
                grossPnl = dGross,
                charges = dCharges,
                netPnl = dNet,
                totalR = dR,
                winRate = dWinRate,
                trades = dTrades
            )
        }

        // Monthly Performance
        val monthlyGroups = trades.groupBy {
            if (it.date.length >= 7) it.date.substring(0, 7) else "Unknown"
        }.toSortedMap(reverseOrder())

        val monthlyPerformanceList = monthlyGroups.map { (monthStr, mTrades) ->
            val mTotal = mTrades.size
            val mWins = mTrades.count { it.netPnl > 0 }
            val mLosses = mTrades.count { it.netPnl < 0 }
            val mNet = mTrades.sumOf { it.netPnl }
            val mNetPct = if (startingCapital > 0) (mNet / startingCapital) * 100.0 else 0.0
            val mWinRate = if (mTotal > 0) (mWins.toDouble() / mTotal) * 100.0 else 0.0
            val mTotalR = mTrades.sumOf { it.rMultiple }
            val mAvgR = if (mTotal > 0) mTotalR / mTotal else 0.0
            val mGrossProfit = mTrades.filter { it.grossPnl > 0 }.sumOf { it.grossPnl }
            val mGrossLoss = mTrades.filter { it.grossPnl < 0 }.sumOf { it.grossPnl }
            val mPf = if (abs(mGrossLoss) > 0.0001) mGrossProfit / abs(mGrossLoss) else if (mGrossProfit > 0) 99.9 else 0.0
            val mExp = if (mTotal > 0) mTotalR / mTotal else 0.0

            val setupInMonth = mTrades.groupBy { it.setup }
            val bestSetupInMonth = setupInMonth.maxByOrNull { it.value.sumOf { tr -> tr.netPnl } }?.key ?: "N/A"
            val worstSetupInMonth = setupInMonth.minByOrNull { it.value.sumOf { tr -> tr.netPnl } }?.key ?: "N/A"

            PeriodPerformance(
                periodName = formatMonthLabel(monthStr),
                totalTrades = mTotal,
                winningTrades = mWins,
                losingTrades = mLosses,
                netPnl = mNet,
                netProfitPercent = mNetPct,
                winRate = mWinRate,
                averageR = mAvgR,
                expectancy = mExp,
                profitFactor = mPf,
                bestSetup = bestSetupInMonth,
                worstSetup = worstSetupInMonth,
                maxDrawdown = 0.0
            )
        }

        // Weekly Performance
        val weeklyGroups = trades.groupBy { getWeekLabel(it.date) }
        val weeklyPerformanceList = weeklyGroups.map { (weekLabel, wTrades) ->
            val wTotal = wTrades.size
            val wWins = wTrades.count { it.netPnl > 0 }
            val wLosses = wTrades.count { it.netPnl < 0 }
            val wNet = wTrades.sumOf { it.netPnl }
            val wNetPct = if (startingCapital > 0) (wNet / startingCapital) * 100.0 else 0.0
            val wWinRate = if (wTotal > 0) (wWins.toDouble() / wTotal) * 100.0 else 0.0
            val wTotalR = wTrades.sumOf { it.rMultiple }
            val wAvgR = if (wTotal > 0) wTotalR / wTotal else 0.0
            val wGrossProfit = wTrades.filter { it.grossPnl > 0 }.sumOf { it.grossPnl }
            val wGrossLoss = wTrades.filter { it.grossPnl < 0 }.sumOf { it.grossPnl }
            val wPf = if (abs(wGrossLoss) > 0.0001) wGrossProfit / abs(wGrossLoss) else if (wGrossProfit > 0) 99.9 else 0.0
            val wExp = if (wTotal > 0) wTotalR / wTotal else 0.0

            PeriodPerformance(
                periodName = weekLabel,
                totalTrades = wTotal,
                winningTrades = wWins,
                losingTrades = wLosses,
                netPnl = wNet,
                netProfitPercent = wNetPct,
                winRate = wWinRate,
                averageR = wAvgR,
                expectancy = wExp,
                profitFactor = wPf,
                bestSetup = "",
                worstSetup = "",
                maxDrawdown = 0.0
            )
        }

        // Mistakes Analysis
        val mistakeCountMap = mutableMapOf<String, Int>()
        val tradesWithMistakes = mutableListOf<Trade>()
        val tradesWithoutMistakes = mutableListOf<Trade>()

        trades.forEach { trade ->
            val mistakeList = trade.mistakes.split(",").map { it.trim() }.filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) }
            if (mistakeList.isNotEmpty()) {
                tradesWithMistakes.add(trade)
                mistakeList.forEach { m ->
                    mistakeCountMap[m] = (mistakeCountMap[m] ?: 0) + 1
                }
            } else {
                tradesWithoutMistakes.add(trade)
            }
        }

        val mostCommonMistake = mistakeCountMap.maxByOrNull { it.value }?.key ?: "None"
        val totalTradesWithMistakes = tradesWithMistakes.size
        val totalPnlLossFromMistakes = tradesWithMistakes.filter { it.netPnl < 0 }.sumOf { abs(it.netPnl) }
        val winRateWithMistakes = if (tradesWithMistakes.isNotEmpty()) {
            (tradesWithMistakes.count { it.netPnl > 0 }.toDouble() / tradesWithMistakes.size) * 100.0
        } else 0.0
        val winRateWithoutMistakes = if (tradesWithoutMistakes.isNotEmpty()) {
            (tradesWithoutMistakes.count { it.netPnl > 0 }.toDouble() / tradesWithoutMistakes.size) * 100.0
        } else 0.0

        val mistakeAnalysis = MistakeAnalysis(
            mostCommonMistake = mostCommonMistake,
            totalTradesWithMistakes = totalTradesWithMistakes,
            totalPnlLossFromMistakes = totalPnlLossFromMistakes,
            winRateWithMistakes = winRateWithMistakes,
            winRateWithoutMistakes = winRateWithoutMistakes,
            mistakeCounts = mistakeCountMap
        )

        // Highlights
        val bestTrade = trades.maxByOrNull { it.netPnl }
        val worstTrade = trades.minByOrNull { it.netPnl }
        val highestRTrade = trades.maxByOrNull { it.rMultiple }
        val largestLossTrade = trades.minByOrNull { it.rMultiple }
        val bestDay = dailyPerformanceList.maxByOrNull { it.netPnl }
        val worstDay = dailyPerformanceList.minByOrNull { it.netPnl }
        val bestSetup = setupStats.maxByOrNull { it.totalNetPnl }?.setupName ?: "N/A"
        val worstSetup = setupStats.minByOrNull { it.totalNetPnl }?.setupName ?: "N/A"

        val highlights = PerformanceHighlights(
            bestTrade = bestTrade,
            worstTrade = worstTrade,
            bestDay = bestDay,
            worstDay = worstDay,
            bestSetup = bestSetup,
            worstSetup = worstSetup,
            highestRTrade = highestRTrade,
            largestLossTrade = largestLossTrade
        )

        // R-Distribution
        val rBuckets = mutableMapOf(
            "<-2R" to 0,
            "-2R to -1R" to 0,
            "-1R to 0R" to 0,
            "0R to 1R" to 0,
            "1R to 2R" to 0,
            "2R to 3R" to 0,
            ">3R" to 0
        )
        trades.forEach {
            when {
                it.rMultiple < -2.0 -> rBuckets["<-2R"] = (rBuckets["<-2R"] ?: 0) + 1
                it.rMultiple in -2.0..-1.0001 -> rBuckets["-2R to -1R"] = (rBuckets["-2R to -1R"] ?: 0) + 1
                it.rMultiple in -1.0..-0.0001 -> rBuckets["-1R to 0R"] = (rBuckets["-1R to 0R"] ?: 0) + 1
                it.rMultiple in 0.0..0.9999 -> rBuckets["0R to 1R"] = (rBuckets["0R to 1R"] ?: 0) + 1
                it.rMultiple in 1.0..1.9999 -> rBuckets["1R to 2R"] = (rBuckets["1R to 2R"] ?: 0) + 1
                it.rMultiple in 2.0..2.9999 -> rBuckets["2R to 3R"] = (rBuckets["2R to 3R"] ?: 0) + 1
                else -> rBuckets[">3R"] = (rBuckets[">3R"] ?: 0) + 1
            }
        }

        // Today's discipline check
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayTrades = trades.filter { it.date == todayStr }
        val todayTradesCount = todayTrades.size
        val todayNetPnl = todayTrades.sumOf { it.netPnl }
        val isMaxTradesExceeded = todayTradesCount >= settings.maxTradesPerDay
        val isMaxLossExceeded = todayNetPnl < 0 && abs(todayNetPnl) >= settings.maxDailyLoss

        return OverallAnalytics(
            totalTrades = totalTrades,
            winningTrades = winningTrades,
            losingTrades = losingTrades,
            breakevenTrades = breakevenTrades,
            winRate = winRate,
            lossRate = lossRate,
            averageR = averageR,
            averageWinR = averageWinR,
            averageLossR = averageLossR,
            expectancy = expectancy,
            profitFactor = profitFactor,
            totalGrossProfit = totalGrossProfit,
            totalGrossLoss = totalGrossLoss,
            totalCharges = totalCharges,
            netProfit = netProfit,
            netProfitPercent = netProfitPercent,
            startingCapital = startingCapital,
            currentCapital = currentCapital,
            peakEquity = peakEquity,
            currentDrawdown = currentDrawdown,
            currentDrawdownPercent = currentDrawdownPercent,
            maxDrawdown = maxDrawdownAmount,
            maxDrawdownPercent = maxDrawdownPct,
            morningBreakoutWinRate = morningBreakoutWinRate,
            resistanceRejectionWinRate = resistanceRejectionWinRate,
            equityCurve = equityCurve,
            setupStats = setupStats,
            dailyPerformanceList = dailyPerformanceList,
            weeklyPerformanceList = weeklyPerformanceList,
            monthlyPerformanceList = monthlyPerformanceList,
            mistakeAnalysis = mistakeAnalysis,
            highlights = highlights,
            rDistribution = rBuckets,
            todayTradesCount = todayTradesCount,
            todayNetPnl = todayNetPnl,
            isMaxTradesExceeded = isMaxTradesExceeded,
            isMaxLossExceeded = isMaxLossExceeded
        )
    }

    private fun formatMonthLabel(yyyyMm: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val date = parser.parse(yyyyMm)
            if (date != null) formatter.format(date) else yyyyMm
        } catch (_: Exception) {
            yyyyMm
        }
    }

    private fun getWeekLabel(dateStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("'W'w - MMM d", Locale.getDefault())
            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }
}
