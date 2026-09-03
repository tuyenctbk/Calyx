package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.CyclePhase
import com.example.data.remote.HormonalWellnessInsight
import com.example.data.remote.HormonalWellnessService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.components.ActionableGeminiInsightsCard
import com.example.ui.components.CycleFitnessCorrelationChart
import com.example.ui.components.DailyCheckInWidget
import com.example.ui.components.EnergyWaveCanvas
import com.example.ui.components.PhaseCircularProgressDial
import com.example.ui.components.QuickSymptomInputSheet
import com.example.ui.theme.CalyxDarkBg
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    logs: List<CycleLogEntity>,
    periods: List<CyclePeriodEntity>,
    onSaveLog: (CycleLogEntity) -> Unit = {},
    onOpenLog: () -> Unit,
    onOpenPeriodLogger: () -> Unit,
    onEmergencyLock: () -> Unit,
    onTriggerNotification: ((CyclePhase, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val latestPeriod = periods.maxByOrNull { it.startDate }
    val avgCycleLen = if (periods.isNotEmpty()) periods.map { it.cycleLengthDays }.average().toInt().coerceIn(21, 35) else 28

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayLog = remember(logs, todayDateStr) {
        logs.find { it.date == todayDateStr } ?: CycleLogEntity(date = todayDateStr)
    }

    val todayCycleDay = remember(latestPeriod, avgCycleLen) {
        if (latestPeriod != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val periodDate = runCatching { sdf.parse(latestPeriod.startDate) }.getOrNull()
            if (periodDate != null) {
                val diffDays = ((System.currentTimeMillis() - periodDate.time) / (1000L * 60 * 60 * 24)).toInt()
                if (diffDays >= 0) ((diffDays % avgCycleLen) + 1).coerceIn(1, avgCycleLen) else 14
            } else 14
        } else 14
    }

    var selectedDayOfCycle by remember(todayCycleDay) { mutableIntStateOf(todayCycleDay) }
    var selectedVisualizationMode by remember { mutableIntStateOf(0) } // 0: Circular Dial, 1: Wave Canvas
    var showQuickLogSheet by remember { mutableStateOf(false) }
    var showPhaseGuideSheet by remember { mutableStateOf(false) }
    var showDeepPhysiologySheet by remember { mutableStateOf(false) }
    var lastSyncText by remember { mutableStateOf("Synced 2m ago") }
    var isSyncingProvider by remember { mutableStateOf(false) }

    val wellnessService = remember { HormonalWellnessService() }
    var aiInsight by remember { mutableStateOf<HormonalWellnessInsight?>(null) }
    var isGeneratingInsight by remember { mutableStateOf(false) }

    // Derive phase for selected day
    val activePhase = when (selectedDayOfCycle) {
        in 1..5 -> CyclePhase.MENSTRUAL
        in 6..12 -> CyclePhase.FOLLICULAR
        in 13..16 -> CyclePhase.OVULATORY
        else -> CyclePhase.LUTEAL
    }

    LaunchedEffect(activePhase, logs.size) {
        isGeneratingInsight = true
        aiInsight = wellnessService.generatePersonalizedInsights(activePhase, logs, periods)
        isGeneratingInsight = false
    }

    // Dynamic phase color animation for smooth transitions
    val activePhaseColor by animateColorAsState(
        targetValue = when (activePhase) {
            CyclePhase.MENSTRUAL -> PhaseMenstrualColor.copy(alpha = 0.08f)
            CyclePhase.FOLLICULAR -> PhaseFollicularColor.copy(alpha = 0.07f)
            CyclePhase.OVULATORY -> PhaseOvulatoryColor.copy(alpha = 0.09f)
            CyclePhase.LUTEAL -> PhaseLutealColor.copy(alpha = 0.08f)
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "home_phase_glow"
    )

    // Subtle pulsing animation for Health Connect status dot
    val infiniteTransition = rememberInfiniteTransition(label = "sync_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    if (showQuickLogSheet) {
        QuickSymptomInputSheet(
            todayLog = todayLog,
            currentPhase = activePhase,
            onDismiss = { showQuickLogSheet = false },
            onSaveLog = { updatedLog ->
                onSaveLog(updatedLog)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        activePhaseColor,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // ==========================================
            // RANK 0: Top Haven Header & Provider Status Indicator
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CalyxPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Calyx Emblem",
                            tint = CalyxPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CALYX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.5.sp,
                            maxLines = 1
                        )
                        // Subtle Health Data Provider Sync Indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            modifier = Modifier.clickable {
                                HapticUtil.performHeartbeat(context)
                                isSyncingProvider = true
                                lastSyncText = "Syncing..."
                                scope.launch {
                                    kotlinx.coroutines.delay(800)
                                    isSyncingProvider = false
                                    lastSyncText = "Health Connect: Just now"
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CalyxSecondary.copy(alpha = if (isSyncingProvider) 1f else pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lastSyncText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            onEmergencyLock()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("emergency_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.lock_haven),
                            tint = CalyxPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            onOpenPeriodLogger()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CalyxRose.copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("open_period_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = stringResource(R.string.period_history),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.period_history),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            showQuickLogSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("quick_log_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = stringResource(R.string.log_symptoms),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.log_symptoms),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 1: Circular Progress Phase Dial & Wave Visualizer
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOLOGICAL VISUALIZER",
                    style = MaterialTheme.typography.labelSmall,
                    color = CalyxPrimary,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                // Toggle between Circular Orbit Dial & Energy Wave
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedVisualizationMode == 0) CalyxPrimary.copy(alpha = 0.2f) else CalyxSurfaceVariant.copy(alpha = 0.4f),
                        border = if (selectedVisualizationMode == 0) androidx.compose.foundation.BorderStroke(1.dp, CalyxPrimary) else null,
                        modifier = Modifier.clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedVisualizationMode = 0
                        }
                    ) {
                        Text(
                            text = "Orbit Dial",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (selectedVisualizationMode == 0) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedVisualizationMode == 1) CalyxPrimary.copy(alpha = 0.2f) else CalyxSurfaceVariant.copy(alpha = 0.4f),
                        border = if (selectedVisualizationMode == 1) androidx.compose.foundation.BorderStroke(1.dp, CalyxPrimary) else null,
                        modifier = Modifier.clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedVisualizationMode = 1
                        }
                    ) {
                        Text(
                            text = "Energy Wave",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (selectedVisualizationMode == 1) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = selectedVisualizationMode,
                label = "vis_mode_anim"
            ) { mode ->
                if (mode == 0) {
                    PhaseCircularProgressDial(
                        currentDayOfCycle = selectedDayOfCycle,
                        totalCycleDays = avgCycleLen,
                        onDaySelect = { selectedDayOfCycle = it },
                        onInfoClick = { showPhaseGuideSheet = true }
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EnergyWaveCanvas(
                            currentPhase = activePhase,
                            dayOfCycle = selectedDayOfCycle,
                            onInfoClick = { showPhaseGuideSheet = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 2: 28-Day Biological Orbit Quick Selector
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cycle_orbit_timeline),
                    style = MaterialTheme.typography.labelSmall,
                    color = CalyxPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                if (selectedDayOfCycle != todayCycleDay) {
                    Surface(
                        shape = CircleShape,
                        color = CalyxSecondary.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedDayOfCycle = todayCycleDay
                            }
                            .testTag("jump_to_today_button")
                    ) {
                        Text(
                            text = stringResource(R.string.jump_to_today, todayCycleDay),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalyxSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items((1..avgCycleLen).toList()) { day ->
                    val isSelected = selectedDayOfCycle == day
                    val isToday = day == todayCycleDay
                    val phaseForDay = when (day) {
                        in 1..5 -> CyclePhase.MENSTRUAL
                        in 6..12 -> CyclePhase.FOLLICULAR
                        in 13..16 -> CyclePhase.OVULATORY
                        else -> CyclePhase.LUTEAL
                    }
                    val phaseColor = when (phaseForDay) {
                        CyclePhase.MENSTRUAL -> PhaseMenstrualColor
                        CyclePhase.FOLLICULAR -> PhaseFollicularColor
                        CyclePhase.OVULATORY -> PhaseOvulatoryColor
                        CyclePhase.LUTEAL -> PhaseLutealColor
                    }

                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) phaseColor.copy(alpha = 0.28f)
                                else CalyxSurface
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.5.dp,
                                color = if (isSelected) phaseColor else if (isToday) CalyxPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                selectedDayOfCycle = day
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "D$day",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) phaseColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor)
                            )
                            if (isToday) {
                                Text(
                                    text = "NOW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CalyxSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 3: Actionable Gemini Health Insights Card (Expandable prioritized)
            // ==========================================
            ActionableGeminiInsightsCard(
                insight = aiInsight,
                currentPhase = activePhase,
                isLoading = isGeneratingInsight,
                onRefresh = {
                    scope.launch {
                        isGeneratingInsight = true
                        aiInsight = wellnessService.generatePersonalizedInsights(activePhase, logs, periods)
                        isGeneratingInsight = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 4: Fitness Performance & Cycle Phase Correlation Chart
            // ==========================================
            CycleFitnessCorrelationChart(
                logs = logs,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 5: Biomarker Metrics Grid & Daily Check-In
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOMARKER SIGNALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CalyxPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        HapticUtil.performHeartbeat(context)
                        showDeepPhysiologySheet = true
                    },
                    modifier = Modifier.size(24.dp).testTag("biomarker_hint_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.info_hint),
                        tint = CalyxPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BiomarkerMetricCard(
                    icon = Icons.Default.Thermostat,
                    label = "BBT TEMP",
                    value = if (activePhase == CyclePhase.OVULATORY || activePhase == CyclePhase.LUTEAL) "36.72°C" else "36.35°C",
                    delta = if (activePhase == CyclePhase.OVULATORY) "+0.35° Shift" else "Baseline",
                    accentColor = if (activePhase == CyclePhase.OVULATORY) PhaseOvulatoryColor else CalyxSecondary,
                    onClick = { showDeepPhysiologySheet = true },
                    modifier = Modifier.weight(1f)
                )

                BiomarkerMetricCard(
                    icon = Icons.Default.Favorite,
                    label = "MEAN HRV",
                    value = when (activePhase) {
                        CyclePhase.OVULATORY -> "68 ms"
                        CyclePhase.FOLLICULAR -> "64 ms"
                        CyclePhase.LUTEAL -> "52 ms"
                        CyclePhase.MENSTRUAL -> "48 ms"
                    },
                    delta = when (activePhase) {
                        CyclePhase.OVULATORY -> "High Recovery"
                        CyclePhase.FOLLICULAR -> "Building"
                        CyclePhase.LUTEAL -> "Manage Cortisol"
                        CyclePhase.MENSTRUAL -> "Rest Mode"
                    },
                    accentColor = CalyxPrimary,
                    onClick = { showDeepPhysiologySheet = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BiomarkerMetricCard(
                    icon = Icons.Default.Bedtime,
                    label = "SLEEP REST",
                    value = "${todayLog.sleepHours}h",
                    delta = "Optimal Continuity",
                    accentColor = CalyxTertiary,
                    onClick = { showDeepPhysiologySheet = true },
                    modifier = Modifier.weight(1f)
                )

                BiomarkerMetricCard(
                    icon = Icons.Default.Eco,
                    label = "SEED CYCLING",
                    value = if (selectedDayOfCycle <= 14) "Flax + Pumpkin" else "Sesame + Sunflower",
                    delta = if (selectedDayOfCycle <= 14) "Estrogen Boost" else "Progesterone Boost",
                    accentColor = if (selectedDayOfCycle <= 14) PhaseFollicularColor else PhaseLutealColor,
                    onClick = { showDeepPhysiologySheet = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Check-In Widget
            DailyCheckInWidget(
                todayLog = todayLog,
                currentPhase = activePhase,
                onSaveCheckIn = { updatedLog ->
                    onSaveLog(updatedLog)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // RANK 6: Period History & Biological Forecast
            // ==========================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        onOpenPeriodLogger()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CalyxRose.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CalyxRose,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (latestPeriod != null) "Last Period: ${latestPeriod.startDate}" else "Track Your Cycle Interval",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Avg Cycle: $avgCycleLen Days • Menses: ${latestPeriod?.periodLengthDays ?: 5} Days",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CalyxRose.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Edit Log",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = CalyxRose,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Deep Physiology Breakdown Bottom Sheet
        if (showDeepPhysiologySheet) {
            ModalBottomSheet(
                onDismissRequest = { showDeepPhysiologySheet = false },
                containerColor = CalyxSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "PHYSIOLOGICAL MECHANISMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CalyxPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PhaseGuideItem(
                        title = "Circadian & Thermoregulation",
                        subtitle = "Progesterone Thermogenic Effect",
                        desc = "Post-ovulation, progesterone elevates baseline body temperature by ~0.35°C, increasing metabolic caloric expenditure by 150-250 kcal/day.",
                        color = PhaseOvulatoryColor
                    )

                    PhaseGuideItem(
                        title = "Heart Rate Variability (HRV)",
                        subtitle = "Autonomic Nervous System",
                        desc = "Parasympathetic tone peaks during Late Follicular/Ovulatory phases, allowing faster athletic recovery and higher stress resilience.",
                        color = CalyxPrimary
                    )

                    PhaseGuideItem(
                        title = "Neurotransmitter Modulation",
                        subtitle = "GABA & Serotonin Flux",
                        desc = "Estrogen potentiates serotonin receptors. When estrogen dips in the late luteal window, dietary magnesium and complex carbs support neurotransmitter stability.",
                        color = PhaseLutealColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Modal Phase Guide Bottom Sheet
        if (showPhaseGuideSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPhaseGuideSheet = false },
                containerColor = CalyxSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "THE 4 HORMONAL SEASONS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CalyxPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PhaseGuideItem(
                        title = "1. Menstrual Phase (Days 1–5)",
                        subtitle = "Winter Season • Rest & Reset",
                        desc = "Estrogen & progesterone are at baseline. Lower physical energy, enhanced intuitive reflection. Prioritize magnesium, warmth, iron-dense foods, and restorative Yin yoga.",
                        color = PhaseMenstrualColor
                    )

                    PhaseGuideItem(
                        title = "2. Follicular Phase (Days 6–12)",
                        subtitle = "Spring Season • Energy & Creation",
                        desc = "Estrogen rises, increasing neuroplasticity, curiosity, and insulin sensitivity. Optimal phase for complex problem solving, new learning, and high-intensity resistance training.",
                        color = PhaseFollicularColor
                    )

                    PhaseGuideItem(
                        title = "3. Ovulatory Phase (Days 13–16)",
                        subtitle = "Summer Season • Peak Charisma & Stamina",
                        desc = "Estrogen & testosterone spike. Peak verbal fluency, magnetic social energy, highest HRV scores, and maximum physical endurance. Ideal for pitches, presentations, and races.",
                        color = PhaseOvulatoryColor
                    )

                    PhaseGuideItem(
                        title = "4. Luteal Phase (Days 17–28)",
                        subtitle = "Autumn Season • Focus & Completion",
                        desc = "Progesterone dominates, promoting calm and deep single-task focus. Basal body temperature increases +0.35°C. Support with complex carbs, hydration, and steady-state training.",
                        color = PhaseLutealColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BiomarkerMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    delta: String,
    accentColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = delta,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PhaseGuideItem(
    title: String,
    subtitle: String,
    desc: String,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 18.dp, top = 2.dp)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 18.dp, top = 4.dp),
            lineHeight = 16.sp
        )
    }
}
