package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Circular progress visualization mapping the 28-day menstrual cycle to hormonal phases.
 * Visually emphasizes the continuous transitions between Menstrual, Follicular, Ovulatory, and Luteal phases.
 */
@Composable
fun PhaseCircularProgressDial(
    currentDayOfCycle: Int,
    totalCycleDays: Int = 28,
    onDaySelect: ((Int) -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val normalizedDay = currentDayOfCycle.coerceIn(1, totalCycleDays)

    val currentPhase = when (normalizedDay) {
        in 1..5 -> CyclePhase.MENSTRUAL
        in 6..12 -> CyclePhase.FOLLICULAR
        in 13..16 -> CyclePhase.OVULATORY
        else -> CyclePhase.LUTEAL
    }

    val activeColor = when (currentPhase) {
        CyclePhase.MENSTRUAL -> PhaseMenstrualColor
        CyclePhase.FOLLICULAR -> PhaseFollicularColor
        CyclePhase.OVULATORY -> PhaseOvulatoryColor
        CyclePhase.LUTEAL -> PhaseLutealColor
    }

    // Gentle pulsing animation for the transition indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_marker")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CalyxSurface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("circular_phase_dial_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                            .background(activeColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HORMONAL ORBIT DIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = CalyxPrimary,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentPhase.name.lowercase().replaceFirstChar { it.uppercase() }} Transition Phase",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (onInfoClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = CalyxSurfaceVariant,
                        modifier = Modifier.clickable {
                            HapticUtil.performHeartbeat(context)
                            onInfoClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Phase Guide",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Circular Canvas Dial
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                        .pointerInput(totalCycleDays) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist in 40.dp.toPx()..110.dp.toPx()) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    angle = (angle + 90f + 360f) % 360f
                                    val day = ((angle / 360f) * totalCycleDays).toInt() + 1
                                    HapticUtil.performHeartbeat(context)
                                    onDaySelect?.invoke(day.coerceIn(1, totalCycleDays))
                                }
                            }
                        }
                        .pointerInput(totalCycleDays) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = change.position.x - center.x
                                val dy = change.position.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist in 35.dp.toPx()..120.dp.toPx()) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    angle = (angle + 90f + 360f) % 360f
                                    val day = ((angle / 360f) * totalCycleDays).toInt() + 1
                                    onDaySelect?.invoke(day.coerceIn(1, totalCycleDays))
                                }
                            }
                        }
                ) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)
                    val radius = diameter / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Phase definitions with angle ranges (Total 360 degrees starting at top: -90°)
                    // Menstrual: Days 1-5 (~64.3°)
                    // Follicular: Days 6-12 (~90.0°)
                    // Ovulatory: Days 13-16 (~51.4°)
                    // Luteal: Days 17-28 (~154.3°)
                    val startAngle = -90f
                    val anglePerDay = 360f / totalCycleDays

                    val mAngle = 5f * anglePerDay
                    val fAngle = 7f * anglePerDay
                    val oAngle = 4f * anglePerDay
                    val lAngle = 12f * anglePerDay

                    // 1. Background full track
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    // 2. Segmented Phase Arcs
                    // Menstrual Arc
                    drawArc(
                        color = PhaseMenstrualColor.copy(alpha = if (currentPhase == CyclePhase.MENSTRUAL) 1f else 0.45f),
                        startAngle = startAngle,
                        sweepAngle = mAngle - 3f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = if (currentPhase == CyclePhase.MENSTRUAL) strokeWidth * 1.15f else strokeWidth, cap = StrokeCap.Round)
                    )

                    // Follicular Arc
                    drawArc(
                        color = PhaseFollicularColor.copy(alpha = if (currentPhase == CyclePhase.FOLLICULAR) 1f else 0.45f),
                        startAngle = startAngle + mAngle,
                        sweepAngle = fAngle - 3f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = if (currentPhase == CyclePhase.FOLLICULAR) strokeWidth * 1.15f else strokeWidth, cap = StrokeCap.Round)
                    )

                    // Ovulatory Arc
                    drawArc(
                        color = PhaseOvulatoryColor.copy(alpha = if (currentPhase == CyclePhase.OVULATORY) 1f else 0.45f),
                        startAngle = startAngle + mAngle + fAngle,
                        sweepAngle = oAngle - 3f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = if (currentPhase == CyclePhase.OVULATORY) strokeWidth * 1.15f else strokeWidth, cap = StrokeCap.Round)
                    )

                    // Luteal Arc
                    drawArc(
                        color = PhaseLutealColor.copy(alpha = if (currentPhase == CyclePhase.LUTEAL) 1f else 0.45f),
                        startAngle = startAngle + mAngle + fAngle + oAngle,
                        sweepAngle = lAngle - 3f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = if (currentPhase == CyclePhase.LUTEAL) strokeWidth * 1.15f else strokeWidth, cap = StrokeCap.Round)
                    )

                    // 3. Transition Markers (Subtle glowing boundary dots between phases)
                    val transitionDays = listOf(5f, 12f, 16f, 28f)
                    transitionDays.forEach { tDay ->
                        val rad = Math.toRadians((startAngle + tDay * anglePerDay).toDouble())
                        val tx = center.x + (radius * cos(rad)).toFloat()
                        val ty = center.y + (radius * sin(rad)).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 3.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                    }

                    // 4. Current Day Pointer Indicator on the Orbit
                    val currentProgressAngle = startAngle + (normalizedDay - 0.5f) * anglePerDay
                    val curRad = Math.toRadians(currentProgressAngle.toDouble())
                    val px = center.x + (radius * cos(curRad)).toFloat()
                    val py = center.y + (radius * sin(curRad)).toFloat()

                    // Glowing outer aura
                    drawCircle(
                        color = activeColor.copy(alpha = pulseAlpha * 0.4f),
                        radius = 12.dp.toPx(),
                        center = Offset(px, py)
                    )
                    // Solid inner dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // Center Information Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "DAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$normalizedDay",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeColor
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = activeColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = currentPhase.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phase Legend Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhaseLegendItem("Menses (1-5)", PhaseMenstrualColor, currentPhase == CyclePhase.MENSTRUAL)
                PhaseLegendItem("Follicular (6-12)", PhaseFollicularColor, currentPhase == CyclePhase.FOLLICULAR)
                PhaseLegendItem("Ovulatory (13-16)", PhaseOvulatoryColor, currentPhase == CyclePhase.OVULATORY)
                PhaseLegendItem("Luteal (17-28)", PhaseLutealColor, currentPhase == CyclePhase.LUTEAL)
            }
        }
    }
}

@Composable
private fun PhaseLegendItem(
    label: String,
    color: Color,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isActive) color else color.copy(alpha = 0.4f))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
