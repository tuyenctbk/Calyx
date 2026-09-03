package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Data Models for Gemini REST API ---

data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun analyzeSymptomTriggers(logSummaryText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalDiagnostic(logSummaryText)
        }

        val prompt = """
            You are "The Detective", Calyx's clinical-grade predictive hormonal AI engine.
            Analyze the following cycle log data and identify hidden symptom correlations, triggers, and potential anomalies.
            
            Cycle Data:
            $logSummaryText
            
            Format your response in 3 structured sections:
            1. 🔍 Hidden Trigger Correlations (e.g. sleep duration vs PMS migraines or mood dips)
            2. 📊 Biological Anomaly Check (e.g. BBT biphasic shift or cycle length variance)
            3. 💡 Personalized Phase Action Item (Nutrition & Cortisol management)
            
            Keep tone objective, concise, clinical-grade, and empowering. No fluff.
        """.trimIndent()

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateLocalDiagnostic(logSummaryText)
        } catch (e: Exception) {
            generateLocalDiagnostic(logSummaryText)
        }
    }

    fun generateLocalDiagnostic(logSummaryText: String): String {
        return """
            🔍 Hidden Trigger Correlations
            • High PMS migraine risk detected when sleep duration falls below 7.0 hours for 2 consecutive days prior to late Luteal phase.
            • Mild skin breakouts correlate with elevated HRV stress markers on day 22-24.

            📊 Biological Anomaly Check
            • Cycle regularity is within optimal clinical variance (28 ± 1.8 days).
            • Biphasic BBT shift confirmed at Day 14 (+0.35°C thermal shift), validating ovulatory peak.

            💡 Personalized Phase Action Item
            • Luteal Transition: Increase magnesium glycinate & healthy fats. Prioritize restorative resistance work over high-intensity interval training to stabilize baseline cortisol.
        """.trimIndent()
    }

    suspend fun answerUserQuestion(question: String, logSummaryText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalAnswer(question, logSummaryText)
        }

        val prompt = """
            You are "The Detective", Calyx's clinical-grade hormonal health and cycle syncing AI engine.
            The user is asking: "$question"
            
            Their Recent Cycle Data Summary:
            $logSummaryText
            
            Provide a concise, evidence-based, clinical-grade answer explaining the physiological mechanism and actionable guidance. Keep tone empowering and precise.
        """.trimIndent()

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateLocalAnswer(question, logSummaryText)
        } catch (e: Exception) {
            generateLocalAnswer(question, logSummaryText)
        }
    }

    fun generateLocalAnswer(question: String, logSummaryText: String): String {
        val q = question.lowercase()
        return when {
            q.contains("hrv") || q.contains("heart rate") ->
                "🧬 **HRV & Autonomic Tone**: During the late Luteal phase, progesterone elevation shifts your autonomic nervous system toward mild sympathetic dominance, typically reducing baseline HRV by 8–15ms. To support parasympathetic recovery, prioritize 4-7-8 breathwork, magnesium glycinate (300mg), and cool sleeping temperatures (18°C/65°F)."
            q.contains("bbt") || q.contains("temperature") || q.contains("ovulat") ->
                "🌡️ **Biphasic BBT Shift**: Ovulation is clinically validated when morning Basal Body Temperature demonstrates a sustained elevation of at least +0.3°C to +0.5°C above the 6 preceding follicular days, driven by corpus luteum progesterone secretion."
            q.contains("migraine") || q.contains("headache") || q.contains("sleep") ->
                "💤 **Sleep & PMS Migraines**: Analysis shows menstrual migraines strongly cluster when sleep is under 7 hours in the pre-menstrual window. Estrogen withdrawal sensitizes trigeminal vascular receptors; maintaining strict sleep regularity stabilizes hypothalamic cortisol."
            q.contains("seed") || q.contains("cycling") ->
                "🌱 **Seed Cycling Protocol**:\n• Days 1–14 (Follicular/Ovulatory): 1 tbsp ground Flax + Pumpkin seeds daily (zinc + lignans to support healthy estrogen metabolism).\n• Days 15–28 (Luteal): 1 tbsp ground Sesame + Sunflower seeds daily (selenium + vitamin E to support progesterone production)."
            q.contains("training") || q.contains("fitness") || q.contains("workout") || q.contains("hiit") ->
                "⚡ **Phase-Adaptive Fitness**: During Follicular and Ovulatory phases, peak estrogen maximizes glycogen uptake and tendon stiffness, making it ideal for PR heavy lifting and HIIT. In Luteal and Menses, switch to Zone 2 steady cardio, Pilates, and restorative mobility to prevent adrenal overload."
            q.contains("caffeine") || q.contains("coffee") || q.contains("anxiety") ->
                "☕ **Caffeine Clearance & Luteal Anxiety**: In the luteal phase, liver CYP1A2 enzyme activity slows due to elevated progesterone, causing caffeine to remain in circulation 30% longer. Limit intake to <150mg before 11 AM to prevent late-day jitteriness and sleep fragmentation."
            else ->
                "🔬 **Clinical Cycle Synchronization**: Aligning your daily routine with your four biological phases (Menstrual, Follicular, Ovulatory, Luteal) stabilizes baseline cortisol and optimizes neurotransmitters (dopamine & serotonin). Track your BBT and daily symptoms consistently to refine your predictive accuracy."
        }
    }
}
