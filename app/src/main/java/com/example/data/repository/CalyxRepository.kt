package com.example.data.repository

import com.example.data.local.dao.CalyxDao
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.CyclePhase
import com.example.data.local.entities.FlowIntensity
import com.example.data.local.entities.SecurityEntity
import com.example.util.SeedPhraseGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.local.dao.DailySymptomDao
import com.example.data.local.entities.DailySymptomLogEntity

class CalyxRepository(
    private val dao: CalyxDao,
    private val symptomDao: DailySymptomDao? = null
) {

    val allLogs: Flow<List<CycleLogEntity>> = dao.getAllLogs()
    val allPeriods: Flow<List<CyclePeriodEntity>> = dao.getAllPeriods()
    val securitySettings: Flow<SecurityEntity?> = dao.getSecuritySettings()
    val allSymptomLogs: Flow<List<DailySymptomLogEntity>> = symptomDao?.getAllSymptomLogs() ?: dao.getAllLogs().let { flow ->
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun getLogForDate(date: String): CycleLogEntity? = dao.getLogForDate(date)

    fun getLogsInRange(startDate: String, endDate: String): Flow<List<CycleLogEntity>> =
        dao.getLogsInRange(startDate, endDate)

    fun getLogsWithSymptom(symptom: String): Flow<List<CycleLogEntity>> =
        dao.getLogsWithSymptom(symptom)

    suspend fun insertLog(log: CycleLogEntity) {
        dao.insertLog(log)
        // Also mirror to DailySymptomLogEntity if symptomDao is available
        symptomDao?.let { sDao ->
            val symptomEntity = DailySymptomLogEntity(
                date = log.date,
                timestamp = log.timestamp,
                phase = log.phase,
                mood = log.mood,
                moodScore = when {
                    log.mood.contains("Radiant", ignoreCase = true) -> 5
                    log.mood.contains("Calm", ignoreCase = true) -> 4
                    log.mood.contains("Sensitive", ignoreCase = true) -> 3
                    log.mood.contains("Fatigued", ignoreCase = true) -> 2
                    log.mood.contains("Anxious", ignoreCase = true) -> 1
                    else -> 3
                },
                energyLevel = log.energyLevel,
                physicalSymptoms = log.symptoms,
                painLevel = log.painLevel,
                flowIntensity = log.flow,
                bbt = log.bbt,
                hrv = log.hrv,
                sleepHours = log.sleepHours,
                notes = log.notes,
                isEncrypted = true
            )
            sDao.insertSymptomLog(symptomEntity)
        }
    }

    suspend fun insertSymptomLog(symptomLog: DailySymptomLogEntity) {
        symptomDao?.insertSymptomLog(symptomLog)
    }

    fun getSymptomLogsInRange(startDate: String, endDate: String): Flow<List<DailySymptomLogEntity>> =
        symptomDao?.getSymptomLogsInRange(startDate, endDate) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertPeriod(period: CyclePeriodEntity) = dao.insertPeriod(period)

    suspend fun deletePeriod(id: Long) = dao.deletePeriodById(id)

    suspend fun getRecentPeriods(): List<CyclePeriodEntity> = dao.getRecentPeriodsDirect()

    suspend fun deleteLog(date: String) {
        dao.deleteLogForDate(date)
        symptomDao?.deleteSymptomLogForDate(date)
    }

    suspend fun saveSecuritySettings(settings: SecurityEntity) = dao.saveSecuritySettings(settings)

    suspend fun clearAllData() {
        dao.clearLogs()
        dao.clearPeriods()
        symptomDao?.clearAllSymptomLogs()
    }

    suspend fun seedInitialDataIfNeeded() {
        val currentSecurity = dao.getSecuritySettingsDirect()
        if (currentSecurity == null) {
            dao.saveSecuritySettings(
                SecurityEntity(
                    id = 1,
                    pinHash = SeedPhraseGenerator.hashPin("1234"),
                    decoyPinHash = SeedPhraseGenerator.hashPin("0000"),
                    recoverySeedPhrase = SeedPhraseGenerator.generate24Words(),
                    isSetupCompleted = true
                )
            )
        }
    }
}

private data class Tuple8<A, B, C, D, E, F, G, H>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H
)
