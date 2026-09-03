package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_periods")
data class CyclePeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String, // YYYY-MM-DD
    val endDate: String,   // YYYY-MM-DD
    val cycleLengthDays: Int = 28,
    val periodLengthDays: Int = 5,
    val isAnomaly: Boolean = false,
    val anomalyReason: String = ""
)
