package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailyPerformance
import com.example.analytics.MistakeAnalysis
import com.example.analytics.OverallAnalytics
import com.example.analytics.PerformanceHighlights
import com.example.analytics.PeriodPerformance
import com.example.ui.components.DailyPnlBarChart
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.RDistributionChart
import com.example.ui.components.SetupStatsCard
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.BreakevenGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate700
import java.util.Locale

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AnalyticsScreen(
    analytics: OverallAnalytics,
    currencySymbol: String,
    onAddNewSetup: (String, String) -> Unit,
    onNavigateToCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Setups", "Equity & Daily", "Mistakes", "Best / Worst")

    var showAddSetupDialog by remember { mutableStateOf(false) }
    var newSetupName by remember { mutableStateOf("") }
    var newSetupDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen")
    ) {
        // Screen Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Analytics & Setups",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Comprehensive performance and behavioral diagnostics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            0 -> SetupsTabContent(
                analytics = analytics,
                currencySymbol = currencySymbol,
                onAddSetupClick = { showAddSetupDialog = true }
            )
            1 -> EquityAndDailyTabContent(
                analytics = analytics,
                currencySymbol = currencySymbol,
                onOpenCalendar = onNavigateToCalendar
            )
            2 -> MistakesTabContent(
                mistakeAnalysis = analytics.mistakeAnalysis,
                currencySymbol = currencySymbol
            )
            3 -> BestWorstTabContent(
                highlights = analytics.highlights,
                analytics = analytics,
                currencySymbol = currencySymbol
            )
        }
    }

    if (showAddSetupDialog) {
        AlertDialog(
            onDismissRequest = { showAddSetupDialog = false },
            title = { Text("Add Custom Trading Setup") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSetupName,
                        onValueChange = { newSetupName = it },
                        label = { Text("Setup Name") },
                        placeholder = { Text("e.g. 5 EMA Mean Reversion") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSetupDesc,
                        onValueChange = { newSetupDesc = it },
                        label = { Text("Description & Criteria") },
                        placeholder = { Text("Entry rules, timeframe, confirmation...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSetupName.isNotBlank()) {
                            onAddNewSetup(newSetupName.trim(), newSetupDesc.trim())
                            newSetupName = ""
                            newSetupDesc = ""
                            showAddSetupDialog = false
                        }
                    }
                ) {
                    Text("Save Setup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSetupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SetupsTabContent(
    analytics: OverallAnalytics,
    currencySymbol: String,
    onAddSetupClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Setup Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddSetupClick,
                    modifier = Modifier.testTag("button_add_setup_analytics")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Setup")
                }
            }
        }

        item {
            SetupStatsCard(
                stats = analytics.setupStats,
                currencySymbol = currencySymbol,
                onAddSetupClick = onAddSetupClick
            )
        }
    }
}

@Composable
private fun EquityAndDailyTabContent(
    analytics: OverallAnalytics,
    currencySymbol: String,
    onOpenCalendar: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EquityCurveChart(
                equityPoints = analytics.equityCurve,
                currencySymbol = currencySymbol
            )
        }

        item {
            DailyPnlBarChart(
                dailyList = analytics.dailyPerformanceList,
                currencySymbol = currencySymbol
            )
        }

        // Trading Calendar Shortcut Banner
        item {
            OutlinedButton(
                onClick = onOpenCalendar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Monthly Trading Calendar Screen", fontWeight = FontWeight.Bold)
            }
        }

        item {
            RDistributionChart(distribution = analytics.rDistribution)
        }

        // Daily Breakdown Table / List
        item {
            Text(
                text = "Chronological Daily History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (analytics.dailyPerformanceList.isEmpty()) {
            item {
                Text(
                    text = "No daily performance recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(analytics.dailyPerformanceList) { day ->
                DailyPerformanceItem(day = day, currencySymbol = currencySymbol)
            }
        }

        // Monthly Breakdown
        if (analytics.monthlyPerformanceList.isNotEmpty()) {
            item {
                Text(
                    text = "Monthly Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(analytics.monthlyPerformanceList) { month ->
                MonthlyPerformanceItem(month = month, currencySymbol = currencySymbol)
            }
        }
    }
}

@Composable
private fun DailyPerformanceItem(
    day: DailyPerformance,
    currencySymbol: String
) {
    val isProfit = day.netPnl >= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isProfit) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", day.netPnl)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) ProfitGreen else LossRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${day.tradeCount} Trades (${String.format(Locale.getDefault(), "%.0f", day.winRate)}% WR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Gross: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", day.grossPnl)} | Charges: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", day.charges)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.2f", day.totalR)}R",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (day.totalR >= 0) ProfitGreen else LossRed
                )
            }
        }
    }
}

