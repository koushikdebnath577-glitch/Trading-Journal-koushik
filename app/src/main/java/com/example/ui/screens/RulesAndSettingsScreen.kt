package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.analytics.OverallAnalytics
import com.example.data.model.TradingRule
import com.example.data.model.UserSettings
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate700
import java.util.Locale

@Composable
fun RulesAndSettingsScreen(
    analytics: OverallAnalytics,
    rules: List<TradingRule>,
    settings: UserSettings,
    onUpdateSettings: (UserSettings) -> Unit,
    onAddRule: (String, String) -> Unit,
    onToggleRule: (TradingRule) -> Unit,
    onDeleteRule: (TradingRule) -> Unit,
    onResetSampleData: () -> Unit,
    onExportCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var startingCapitalStr by remember(settings.startingCapital) {
        mutableStateOf(settings.startingCapital.toInt().toString())
    }
    var maxDailyLossStr by remember(settings.maxDailyLoss) {
        mutableStateOf(settings.maxDailyLoss.toInt().toString())
    }
    var maxTradesPerDayStr by remember(settings.maxTradesPerDay) {
        mutableStateOf(settings.maxTradesPerDay.toString())
    }
    var defaultRiskStr by remember(settings.defaultRiskAmount) {
        mutableStateOf(settings.defaultRiskAmount.toInt().toString())
    }
    var defaultChargesStr by remember(settings.defaultChargesRate) {
        mutableStateOf(settings.defaultChargesRate.toInt().toString())
    }

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newRuleTitle by remember { mutableStateOf("") }
    var newRuleDesc by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rules_settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Capital & Risk Rules",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Capital management, discipline rules, and app settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Capital & Drawdown Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capital_drawdown_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capital & Equity State",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            title = "Starting Capital",
                            value = "${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", settings.startingCapital)}",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Current Equity",
                            value = "${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", analytics.currentCapital)}",
                            valueColor = if (analytics.netProfit >= 0) ProfitGreen else LossRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            title = "Peak Equity",
                            value = "${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", analytics.peakEquity)}",
                            subtitle = "High Water Mark",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Max Drawdown",
                            value = "-${settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", analytics.maxDrawdown)}",
                            subtitle = "${String.format(Locale.getDefault(), "%.1f", analytics.maxDrawdownPercent)}% from peak",
                            valueColor = if (analytics.maxDrawdown > 0) LossRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Risk & Capital Parameters
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
                        Text(
                            text = "Risk Limits & Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                val newCap = startingCapitalStr.toDoubleOrNull() ?: settings.startingCapital
                                val newLoss = maxDailyLossStr.toDoubleOrNull() ?: settings.maxDailyLoss
                                val newTrades = maxTradesPerDayStr.toIntOrNull() ?: settings.maxTradesPerDay
                                val newRisk = defaultRiskStr.toDoubleOrNull() ?: settings.defaultRiskAmount
                                val newCharges = defaultChargesStr.toDoubleOrNull() ?: settings.defaultChargesRate

                                onUpdateSettings(
                                    settings.copy(
                                        startingCapital = newCap,
                                        maxDailyLoss = newLoss,
                                        maxTradesPerDay = newTrades,
                                        defaultRiskAmount = newRisk,
                                        defaultChargesRate = newCharges
                                    )
                                )
                                Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("button_save_settings")
                        ) {
                            Text("Save")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startingCapitalStr,
                            onValueChange = { startingCapitalStr = it },
                            label = { Text("Starting Capital (${settings.currencySymbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = defaultRiskStr,
                            onValueChange = { defaultRiskStr = it },
                            label = { Text("Default Risk/Trade") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = maxDailyLossStr,
                            onValueChange = { maxDailyLossStr = it },
                            label = { Text("Max Daily Loss (${settings.currencySymbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxTradesPerDayStr,
                            onValueChange = { maxTradesPerDayStr = it },
                            label = { Text("Max Trades / Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = defaultChargesStr,
                        onValueChange = { defaultChargesStr = it },
                        label = { Text("Default Charges per Trade (${settings.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Trading Rules & Discipline Checklist
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
                                text = "Discipline & Trading Rules",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personal trading constitution and guardrails",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showAddRuleDialog = true },
                            modifier = Modifier.testTag("button_add_rule")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Rule", tint = ElectricBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (rules.isEmpty()) {
                        Text(
                            text = "No rules defined. Tap '+' to create your first discipline rule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        rules.forEach { rule ->
                            RuleItemRow(
                                rule = rule,
                                onToggle = { onToggleRule(rule) },
                                onDelete = { onDeleteRule(rule) }
                            )
                            HorizontalDivider(
                                color = Slate700.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // App Theme Mode Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme & Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.themeMode == "DARK",
                            onClick = { onUpdateSettings(settings.copy(themeMode = "DARK")) },
                            label = { Text("Dark Mode") },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.themeMode == "LIGHT",
                            onClick = { onUpdateSettings(settings.copy(themeMode = "LIGHT")) },
                            label = { Text("Light Mode") },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.themeMode == "SYSTEM",
                            onClick = { onUpdateSettings(settings.copy(themeMode = "SYSTEM")) },
                            label = { Text("System") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Data & Google Sheets Export / Backup
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Backup & Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val csv = onExportCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Trading Journal CSV", csv)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Full journal CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_export_csv_settings")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export to CSV (Google Sheets / Excel)")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_reset_sample_data"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reload Sample Trading Data")
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Add Personal Trading Rule") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newRuleTitle,
                        onValueChange = { newRuleTitle = it },
                        label = { Text("Rule Title") },
                        placeholder = { Text("e.g. No trading in first 10 minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newRuleDesc,
                        onValueChange = { newRuleDesc = it },
                        label = { Text("Description & Trigger") },
                        placeholder = { Text("Let market establish opening range before execution") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRuleTitle.isNotBlank()) {
                            onAddRule(newRuleTitle.trim(), newRuleDesc.trim())
                            newRuleTitle = ""
                            newRuleDesc = ""
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text("Add Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reload Sample Trades?") },
            text = {
                Text("This will populate realistic Indian intraday trades (Reliance, Nifty, HDFC Bank, TCS, Tata Motors, etc.) and reset test data.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSampleData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Sample trades reloaded!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RuleItemRow(
    rule: TradingRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (rule.description.isNotBlank()) {
                Text(
                    text = rule.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("switch_rule_${rule.id}")
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Rule",
                    tint = LossRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
