package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Trade
import com.example.data.model.TradingSetup
import com.example.ui.components.TradeDetailDialog
import com.example.ui.components.TradeItemCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate700
import java.util.Locale

@Composable
fun TradeHistoryScreen(
    trades: List<Trade>,
    filteredTrades: List<Trade>,
    setups: List<TradingSetup>,
    currencySymbol: String,
    searchQuery: String,
    filterSetup: String?,
    filterResult: String?,
    selectedTradeDetail: Trade?,
    onSearchChange: (String) -> Unit,
    onFilterSetupChange: (String?) -> Unit,
    onFilterResultChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onTradeClick: (Trade) -> Unit,
    onEditTrade: (Trade) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onAddTradeClick: () -> Unit,
    onDismissDetail: () -> Unit,
    onExportCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tradeToDelete by remember { mutableStateOf<Trade?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val filteredNetPnl = filteredTrades.sumOf { it.netPnl }
    val filteredWins = filteredTrades.count { it.netPnl > 0 }
    val filteredWinRate = if (filteredTrades.isNotEmpty()) {
        (filteredWins.toDouble() / filteredTrades.size) * 100.0
    } else 0.0

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("trade_history_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trade History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${trades.size} total trades recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("button_export_csv")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export CSV",
                                tint = ElectricBlue
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search stock, setup, notes, mistake...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_trades"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = filterResult == null && filterSetup == null,
                            onClick = onClearFilters,
                            label = { Text("All (${trades.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterResult == "WIN",
                            onClick = { onFilterResultChange(if (filterResult == "WIN") null else "WIN") },
                            label = { Text("Wins") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ProfitGreen.copy(alpha = 0.2f),
                                selectedLabelColor = ProfitGreen
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterResult == "LOSS",
                            onClick = { onFilterResultChange(if (filterResult == "LOSS") null else "LOSS") },
                            label = { Text("Losses") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LossRed.copy(alpha = 0.2f),
                                selectedLabelColor = LossRed
                            )
                        )
                    }

                    // Setup filters
                    items(setups) { setup ->
                        val isSel = filterSetup.equals(setup.name, ignoreCase = true)
                        FilterChip(
                            selected = isSel,
                            onClick = { onFilterSetupChange(if (isSel) null else setup.name) },
                            label = { Text(setup.name) }
                        )
                    }
                }
            }

            // Filter Results Summary Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing ${filteredTrades.size} of ${trades.size} trades (${String.format(Locale.getDefault(), "%.1f", filteredWinRate)}% WR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (filteredNetPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", filteredNetPnl)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (filteredNetPnl >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }

            // Trade List or Empty state
            if (filteredTrades.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching trades found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try clearing filters or log a new trade",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onClearFilters) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredTrades, key = { it.id }) { trade ->
                    TradeItemCard(
                        trade = trade,
                        currencySymbol = currencySymbol,
                        onClick = { onTradeClick(trade) },
                        onEdit = { onEditTrade(trade) },
                        onDelete = { tradeToDelete = trade }
                    )
                }
            }
        }
    }

    // Trade Detail Dialog
    selectedTradeDetail?.let { trade ->
        TradeDetailDialog(
            trade = trade,
            currencySymbol = currencySymbol,
            onDismiss = onDismissDetail,
            onEdit = {
                onDismissDetail()
                onEditTrade(trade)
            }
        )
    }

    // Delete Confirmation Dialog
    tradeToDelete?.let { trade ->
        AlertDialog(
            onDismissRequest = { tradeToDelete = null },
            title = { Text("Delete Trade") },
            text = {
                Text("Are you sure you want to delete this trade (${trade.stockName} on ${trade.date})? All metrics and the equity curve will recalculate automatically.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTrade(trade)
                        tradeToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tradeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export CSV Dialog
    if (showExportDialog) {
        val csvData = remember { onExportCsv() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Trades to CSV") },
            text = {
                Column {
                    Text(
                        text = "Your trading journal is ready to export for Google Sheets or Excel backup. Tap copy to copy full CSV data to clipboard.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = csvData.take(200) + if (csvData.length > 200) "\n..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Trading Journal CSV", csvData)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("Copy CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
