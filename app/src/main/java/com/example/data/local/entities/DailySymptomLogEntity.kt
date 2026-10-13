package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for securely storing daily cycle symptom logs,
 * including mood ratings, energy levels, physical symptoms, pain severity,
 * and biometric markers.
 */
@Entity(
    tableName = "daily_symptom_logs",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailySymptomLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val cycleDay: Int = 1,
    val phase: String = CyclePhase.FOLLICULAR.name,
    val mood: String = "Calm & Grounded",
    val moodScore: Int = 3, // 1 (Low) to 5 (Radiant)
    val energyLevel: Int = 7, // 1 to 10
    val physicalSymptoms: String = "", // Comma-separated list of symptoms
    val painLevel: Int = 0, // 0 (None) to 10 (Severe)
    val flowIntensity: String = FlowIntensity.NONE.name,
    val bbt: Float = 0f, // Basal Body Temperature (°C)
    val hrv: Int = 0, // Heart Rate Variability (ms)
    val sleepHours: Float = 0f, // Sleep duration
    val cervicalMucus: String = "", // Mucus consistency
    val digestiveSymptoms: String = "", // Digestive tags
    val stressLevel: Int = 2, // 1 to 5
    val notes: String = "",
    val isEncrypted: Boolean = true
)
