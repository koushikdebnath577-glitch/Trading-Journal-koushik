package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailyPerformance
import com.example.analytics.EquityPoint
import com.example.analytics.SetupStats
import com.example.ui.theme.ElectricBlue
import kotlin.math.abs
import kotlin.math.max
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun EquityCurveChart(
    equityPoints: List<EquityPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    if (equityPoints.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add trades to visualize your Equity Curve",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedPoint by remember { mutableStateOf<EquityPoint?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(equityPoints.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val cumulativeValues = equityPoints.map { it.cumulativeNetPnl }
    val maxVal = max(500.0, cumulativeValues.maxOrNull() ?: 500.0)
    val minVal = min(-500.0, cumulativeValues.minOrNull() ?: -500.0)
    val valRange = (maxVal - minVal).coerceAtLeast(100.0)

    val isNetProfitPositive = (equityPoints.lastOrNull()?.cumulativeNetPnl ?: 0.0) >= 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showTitle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Equity Curve (Cumulative P&L)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tracks running net profit trade by trade",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val latestNet = equityPoints.lastOrNull()?.cumulativeNetPnl ?: 0.0
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (latestNet >= 0) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${if (latestNet >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.0f", latestNet)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (latestNet >= 0) ProfitGreen else LossRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Interactive Tooltip if tapped
            selectedPoint?.let { pt ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (pt.tradeIndex == 0) "Starting Base" else "#${pt.tradeIndex} ${pt.stockName} (${pt.date})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (pt.tradeIndex > 0) {
                                Text(
                                    text = "Trade P&L: ${if (pt.netPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", pt.netPnl)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pt.netPnl >= 0) ProfitGreen else LossRed
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cumulative",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (pt.cumulativeNetPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.1f", pt.cumulativeNetPnl)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pt.cumulativeNetPnl >= 0) ProfitGreen else LossRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(equityPoints) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (equityPoints.size - 1).coerceAtLeast(1)
                                val index = ((offset.x / spacing) + 0.5f).toInt().coerceIn(0, equityPoints.size - 1)
                                selectedPoint = equityPoints.getOrNull(index)
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24.dp.toPx()
                    val paddingTop = 12.dp.toPx()
                    val usableHeight = height - paddingTop - paddingBottom

                    val count = equityPoints.size
                    val stepX = if (count > 1) width / (count - 1) else width

                    // Y for zero
                    val zeroY = paddingTop + usableHeight * (1.0f - ((0.0 - minVal) / valRange).toFloat())

                    // Draw grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val frac = i.toFloat() / gridSteps
                        val y = paddingTop + usableHeight * (1f - frac)
                        drawLine(
                            color = Slate700.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Draw Zero line (solid)
                    drawLine(
                        color = Slate400.copy(alpha = 0.6f),
                        start = Offset(0f, zeroY),
                        end = Offset(width, zeroY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Build path
                    val linePath = Path()
                    val fillPath = Path()
                    val pointsList = mutableListOf<Offset>()

                    equityPoints.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val yNormalized = ((pt.cumulativeNetPnl - minVal) / valRange).toFloat()
                        val targetY = paddingTop + usableHeight * (1.0f - yNormalized)
                        // apply animated height
                        val y = zeroY + (targetY - zeroY) * animProgress.value

                        val offset = Offset(x, y)
                        pointsList.add(offset)

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, zeroY)
                            fillPath.lineTo(x, y)
                        } else {
                            val prev = pointsList[i - 1]
                            val cx = (prev.x + x) / 2f
                            linePath.cubicTo(cx, prev.y, cx, y, x, y)
                            fillPath.cubicTo(cx, prev.y, cx, y, x, y)
                        }
                    }

                    if (pointsList.isNotEmpty()) {
                        fillPath.lineTo(pointsList.last().x, zeroY)
                        fillPath.close()

                        // Gradient fill
                        val gradientBrush = Brush.verticalGradient(
                            colors = if (isNetProfitPositive) {
                                listOf(ProfitGreen.copy(alpha = 0.35f), ProfitGreen.copy(alpha = 0.02f))
                            } else {
                                listOf(LossRed.copy(alpha = 0.35f), LossRed.copy(alpha = 0.02f))
                            },
                            startY = paddingTop,
                            endY = height
                        )
                        drawPath(fillPath, brush = gradientBrush)

                        // Stroke line
                        val strokeColor = if (isNetProfitPositive) ProfitGreen else LossRed
                        drawPath(
                            linePath,
                            color = strokeColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw trade points
                        pointsList.forEachIndexed { idx, pt ->
                            val isSel = selectedPoint == equityPoints[idx]
                            drawCircle(
                                color = if (isSel) Color.White else strokeColor,
                                radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx(),
                                center = pt
                            )
                            if (isSel) {
                                drawCircle(
                                    color = strokeColor,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }

            // X Axis trade progression indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = equityPoints.firstOrNull()?.date ?: "Start",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Trades: ${equityPoints.size - 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = equityPoints.lastOrNull()?.date ?: "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DailyPnlBarChart(
    dailyList: List<DailyPerformance>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No daily trading history recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Take last 10 days for clean mobile visualization
    val displayDays = dailyList.take(10).reversed()
    val maxDailyPnl = max(500.0, displayDays.maxOfOrNull { abs(it.netPnl) } ?: 500.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Net P&L Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Green: Profit days | Red: Loss days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f

                    // Draw baseline at 0
                    drawLine(
                        color = Slate600.copy(alpha = 0.5f),
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1.dp.toPx()
                    )

                    val barCount = displayDays.size
                    val totalBarSpace = width / barCount
                    val barWidth = (totalBarSpace * 0.55f).coerceIn(12.dp.toPx(), 28.dp.toPx())

                    displayDays.forEachIndexed { index, day ->
                        val centerX = (index * totalBarSpace) + (totalBarSpace / 2f)
                        val barHeight = ((abs(day.netPnl) / maxDailyPnl) * (midY - 12.dp.toPx())).toFloat().coerceAtLeast(4f)

                        val isProfit = day.netPnl >= 0
                        val barTop = if (isProfit) midY - barHeight else midY
                        val barColor = if (isProfit) ProfitGreen else LossRed

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(centerX - barWidth / 2f, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayDays.forEach { day ->
                    val shortDate = if (day.date.length >= 10) day.date.substring(5) else day.date
                    Text(
                        text = shortDate,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SetupStatsCard(
    stats: List<SetupStats>,
    currencySymbol: String,
    onAddSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                        text = "Setup-Wise Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Win rate & profitability per strategy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (stats.isEmpty()) {
                Text(
                    text = "No setup data recorded yet. Tag your trades with setups!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                stats.forEach { item ->
                    SetupItemRow(item = item, currencySymbol = currencySymbol)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SetupItemRow(
    item: SetupStats,
    currencySymbol: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (item.winRate >= 50) ProfitGreen else LossRed,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.setupName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${if (item.totalNetPnl >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.0f", item.totalNetPnl)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.totalNetPnl >= 0) ProfitGreen else LossRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar for win rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Slate700.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.winRate / 100f).toFloat().coerceIn(0f, 1f))
                            .height(8.dp)
                            .background(
                                color = if (item.winRate >= 50) ProfitGreen else LossRed,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", item.winRate)}% Win",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.winRate >= 50) ProfitGreen else LossRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trades: ${item.totalTrades} (${item.winningTrades}W / ${item.losingTrades}L)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Avg R: ${String.format(Locale.getDefault(), "%.2f", item.averageR)}R",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "PF: ${String.format(Locale.getDefault(), "%.2f", item.profitFactor)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Exp: ${String.format(Locale.getDefault(), "%.2f", item.expectancy)}R",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = ElectricBlue
                )
            }
        }
    }
}

@Composable
fun RDistributionChart(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "R-Multiple Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Frequency of returns normalized by risk taken",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val maxCount = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)

            distribution.forEach { (bucket, count) ->
                val isNegative = bucket.contains("-") || bucket.contains("<")
                val isHighPositive = bucket.contains("2R") || bucket.contains("3R") || bucket.contains(">")

                val barColor = when {
                    isNegative -> LossRed
                    isHighPositive -> ProfitGreen
                    else -> ElectricBlue
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bucket,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(75.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .background(Slate700.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    ) {
                        val fraction = (count.toFloat() / maxCount).coerceIn(0f, 1f)
                        if (fraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(14.dp)
                                    .background(barColor, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
