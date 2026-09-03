package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickSymptomInputSheet(
    todayLog: CycleLogEntity?,
    currentPhase: CyclePhase,
    onDismiss: () -> Unit,
    onSaveLog: (CycleLogEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val moodOptions = listOf(
        Pair("🔥 Vibrant", "Energetic & confident"),
        Pair("✨ Balanced", "Calm and focused"),
        Pair("⚡ High Drive", "Peak cognitive power"),
        Pair("🌧️ Sensitive", "Tender and reflective"),
        Pair("😴 Low Battery", "Rest & recovery needed")
    )
    var selectedMood by remember {
        mutableStateOf(todayLog?.mood ?: "✨ Balanced")
    }

    val energyPresets = listOf(
        Pair("Low (1-3)", 2),
        Pair("Mod (4-6)", 5),
        Pair("High (7-8)", 8),
        Pair("Peak (9-10)", 10)
    )
    var selectedEnergy by remember {
        mutableIntStateOf(todayLog?.energyLevel ?: 7)
    }

    val flowOptions = listOf("NONE", "SPOTTING", "LIGHT", "MEDIUM", "HEAVY")
    var selectedFlow by remember {
        mutableStateOf(todayLog?.flow ?: "NONE")
    }

    val quickSymptoms = listOf(
        "Cramps", "Peak Focus", "Radiant Skin", "Headache", "Bloating",
        "Tender Breasts", "Fatigue", "Brain Fog", "Backache", "Sweet Craving",
        "Calm & Centered", "High Stamina"
    )
    val selectedSymptoms = remember {
        val initial = todayLog?.symptoms?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        mutableStateListOf<String>().apply { addAll(initial) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CalyxSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CalyxPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = null,
                            tint = CalyxPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "QUICK BIOMARKER LOG",
                            style = MaterialTheme.typography.labelSmall,
                            color = CalyxPrimary,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1-Tap Symptom & Mood Capture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Mood Quick Select
            Text(
                text = "1. TODAY'S MOOD & VIBE",
                style = MaterialTheme.typography.labelSmall,
                color = CalyxSecondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                moodOptions.forEach { mood ->
                    val isSelected = selectedMood == mood.first
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CalyxSecondary.copy(alpha = 0.25f) else CalyxSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CalyxSecondary else Color.Transparent
                        ),
                        modifier = Modifier
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedMood = mood.first
                            }
                            .testTag("mood_chip_${mood.first.filter { it.isLetter() }}")
                    ) {
                        Text(
                            text = mood.first,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CalyxSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Energy Level Quick Select
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = CalyxPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2. ENERGY LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalyxPrimary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$selectedEnergy / 10",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CalyxPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                energyPresets.forEach { preset ->
                    val isSelected = selectedEnergy == preset.second
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CalyxPrimary.copy(alpha = 0.25f) else CalyxSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CalyxPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedEnergy = preset.second
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.first,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Flow Intensity (Quick Select)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = CalyxRose, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "3. FLOW INTENSITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CalyxRose,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                flowOptions.forEach { flow ->
                    val isSelected = selectedFlow.equals(flow, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CalyxRose.copy(alpha = 0.22f) else CalyxSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CalyxRose else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedFlow = flow
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = flow.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CalyxRose else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Quick Symptoms & Body Sensations
            Text(
                text = "4. PHYSICAL & COGNITIVE SIGNALS",
                style = MaterialTheme.typography.labelSmall,
                color = CalyxTertiary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickSymptoms.forEach { symptom ->
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
                                if (isSelected) {
                                    selectedSymptoms.remove(symptom)
                                } else {
                                    selectedSymptoms.add(symptom)
                                }
                            }
                            .testTag("symptom_chip_${symptom.replace(" ", "_")}")
                    ) {
                        Text(
                            text = symptom,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Action Button
            Button(
                onClick = {
                    HapticUtil.performHeartbeat(context)
                    val currentDate = todayLog?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val newLog = (todayLog ?: CycleLogEntity(date = currentDate)).copy(
                        mood = selectedMood,
                        energyLevel = selectedEnergy,
                        flow = selectedFlow,
                        symptoms = selectedSymptoms.joinToString(", "),
                        phase = currentPhase.name
                    )
                    onSaveLog(newLog)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_quick_biomarker_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save Daily Biomarkers",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
