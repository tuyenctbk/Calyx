package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.SecurityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalyxDao {
    @Query("SELECT * FROM cycle_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<CycleLogEntity>>

    @Query("SELECT * FROM cycle_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): CycleLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CycleLogEntity)

    @Query("DELETE FROM cycle_logs WHERE date = :date")
    suspend fun deleteLogForDate(date: String)

    @Query("SELECT * FROM cycle_periods ORDER BY startDate DESC")
    fun getAllPeriods(): Flow<List<CyclePeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: CyclePeriodEntity)

    @Query("DELETE FROM cycle_periods WHERE id = :id")
    suspend fun deletePeriodById(id: Long)

    @Query("SELECT * FROM cycle_periods ORDER BY startDate DESC LIMIT 12")
    suspend fun getRecentPeriodsDirect(): List<CyclePeriodEntity>

    @Query("SELECT * FROM security_settings WHERE id = 1 LIMIT 1")
    fun getSecuritySettings(): Flow<SecurityEntity?>

    @Query("SELECT * FROM security_settings WHERE id = 1 LIMIT 1")
    suspend fun getSecuritySettingsDirect(): SecurityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSecuritySettings(settings: SecurityEntity)

    @Query("DELETE FROM cycle_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM cycle_periods")
    suspend fun clearPeriods()
}
