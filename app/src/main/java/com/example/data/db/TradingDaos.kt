package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Trade
import com.example.data.model.TradingRule
import com.example.data.model.TradingSetup
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY date DESC, timestamp DESC")
    fun getAllTrades(): Flow<List<Trade>>

    @Query("SELECT * FROM trades ORDER BY date ASC, timestamp ASC")
    fun getAllTradesChronological(): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: Long): Trade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: Trade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<Trade>)

    @Update
    suspend fun updateTrade(trade: Trade)

    @Delete
    suspend fun deleteTrade(trade: Trade)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trades")
    suspend fun deleteAllTrades()
}

@Dao
interface TradingRuleDao {
    @Query("SELECT * FROM trading_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<TradingRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TradingRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<TradingRule>)

    @Update
    suspend fun updateRule(rule: TradingRule)

    @Delete
    suspend fun deleteRule(rule: TradingRule)

    @Query("DELETE FROM trading_rules")
    suspend fun deleteAllRules()
}

@Dao
interface TradingSetupDao {
    @Query("SELECT * FROM trading_setups ORDER BY name ASC")
    fun getAllSetups(): Flow<List<TradingSetup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: TradingSetup): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetups(setups: List<TradingSetup>)

    @Delete
    suspend fun deleteSetup(setup: TradingSetup)
}
