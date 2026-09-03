package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
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
import com.example.data.local.entities.CyclePhase
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

enum class FitnessMetric(
    val label: String,
    val unit: String,
    val primaryColor: Color,
    val baselineData: List<Float>, // Menstrual, Follicular, Ovulatory, Luteal normalized (0-100)
    val insight: String
) {
    CARDIO_STRAIN(
        label = "Cardio Strain",
        unit = "Strain",
        primaryColor = CalyxSecondary,
        baselineData = listOf(42f, 78f, 92f, 58f),
        insight = "Peak VO2 max and cardio stamina surge during Follicular and Ovulatory phases."
    ),
    STRENGTH_POWER(
        label = "Strength & Power",
        unit = "Power %",
        primaryColor = CalyxPrimary,
        baselineData = listOf(50f, 85f, 96f, 62f),
        insight = "High estrogen pre-ovulation enhances muscle protein synthesis and neuromuscular firing."
    ),
    RECOVERY_HRV(
        label = "Recovery / HRV",
        unit = "ms / Score",
        primaryColor = CalyxTertiary,
        baselineData = listOf(68f, 88f, 82f, 48f),
        insight = "Elevated progesterone post-ovulation raises core temperature, reducing overnight HRV."
    )
}

/**
 * Recharts-equivalent Compose canvas chart displaying the correlation
 * between menstrual cycle phases and historical fitness performance data.
 */
@Composable
fun CycleFitnessCorrelationChart(
    logs: List<CycleLogEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMetric by remember { mutableStateOf(FitnessMetric.STRENGTH_POWER) }
    var hoveredPhaseIndex by remember { mutableIntStateOf(2) } // Default highlight Ovulatory peak
    var showExplanation by remember { mutableStateOf(false) }

    val phases = listOf(
        Pair("Menses", PhaseMenstrualColor),
        Pair("Follicular", PhaseFollicularColor),
        Pair("Ovulatory", PhaseOvulatoryColor),
        Pair("Luteal", PhaseLutealColor)
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("fitness_correlation_chart_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(selectedMetric.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedMetric) {
                                FitnessMetric.CARDIO_STRAIN -> Icons.Default.Speed
                                FitnessMetric.STRENGTH_POWER -> Icons.Default.FitnessCenter
                                FitnessMetric.RECOVERY_HRV -> Icons.AutoMirrored.Filled.DirectionsRun
                            },
                            contentDescription = null,
                            tint = selectedMetric.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "FITNESS CORRELATION TREND",
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedMetric.primaryColor,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hormonal Phase vs. Athletic Output",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = CalyxSurfaceVariant,
                    modifier = Modifier.clickable {
                        HapticUtil.performHeartbeat(context)
                        showExplanation = !showExplanation
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Switcher Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FitnessMetric.values().forEach { metric ->
                    val isSelected = selectedMetric == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            selectedMetric = metric
                        },
                        label = { Text(metric.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = metric.primaryColor.copy(alpha = 0.2f),
                            selectedLabelColor = metric.primaryColor,
                            containerColor = CalyxSurfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) metric.primaryColor else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fitness_metric_${metric.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val stepX = size.width / 4f
                                val idx = (offset.x / stepX).toInt().coerceIn(0, 3)
                                hoveredPhaseIndex = idx
                                HapticUtil.performHeartbeat(context)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 24.dp.toPx()
                    val paddingTop = 12.dp.toPx()
                    val graphHeight = h - paddingBottom - paddingTop
                    val stepX = w / 4f

                    // 1. Draw Phase Background Band Shading
                    phases.forEachIndexed { index, pair ->
                        val leftX = index * stepX
                        drawRect(
                            color = pair.second.copy(alpha = if (hoveredPhaseIndex == index) 0.12f else 0.04f),
                            topLeft = Offset(leftX, 0f),
                            size = androidx.compose.ui.geometry.Size(stepX, h - paddingBottom)
                        )
                    }

                    // 2. Horizontal Grid Lines (25%, 50%, 75%, 100%)
                    listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { fraction ->
                        val y = paddingTop + graphHeight * (1f - fraction)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    // 3. Compute Data Coordinates with Smooth Spline (Cubic Bezier)
                    val points = selectedMetric.baselineData.mapIndexed { idx, value ->
                        val px = idx * stepX + (stepX / 2f)
                        val py = paddingTop + graphHeight * (1f - (value / 100f))
                        Offset(px, py)
                    }

                    // 4. Area Gradient Fill under curve
                    val areaPath = Path().apply {
                        moveTo(points.first().x, h - paddingBottom)
                        lineTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                        lineTo(points.last().x, h - paddingBottom)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                selectedMetric.primaryColor.copy(alpha = 0.35f),
                                selectedMetric.primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = paddingTop,
                            endY = h - paddingBottom
                        )
                    )

                    // 5. Line Curve Stroke
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = selectedMetric.primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 6. Draw Interactive Points and Active Cursor
                    points.forEachIndexed { idx, point ->
                        val isHovered = idx == hoveredPhaseIndex
                        if (isHovered) {
                            // Vertical guide line
                            drawLine(
                                color = selectedMetric.primaryColor.copy(alpha = 0.5f),
                                start = Offset(point.x, paddingTop),
                                end = Offset(point.x, h - paddingBottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                            // Outer glow circle
                            drawCircle(
                                color = selectedMetric.primaryColor.copy(alpha = 0.35f),
                                radius = 9.dp.toPx(),
                                center = point
                            )
                        }
                        // Core dot
                        drawCircle(
                            color = if (isHovered) Color.White else selectedMetric.primaryColor,
                            radius = if (isHovered) 4.5.dp.toPx() else 3.5.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            // X-Axis Phase Labels Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                phases.forEachIndexed { idx, pair ->
                    val isSelected = idx == hoveredPhaseIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            HapticUtil.performHeartbeat(context)
                            hoveredPhaseIndex = idx
                        }
                    ) {
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) pair.second else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${selectedMetric.baselineData[idx].toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) selectedMetric.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Context Insight Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CalyxSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = selectedMetric.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${phases[hoveredPhaseIndex].first} Phase: ${selectedMetric.baselineData[hoveredPhaseIndex].toInt()}% capacity. ${selectedMetric.insight}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
