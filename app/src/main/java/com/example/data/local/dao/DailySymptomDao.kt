package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DailySymptomLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for querying, inserting, and managing daily cycle symptom logs.
 * All read operations return reactive Flow instances for real-time Compose UI updates.
 */
@Dao
interface DailySymptomDao {

    @Query("SELECT * FROM daily_symptom_logs ORDER BY date DESC")
    fun getAllSymptomLogs(): Flow<List<DailySymptomLogEntity>>

    @Query("SELECT * FROM daily_symptom_logs WHERE date = :date LIMIT 1")
    fun getSymptomLogForDate(date: String): Flow<DailySymptomLogEntity?>

    @Query("SELECT * FROM daily_symptom_logs WHERE date = :date LIMIT 1")
    suspend fun getSymptomLogForDateDirect(date: String): DailySymptomLogEntity?

    @Query("SELECT * FROM daily_symptom_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): DailySymptomLogEntity? = getSymptomLogForDateDirect(date)

    @Query("SELECT * FROM daily_symptom_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSymptomLogsInRange(startDate: String, endDate: String): Flow<List<DailySymptomLogEntity>>

    @Query("SELECT * FROM daily_symptom_logs WHERE physicalSymptoms LIKE '%' || :symptom || '%' ORDER BY date DESC")
    fun getSymptomLogsWithSymptom(symptom: String): Flow<List<DailySymptomLogEntity>>

    @Query("SELECT * FROM daily_symptom_logs WHERE mood = :mood ORDER BY date DESC")
    fun getSymptomLogsByMood(mood: String): Flow<List<DailySymptomLogEntity>>

    @Query("SELECT * FROM daily_symptom_logs ORDER BY date DESC LIMIT :limit")
    fun getRecentSymptomLogs(limit: Int = 30): Flow<List<DailySymptomLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomLog(log: DailySymptomLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailySymptomLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomLogs(logs: List<DailySymptomLogEntity>)

    @Update
    suspend fun updateSymptomLog(log: DailySymptomLogEntity)

    @Query("DELETE FROM daily_symptom_logs WHERE date = :date")
    suspend fun deleteSymptomLogForDate(date: String)

    @Delete
    suspend fun deleteSymptomLog(log: DailySymptomLogEntity)

    @Query("SELECT AVG(energyLevel) FROM daily_symptom_logs WHERE energyLevel > 0")
    fun getAverageEnergyLevel(): Flow<Float?>

    @Query("SELECT AVG(painLevel) FROM daily_symptom_logs WHERE painLevel >= 0")
    fun getAveragePainLevel(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM daily_symptom_logs")
    fun getSymptomLogCount(): Flow<Int>

    @Query("DELETE FROM daily_symptom_logs")
    suspend fun clearAllSymptomLogs()
}
