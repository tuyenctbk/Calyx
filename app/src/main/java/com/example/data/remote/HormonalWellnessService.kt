package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.CyclePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HormonalWellnessInsight(
    val phaseOverview: String,
    val hormonalStatus: String,
    val nutritionGuidance: String,
    val fitnessRecommendation: String,
    val sleepOptimization: String,
    val keyBiomarkerAnalysis: String
)

class HormonalWellnessService {

    suspend fun generatePersonalizedInsights(
        currentPhase: CyclePhase,
        recentLogs: List<CycleLogEntity>,
        recentPeriods: List<CyclePeriodEntity>
    ): HormonalWellnessInsight = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineInsight(currentPhase, recentLogs)
        }

        val logSummary = recentLogs.take(14).joinToString("\n") {
            "Date: ${it.date}, Phase: ${it.phase}, BBT: ${it.bbt}°C, HRV: ${it.hrv}ms, Sleep: ${it.sleepHours}h, Symptoms: ${it.symptoms}, Energy: ${it.energyLevel}/10"
        }
        val periodSummary = recentPeriods.take(4).joinToString("\n") {
            "Start: ${it.startDate}, End: ${it.endDate}, CycleLength: ${it.cycleLengthDays}d, PeriodLength: ${it.periodLengthDays}d"
        }

        val prompt = """
            You are Calyx's Clinical Hormonal Wellness AI Consultant.
            Current User Phase: ${currentPhase.displayName} (${currentPhase.description})
            
            Cycle History:
            $periodSummary
            
            Recent Daily Biomarkers & Symptoms:
            $logSummary
            
            Provide a personalized, evidence-based hormonal wellness analysis in clear structured paragraphs covering:
            1. Hormonal Status & Neurotransmitter Profile
            2. Phase-Specific Nutrition & Micronutrients
            3. Adaptive Exercise & Adrenal Load
            4. Sleep Architecture & Restorative Recovery
            5. Key Biomarker Correlation (BBT & HRV analysis)
            
            Be empowering, medically rigorous, and actionable.
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
            val response = GeminiClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null && text.isNotBlank()) {
                parseAiInsight(text, currentPhase)
            } else {
                getOfflineInsight(currentPhase, recentLogs)
            }
        } catch (e: Exception) {
            getOfflineInsight(currentPhase, recentLogs)
        }
    }

    private fun parseAiInsight(aiText: String, currentPhase: CyclePhase): HormonalWellnessInsight {
        val defaultInsight = getOfflineInsight(currentPhase, emptyList())
        val lines = aiText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var hormonalStatus = ""
        var nutritionGuidance = ""
        var fitnessRecommendation = ""
        var sleepOptimization = ""
        var keyBiomarkerAnalysis = ""

        var currentSection = 0
        for (line in lines) {
            when {
                line.contains("Hormonal Status", ignoreCase = true) || line.startsWith("1.") -> currentSection = 1
                line.contains("Nutrition", ignoreCase = true) || line.startsWith("2.") -> currentSection = 2
                line.contains("Exercise", ignoreCase = true) || line.contains("Fitness", ignoreCase = true) || line.startsWith("3.") -> currentSection = 3
                line.contains("Sleep", ignoreCase = true) || line.startsWith("4.") -> currentSection = 4
                line.contains("Biomarker", ignoreCase = true) || line.startsWith("5.") -> currentSection = 5
                else -> {
                    when (currentSection) {
                        1 -> hormonalStatus = if (hormonalStatus.isEmpty()) line else "$hormonalStatus $line"
                        2 -> nutritionGuidance = if (nutritionGuidance.isEmpty()) line else "$nutritionGuidance\n$line"
                        3 -> fitnessRecommendation = if (fitnessRecommendation.isEmpty()) line else "$fitnessRecommendation $line"
                        4 -> sleepOptimization = if (sleepOptimization.isEmpty()) line else "$sleepOptimization $line"
                        5 -> keyBiomarkerAnalysis = if (keyBiomarkerAnalysis.isEmpty()) line else "$keyBiomarkerAnalysis $line"
                    }
                }
            }
        }

        return HormonalWellnessInsight(
            phaseOverview = "Personalized for ${currentPhase.displayName}",
            hormonalStatus = hormonalStatus.ifEmpty { defaultInsight.hormonalStatus },
            nutritionGuidance = nutritionGuidance.ifEmpty { defaultInsight.nutritionGuidance },
            fitnessRecommendation = fitnessRecommendation.ifEmpty { defaultInsight.fitnessRecommendation },
            sleepOptimization = sleepOptimization.ifEmpty { defaultInsight.sleepOptimization },
            keyBiomarkerAnalysis = keyBiomarkerAnalysis.ifEmpty { defaultInsight.keyBiomarkerAnalysis }
        )
    }

    fun getOfflineInsight(phase: CyclePhase, logs: List<CycleLogEntity>): HormonalWellnessInsight {
        val avgHrv = if (logs.isNotEmpty()) logs.map { it.hrv }.average().toInt() else 60
        val avgSleep = if (logs.isNotEmpty()) String.format("%.1f", logs.map { it.sleepHours }.average()) else "7.6"

        return when (phase) {
            CyclePhase.MENSTRUAL -> HormonalWellnessInsight(
                phaseOverview = "Rest & Rejuvenation • Basal Estrogen & Progesterone",
                hormonalStatus = "Both estrogen and progesterone are at their baseline nadir. The uterine lining is shedding. Energy is directed inward for immune rejuvenation and cellular repair.",
                nutritionGuidance = "• Iron & Zinc rich foods (grass-fed beef, lentils, spinach)\n• Warm bone broths & mineral-dense stews\n• Anti-inflammatory turmeric & ginger tea\n• Seed cycling: 1 tbsp ground flax + pumpkin seeds daily",
                fitnessRecommendation = "Gentle walking, restorative yin yoga, and breathwork. Avoid high-intensity interval training (HIIT) or heavy max lifting to conserve adrenal capacity.",
                sleepOptimization = "Target 8.0–8.5 hours. Core body temperature drops during menses, supporting deeper slow-wave restorative sleep.",
                keyBiomarkerAnalysis = "Baseline BBT ~36.2°C; Resting HRV currently averaging ${avgHrv}ms with ${avgSleep}h sleep continuity."
            )
            CyclePhase.FOLLICULAR -> HormonalWellnessInsight(
                phaseOverview = "Renewal & Cognitive Surge • Rising Estrogen (Estradiol)",
                hormonalStatus = "FSH stimulates dominant follicle development while estradiol progressively climbs. Neurotransmitters dopamine and serotonin increase, enhancing cognitive clarity and optimism.",
                nutritionGuidance = "• Light, vibrant, fermented foods (kimchi, kefir, sauerkraut)\n• Sprouted greens, broccoli sprouts & citrus for healthy liver estrogen clearance\n• High-quality lean proteins to support muscle protein synthesis\n• Seed cycling: 1 tbsp ground flax + pumpkin seeds daily",
                fitnessRecommendation = "Ideal window for strength hypertrophy, progressive overload, dance, and cardiovascular conditioning. Tendon stiffness and glycogen storage are optimal.",
                sleepOptimization = "Energy is naturally elevated; 7.5–8.0 hours is sufficient. Ensure morning natural light exposure to anchor circadian rhythm.",
                keyBiomarkerAnalysis = "BBT remains steady low (~36.3°C–36.5°C) before the pre-ovulatory dip."
            )
            CyclePhase.OVULATORY -> HormonalWellnessInsight(
                phaseOverview = "Peak Vitality & Magnetic Presence • LH Surge & Estrogen Peak",
                hormonalStatus = "Luteinizing Hormone (LH) surges alongside peak estradiol, triggering oocyte release. Testosterone experiences a minor mid-cycle spike, elevating libido, verbal fluency, and confidence.",
                nutritionGuidance = "• High-fiber cruciferous vegetables (cauliflower, kale) to bind excess estrogen\n• Berries, pomegranate & glutathione-rich avocados\n• Hydration with trace mineral electrolytes\n• Seed cycling: 1 tbsp ground flax + pumpkin seeds daily",
                fitnessRecommendation = "Peak power output! Great time for personal records (PRs), sprinting, intense circuit training, and high-impact social sports.",
                sleepOptimization = "Body temperature begins rising slightly. Maintain bedroom temperature below 19°C (66°F) to prevent nocturnal awakenings.",
                keyBiomarkerAnalysis = "Sharp thermal shift (+0.3°C to +0.5°C) validates successful follicle rupture."
            )
            CyclePhase.LUTEAL -> HormonalWellnessInsight(
                phaseOverview = "Grounding & Focus • Progesterone Dominance",
                hormonalStatus = "Corpus luteum secretes progesterone, inducing a calming GABAergic effect. Metabolic rate increases by 100–300 kcal/day while insulin sensitivity temporarily decreases.",
                nutritionGuidance = "• Complex low-glycemic carbohydrates (sweet potatoes, oats, quinoa) to prevent serotonin dips\n• Magnesium glycinate (300mg) and vitamin B6 to reduce PMS fluid retention\n• Dark chocolate (>75% cacao) and roasted root vegetables\n• Seed cycling: 1 tbsp ground sesame + sunflower seeds daily",
                fitnessRecommendation = "Shift to steady-state Zone 2 cardio, Pilates, moderate resistance training, and outdoor hikes. Lower total cortisol impact.",
                sleepOptimization = "Progesterone elevates core body temperature by +0.35°C, which can reduce REM sleep quality if unmanaged. Practice 4-7-8 wind-down breathwork.",
                keyBiomarkerAnalysis = "Sustained high BBT (~36.7°C–37.0°C). HRV typically dips 8–12ms due to mild sympathetic tone."
            )
        }
    }
}
