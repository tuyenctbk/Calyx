package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entities.CyclePhase

object CycleNotificationHelper {

    const val CHANNEL_ID = "calyx_hormonal_focus_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Hormonal Focus"
            val descriptionText = "Daily circadian & cycle phase reminders for performance, nutrition, and recovery"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getDailyHormonalHint(phase: CyclePhase, dayOfCycle: Int): Pair<String, String> {
        return when (phase) {
            CyclePhase.MENSTRUAL -> {
                val title = "🌸 Menstrual Phase (Day $dayOfCycle): Restorative Rhythm"
                val body = "Estrogen & progesterone baseline. Prioritize iron-replenishing nutrition, magnesium, gentle pelvic mobility, and low-demand cognitive pacing."
                Pair(title, body)
            }
            CyclePhase.FOLLICULAR -> {
                val title = "🌱 Follicular Rise (Day $dayOfCycle): High Energy & Novelty"
                val body = "Estrogen is building! Optimal window for creative brainstorming, complex learning, and progressive overload in strength training."
                Pair(title, body)
            }
            CyclePhase.OVULATORY -> {
                val title = "☀️ Ovulatory Peak (Day $dayOfCycle): Peak Charisma & Output"
                val body = "Estrogen & testosterone peak! Maximum verbal fluency, peak HRV recovery, and personal best athletic stamina. Ideal for pitch presentations."
                Pair(title, body)
            }
            CyclePhase.LUTEAL -> {
                val title = "🌿 Luteal Focus (Day $dayOfCycle): Analytical Depth"
                val body = "Progesterone is dominant. High attention to detail for deep solo focus. Pair complex carbohydrates with healthy fats and prioritize sleep continuity."
                Pair(title, body)
            }
        }
    }

    fun sendDailyHormonalFocusNotification(
        context: Context,
        phase: CyclePhase,
        dayOfCycle: Int
    ): Boolean {
        createNotificationChannel(context)

        val (title, content) = getDailyHormonalHint(phase, dayOfCycle)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
