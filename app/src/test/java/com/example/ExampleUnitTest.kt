package com.example

import com.example.data.local.entities.CyclePhase
import com.example.data.remote.HormonalWellnessService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun cyclePhaseMapping_isCorrect() {
        fun phaseForDay(day: Int): CyclePhase = when (day) {
            in 1..5 -> CyclePhase.MENSTRUAL
            in 6..12 -> CyclePhase.FOLLICULAR
            in 13..16 -> CyclePhase.OVULATORY
            else -> CyclePhase.LUTEAL
        }

        assertEquals(CyclePhase.MENSTRUAL, phaseForDay(1))
        assertEquals(CyclePhase.MENSTRUAL, phaseForDay(5))
        assertEquals(CyclePhase.FOLLICULAR, phaseForDay(6))
        assertEquals(CyclePhase.FOLLICULAR, phaseForDay(12))
        assertEquals(CyclePhase.OVULATORY, phaseForDay(13))
        assertEquals(CyclePhase.OVULATORY, phaseForDay(16))
        assertEquals(CyclePhase.LUTEAL, phaseForDay(17))
        assertEquals(CyclePhase.LUTEAL, phaseForDay(28))
    }

    @Test
    fun wellnessService_offlineInsightsGenerateValidGuidanceForAllPhases() {
        val service = HormonalWellnessService()
        CyclePhase.entries.forEach { phase ->
            val insight = service.getOfflineInsight(phase, emptyList())
            assertNotNull(insight)
            assertTrue(insight.hormonalStatus.isNotEmpty())
            assertTrue(insight.nutritionGuidance.isNotEmpty())
            assertTrue(insight.fitnessRecommendation.isNotEmpty())
            assertTrue(insight.sleepOptimization.isNotEmpty())
        }
    }
}
