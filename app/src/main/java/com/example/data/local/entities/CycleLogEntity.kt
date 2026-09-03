package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R

enum class CyclePhase(val displayName: String, val description: String, val nameResId: Int, val descResId: Int) {
    MENSTRUAL("Menstrual Phase", "Rest & Nurture • Fat-burning metabolic shift", R.string.phase_menstrual, R.string.phase_menstrual_desc),
    FOLLICULAR("Follicular Phase", "Rise & Bloom • High energy, creativity & stamina", R.string.phase_follicular, R.string.phase_follicular_desc),
    OVULATORY("Ovulatory Peak", "Peak Vibe • High verbal fluency & social connection", R.string.phase_ovulatory, R.string.phase_ovulatory_desc),
    LUTEAL("Luteal Phase", "Ground & Focus • Endurance, precision & steady workload", R.string.phase_luteal, R.string.phase_luteal_desc)
}

enum class FlowIntensity(val label: String, val labelResId: Int) {
    NONE("None", R.string.flow_none),
    SPOTTING("Spotting", R.string.flow_spotting),
    LIGHT("Light", R.string.flow_light),
    MEDIUM("Medium", R.string.flow_medium),
    HEAVY("Heavy", R.string.flow_heavy)
}

@Entity(tableName = "cycle_logs")
data class CycleLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val phase: String = CyclePhase.FOLLICULAR.name,
    val flow: String = FlowIntensity.NONE.name,
    val bbt: Float = 36.5f, // Basal Body Temp in Celsius
    val hrv: Int = 58, // Heart Rate Variability ms
    val sleepHours: Float = 7.5f,
    val mood: String = "Balanced",
    val energyLevel: Int = 7, // 1 to 10
    val painLevel: Int = 0, // 0 (None) to 10 (Severe)
    val symptoms: String = "", // Comma-separated symptom tags
    val notes: String = ""
)