@Composable
private fun MonthlyPerformanceItem(
    month: PeriodPerformance,
    currencySymbol: String
) {
    val isProfit = month.netPnl >= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = month.periodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isProfit) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", month.netPnl)} (${String.format(Locale.getDefault(), "%.1f", month.netProfitPercent)}%)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) ProfitGreen else LossRed
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trades: ${month.totalTrades} (${month.winningTrades}W / ${month.losingTrades}L)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Win Rate: ${String.format(Locale.getDefault(), "%.1f", month.winRate)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (month.winRate >= 50) ProfitGreen else LossRed
                )
                Text(
                    text = "PF: ${String.format(Locale.getDefault(), "%.2f", month.profitFactor)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (month.bestSetup.isNotBlank() && month.bestSetup != "N/A") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Best Setup: ${month.bestSetup} | Worst: ${month.worstSetup}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MistakesTabContent(
    mistakeAnalysis: MistakeAnalysis,
    currencySymbol: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mistake & Psychology Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quantifying the financial cost of indiscipline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = LossRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            title = "Most Common",
                            value = mistakeAnalysis.mostCommonMistake,
                            subtitle = "Discipline Leak",
                            valueColor = LossRed,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Trades with Flaws",
                            value = "${mistakeAnalysis.totalTradesWithMistakes}",
                            subtitle = "Cost: -$currencySymbol${String.format(Locale.getDefault(), "%,.0f", mistakeAnalysis.totalPnlLossFromMistakes)}",
                            valueColor = LossRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Win rate comparison: with vs without mistakes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Win Rate: Disciplined vs Flawed Trades",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Without Mistakes (A+)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${String.format(Locale.getDefault(), "%.1f", mistakeAnalysis.winRateWithoutMistakes)}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("With Mistakes (FOMO/Late)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${String.format(Locale.getDefault(), "%.1f", mistakeAnalysis.winRateWithMistakes)}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LossRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mistake frequency list
        item {
            Text(
                text = "Mistake Frequency Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (mistakeAnalysis.mistakeCounts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No trading mistakes logged! Great discipline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ProfitGreen
                        )
                    }
                }
            }
        } else {
            items(mistakeAnalysis.mistakeCounts.toList().sortedByDescending { it.second }) { (mistake, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mistake,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LossRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$count times",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LossRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BestWorstTabContent(
    highlights: PerformanceHighlights,
    analytics: OverallAnalytics,
    currencySymbol: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Performance Extremes & Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Best & Worst Trade Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Trade
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Best Trade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = highlights.bestTrade?.stockName ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (highlights.bestTrade != null) "+$currencySymbol${String.format(Locale.getDefault(), "%,.0f", highlights.bestTrade.netPnl)} (${String.format(Locale.getDefault(), "%.2f", highlights.bestTrade.rMultiple)}R)" else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                        Text(
                            text = highlights.bestTrade?.date ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Worst Trade
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Worst Trade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LossRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = highlights.worstTrade?.stockName ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (highlights.worstTrade != null) "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", highlights.worstTrade.netPnl)} (${String.format(Locale.getDefault(), "%.2f", highlights.worstTrade.rMultiple)}R)" else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                        Text(
                            text = highlights.worstTrade?.date ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Best & Worst Day
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Day
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Best Day", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = highlights.bestDay?.date ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (highlights.bestDay != null) "+$currencySymbol${String.format(Locale.getDefault(), "%,.0f", highlights.bestDay.netPnl)}" else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                    }
                }

                // Worst Day
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Worst Day", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LossRed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = highlights.worstDay?.date ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (highlights.worstDay != null) "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", highlights.worstDay.netPnl)}" else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                    }
                }
            }
        }

        // Best & Worst Setup
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Best Setup", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = highlights.bestSetup,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Worst Setup", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = highlights.worstSetup,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
