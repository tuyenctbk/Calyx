package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CyclePhase
import com.example.ui.theme.PhaseFollicularColor
import com.example.ui.theme.PhaseLutealColor
import com.example.ui.theme.PhaseMenstrualColor
import com.example.ui.theme.PhaseOvulatoryColor
import kotlin.math.sin

@Composable
fun EnergyWaveCanvas(
    currentPhase: CyclePhase,
    dayOfCycle: Int,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (currentPhase) {
        CyclePhase.MENSTRUAL -> PhaseMenstrualColor
        CyclePhase.FOLLICULAR -> PhaseFollicularColor
        CyclePhase.OVULATORY -> PhaseOvulatoryColor
        CyclePhase.LUTEAL -> PhaseLutealColor
    }

    val energyScore = when (currentPhase) {
        CyclePhase.OVULATORY -> 92
        CyclePhase.FOLLICULAR -> 84
        CyclePhase.LUTEAL -> 62
        CyclePhase.MENSTRUAL -> 45
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = onInfoClick != null) { onInfoClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height * 0.55f

            val amplitude1 = when (currentPhase) {
                CyclePhase.OVULATORY -> 36f
                CyclePhase.FOLLICULAR -> 28f
                CyclePhase.LUTEAL -> 22f
                CyclePhase.MENSTRUAL -> 16f
            }
            val amplitude2 = amplitude1 * 0.7f

            // Background Deep Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.22f * pulseGlow),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, centerY),
                    radius = width * 0.55f
                )
            )

            // Layer 1: Ambient Fill Wave
            val path1 = Path().apply {
                moveTo(0f, centerY)
                var x = 0f
                while (x <= width) {
                    val y = centerY + amplitude1 * sin((x / width * 2.8 * Math.PI) + phaseShift).toFloat()
                    lineTo(x, y)
                    x += 8f
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path1,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.35f),
                        phaseColor.copy(alpha = 0.04f)
                    ),
                    startY = centerY - amplitude1,
                    endY = height
                )
            )

            // Layer 2: Harmonic Secondary Wave
            val path2 = Path().apply {
                moveTo(0f, centerY)
                var x = 0f
                while (x <= width) {
                    val y = centerY + amplitude2 * sin((x / width * 3.5 * Math.PI) - phaseShift * 0.8f + 1.2f).toFloat()
                    lineTo(x, y)
                    x += 8f
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path2,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = centerY - amplitude2,
                    endY = height
                )
            )

            // Layer 3: Crisp Surface Stroke Wave
            val pathStroke = Path().apply {
                moveTo(0f, centerY)
                var x = 0f
                while (x <= width) {
                    val y = centerY + amplitude1 * sin((x / width * 2.8 * Math.PI) + phaseShift).toFloat()
                    lineTo(x, y)
                    x += 6f
                }
            }

            drawPath(
                path = pathStroke,
                color = phaseColor.copy(alpha = 0.85f),
                style = Stroke(width = 2.8.dp.toPx())
            )

            // Layer 4: Floating Dynamic Bioluminescent Sparkle Dots along the wave
            val samplePositions = listOf(0.18f, 0.45f, 0.72f, 0.90f)
            samplePositions.forEachIndexed { i, ratio ->
                val px = width * ratio
                val py = centerY + amplitude1 * sin((px / width * 2.8 * Math.PI) + phaseShift).toFloat()
                drawCircle(
                    color = phaseColor,
                    radius = (2.5f + (i % 2) * 1.5f) * pulseGlow,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 1.5f * pulseGlow,
                    center = Offset(px, py)
                )
            }
        }

        // Overlay Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = phaseColor.copy(alpha = 0.18f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(phaseColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAY $dayOfCycle OF 28 • VITALITY $energyScore%",
                        style = MaterialTheme.typography.labelSmall,
                        color = phaseColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentPhase.displayName,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = currentPhase.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (onInfoClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Phase Guide",
                        tint = phaseColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tap to view Phase Blueprint Guide",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = phaseColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
