package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AddEditTradeScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RulesAndSettingsScreen
import com.example.ui.screens.TradeHistoryScreen
import com.example.ui.screens.TradingCalendarScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TradingJournalTheme
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            TradingJournalTheme(themeMode = uiState.settings.themeMode) {
                MainAppScaffold(
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: TradingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Trading Journal",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ElectricBlue.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "NSE / BSE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Intraday Performance & Risk Log",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Today's live P&L badge in top bar
                    val todayPnl = uiState.analytics.todayNetPnl
                    val isPos = todayPnl >= 0
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (isPos) ProfitGreen else LossRed).copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today: ${if (isPos) "+" else ""}${uiState.settings.currencySymbol}${String.format(Locale.getDefault(), "%,.0f", todayPnl)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPos) ProfitGreen else LossRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.DASHBOARD,
                    onClick = { viewModel.navigateTo(ScreenTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                // Trades Tab
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.TRADES,
                    onClick = { viewModel.navigateTo(ScreenTab.TRADES) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Trades") },
                    label = { Text("Trades", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_trades")
                )

                // Calendar Tab
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.CALENDAR,
                    onClick = { viewModel.navigateTo(ScreenTab.CALENDAR) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_calendar")
                )

                // Analytics Tab
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.ANALYTICS,
                    onClick = { viewModel.navigateTo(ScreenTab.ANALYTICS) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_analytics")
                )

                // Rules & Settings Tab
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.navigateTo(ScreenTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Rules") },
                    label = { Text("Rules", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_rules")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        analytics = uiState.analytics,
                        recentTrades = uiState.trades,
                        currencySymbol = uiState.settings.currencySymbol,
                        onAddTradeClick = {
                            viewModel.setTradeToEdit(null)
                            viewModel.navigateTo(ScreenTab.ADD_TRADE)
                        },
                        onViewAllTradesClick = { viewModel.navigateTo(ScreenTab.TRADES) },
                        onTradeClick = { trade -> viewModel.setSelectedTradeDetail(trade) },
                        onEditTrade = { trade -> viewModel.setTradeToEdit(trade) },
                        onDeleteTrade = { trade -> viewModel.deleteTrade(trade) },
                        onOpenCalendarClick = { viewModel.navigateTo(ScreenTab.CALENDAR) }
                    )

                    ScreenTab.TRADES -> TradeHistoryScreen(
                        trades = uiState.trades,
                        filteredTrades = uiState.filteredTrades,
                        setups = uiState.setups,
                        currencySymbol = uiState.settings.currencySymbol,
                        searchQuery = uiState.searchQuery,
                        filterSetup = uiState.filterSetup,
                        filterResult = uiState.filterResult,
                        selectedTradeDetail = uiState.selectedTradeDetail,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterSetupChange = { viewModel.setFilterSetup(it) },
                        onFilterResultChange = { viewModel.setFilterResult(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onTradeClick = { trade -> viewModel.setSelectedTradeDetail(trade) },
                        onEditTrade = { trade -> viewModel.setTradeToEdit(trade) },
                        onDeleteTrade = { trade -> viewModel.deleteTrade(trade) },
                        onAddTradeClick = {
                            viewModel.setTradeToEdit(null)
                            viewModel.navigateTo(ScreenTab.ADD_TRADE)
                        },
                        onDismissDetail = { viewModel.setSelectedTradeDetail(null) },
                        onExportCsv = { viewModel.getCsvExportData() }
                    )

                    ScreenTab.CALENDAR -> TradingCalendarScreen(
                        trades = uiState.trades,
                        currencySymbol = uiState.settings.currencySymbol,
                        onTradeClick = { trade -> viewModel.setSelectedTradeDetail(trade) },
                        onEditTrade = { trade -> viewModel.setTradeToEdit(trade) },
                        onDeleteTrade = { trade -> viewModel.deleteTrade(trade) },
                        onAddTradeForDate = { dateStr -> viewModel.startNewTradeForDate(dateStr) }
                    )

                    ScreenTab.ADD_TRADE -> AddEditTradeScreen(
                        tradeToEdit = uiState.tradeToEdit,
                        setups = uiState.setups,
                        settings = uiState.settings,
                        todayTradesCount = uiState.analytics.todayTradesCount,
                        todayNetPnl = uiState.analytics.todayNetPnl,
                        onSaveTrade = { id, date, stock, setup, dir, entry, exit, qty, sl, tgt, charges, risk, eTime, xTime, notes, mistakes, uri ->
                            viewModel.saveTrade(
                                id, date, stock, setup, dir, entry, exit, qty, sl, tgt, charges, risk, eTime, xTime, notes, mistakes, uri
                            )
                        },
                        onAddNewSetup = { name, desc -> viewModel.addSetup(name, desc) },
                        onCancel = {
                            viewModel.setTradeToEdit(null)
                            viewModel.navigateTo(ScreenTab.DASHBOARD)
                        }
                    )

                    ScreenTab.ANALYTICS -> AnalyticsScreen(
                        analytics = uiState.analytics,
                        currencySymbol = uiState.settings.currencySymbol,
                        onAddNewSetup = { name, desc -> viewModel.addSetup(name, desc) },
                        onNavigateToCalendar = { viewModel.navigateTo(ScreenTab.CALENDAR) }
                    )

                    ScreenTab.SETTINGS -> RulesAndSettingsScreen(
                        analytics = uiState.analytics,
                        rules = uiState.rules,
                        settings = uiState.settings,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onAddRule = { title, desc -> viewModel.addRule(title, desc) },
                        onToggleRule = { viewModel.toggleRule(it) },
                        onDeleteRule = { viewModel.deleteRule(it) },
                        onResetSampleData = { viewModel.resetSampleData() },
                        onExportCsv = { viewModel.getCsvExportData() }
                    )
                }
            }
        }
    }
}
