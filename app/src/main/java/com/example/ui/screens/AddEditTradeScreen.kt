package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.data.model.TradingSetup
import com.example.data.model.UserSettings
import com.example.ui.theme.BreakevenGold
import com.example.ui.theme.BreakevenGoldBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

val POPULAR_INDIAN_STOCKS = listOf(
    "NIFTY", "BANKNIFTY", "RELIANCE", "HDFCBANK", "TCS",
    "INFY", "TATAMOTORS", "SBIN", "TATASTEEL", "BAJFINANCE", "ITC"
)

val MISTAKE_OPTIONS = listOf(
    "None", "FOMO", "Early Entry", "Late Entry",
    "Overtrading", "Revenge Trading", "Moved Stop Loss",
    "Did Not Follow Setup", "Emotional Trading", "Sizing Too Large"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTradeScreen(
    tradeToEdit: Trade?,
    setups: List<TradingSetup>,
    settings: UserSettings,
    todayTradesCount: Int,
    todayNetPnl: Double,
    onSaveTrade: (
        id: Long,
        date: String,
        stockName: String,
        setup: String,
        direction: String,
        entryPrice: Double,
        exitPrice: Double,
        quantity: Int,
        stopLoss: Double,
        target: Double,
        charges: Double,
        riskAmount: Double,
        entryTime: String,
        exitTime: String,
        notes: String,
        mistakes: String,
        screenshotUri: String?
    ) -> Unit,
    onAddNewSetup: (String, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayFormatted = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val nowTimeFormatted = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }

    var date by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.date ?: todayFormatted) }
    var stockName by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.stockName ?: "") }
    var selectedSetup by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.setup ?: (setups.firstOrNull()?.name ?: "Morning Breakout")) }
    var direction by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.direction ?: "BUY") } // "BUY" or "SELL"

    var entryPriceStr by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.entryPrice?.toString() ?: "") }
    var exitPriceStr by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.exitPrice?.toString() ?: "") }
    var quantityStr by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.quantity?.toString() ?: "50") }
    var stopLossStr by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.stopLoss?.toString() ?: "") }
    var targetStr by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.target?.toString() ?: "") }

    var chargesStr by remember(tradeToEdit) {
        mutableStateOf(tradeToEdit?.charges?.toString() ?: settings.defaultChargesRate.toString())
    }
    var riskAmountStr by remember(tradeToEdit) {
        mutableStateOf(tradeToEdit?.riskAmount?.toString() ?: "")
    }

    var entryTime by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.entryTime ?: nowTimeFormatted) }
    var exitTime by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.exitTime ?: "") }
    var notes by remember(tradeToEdit) { mutableStateOf(tradeToEdit?.notes ?: "") }

    val initialMistakes = remember(tradeToEdit) {
        tradeToEdit?.mistakes?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }
    var selectedMistakes by remember(tradeToEdit) { mutableStateOf(initialMistakes) }

    var showAddSetupDialog by remember { mutableStateOf(false) }
    var newSetupName by remember { mutableStateOf("") }
    var setupDropdownExpanded by remember { mutableStateOf(false) }

    // Live calculations derived
    val entryPrice = entryPriceStr.toDoubleOrNull() ?: 0.0
    val exitPrice = exitPriceStr.toDoubleOrNull() ?: 0.0
    val quantity = quantityStr.toIntOrNull() ?: 0
    val stopLoss = stopLossStr.toDoubleOrNull() ?: 0.0
    val target = targetStr.toDoubleOrNull() ?: 0.0
    val charges = chargesStr.toDoubleOrNull() ?: 0.0

    val grossPnl by remember(direction, entryPrice, exitPrice, quantity) {
        derivedStateOf {
            if (entryPrice > 0 && exitPrice > 0 && quantity > 0) {
                if (direction == "BUY") (exitPrice - entryPrice) * quantity else (entryPrice - exitPrice) * quantity
            } else 0.0
        }
    }

    val netPnl by remember(grossPnl, charges) {
        derivedStateOf { grossPnl - charges }
    }

    val calculatedRisk by remember(direction, entryPrice, stopLoss, quantity, riskAmountStr) {
        derivedStateOf {
            val userRisk = riskAmountStr.toDoubleOrNull()
            if (userRisk != null && userRisk > 0) {
                userRisk
            } else if (entryPrice > 0 && stopLoss > 0 && quantity > 0) {
                abs(entryPrice - stopLoss) * quantity
            } else {
                settings.defaultRiskAmount
            }
        }
    }

    val rMultiple by remember(netPnl, calculatedRisk) {
        derivedStateOf {
            if (calculatedRisk > 0 && (entryPrice > 0 && exitPrice > 0)) {
                netPnl / calculatedRisk
            } else 0.0
        }
    }

    val resultType by remember(netPnl, entryPrice, exitPrice) {
        derivedStateOf {
            if (entryPrice <= 0 || exitPrice <= 0) "PENDING"
            else if (netPnl > 0.001) "WIN"
            else if (netPnl < -0.001) "LOSS"
            else "BREAKEVEN"
        }
    }

    val isDisciplineWarningActive = tradeToEdit == null && (
        todayTradesCount >= settings.maxTradesPerDay ||
        (todayNetPnl < 0 && abs(todayNetPnl) >= settings.maxDailyLoss)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_edit_trade_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (tradeToEdit != null) "Edit Trade #${tradeToEdit.id}" else "Log New Trade",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Discipline Check Warning if daily limit exceeded
        if (isDisciplineWarningActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LossRedBg),
                    border = BorderStroke(1.dp, LossRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = LossRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rule Violation Alert!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LossRed
                            )
                            Text(
                                text = "You have already taken $todayTradesCount trades today (Limit: ${settings.maxTradesPerDay}). Overtrading leads to account drawdown!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Live Auto-Calculation Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_calculation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Calculated Metrics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val resultColor = when (resultType) {
                            "WIN" -> ProfitGreen
                            "LOSS" -> LossRed
                            "BREAKEVEN" -> BreakevenGold
                            else -> Slate700
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = resultColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = resultType,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = resultColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Gross P&L",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (grossPnl >= 0) "+" else ""}${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.1f", grossPnl)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (grossPnl >= 0) ProfitGreen else LossRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Charges",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "-${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.1f", charges)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net P&L",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (netPnl >= 0) "+" else ""}${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.1f", netPnl)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netPnl >= 0) ProfitGreen else LossRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Slate700.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Risk: ${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", calculatedRisk)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "R Multiple: ${if (rMultiple >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.2f", rMultiple)}R",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (rMultiple >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }
        }

        // Direction Selector (BUY vs SELL)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BUY Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { direction = "BUY" }
                        .testTag("direction_buy_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (direction == "BUY") ProfitGreenBg else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (direction == "BUY") 2.dp else 1.dp,
                        color = if (direction == "BUY") ProfitGreen else Slate700.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (direction == "BUY") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "BUY / LONG",
                            fontWeight = FontWeight.Bold,
                            color = if (direction == "BUY") ProfitGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // SELL Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { direction = "SELL" }
                        .testTag("direction_sell_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (direction == "SELL") LossRedBg else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (direction == "SELL") 2.dp else 1.dp,
                        color = if (direction == "SELL") LossRed else Slate700.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (direction == "SELL") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "SELL / SHORT",
                            fontWeight = FontWeight.Bold,
                            color = if (direction == "SELL") LossRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Date & Stock Name
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Trade Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_trade_date"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stockName,
                    onValueChange = { stockName = it },
                    label = { Text("Stock / Symbol") },
                    placeholder = { Text("e.g. RELIANCE") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_stock_name"),
                    singleLine = true
                )
            }
        }

        // Quick Stock Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(POPULAR_INDIAN_STOCKS) { stock ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (stockName.equals(stock, ignoreCase = true)) ElectricBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (stockName.equals(stock, ignoreCase = true)) BorderStroke(1.dp, ElectricBlue) else null,
                        modifier = Modifier.clickable { stockName = stock }
                    ) {
                        Text(
                            text = stock,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (stockName.equals(stock, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                            color = if (stockName.equals(stock, ignoreCase = true)) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Setup Selection Dropdown + Add Setup Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = setupDropdownExpanded,
                    onExpandedChange = { setupDropdownExpanded = !setupDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedSetup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trading Setup") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = setupDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("input_setup_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = setupDropdownExpanded,
                        onDismissRequest = { setupDropdownExpanded = false }
                    ) {
                        setups.forEach { setup ->
                            DropdownMenuItem(
                                text = { Text(setup.name) },
                                onClick = {
                                    selectedSetup = setup.name
                                    setupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showAddSetupDialog = true },
                    modifier = Modifier.testTag("button_add_new_setup")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Setup",
                        tint = ElectricBlue
                    )
                }
            }
        }

        // Prices & Quantity: Entry Price, Exit Price, Quantity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = entryPriceStr,
                    onValueChange = { entryPriceStr = it },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_entry_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = exitPriceStr,
                    onValueChange = { exitPriceStr = it },
                    label = { Text("Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_exit_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_quantity"),
                    singleLine = true
                )
            }
        }

        // Stop Loss, Target, Charges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = stopLossStr,
                    onValueChange = { stopLossStr = it },
                    label = { Text("Stop Loss") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_stop_loss"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_target"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = chargesStr,
                    onValueChange = { chargesStr = it },
                    label = { Text("Charges (${settings.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_charges"),
                    singleLine = true
                )
            }
        }

        // Execution Times: Entry Time & Exit Time
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = entryTime,
                    onValueChange = { entryTime = it },
                    label = { Text("Entry Time") },
                    placeholder = { Text("09:20 AM") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_entry_time"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = exitTime,
                    onValueChange = { exitTime = it },
                    label = { Text("Exit Time") },
                    placeholder = { Text("10:15 AM") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_exit_time"),
                    singleLine = true
                )
            }
        }

        // Mistake / Trade Reason Multi-Select Chips
        item {
            Column {
                Text(
                    text = "Mistakes / Discipline Tags",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MISTAKE_OPTIONS.forEach { option ->
                        val isSelected = if (option == "None") {
                            selectedMistakes.isEmpty() || selectedMistakes.contains("None")
                        } else {
                            selectedMistakes.contains(option)
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (option == "None") {
                                    selectedMistakes = emptySet()
                                } else {
                                    val current = selectedMistakes.toMutableSet()
                                    current.remove("None")
                                    if (current.contains(option)) {
                                        current.remove(option)
                                    } else {
                                        current.add(option)
                                    }
                                    selectedMistakes = current
                                }
                            },
                            label = { Text(option, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (option == "None") ProfitGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f),
                                selectedLabelColor = if (option == "None") ProfitGreen else LossRed
                            )
                        )
                    }
                }
            }
        }

        // Trade Notes & Learning
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Trade Notes & Psychological Mindset") },
                placeholder = { Text("Why did you enter? Was it strictly according to plan? Did you follow your risk rules?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("input_notes"),
                maxLines = 4
            )
        }

        // Action Buttons: Save & Cancel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("button_cancel_trade")
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val validStock = stockName.ifBlank { "STOCK" }
                        val validSetup = selectedSetup.ifBlank { "General" }
                        val mistakesString = if (selectedMistakes.isEmpty()) "None" else selectedMistakes.joinToString(",")

                        onSaveTrade(
                            tradeToEdit?.id ?: 0L,
                            date,
                            validStock,
                            validSetup,
                            direction,
                            entryPrice,
                            exitPrice,
                            quantity,
                            stopLoss,
                            target,
                            charges,
                            calculatedRisk,
                            entryTime,
                            exitTime,
                            notes,
                            mistakesString,
                            null
                        )
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("button_save_trade"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    enabled = entryPrice > 0 && exitPrice > 0 && quantity > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (tradeToEdit != null) "Update Trade" else "Save Trade",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }

    // Add Setup Dialog
    if (showAddSetupDialog) {
        AlertDialog(
            onDismissRequest = { showAddSetupDialog = false },
            title = { Text("Add New Setup") },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSetupName.isNotBlank()) {
                            onAddNewSetup(newSetupName.trim(), "")
                            selectedSetup = newSetupName.trim()
                            newSetupName = ""
                            showAddSetupDialog = false
                        }
                    }
                ) {
                    Text("Add")
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
