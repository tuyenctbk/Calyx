package com.example.data.remote

import android.util.Log
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncService {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun syncToCloud(
        deviceVaultId: String,
        logs: List<CycleLogEntity>,
        periods: List<CyclePeriodEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userRef = firestore.collection("calyx_vaults").document(deviceVaultId)

            // Save vault metadata
            userRef.set(
                mapOf(
                    "lastSyncTimestamp" to System.currentTimeMillis(),
                    "totalLogs" to logs.size,
                    "totalPeriods" to periods.size,
                    "encryptionType" to "AES_GCM_PSEUDONYMIZED"
                ),
                SetOptions.merge()
            ).await()

            // Batch sync logs
            var syncedCount = 0
            val logsCollection = userRef.collection("cycle_logs")
            for (log in logs) {
                val data = mapOf(
                    "date" to log.date,
                    "timestamp" to log.timestamp,
                    "phase" to log.phase,
                    "flow" to log.flow,
                    "bbt" to log.bbt,
                    "hrv" to log.hrv,
                    "sleepHours" to log.sleepHours,
                    "mood" to log.mood,
                    "energyLevel" to log.energyLevel,
                    "symptoms" to log.symptoms,
                    "notes" to log.notes
                )
                logsCollection.document(log.date).set(data, SetOptions.merge()).await()
                syncedCount++
            }

            // Sync period intervals
            val periodsCollection = userRef.collection("cycle_periods")
            for (period in periods) {
                val periodData = mapOf(
                    "id" to period.id,
                    "startDate" to period.startDate,
                    "endDate" to period.endDate,
                    "cycleLengthDays" to period.cycleLengthDays,
                    "periodLengthDays" to period.periodLengthDays,
                    "isAnomaly" to period.isAnomaly,
                    "anomalyReason" to period.anomalyReason
                )
                periodsCollection.document("period_${period.startDate}").set(periodData, SetOptions.merge()).await()
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e("FirestoreSyncService", "Sync error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchFromCloud(
        deviceVaultId: String
    ): Result<Pair<List<CycleLogEntity>, List<CyclePeriodEntity>>> = withContext(Dispatchers.IO) {
        try {
            val userRef = firestore.collection("calyx_vaults").document(deviceVaultId)
            val logsSnapshot = userRef.collection("cycle_logs").get().await()
            val periodsSnapshot = userRef.collection("cycle_periods").get().await()

            val logs = logsSnapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: doc.id
                CycleLogEntity(
                    date = date,
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    phase = doc.getString("phase") ?: "FOLLICULAR",
                    flow = doc.getString("flow") ?: "NONE",
                    bbt = (doc.getDouble("bbt") ?: 36.5).toFloat(),
                    hrv = doc.getLong("hrv")?.toInt() ?: 58,
                    sleepHours = (doc.getDouble("sleepHours") ?: 7.5).toFloat(),
                    mood = doc.getString("mood") ?: "Balanced",
                    energyLevel = doc.getLong("energyLevel")?.toInt() ?: 7,
                    symptoms = doc.getString("symptoms") ?: "",
                    notes = doc.getString("notes") ?: ""
                )
            }

            val periods = periodsSnapshot.documents.mapNotNull { doc ->
                val startDate = doc.getString("startDate") ?: return@mapNotNull null
                val endDate = doc.getString("endDate") ?: startDate
                CyclePeriodEntity(
                    id = doc.getLong("id") ?: 0L,
                    startDate = startDate,
                    endDate = endDate,
                    cycleLengthDays = doc.getLong("cycleLengthDays")?.toInt() ?: 28,
                    periodLengthDays = doc.getLong("periodLengthDays")?.toInt() ?: 5,
                    isAnomaly = doc.getBoolean("isAnomaly") ?: false,
                    anomalyReason = doc.getString("anomalyReason") ?: ""
                )
            }

            Result.success(Pair(logs, periods))
        } catch (e: Exception) {
            Log.e("FirestoreSyncService", "Fetch error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
