package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePhase
import com.example.data.local.entities.FlowIntensity
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxRose
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.ui.theme.CalyxTertiary
import com.example.ui.theme.PhaseFollicularColor
import com.example.ui.theme.PhaseLutealColor
import com.example.ui.theme.PhaseMenstrualColor
import com.example.ui.theme.PhaseOvulatoryColor
import com.example.util.HapticUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MucusType(val label: String, val description: String) {
    DRY("Dry / None", "Low fertility window"),
    STICKY("Sticky / Thick", "Post-menstrual baseline"),
    CREAMY("Creamy / Lotion", "Estrogen rising"),
    EGG_WHITE("Egg White / Clear", "Peak ovulatory fertility")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSymptomScreen(
    onSaveLog: (CycleLogEntity) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var selectedPhase by remember { mutableStateOf(CyclePhase.OVULATORY) }
    var selectedFlow by remember { mutableStateOf(FlowIntensity.NONE) }
    var selectedMucus by remember { mutableStateOf(MucusType.EGG_WHITE) }
    var bbtTemp by remember { mutableFloatStateOf(36.65f) }
    var hrvValue by remember { mutableFloatStateOf(68f) }
    var sleepHours by remember { mutableFloatStateOf(7.8f) }
    var energyLevel by remember { mutableFloatStateOf(8f) }
    var seedCyclingChecked by remember { mutableStateOf(true) }
    var magnesiumTaken by remember { mutableStateOf(true) }
    var workoutType by remember { mutableStateOf("Strength & Interval") }
    var notesText by remember { mutableStateOf("") }

    val physicalSymptoms = listOf("Cramps", "Headache", "Bloating", "Tender Breasts", "Skin Breakout", "Radiant Skin")
    val cognitiveSymptoms = listOf("Peak Focus", "Brain Fog", "Social Charisma", "Anxious", "Calm & Grounded", "Fatigued")

    val selectedSymptoms = remember { mutableStateListOf("Peak Focus", "Radiant Skin", "Social Charisma") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "LOG BLUEPRINT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                    Text(
                        text = todayStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    HapticUtil.performHeartbeat(context)
                    val symptomList = selectedSymptoms.toMutableList()
                    if (seedCyclingChecked) symptomList.add("Seed Cycling")
                    if (magnesiumTaken) symptomList.add("Magnesium")
                    symptomList.add("Mucus: ${selectedMucus.label}")

                    val log = CycleLogEntity(
                        date = todayStr,
                        phase = selectedPhase.name,
                        flow = selectedFlow.name,
                        bbt = String.format(Locale.US, "%.2f", bbtTemp).toFloat(),
                        hrv = hrvValue.toInt(),
                        sleepHours = String.format(Locale.US, "%.1f", sleepHours).toFloat(),
                        mood = if (energyLevel >= 7) "Vibrant & High Energy" else "Rest & Grounding",
                        energyLevel = energyLevel.toInt(),
                        symptoms = symptomList.joinToString(", "),
                        notes = notesText
                    )
                    onSaveLog(log)
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("save_log_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Presets Bar
        Text(
            text = "SMART PRESETS",
            style = MaterialTheme.typography.labelSmall,
            color = CalyxPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CalyxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CalyxSecondary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        selectedPhase = CyclePhase.OVULATORY
                        energyLevel = 9f
                        bbtTemp = 36.75f
                        hrvValue = 72f
                        selectedMucus = MucusType.EGG_WHITE
                        selectedSymptoms.clear()
                        selectedSymptoms.addAll(listOf("Peak Focus", "Radiant Skin", "Social Charisma"))
                    }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Peak", tint = CalyxSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Peak Vitality", style = MaterialTheme.typography.labelSmall, color = CalyxSecondary)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CalyxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CalyxRose.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        selectedPhase = CyclePhase.MENSTRUAL
                        selectedFlow = FlowIntensity.MEDIUM
                        energyLevel = 4f
                        bbtTemp = 36.25f
                        hrvValue = 48f
                        selectedMucus = MucusType.DRY
                        selectedSymptoms.clear()
                        selectedSymptoms.addAll(listOf("Cramps", "Fatigued", "Calm & Grounded"))
                    }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Menses", tint = CalyxRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menses Rest", style = MaterialTheme.typography.labelSmall, color = CalyxRose)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phase Selector
        Text(
            text = "CYCLE PHASE",
            style = MaterialTheme.typography.labelSmall,
            color = CalyxPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyclePhase.entries.forEach { phase ->
                val isSelected = selectedPhase == phase
                val phaseColor = when (phase) {
                    CyclePhase.MENSTRUAL -> PhaseMenstrualColor
                    CyclePhase.FOLLICULAR -> PhaseFollicularColor
                    CyclePhase.OVULATORY -> PhaseOvulatoryColor
                    CyclePhase.LUTEAL -> PhaseLutealColor
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) phaseColor.copy(alpha = 0.25f) else CalyxSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) phaseColor else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedPhase = phase
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phase.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) phaseColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Flow Intensity
        Text(
            text = "FLOW INTENSITY",
            style = MaterialTheme.typography.labelSmall,
            color = CalyxPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowIntensity.entries.forEach { flow ->
                val isSelected = selectedFlow == flow
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CalyxRose.copy(alpha = 0.2f) else CalyxSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CalyxRose else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedFlow = flow
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flow.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) CalyxRose else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cervical Mucus Consistency
        Text(
            text = "CERVICAL FLUID CONSISTENCY",
            style = MaterialTheme.typography.labelSmall,
            color = CalyxPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MucusType.entries.forEach { mucus ->
                val isSelected = selectedMucus == mucus
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) CalyxSecondary.copy(alpha = 0.18f) else CalyxSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CalyxSecondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedMucus = mucus
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mucus.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CalyxSecondary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = mucus.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = CalyxSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Biometrics Card (BBT, HRV, Sleep)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CalyxSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // BBT Temperature with fine step buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Basal Body Temp (BBT)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                bbtTemp = (bbtTemp - 0.05f).coerceAtLeast(35.5f)
                                HapticUtil.performHeartbeat(context)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Temp", tint = CalyxSecondary)
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.2f", bbtTemp)} °C",
                            style = MaterialTheme.typography.titleMedium,
                            color = CalyxSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = {
                                bbtTemp = (bbtTemp + 0.05f).coerceAtMost(37.8f)
                                HapticUtil.performHeartbeat(context)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Temp", tint = CalyxSecondary)
                        }
                    }
                }
                Slider(
                    value = bbtTemp,
                    onValueChange = { bbtTemp = it },
                    valueRange = 35.8f..37.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = CalyxSecondary,
                        activeTrackColor = CalyxSecondary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // HRV Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Heart Rate Variability (HRV)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${hrvValue.toInt()} ms",
                        style = MaterialTheme.typography.titleMedium,
                        color = CalyxPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = hrvValue,
                    onValueChange = { hrvValue = it },
                    valueRange = 20f..110f,
                    colors = SliderDefaults.colors(
                        thumbColor = CalyxPrimary,
                        activeTrackColor = CalyxPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sleep Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sleep Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", sleepHours)} hours",
                        style = MaterialTheme.typography.titleMedium,
                        color = CalyxTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    valueRange = 4f..12f,
                    colors = SliderDefaults.colors(
                        thumbColor = CalyxTertiary,
                        activeTrackColor = CalyxTertiary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Symptoms & Vibe Chips
        Text(
            text = "SYMPTOMS & COGNITIVE VIBE",
            style = MaterialTheme.typography.labelSmall,
            color = CalyxPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            (physicalSymptoms + cognitiveSymptoms).forEach { symptom ->
                val isSelected = selectedSymptoms.contains(symptom)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) CalyxPrimary.copy(alpha = 0.25f) else CalyxSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .testTag("symptom_chip_$symptom")
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            if (isSelected) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                        }
                ) {
                    Text(
                        text = symptom,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lifestyle & Nutrition Toggles
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CalyxSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LIFESTYLE & PROTOCOLS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CalyxTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            seedCyclingChecked = !seedCyclingChecked
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Eco, contentDescription = "Seed Cycling", tint = CalyxSecondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedPhase == CyclePhase.FOLLICULAR || selectedPhase == CyclePhase.MENSTRUAL) "Seed Cycling: Flax + Pumpkin" else "Seed Cycling: Sesame + Sunflower",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (seedCyclingChecked) CalyxSecondary else Color.Transparent)
                            .border(1.dp, if (seedCyclingChecked) CalyxSecondary else Color.Gray, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (seedCyclingChecked) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Checked", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            magnesiumTaken = !magnesiumTaken
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Magnesium", tint = CalyxPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Magnesium Glycinate Supplemented",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (magnesiumTaken) CalyxPrimary else Color.Transparent)
                            .border(1.dp, if (magnesiumTaken) CalyxPrimary else Color.Gray, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (magnesiumTaken) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Checked", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notes Input
        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            label = { Text("Private Zero-Knowledge Notes") },
            placeholder = { Text("Log subjective feelings, diet, energy shifts...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CalyxPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_input_field")
        )

        Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
