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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.OverallAnalytics
import com.example.data.model.Trade
import com.example.ui.components.DisciplineAlertBanner
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TradeItemCard
import com.example.ui.theme.BreakevenGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import java.util.Locale

@Composable
fun DashboardScreen(
    analytics: OverallAnalytics,
    recentTrades: List<Trade>,
    currencySymbol: String,
    onAddTradeClick: () -> Unit,
    onViewAllTradesClick: () -> Unit,
    onTradeClick: (Trade) -> Unit,
    onEditTrade: (Trade) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onOpenCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isNetProfitPositive = analytics.netProfit >= 0
    val netColor = if (isNetProfitPositive) ProfitGreen else LossRed

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Discipline Alert Banner if limits are breached today
            if (analytics.isMaxTradesExceeded || analytics.isMaxLossExceeded) {
                item {
                    DisciplineAlertBanner(
                        isMaxTradesExceeded = analytics.isMaxTradesExceeded,
                        isMaxLossExceeded = analytics.isMaxLossExceeded,
                        todayTradesCount = analytics.todayTradesCount,
                        maxTradesAllowed = 3,
                        todayNetPnl = analytics.todayNetPnl,
                        maxDailyLossAllowed = 3000.0,
                        currencySymbol = currencySymbol
                    )
                }
            }

            // Top Hero Account Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_account_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Equity",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol${String.format(Locale.getDefault(), "%,.1f", analytics.currentCapital)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Net Profit Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = netColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isNetProfitPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = netColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${if (isNetProfitPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", analytics.netProfitPercent)}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Secondary capital info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Starting Capital",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", analytics.startingCapital)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Net P&L",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (isNetProfitPositive) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", analytics.netProfit)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = netColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Max Drawdown",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-$currencySymbol${String.format(Locale.getDefault(), "%,.0f", analytics.maxDrawdown)} (${String.format(Locale.getDefault(), "%.1f", analytics.maxDrawdownPercent)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (analytics.maxDrawdown > 0) LossRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Equity Curve Line Chart
            item {
                EquityCurveChart(
                    equityPoints = analytics.equityCurve,
                    currencySymbol = currencySymbol
                )
            }

            // Quick Trading Performance Calendar Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_calendar_shortcut_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Monthly Trading Calendar",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "View date-wise P&L matrix & day breakdown",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenCalendarClick,
                            modifier = Modifier.testTag("button_open_calendar_from_dashboard")
                        ) {
                            Text("Open", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Primary Performance Metrics Grid Section
            item {
                Text(
                    text = "Key Trading Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Grid Row 1: Total Trades & Win Rate
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Trades",
                        value = "${analytics.totalTrades}",
                        subtitle = "${analytics.winningTrades}W / ${analytics.losingTrades}L / ${analytics.breakevenTrades}BE",
                        icon = Icons.Default.Assessment,
                        iconTint = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Win Rate %",
                        value = "${String.format(Locale.getDefault(), "%.1f", analytics.winRate)}%",
                        subtitle = "Loss Rate: ${String.format(Locale.getDefault(), "%.1f", analytics.lossRate)}%",
                        valueColor = if (analytics.winRate >= 50) ProfitGreen else LossRed,
                        icon = Icons.Default.Percent,
                        iconTint = if (analytics.winRate >= 50) ProfitGreen else LossRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Row 2: Winning Trades & Losing Trades
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Winning Trades",
                        value = "${analytics.winningTrades}",
                        subtitle = "Avg Win: ${String.format(Locale.getDefault(), "%.2f", analytics.averageWinR)}R",
                        valueColor = ProfitGreen,
                        icon = Icons.Default.CheckCircle,
                        iconTint = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Losing Trades",
                        value = "${analytics.losingTrades}",
                        subtitle = "Avg Loss: ${String.format(Locale.getDefault(), "%.2f", analytics.averageLossR)}R",
                        valueColor = LossRed,
                        icon = Icons.Outlined.Cancel,
                        iconTint = LossRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Row 3: Average R & Average Win R
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Average R",
                        value = "${String.format(Locale.getDefault(), "%.2f", analytics.averageR)}R",
                        subtitle = "Per trade return in risk units",
                        valueColor = if (analytics.averageR > 0) ProfitGreen else if (analytics.averageR < 0) LossRed else MaterialTheme.colorScheme.onSurface,
                        icon = Icons.Default.Timeline,
                        iconTint = IndigoAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Average Win R",
                        value = "${String.format(Locale.getDefault(), "%.2f", analytics.averageWinR)}R",
                        subtitle = "Reward on winning trades",
                        valueColor = ProfitGreen,
                        icon = Icons.Default.ArrowUpward,
                        iconTint = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Row 4: Expectancy (R) & Profit Factor
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Expectancy (R)",
                        value = "${String.format(Locale.getDefault(), "%.2f", analytics.expectancy)}R",
                        subtitle = if (analytics.expectancy > 0) "Positive trading edge" else "Negative expectancy",
                        valueColor = if (analytics.expectancy > 0) ProfitGreen else LossRed,
                        icon = Icons.Default.ShowChart,
                        iconTint = if (analytics.expectancy > 0) ProfitGreen else LossRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Profit Factor",
                        value = String.format(Locale.getDefault(), "%.2f", analytics.profitFactor),
                        subtitle = "Gross Profit / Gross Loss",
                        valueColor = if (analytics.profitFactor >= 1.5) ProfitGreen else if (analytics.profitFactor >= 1.0) BreakevenGold else LossRed,
                        icon = Icons.Default.AttachMoney,
                        iconTint = BreakevenGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Row 5: Total Gross Profit, Total Charges, Net Profit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Gross Profit",
                        value = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", analytics.totalGrossProfit)}",
                        subtitle = "Gross Loss: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", analytics.totalGrossLoss)}",
                        valueColor = ProfitGreen,
                        icon = Icons.Default.TrendingUp,
                        iconTint = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total Charges",
                        value = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", analytics.totalCharges)}",
                        subtitle = "Brokerage, STT & taxes",
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = Icons.Default.Receipt,
                        iconTint = Slate700,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Row 6: Setup Win Rates (Morning Breakout & Resistance Rejection)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Morning Breakout",
                        value = "${String.format(Locale.getDefault(), "%.1f", analytics.morningBreakoutWinRate)}%",
                        subtitle = "Win Rate on Breakouts",
                        valueColor = if (analytics.morningBreakoutWinRate >= 50) ProfitGreen else MaterialTheme.colorScheme.onSurface,
                        icon = Icons.Default.ShowChart,
                        iconTint = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Resistance Rejection",
                        value = "${String.format(Locale.getDefault(), "%.1f", analytics.resistanceRejectionWinRate)}%",
                        subtitle = "Win Rate on Rejections",
                        valueColor = if (analytics.resistanceRejectionWinRate >= 50) ProfitGreen else MaterialTheme.colorScheme.onSurface,
                        icon = Icons.Default.TrendingDown,
                        iconTint = IndigoAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Trades Header & List Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Trades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onViewAllTradesClick) {
                        Text("View All (${analytics.totalTrades})")
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (recentTrades.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No trades logged yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Trade' to record your first trade entry!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddTradeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Trade Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(recentTrades.take(4).size) { idx ->
                    val trade = recentTrades[idx]
                    TradeItemCard(
                        trade = trade,
                        currencySymbol = currencySymbol,
                        onClick = { onTradeClick(trade) },
                        onEdit = { onEditTrade(trade) },
                        onDelete = { onDeleteTrade(trade) }
                    )
                }
            }
        }

        // Floating Action Button to Add New Trade fast
        ExtendedFloatingActionButton(
            onClick = onAddTradeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("fab_add_trade"),
            containerColor = ElectricBlue,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Trade")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Trade", fontWeight = FontWeight.Bold)
        }
    }
}
