package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.components.TradeDetailDialog
import com.example.ui.theme.BreakevenGold
import com.example.ui.theme.BreakevenGoldBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private val WEEKDAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

data class CalendarDayData(
    val dayNumber: Int,
    val dateString: String, // YYYY-MM-DD
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val trades: List<Trade>
) {
    val totalTrades: Int = trades.size
    val grossPnl: Double = trades.sumOf { it.grossPnl }
    val charges: Double = trades.sumOf { it.charges }
    val netPnl: Double = trades.sumOf { it.netPnl }
    val totalR: Double = trades.sumOf { it.rMultiple }
    val winningTrades: Int = trades.count { it.netPnl > 0 }
    val losingTrades: Int = trades.count { it.netPnl < 0 }
    val isProfitable: Boolean = netPnl > 0
    val isLosing: Boolean = netPnl < 0
    val isBreakeven: Boolean = totalTrades > 0 && netPnl == 0.0
    val hasTrades: Boolean = totalTrades > 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingCalendarScreen(
    trades: List<Trade>,
    currencySymbol: String,
    onTradeClick: (Trade) -> Unit,
    onEditTrade: (Trade) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onAddTradeForDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current real-world date for defaults
    val calendarInstance = remember { Calendar.getInstance() }
    val todayYear = remember { calendarInstance.get(Calendar.YEAR) }
    val todayMonth = remember { calendarInstance.get(Calendar.MONTH) + 1 } // 1-12
    val todayDay = remember { calendarInstance.get(Calendar.DAY_OF_MONTH) }
    val todayDateString = remember {
        String.format(Locale.getDefault(), "%04d-%02d-%02d", todayYear, todayMonth, todayDay)
    }

    // Selected Month & Year state
    var selectedYear by remember { mutableIntStateOf(todayYear) }
    var selectedMonth by remember { mutableIntStateOf(todayMonth) } // 1-12

    // Selected Date for Day Details Sheet
    var selectedDayData by remember { mutableStateOf<CalendarDayData?>(null) }
    var tradeToInspect by remember { mutableStateOf<Trade?>(null) }
    var tradeToDelete by remember { mutableStateOf<Trade?>(null) }

    // Synchronize selected day data if trades in repository change
    val activeSelectedDayData = remember(trades, selectedDayData?.dateString) {
        selectedDayData?.let { current ->
            val dateStr = current.dateString
            val matchingTrades = trades.filter { it.date == dateStr }
            val parts = dateStr.split("-")
            val dNum = parts.getOrNull(2)?.toIntOrNull() ?: current.dayNumber
            CalendarDayData(
                dayNumber = dNum,
                dateString = dateStr,
                isCurrentMonth = true,
                isToday = dateStr == todayDateString,
                trades = matchingTrades
            )
        }
    }

    // Group trades by date String "YYYY-MM-DD"
    val tradesByDate = remember(trades) {
        trades.groupBy { it.date }
    }

    // Filter trades in the selected month
    val monthPrefix = remember(selectedYear, selectedMonth) {
        String.format(Locale.getDefault(), "%04d-%02d", selectedYear, selectedMonth)
    }

    val selectedMonthTrades = remember(trades, monthPrefix) {
        trades.filter { it.date.startsWith(monthPrefix) }
    }

    // Selected Month Aggregate Metrics
    val monthlyNetPnl = remember(selectedMonthTrades) {
        selectedMonthTrades.sumOf { it.netPnl }
    }
    val monthlyGrossPnl = remember(selectedMonthTrades) {
        selectedMonthTrades.sumOf { it.grossPnl }
    }
    val monthlyCharges = remember(selectedMonthTrades) {
        selectedMonthTrades.sumOf { it.charges }
    }
    val monthlyTradesCount = selectedMonthTrades.size
    val monthlyWinningTrades = remember(selectedMonthTrades) {
        selectedMonthTrades.count { it.netPnl > 0 }
    }
    val monthlyLosingTrades = remember(selectedMonthTrades) {
        selectedMonthTrades.count { it.netPnl < 0 }
    }
    val monthlyWinRate = remember(monthlyTradesCount, monthlyWinningTrades) {
        if (monthlyTradesCount > 0) (monthlyWinningTrades.toDouble() / monthlyTradesCount) * 100.0 else 0.0
    }
    val monthlyTotalR = remember(selectedMonthTrades) {
        selectedMonthTrades.sumOf { it.rMultiple }
    }

    // Distinct Trading Days in Month
    val monthlyTradingDaysCount = remember(selectedMonthTrades) {
        selectedMonthTrades.map { it.date }.distinct().size
    }
    val monthlyGreenDays = remember(selectedMonthTrades) {
        selectedMonthTrades.groupBy { it.date }.count { (_, dayTrades) -> dayTrades.sumOf { it.netPnl } > 0 }
    }
    val monthlyRedDays = remember(selectedMonthTrades) {
        selectedMonthTrades.groupBy { it.date }.count { (_, dayTrades) -> dayTrades.sumOf { it.netPnl } < 0 }
    }

    // Build calendar grid days
    val calendarDays = remember(selectedYear, selectedMonth, tradesByDate, todayDateString) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday ... 7 = Saturday
        val leadingEmptyDays = firstDayOfWeek - 1 // 0 for Sunday, 1 for Monday...
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysList = mutableListOf<CalendarDayData?>()

        // Leading empty slots for alignment
        for (i in 0 until leadingEmptyDays) {
            daysList.add(null)
        }

        // Days of month
        for (day in 1..maxDaysInMonth) {
            val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth, day)
            val dayTrades = tradesByDate[dateStr] ?: emptyList()
            daysList.add(
                CalendarDayData(
                    dayNumber = day,
                    dateString = dateStr,
                    isCurrentMonth = true,
                    isToday = dateStr == todayDateString,
                    trades = dayTrades
                )
            )
        }

        // Pad trailing days to fill the final row (multiple of 7)
        while (daysList.size % 7 != 0) {
            daysList.add(null)
        }

        daysList
    }

    val isCurrentMonthSelected = selectedYear == todayYear && selectedMonth == todayMonth

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("trading_calendar_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title and Navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Trading Calendar",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Daily performance and monthly P&L matrix",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isCurrentMonthSelected) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricBlue.copy(alpha = 0.12f),
                            modifier = Modifier.clickable {
                                selectedYear = todayYear
                                selectedMonth = todayMonth
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = "Today",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Current Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            }
                        }
                    }
                }
            }

            // Month Navigation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Month & Year Selector Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedMonth == 1) {
                                        selectedMonth = 12
                                        selectedYear -= 1
                                    } else {
                                        selectedMonth -= 1
                                    }
                                },
                                modifier = Modifier.testTag("button_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${MONTH_NAMES[selectedMonth - 1]} $selectedYear",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$monthlyTradingDaysCount Trading ${if (monthlyTradingDaysCount == 1) "Day" else "Days"} • $monthlyTradesCount Trades",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (selectedMonth == 12) {
                                        selectedMonth = 1
                                        selectedYear += 1
                                    } else {
                                        selectedMonth += 1
                                    }
                                },
                                modifier = Modifier.testTag("button_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Monthly Total P&L Banner
                        val isMonthProfit = monthlyNetPnl > 0
                        val isMonthLoss = monthlyNetPnl < 0
                        val pnlColor = when {
                            isMonthProfit -> ProfitGreen
                            isMonthLoss -> LossRed
                            else -> BreakevenGold
                        }
                        val pnlContainerBg = when {
                            isMonthProfit -> ProfitGreenBg
                            isMonthLoss -> LossRedBg
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = pnlContainerBg,
                            border = BorderStroke(1.dp, pnlColor.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Monthly Net Performance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total: ${if (isMonthProfit) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.2f", monthlyNetPnl)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = pnlColor,
                                    modifier = Modifier.testTag("calendar_monthly_total_pnl")
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini Monthly KPI Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Green vs Red Days
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Day Record",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${monthlyGreenDays}W - ${monthlyRedDays}L",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (monthlyGreenDays >= monthlyRedDays) ProfitGreen else LossRed
                                        )
                                    }

                                    // Win Rate
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Win Rate",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", monthlyWinRate)}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (monthlyWinRate >= 50.0) ProfitGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Total R
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Total R",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${if (monthlyTotalR > 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", monthlyTotalR)}R",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (monthlyTotalR >= 0) ProfitGreen else LossRed
                                        )
                                    }

                                    // Charges
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Charges",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", monthlyCharges)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calendar Grid Container
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Weekday Headers: Sun | Mon | Tue | Wed | Thu | Fri | Sat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WEEKDAY_LABELS.forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayLabel == "Sun" || dayLabel == "Sat") Slate400 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Cells Grid: 7 columns per row
                        val rowsCount = calendarDays.size / 7
                        for (rowIndex in 0 until rowsCount) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (colIndex in 0 until 7) {
                                    val cellIndex = rowIndex * 7 + colIndex
                                    val dayData = calendarDays.getOrNull(cellIndex)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.85f)
                                    ) {
                                        if (dayData != null) {
                                            CalendarDateCell(
                                                dayData = dayData,
                                                currencySymbol = currencySymbol,
                                                onClick = {
                                                    selectedDayData = dayData
                                                }
                                            )
                                        } else {
                                            // Empty padding slot
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legend Information Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(ProfitGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Profit Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(LossRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Loss Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(BreakevenGold, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Breakeven",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .border(1.5.dp, ElectricBlue, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Day Details Bottom Sheet / Dialog
        activeSelectedDayData?.let { day ->
            DayDetailsBottomSheet(
                dayData = day,
                currencySymbol = currencySymbol,
                onDismiss = { selectedDayData = null },
                onTradeClick = { trade ->
                    tradeToInspect = trade
                },
                onEditTrade = { trade ->
                    selectedDayData = null
                    onEditTrade(trade)
                },
                onDeleteTrade = { trade ->
                    tradeToDelete = trade
                },
                onAddTradeForDate = { dateStr ->
                    selectedDayData = null
                    onAddTradeForDate(dateStr)
                }
            )
        }

        // Full Trade Inspect Dialog
        tradeToInspect?.let { trade ->
            TradeDetailDialog(
                trade = trade,
                currencySymbol = currencySymbol,
                onDismiss = { tradeToInspect = null },
                onEdit = {
                    tradeToInspect = null
                    selectedDayData = null
                    onEditTrade(trade)
                }
            )
        }

        // Trade Delete Confirmation Dialog
        tradeToDelete?.let { trade ->
            AlertDialog(
                onDismissRequest = { tradeToDelete = null },
                title = { Text("Delete Trade?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete ${trade.stockName} (${trade.date}) with P&L of ${if (trade.netPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.netPnl)}?\n\nThis will recalculate all calendar totals, dashboard analytics, and equity curve."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteTrade(trade)
                            tradeToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                        modifier = Modifier.testTag("confirm_delete_trade_dialog")
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tradeToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Individual Calendar Date Card Cell
 */
@Composable
fun CalendarDateCell(
    dayData: CalendarDayData,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWin = dayData.isProfitable
    val isLoss = dayData.isLosing
    val isBreakeven = dayData.isBreakeven
    val hasTrades = dayData.hasTrades

    // Container Background & Border based on P&L
    val cellBgColor = when {
        isWin -> ProfitGreenBg
        isLoss -> LossRedBg
        isBreakeven -> BreakevenGoldBg
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val cellBorderColor = when {
        dayData.isToday -> ElectricBlue
        isWin -> ProfitGreen.copy(alpha = 0.5f)
        isLoss -> LossRed.copy(alpha = 0.5f)
        isBreakeven -> BreakevenGold.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    val pnlTextColor = when {
        isWin -> ProfitGreen
        isLoss -> LossRed
        else -> BreakevenGold
    }

    val borderWidth = if (dayData.isToday) 2.dp else if (hasTrades) 1.dp else 0.5.dp

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = cellBgColor,
        border = BorderStroke(borderWidth, cellBorderColor),
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .testTag("calendar_date_cell_${dayData.dateString}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Date Number & Today/Count indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dayData.dayNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (dayData.isToday || hasTrades) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (dayData.isToday) ElectricBlue else if (hasTrades) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (dayData.isToday) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(ElectricBlue, CircleShape)
                    )
                } else if (hasTrades) {
                    Text(
                        text = "${dayData.totalTrades}t",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlTextColor
                    )
                }
            }

            // Bottom Content: Daily Net P&L (Formatted cleanly)
            if (hasTrades) {
                val formattedPnl = formatCalendarPnl(dayData.netPnl, currencySymbol)
                Text(
                    text = formattedPnl,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = pnlTextColor,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Clean short formatting for compact calendar cell
 * e.g. +₹500, -₹300, +₹1.2K, -₹25K
 */
private fun formatCalendarPnl(netPnl: Double, currencySymbol: String): String {
    val sign = if (netPnl > 0) "+" else if (netPnl < 0) "-" else ""
    val absPnl = abs(netPnl)
    return when {
        absPnl >= 100000 -> "$sign$currencySymbol${String.format(Locale.getDefault(), "%.1fL", absPnl / 100000.0)}"
        absPnl >= 10000 -> "$sign$currencySymbol${String.format(Locale.getDefault(), "%.1fK", absPnl / 1000.0)}"
        absPnl >= 1000 -> "$sign$currencySymbol${String.format(Locale.getDefault(), "%,.0f", absPnl)}"
        else -> "$sign$currencySymbol${String.format(Locale.getDefault(), "%,.0f", absPnl)}"
    }
}

/**
 * Comprehensive Day Details Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DayDetailsBottomSheet(
    dayData: CalendarDayData,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onTradeClick: (Trade) -> Unit,
    onEditTrade: (Trade) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onAddTradeForDate: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Formatted readable date header
    val formattedReadableDate = remember(dayData.dateString) {
        try {
            val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayData.dateString)
            if (dateObj != null) {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(dateObj)
            } else {
                dayData.dateString
            }
        } catch (_: Exception) {
            dayData.dateString
        }
    }

    val isProfit = dayData.netPnl > 0
    val isLoss = dayData.netPnl < 0
    val outcomeColor = when {
        isProfit -> ProfitGreen
        isLoss -> LossRed
        dayData.hasTrades -> BreakevenGold
        else -> Slate400
    }
    val outcomeBg = when {
        isProfit -> ProfitGreenBg
        isLoss -> LossRedBg
        dayData.hasTrades -> BreakevenGoldBg
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val outcomeText = when {
        isProfit -> "PROFITABLE DAY"
        isLoss -> "LOSING DAY"
        dayData.hasTrades -> "BREAKEVEN DAY"
        else -> "NO TRADES RECORDED"
    }

    val winRate = if (dayData.totalTrades > 0) {
        (dayData.winningTrades.toDouble() / dayData.totalTrades) * 100.0
    } else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("day_details_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Date & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedReadableDate,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = outcomeBg,
                        border = BorderStroke(1.dp, outcomeColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = outcomeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = outcomeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("button_close_day_details")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Metrics Grid
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Top Row: Net PnL & Total R
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Net P&L",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (isProfit) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.2f", dayData.netPnl)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = outcomeColor,
                                modifier = Modifier.testTag("day_details_net_pnl")
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total R Multiple",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (dayData.totalR > 0) "+" else ""}${String.format(Locale.getDefault(), "%.2f", dayData.totalR)}R",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = outcomeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Metrics: Trades, Win/Loss, Gross, Charges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Trades",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dayData.totalTrades}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "Win / Loss",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dayData.winningTrades}W - ${dayData.losingTrades}L (${String.format(Locale.getDefault(), "%.0f", winRate)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (dayData.winningTrades >= dayData.losingTrades && dayData.hasTrades) ProfitGreen else LossRed
                            )
                        }

                        Column {
                            Text(
                                text = "Gross P&L",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (dayData.grossPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", dayData.grossPnl)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (dayData.grossPnl >= 0) ProfitGreen else LossRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Charges",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.1f", dayData.charges)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trades on this date section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trades for this Day (${dayData.totalTrades})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = { onAddTradeForDate(dayData.dateString) },
                    modifier = Modifier.testTag("button_add_trade_for_date")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Trade", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (dayData.trades.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trades logged on this date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onAddTradeForDate(dayData.dateString) },
                            modifier = Modifier.testTag("button_empty_add_trade")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Log Trade for ${dayData.dateString}")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dayData.trades, key = { it.id }) { trade ->
                        DayTradeItemCard(
                            trade = trade,
                            currencySymbol = currencySymbol,
                            onClick = { onTradeClick(trade) },
                            onEdit = { onEditTrade(trade) },
                            onDelete = { onDeleteTrade(trade) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Trade Card within Day Details Sheet
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayTradeItemCard(
    trade: Trade,
    currencySymbol: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWin = trade.netPnl > 0
    val isLoss = trade.netPnl < 0
    val pnlColor = when {
        isWin -> ProfitGreen
        isLoss -> LossRed
        else -> BreakevenGold
    }
    val badgeBg = when {
        isWin -> ProfitGreenBg
        isLoss -> LossRedBg
        else -> BreakevenGoldBg
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("day_trade_card_${trade.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Direction, Stock, Qty, Net PnL, R
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (trade.direction == "BUY") ElectricBlue.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = trade.direction,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (trade.direction == "BUY") ElectricBlue else LossRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trade.stockName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "x${trade.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (trade.netPnl > 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.netPnl)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                    Text(
                        text = "${if (trade.rMultiple > 0) "+" else ""}${String.format(Locale.getDefault(), "%.2f", trade.rMultiple)}R",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Setup, Entry/Exit Prices, SL, Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = trade.setup.ifBlank { "General" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    if (trade.entryPrice > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$currencySymbol${trade.entryPrice} → $currencySymbol${trade.exitPrice}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (trade.stopLoss > 0) {
                    Text(
                        text = "SL: $currencySymbol${trade.stopLoss}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mistakes tags
            val mistakeList = trade.mistakes.split(",").map { it.trim() }.filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) }
            if (mistakeList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mistakeList.forEach { mistake ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LossRed.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, LossRed.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "⚠️ $mistake",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = LossRed,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            if (trade.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trade.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("button_edit_day_trade_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Trade",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("button_delete_day_trade_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Trade",
                        tint = LossRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
