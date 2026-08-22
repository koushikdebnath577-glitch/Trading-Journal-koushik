package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.theme.BreakevenGold
import com.example.ui.theme.BreakevenGoldBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import java.util.Locale

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .testTag(testTag.ifBlank { "stat_card_${title.lowercase().replace(" ", "_")}" }),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(iconTint.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DisciplineAlertBanner(
    isMaxTradesExceeded: Boolean,
    isMaxLossExceeded: Boolean,
    todayTradesCount: Int,
    maxTradesAllowed: Int,
    todayNetPnl: Double,
    maxDailyLossAllowed: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (!isMaxTradesExceeded && !isMaxLossExceeded) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("discipline_alert_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LossRedBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Discipline Warning",
                tint = LossRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Discipline Warning!",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                )
                if (isMaxTradesExceeded) {
                    Text(
                        text = "Daily limit of $maxTradesAllowed trades reached ($todayTradesCount taken today). Stop trading for today!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (isMaxLossExceeded) {
                    Text(
                        text = "Daily loss limit ($currencySymbol${String.format(Locale.getDefault(), "%,.0f", maxDailyLossAllowed)}) breached. Today's loss: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", -todayNetPnl)}. Protect your capital!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TradeItemCard(
    trade: Trade,
    currencySymbol: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWin = trade.netPnl > 0
    val isLoss = trade.netPnl < 0
    val isBreakeven = trade.netPnl == 0.0

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
            .testTag("trade_card_${trade.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Stock, Direction, Net PnL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Buy/Sell Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
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

                // Net PnL
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

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-info: Setup, Prices, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = trade.setup.ifBlank { "General" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (trade.entryPrice > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currencySymbol${trade.entryPrice} → $currencySymbol${trade.exitPrice}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${trade.date} ${trade.entryTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mistakes chips if present
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
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, LossRed.copy(alpha = 0.3f))
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = trade.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Bottom action row
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
                        .size(32.dp)
                        .testTag("edit_trade_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Trade",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_trade_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Trade",
                        tint = LossRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TradeDetailDialog(
    trade: Trade,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val isWin = trade.netPnl > 0
    val pnlColor = if (isWin) ProfitGreen else if (trade.netPnl < 0) LossRed else BreakevenGold

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = trade.setup,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pnlColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = trade.result,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = Slate700.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Date & Time", "${trade.date} | ${trade.entryTime} - ${trade.exitTime}")
                DetailRow("Quantity", "${trade.quantity} shares")
                DetailRow("Entry Price", "$currencySymbol${trade.entryPrice}")
                DetailRow("Exit Price", "$currencySymbol${trade.exitPrice}")
                DetailRow("Stop Loss", "$currencySymbol${trade.stopLoss}")
                DetailRow("Target", "$currencySymbol${trade.target}")
                DetailRow("Risk Amount", "$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.riskAmount)}")
                DetailRow("Gross P&L", "${if (trade.grossPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.grossPnl)}")
                DetailRow("Charges & Brokerage", "$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.charges)}")
                DetailRow(
                    "Net P&L",
                    "${if (trade.netPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", trade.netPnl)}",
                    valueColor = pnlColor,
                    isBold = true
                )
                DetailRow(
                    "R Multiple",
                    "${if (trade.rMultiple >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.2f", trade.rMultiple)}R",
                    valueColor = pnlColor,
                    isBold = true
                )

                if (trade.mistakes.isNotBlank() && !trade.mistakes.equals("None", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mistakes / Discipline Notes:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LossRed
                    )
                    Text(
                        text = trade.mistakes,
                        style = MaterialTheme.typography.bodySmall,
                        color = LossRed.copy(alpha = 0.9f)
                    )
                }

                if (trade.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trade Notes:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trade.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onEdit()
                },
                modifier = Modifier.testTag("dialog_edit_button")
            ) {
                Text("Edit Trade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
