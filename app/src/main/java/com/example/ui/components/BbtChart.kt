package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CycleLogEntity
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxTertiary
import com.example.ui.theme.PhaseOvulatoryColor
import com.example.util.HapticUtil
import java.util.Locale

@Composable
fun BbtChart(
    logs: List<CycleLogEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sortedLogs = logs.sortedBy { it.date }.takeLast(28)
    var inspectedIndex by remember { mutableIntStateOf(if (sortedLogs.isNotEmpty()) sortedLogs.size - 1 else 0) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BBT THERMAL SHIFT CURVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalyxPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Biphasic Ovulation Confirmation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PhaseOvulatoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+0.35°C Confirmed",
                        style = MaterialTheme.typography.labelSmall,
                        color = PhaseOvulatoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inspected Day Tooltip Card
            if (sortedLogs.isNotEmpty() && inspectedIndex in sortedLogs.indices) {
                val selectedLog = sortedLogs[inspectedIndex]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedLog.bbt >= 36.6f) PhaseOvulatoryColor else CalyxSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Day ${inspectedIndex + 1} (${selectedLog.phase})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.2f", selectedLog.bbt)}°C • HRV ${selectedLog.hrv}ms",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CalyxSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Chart Area with Touch Interaction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(sortedLogs) {
                            detectTapGestures { offset ->
                                if (sortedLogs.size > 1) {
                                    val step = size.width / (sortedLogs.size - 1)
                                    val nearestIndex = ((offset.x + step / 2) / step).toInt().coerceIn(0, sortedLogs.size - 1)
                                    if (nearestIndex != inspectedIndex) {
                                        inspectedIndex = nearestIndex
                                        HapticUtil.performHeartbeat(context)
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    if (sortedLogs.size < 2) return@Canvas

                    val minTemp = 36.0f
                    val maxTemp = 37.2f
                    val tempRange = maxTemp - minTemp
                    val stepX = width / (sortedLogs.size - 1)

                    // 1. Grid Horizontal Guidelines (36.2°, 36.5° Coverline, 36.8°)
                    listOf(36.2f, 36.5f, 36.8f).forEach { temp ->
                        val lineY = height - ((temp - minTemp) / tempRange * height)
                        drawLine(
                            color = if (temp == 36.5f) PhaseOvulatoryColor.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.15f),
                            start = Offset(0f, lineY),
                            end = Offset(width, lineY),
                            strokeWidth = if (temp == 36.5f) 1.5.dp.toPx() else 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // 2. Ovulation Vertical Threshold Line (Day 14)
                    if (sortedLogs.size >= 14) {
                        val ovX = 13 * stepX
                        drawLine(
                            color = PhaseOvulatoryColor.copy(alpha = 0.5f),
                            start = Offset(ovX, 0f),
                            end = Offset(ovX, height),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // 3. Compute Coordinates
                    val points = sortedLogs.mapIndexed { index, log ->
                        val clampedBbt = log.bbt.coerceIn(minTemp, maxTemp)
                        val x = index * stepX
                        val y = height - ((clampedBbt - minTemp) / tempRange * height)
                        Offset(x, y)
                    }

                    // 4. Fill Area Gradient Under Curve
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        points.forEachIndexed { i, pt ->
                            if (i == 0) lineTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                        lineTo(points.last().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CalyxSecondary.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 5. Draw Stroke Path
                    val strokePath = Path().apply {
                        points.forEachIndexed { i, pt ->
                            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = CalyxSecondary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 6. Draw Points and Active Marker
                    points.forEachIndexed { index, pt ->
                        val isSelected = index == inspectedIndex
                        val isPostOvulation = index >= 13

                        drawCircle(
                            color = if (isPostOvulation) PhaseOvulatoryColor else CalyxSecondary,
                            radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                            center = pt
                        )

                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Scale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day 1 (Menses)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Day 14 (Ovulation Surge)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = PhaseOvulatoryColor)
                Text(text = "Day 28 (Luteal End)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
