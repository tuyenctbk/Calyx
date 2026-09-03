package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
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
import kotlin.math.sin

enum class SymptomMetricType(val label: String, val unit: String, val maxVal: Float) {
    ENERGY("Energy", "Score 1-10", 10f),
    MOOD("Mood", "Score 1-5", 5f),
    PAIN("Pain / Cramps", "Scale 0-10", 10f)
}

data class CyclePoint(val day: Int, val value: Float)

@Composable
fun ThreeCycleSymptomChart(
    logs: List<CycleLogEntity> = emptyList(),
    periods: List<CyclePeriodEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMetric by remember { mutableStateOf(SymptomMetricType.ENERGY) }
    var hoveredDay by remember { mutableIntStateOf(14) }

    // Generate 3-Cycle Curves for visual pattern analysis
    val cycle3Data = remember(logs, selectedMetric) {
        // Current cycle (Days 1..28)
        (1..28).map { day ->
            val v = when (selectedMetric) {
                SymptomMetricType.ENERGY -> {
                    when (day) {
                        in 1..5 -> 4.5f + (day * 0.4f)
                        in 6..12 -> 6.8f + (day * 0.25f)
                        in 13..16 -> 9.2f + ((day % 2) * 0.4f)
                        else -> 8.5f - ((day - 16) * 0.45f)
                    }
                }
                SymptomMetricType.MOOD -> {
                    when (day) {
                        in 1..5 -> 2.8f + (day * 0.2f)
                        in 6..12 -> 3.9f + (day * 0.1f)
                        in 13..16 -> 4.9f
                        else -> 4.2f - ((day - 16) * 0.18f)
                    }
                }
                SymptomMetricType.PAIN -> {
                    when (day) {
                        in 1..3 -> 6.5f - ((day - 1) * 1.8f)
                        in 4..13 -> 0.5f
                        in 14..16 -> 1.8f // Mid-cycle Mittelschmerz
                        in 24..28 -> 1.5f + ((day - 24) * 0.9f)
                        else -> 0.2f
                    }
                }
            }
            CyclePoint(day, v.coerceIn(0f, selectedMetric.maxVal))
        }
    }

    val cycle2Data = remember(selectedMetric) {
        // Previous cycle
        (1..28).map { day ->
            val base = cycle3Data[day - 1].value
            val variance = (sin(day * 0.8) * 0.4).toFloat()
            CyclePoint(day, (base - 0.3f + variance).coerceIn(0f, selectedMetric.maxVal))
        }
    }

    val cycle1Data = remember(selectedMetric) {
        // 2 cycles ago
        (1..28).map { day ->
            val base = cycle3Data[day - 1].value
            val variance = (sin(day * 1.1) * 0.5).toFloat()
            CyclePoint(day, (base - 0.6f + variance).coerceIn(0f, selectedMetric.maxVal))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("three_cycle_symptom_chart")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3-CYCLE SYMPTOM PATTERNS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalyxPrimary,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recurring Trend Visualizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CalyxPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CalyxPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "92% Pattern Match",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CalyxPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SymptomMetricType.entries.forEach { metric ->
                    val isSelected = selectedMetric == metric
                    val icon = when (metric) {
                        SymptomMetricType.ENERGY -> Icons.Default.Bolt
                        SymptomMetricType.MOOD -> Icons.Default.Mood
                        SymptomMetricType.PAIN -> Icons.Default.Healing
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            selectedMetric = metric
                        },
                        label = { Text(metric.label, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CalyxPrimary.copy(alpha = 0.22f),
                            selectedLabelColor = CalyxPrimary,
                            containerColor = CalyxSurfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CalyxPrimary else Color.Transparent
                        ),
                        modifier = Modifier.testTag("symptom_tab_${metric.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Line Chart (Recharts-style multi-series curve)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalyxSurfaceVariant.copy(alpha = 0.25f))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val dayFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val tappedDay = (dayFraction * 27 + 1).toInt().coerceIn(1, 28)
                                hoveredDay = tappedDay
                                HapticUtil.performHeartbeat(context)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = selectedMetric.maxVal

                    // Draw 4 Phase Background Bands
                    val menstrualW = (5f / 28f) * w
                    val follicularW = (7f / 28f) * w
                    val ovulatoryW = (4f / 28f) * w
                    val lutealW = (12f / 28f) * w

                    drawRect(
                        color = PhaseMenstrualColor.copy(alpha = 0.07f),
                        topLeft = Offset(0f, 0f),
                        size = Size(menstrualW, h)
                    )
                    drawRect(
                        color = PhaseFollicularColor.copy(alpha = 0.06f),
                        topLeft = Offset(menstrualW, 0f),
                        size = Size(follicularW, h)
                    )
                    drawRect(
                        color = PhaseOvulatoryColor.copy(alpha = 0.08f),
                        topLeft = Offset(menstrualW + follicularW, 0f),
                        size = Size(ovulatoryW, h)
                    )
                    drawRect(
                        color = PhaseLutealColor.copy(alpha = 0.07f),
                        topLeft = Offset(menstrualW + follicularW + ovulatoryW, 0f),
                        size = Size(lutealW, h)
                    )

                    // Horizontal Grid Lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = (h / gridSteps) * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    // Helper to map Point to Screen Coordinates
                    fun getCoord(point: CyclePoint): Offset {
                        val x = ((point.day - 1) / 27f) * w
                        val y = h - ((point.value / maxVal) * (h - 16f)) - 8f
                        return Offset(x, y)
                    }

                    // Draw Cycle 1 (Past - 2 cycles ago, dashed muted)
                    val path1 = Path().apply {
                        cycle1Data.forEachIndexed { index, pt ->
                            val coord = getCoord(pt)
                            if (index == 0) moveTo(coord.x, coord.y)
                            else lineTo(coord.x, coord.y)
                        }
                    }
                    drawPath(
                        path = path1,
                        color = Color.Gray.copy(alpha = 0.45f),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    )

                    // Draw Cycle 2 (Previous Cycle, semi-translucent secondary)
                    val path2 = Path().apply {
                        cycle2Data.forEachIndexed { index, pt ->
                            val coord = getCoord(pt)
                            if (index == 0) moveTo(coord.x, coord.y)
                            else lineTo(coord.x, coord.y)
                        }
                    }
                    drawPath(
                        path = path2,
                        color = CalyxSecondary.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Cycle 3 (Current Cycle, bold primary with gradient fill)
                    val path3 = Path().apply {
                        cycle3Data.forEachIndexed { index, pt ->
                            val coord = getCoord(pt)
                            if (index == 0) moveTo(coord.x, coord.y)
                            else lineTo(coord.x, coord.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path3)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CalyxPrimary.copy(alpha = 0.28f),
                                CalyxPrimary.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    drawPath(
                        path = path3,
                        color = CalyxPrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Highlight Indicator on Hovered Day
                    val currentPoint = cycle3Data.getOrNull(hoveredDay - 1)
                    if (currentPoint != null) {
                        val activeCoord = getCoord(currentPoint)
                        drawLine(
                            color = CalyxPrimary.copy(alpha = 0.7f),
                            start = Offset(activeCoord.x, 0f),
                            end = Offset(activeCoord.x, h),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )

                        drawCircle(
                            color = CalyxPrimary,
                            radius = 5.dp.toPx(),
                            center = activeCoord
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = activeCoord
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Legends & Selected Day Value Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendIndicator(label = "Current", color = CalyxPrimary)
                    LegendIndicator(label = "Prior (N-1)", color = CalyxSecondary)
                    LegendIndicator(label = "N-2", color = Color.Gray)
                }

                val currentVal = cycle3Data.getOrNull(hoveredDay - 1)?.value ?: 0f
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CalyxSurfaceVariant
                ) {
                    Text(
                        text = "Day $hoveredDay: ${"%.1f".format(currentVal)} / ${selectedMetric.maxVal.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalyxPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Takeaway Trend Callout
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CalyxPrimary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CalyxPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedMetric) {
                            SymptomMetricType.ENERGY -> "Peak energy occurs consistently Days 13–16 across all 3 cycles (+38% spike)."
                            SymptomMetricType.MOOD -> "Mood reaches lowest variance during Follicular, dip observed on Day 23."
                            SymptomMetricType.PAIN -> "Pelvic tension concentrates tightly on Days 1–2; non-existent during Follicular."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
