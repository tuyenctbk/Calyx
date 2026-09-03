package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePhase
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxRose
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.ui.theme.CalyxTertiary
import com.example.util.HapticUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyCheckInWidget(
    todayLog: CycleLogEntity?,
    currentPhase: CyclePhase,
    onSaveCheckIn: (CycleLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var selectedMood by remember(todayLog) {
        mutableStateOf(todayLog?.mood ?: "Calm & Grounded")
    }
    var energyLevel by remember(todayLog) {
        mutableFloatStateOf((todayLog?.energyLevel ?: 8).toFloat())
    }
    var painLevel by remember(todayLog) {
        mutableFloatStateOf((todayLog?.painLevel ?: 1).toFloat())
    }

    val primarySymptoms = remember {
        listOf("Cramps", "Peak Focus", "Radiant Skin", "Headache", "Bloating", "Fatigue")
    }
    val moreSymptoms = remember {
        listOf("Tender Breasts", "Brain Fog", "Backache", "Insomnia", "Craving Salt", "Craving Sweet")
    }
    var showMoreSymptoms by remember { mutableStateOf(false) }

    val selectedSymptoms = remember(todayLog) {
        val initialList = todayLog?.symptoms?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        mutableStateListOf<String>().apply { addAll(initialList) }
    }

    var isSavedRecently by remember { mutableStateOf(false) }

    val moodOptions = listOf(
        "Radiant" to "✨ Radiant",
        "Calm & Grounded" to "🌿 Calm",
        "Sensitive" to "🌸 Sensitive",
        "Fatigued" to "😴 Fatigued",
        "Anxious" to "⚡ Anxious"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_check_in_widget")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CalyxSecondary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = null,
                            tint = CalyxSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.quick_checkin_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (todayLog != null) CalyxSecondary.copy(alpha = 0.15f) else CalyxTertiary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (todayLog != null) Icons.Default.CheckCircle else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (todayLog != null) CalyxSecondary else CalyxTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (todayLog != null) "Logged" else "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = if (todayLog != null) CalyxSecondary else CalyxTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Mood Selection Chips (Visual 1-tap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                moodOptions.forEach { (key, label) ->
                    val isSelected = selectedMood == key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CalyxSecondary.copy(alpha = 0.25f) else CalyxSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CalyxSecondary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedMood = key
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CalyxSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Dual Visual Meters: Energy & Pain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Energy Slider Sub-card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CalyxSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = CalyxPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ENERGY", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CalyxPrimary)
                            }
                            Text("${energyLevel.toInt()}/10", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CalyxPrimary)
                        }
                        Slider(
                            value = energyLevel,
                            onValueChange = { energyLevel = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = CalyxPrimary, activeTrackColor = CalyxPrimary),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Pain Slider Sub-card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CalyxSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Healing, contentDescription = null, tint = CalyxRose, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PAIN", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CalyxRose)
                            }
                            Text(
                                text = when (painLevel.toInt()) {
                                    0 -> "0 None"
                                    in 1..3 -> "${painLevel.toInt()} Mild"
                                    in 4..6 -> "${painLevel.toInt()} Mod"
                                    else -> "${painLevel.toInt()} Sev"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (painLevel.toInt() > 3) CalyxRose else CalyxPrimary
                            )
                        }
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(thumbColor = CalyxRose, activeTrackColor = CalyxRose),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Compact Symptoms FlowRow & Expandable Secondary Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val symptomsToShow = if (showMoreSymptoms) primarySymptoms + moreSymptoms else primarySymptoms

                symptomsToShow.forEach { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CalyxPrimary.copy(alpha = 0.22f) else CalyxSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CalyxPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                if (isSelected) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                            }
                    ) {
                        Text(
                            text = symptom,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                // Collapsible Toggle Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CalyxSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clickable {
                        HapticUtil.performHeartbeat(context)
                        showMoreSymptoms = !showMoreSymptoms
                    }
                ) {
                    Text(
                        text = if (showMoreSymptoms) "Collapse" else "+ More (${moreSymptoms.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = CalyxSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Save Action
            Button(
                onClick = {
                    HapticUtil.performHeartbeat(context)
                    val existing = todayLog ?: CycleLogEntity(date = todayDate)
                    val updated = existing.copy(
                        date = todayDate,
                        timestamp = System.currentTimeMillis(),
                        phase = currentPhase.name,
                        mood = selectedMood,
                        energyLevel = energyLevel.toInt(),
                        painLevel = painLevel.toInt(),
                        symptoms = selectedSymptoms.joinToString(", ")
                    )
                    onSaveCheckIn(updated)
                    isSavedRecently = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedRecently) CalyxSecondary else CalyxPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("save_daily_checkin_button")
            ) {
                Icon(
                    imageVector = if (isSavedRecently) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSavedRecently) stringResource(R.string.checkin_saved) else stringResource(R.string.save_checkin),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
