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

class CalyxRepository(private val dao: CalyxDao) {

    val allLogs: Flow<List<CycleLogEntity>> = dao.getAllLogs()
    val allPeriods: Flow<List<CyclePeriodEntity>> = dao.getAllPeriods()
    val securitySettings: Flow<SecurityEntity?> = dao.getSecuritySettings()

    suspend fun getLogForDate(date: String): CycleLogEntity? = dao.getLogForDate(date)

    suspend fun insertLog(log: CycleLogEntity) = dao.insertLog(log)

    suspend fun insertPeriod(period: CyclePeriodEntity) = dao.insertPeriod(period)

    suspend fun deletePeriod(id: Long) = dao.deletePeriodById(id)

    suspend fun getRecentPeriods(): List<CyclePeriodEntity> = dao.getRecentPeriodsDirect()

    suspend fun deleteLog(date: String) = dao.deleteLogForDate(date)

    suspend fun saveSecuritySettings(settings: SecurityEntity) = dao.saveSecuritySettings(settings)

    suspend fun clearAllData() {
        dao.clearLogs()
        dao.clearPeriods()
    }

    suspend fun seedInitialDataIfNeeded() {
        val existingLogs = allLogs.firstOrNull()
        if (existingLogs.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()

            // Initialize default security settings with seed phrase
            val currentSecurity = dao.getSecuritySettingsDirect()
            if (currentSecurity == null) {
                dao.saveSecuritySettings(
                    SecurityEntity(
                        id = 1,
                        pinHash = SeedPhraseGenerator.hashPin("1234"), // default pin
                        decoyPinHash = SeedPhraseGenerator.hashPin("0000"), // default decoy pin
                        recoverySeedPhrase = SeedPhraseGenerator.generate24Words(),
                        isSetupCompleted = true
                    )
                )
            }

            // Seed 3 historical period cycles
            val period1Start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -56) }
            val period1End = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -51) }
            dao.insertPeriod(
                CyclePeriodEntity(
                    startDate = sdf.format(period1Start.time),
                    endDate = sdf.format(period1End.time),
                    cycleLengthDays = 28,
                    periodLengthDays = 5
                )
            )

            val period2Start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }
            val period2End = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -23) }
            dao.insertPeriod(
                CyclePeriodEntity(
                    startDate = sdf.format(period2Start.time),
                    endDate = sdf.format(period2End.time),
                    cycleLengthDays = 28,
                    periodLengthDays = 5
                )
            )

            // Seed past 28 days of logs
            for (i in 27 downTo 0) {
                val logCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                val dateStr = sdf.format(logCal.time)
                val dayOfCycle = 28 - i // Day 1 to 28

                val (phase, flow, bbt, hrv, sleep, mood, energy, symptoms) = when (dayOfCycle) {
                    in 1..5 -> Tuple8(
                        CyclePhase.MENSTRUAL.name,
                        if (dayOfCycle in 1..2) FlowIntensity.HEAVY.name else FlowIntensity.LIGHT.name,
                        36.2f, 52, 7.8f, "Restful", 4, "Mild Cramps, Fatigue"
                    )
                    in 6..12 -> Tuple8(
                        CyclePhase.FOLLICULAR.name,
                        FlowIntensity.NONE.name,
                        36.4f, 65, 8.0f, "Energetic", 8, "High Clarity"
                    )
                    in 13..16 -> Tuple8(
                        CyclePhase.OVULATORY.name,
                        FlowIntensity.NONE.name,
                        36.8f, 72, 8.2f, "Vibrant", 9, "Skin Glow, High Social Energy"
                    )
                    else -> Tuple8(
                        CyclePhase.LUTEAL.name,
                        FlowIntensity.NONE.name,
                        36.7f, 58, 7.5f, "Focused", 6, "Mild Bloating"
                    )
                }

                dao.insertLog(
                    CycleLogEntity(
                        date = dateStr,
                        timestamp = logCal.timeInMillis,
                        phase = phase,
                        flow = flow,
                        bbt = bbt,
                        hrv = hrv,
                        sleepHours = sleep,
                        mood = mood,
                        energyLevel = energy,
                        symptoms = symptoms,
                        notes = "Logged via Calyx Haven"
                    )
                )
            }
        }
    }
}

private data class Tuple8<A, B, C, D, E, F, G, H>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H
)
