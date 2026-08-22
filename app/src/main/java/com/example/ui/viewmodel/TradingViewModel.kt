package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.OverallAnalytics
import com.example.analytics.TradeAnalyticsEngine
import com.example.data.db.AppDatabase
import com.example.data.model.Trade
import com.example.data.model.TradingRule
import com.example.data.model.TradingSetup
import com.example.data.model.UserSettings
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DASHBOARD,
    TRADES,
    CALENDAR,
    ADD_TRADE,
    ANALYTICS,
    SETTINGS
}

data class TradingUiState(
    val trades: List<Trade> = emptyList(),
    val filteredTrades: List<Trade> = emptyList(),
    val rules: List<TradingRule> = emptyList(),
    val setups: List<TradingSetup> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val analytics: OverallAnalytics = OverallAnalytics(),
    val currentTab: ScreenTab = ScreenTab.DASHBOARD,
    val searchQuery: String = "",
    val filterSetup: String? = null,
    val filterResult: String? = null, // "WIN", "LOSS", "BREAKEVEN", null for all
    val filterDate: String? = null,
    val tradeToEdit: Trade? = null,
    val selectedTradeDetail: Trade? = null
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = TradingRepository(
        tradeDao = database.tradeDao(),
        ruleDao = database.tradingRuleDao(),
        setupDao = database.tradingSetupDao(),
        context = application
    )

    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    private val _searchQuery = MutableStateFlow("")
    private val _filterSetup = MutableStateFlow<String?>(null)
    private val _filterResult = MutableStateFlow<String?>(null)
    private val _filterDate = MutableStateFlow<String?>(null)
    private val _tradeToEdit = MutableStateFlow<Trade?>(null)
    private val _selectedTradeDetail = MutableStateFlow<Trade?>(null)

    private data class RepositoryData(
        val trades: List<Trade>,
        val rules: List<TradingRule>,
        val setups: List<TradingSetup>,
        val settings: UserSettings
    )

    private data class FilterState(
        val setup: String?,
        val result: String?,
        val date: String?,
        val tradeToEdit: Trade?,
        val selectedTradeDetail: Trade?
    )

    private data class UiControlsData(
        val currentTab: ScreenTab,
        val searchQuery: String,
        val filterSetup: String?,
        val filterResult: String?,
        val filterDate: String?,
        val tradeToEdit: Trade?,
        val selectedTradeDetail: Trade?
    )

    private val repoDataFlow = combine(
        repository.allTrades,
        repository.allRules,
        repository.allSetups,
        repository.settingsFlow
    ) { trades, rules, setups, settings ->
        RepositoryData(trades, rules, setups, settings)
    }

    private val filtersFlow = combine(
        _filterSetup,
        _filterResult,
        _filterDate,
        _tradeToEdit,
        _selectedTradeDetail
    ) { setup, result, date, tradeToEdit, selectedTradeDetail ->
        FilterState(setup, result, date, tradeToEdit, selectedTradeDetail)
    }

    private val uiControlsFlow = combine(
        _currentTab,
        _searchQuery,
        filtersFlow
    ) { currentTab, searchQuery, filters ->
        UiControlsData(
            currentTab = currentTab,
            searchQuery = searchQuery,
            filterSetup = filters.setup,
            filterResult = filters.result,
            filterDate = filters.date,
            tradeToEdit = filters.tradeToEdit,
            selectedTradeDetail = filters.selectedTradeDetail
        )
    }

    val uiState: StateFlow<TradingUiState> = combine(
        repoDataFlow,
        uiControlsFlow
    ) { repo, controls ->
        val analytics = TradeAnalyticsEngine.computeAnalytics(repo.trades, repo.settings)

        val filteredTrades = repo.trades.filter { trade ->
            val matchesQuery = if (controls.searchQuery.isBlank()) true else {
                trade.stockName.contains(controls.searchQuery, ignoreCase = true) ||
                trade.setup.contains(controls.searchQuery, ignoreCase = true) ||
                trade.notes.contains(controls.searchQuery, ignoreCase = true) ||
                trade.mistakes.contains(controls.searchQuery, ignoreCase = true) ||
                trade.date.contains(controls.searchQuery, ignoreCase = true)
            }

            val matchesSetup = controls.filterSetup == null ||
                trade.setup.equals(controls.filterSetup, ignoreCase = true)

            val matchesResult = controls.filterResult == null ||
                trade.result.equals(controls.filterResult, ignoreCase = true)

            val matchesDate = controls.filterDate == null ||
                trade.date.startsWith(controls.filterDate)

            matchesQuery && matchesSetup && matchesResult && matchesDate
        }

        TradingUiState(
            trades = repo.trades,
            filteredTrades = filteredTrades,
            rules = repo.rules,
            setups = repo.setups,
            settings = repo.settings,
            analytics = analytics,
            currentTab = controls.currentTab,
            searchQuery = controls.searchQuery,
            filterSetup = controls.filterSetup,
            filterResult = controls.filterResult,
            filterDate = controls.filterDate,
            tradeToEdit = controls.tradeToEdit,
            selectedTradeDetail = controls.selectedTradeDetail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TradingUiState()
    )

    fun navigateTo(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterSetup(setup: String?) {
        _filterSetup.value = setup
    }

    fun setFilterResult(result: String?) {
        _filterResult.value = result
    }

    fun setFilterDate(date: String?) {
        _filterDate.value = date
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterSetup.value = null
        _filterResult.value = null
        _filterDate.value = null
    }

    fun setTradeToEdit(trade: Trade?) {
        _tradeToEdit.value = trade
        if (trade != null) {
            _currentTab.value = ScreenTab.ADD_TRADE
        }
    }

    fun startNewTradeForDate(dateStr: String) {
        _tradeToEdit.value = Trade(
            id = 0,
            date = dateStr,
            stockName = "",
            setup = "Morning Breakout",
            direction = "BUY",
            entryPrice = 0.0,
            exitPrice = 0.0,
            quantity = 50,
            stopLoss = 0.0,
            target = 0.0,
            grossPnl = 0.0,
            charges = 0.0,
            netPnl = 0.0,
            riskAmount = 0.0,
            rMultiple = 0.0,
            result = "WIN"
        )
        _currentTab.value = ScreenTab.ADD_TRADE
    }

    fun setSelectedTradeDetail(trade: Trade?) {
        _selectedTradeDetail.value = trade
    }

    fun saveTrade(
        id: Long = 0,
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
        screenshotUri: String? = null
    ) {
        viewModelScope.launch {
            // Calculate Gross P&L
            val grossPnl = if (direction.equals("BUY", ignoreCase = true)) {
                (exitPrice - entryPrice) * quantity
            } else {
                (entryPrice - exitPrice) * quantity
            }

            // Calculate Net P&L
            val netPnl = grossPnl - charges

            // Calculate Risk Amount if <= 0
            val effectiveRisk = if (riskAmount > 0) {
                riskAmount
            } else {
                val perShareRisk = kotlin.math.abs(entryPrice - stopLoss)
                if (perShareRisk > 0) perShareRisk * quantity else 1000.0
            }

            // Calculate R Multiple
            val rMultiple = if (effectiveRisk > 0) {
                netPnl / effectiveRisk
            } else 0.0

            // Determine Result
            val result = when {
                netPnl > 0.001 -> "WIN"
                netPnl < -0.001 -> "LOSS"
                else -> "BREAKEVEN"
            }

            val trade = Trade(
                id = id,
                date = date.trim(),
                stockName = stockName.trim().uppercase(),
                setup = setup.trim(),
                direction = direction.trim().uppercase(),
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                quantity = quantity,
                stopLoss = stopLoss,
                target = target,
                grossPnl = grossPnl,
                charges = charges,
                netPnl = netPnl,
                riskAmount = effectiveRisk,
                rMultiple = rMultiple,
                result = result,
                entryTime = entryTime.trim(),
                exitTime = exitTime.trim(),
                notes = notes.trim(),
                mistakes = mistakes.trim(),
                screenshotUri = screenshotUri
            )

            if (id == 0L) {
                repository.insertTrade(trade)
            } else {
                repository.updateTrade(trade)
            }

            _tradeToEdit.value = null
            _currentTab.value = ScreenTab.TRADES
        }
    }

    fun deleteTrade(trade: Trade) {
        viewModelScope.launch {
            repository.deleteTrade(trade)
            if (_selectedTradeDetail.value?.id == trade.id) {
                _selectedTradeDetail.value = null
            }
        }
    }

    fun deleteTradeById(id: Long) {
        viewModelScope.launch {
            repository.deleteTradeById(id)
            if (_selectedTradeDetail.value?.id == id) {
                _selectedTradeDetail.value = null
            }
        }
    }

    fun addRule(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertRule(
                TradingRule(
                    title = title.trim(),
                    description = description.trim(),
                    isEnabled = true
                )
            )
        }
    }

    fun toggleRule(rule: TradingRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(rule: TradingRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun addSetup(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertSetup(
                TradingSetup(
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    fun deleteSetup(setup: TradingSetup) {
        viewModelScope.launch {
            repository.deleteSetup(setup)
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        repository.updateSettings(newSettings)
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetSampleData()
        }
    }

    fun getCsvExportData(): String {
        return repository.exportToCsv(uiState.value.trades)
    }
}
