package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CycleLogEntity
import com.example.data.remote.GeminiClient
import com.example.ui.components.BbtChart
import com.example.ui.components.ThreeCycleSymptomChart
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.ui.theme.CalyxTertiary
import com.example.ui.theme.PhaseOvulatoryColor
import com.example.util.HapticUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetectiveScreen(
    logs: List<CycleLogEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember(logs) {
        mutableStateOf<String>(
            GeminiClient.generateLocalDiagnostic(
                logs.joinToString("\n") { "Date: ${it.date}, Phase: ${it.phase}, BBT: ${it.bbt}, Sleep: ${it.sleepHours}h, Symptoms: ${it.symptoms}" }
            )
        )
    }

    var customQuestion by remember { mutableStateOf("") }
    var customAiAnswer by remember { mutableStateOf<String?>(null) }
    var isAnsweringQuestion by remember { mutableStateOf(false) }
    var showAnomalyDetails by remember { mutableStateOf(false) }
    var showAnalysisSheet by remember { mutableStateOf(false) }
    var showAnswerSheet by remember { mutableStateOf(false) }
    var showAnomalySheet by remember { mutableStateOf(false) }

    val triggerChips = listOf(
        "Sleep < 7.0h & Migraines",
        "High Caffeine & Late Luteal Anxiety",
        "Post-Ovulation BBT Jump",
        "Seed Cycling Impact",
        "HIIT vs Luteal Fatigue"
    )

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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.detective_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.detective_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = CalyxSecondary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Active",
                            tint = CalyxSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Local AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = CalyxSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 1: Visual Charts First (Maximum Visualization)
            ThreeCycleSymptomChart(
                logs = logs,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Basal Body Temperature Biphasic Chart
            BbtChart(logs = logs)

            Spacer(modifier = Modifier.height(16.dp))

            // RANK 2: Symptom Trigger Correlation & Clusters
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(CalyxPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Detective AI",
                                    tint = CalyxPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Trigger Correlation",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                scope.launch {
                                    isAnalyzing = true
                                    val logSummary = logs.take(14).joinToString("\n") {
                                        "Date: ${it.date}, Phase: ${it.phase}, BBT: ${it.bbt}, Sleep: ${it.sleepHours}h, Symptoms: ${it.symptoms}"
                                    }
                                    aiAnalysisResult = GeminiClient.analyzeSymptomTriggers(logSummary)
                                    isAnalyzing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isAnalyzing,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("analyze_triggers_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Re-analyze",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Re-check", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Minimized Trigger Analysis Preview with Hint Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CalyxPrimary.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticUtil.performHeartbeat(context)
                                showAnalysisSheet = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = aiAnalysisResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Full Analysis",
                                tint = CalyxPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Correlation Cluster Chips
                    Text(
                        text = "CORRELATION CLUSTERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = CalyxPrimary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        triggerChips.forEach { chip ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CalyxSurfaceVariant,
                                modifier = Modifier.clickable {
                                    HapticUtil.performHeartbeat(context)
                                    customQuestion = chip
                                    scope.launch {
                                        isAnsweringQuestion = true
                                        val logSummary = logs.take(14).joinToString("\n") {
                                            "Date: ${it.date}, Phase: ${it.phase}, BBT: ${it.bbt}, Sleep: ${it.sleepHours}h, Symptoms: ${it.symptoms}"
                                        }
                                        customAiAnswer = GeminiClient.answerUserQuestion(chip, logSummary)
                                        isAnsweringQuestion = false
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TipsAndUpdates,
                                        contentDescription = chip,
                                        tint = CalyxSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = chip,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RANK 3: AI Consultation & Q&A
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Consultation",
                            tint = CalyxTertiary,
                            modifier = Modifier.size(18.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "Ask AI Consultation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customQuestion,
                        onValueChange = { customQuestion = it },
                        placeholder = { Text(stringResource(R.string.ask_detective_placeholder), fontSize = 13.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (customQuestion.isNotBlank()) {
                                        HapticUtil.performHeartbeat(context)
                                        scope.launch {
                                            isAnsweringQuestion = true
                                            val logSummary = logs.take(14).joinToString("\n") {
                                                "Date: ${it.date}, Phase: ${it.phase}, BBT: ${it.bbt}, Sleep: ${it.sleepHours}h, Symptoms: ${it.symptoms}"
                                            }
                                            customAiAnswer = GeminiClient.answerUserQuestion(customQuestion, logSummary)
                                            isAnsweringQuestion = false
                                        }
                                    }
                                }
                            ) {
                                if (isAnsweringQuestion) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CalyxPrimary)
                                } else {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send), tint = CalyxPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CalyxPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = customAiAnswer != null) {
                        customAiAnswer?.let { answer ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clickable {
                                        if (answer.length > 120) {
                                            HapticUtil.performHeartbeat(context)
                                            showAnswerSheet = true
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (answer.length > 120) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Read Full Response ↗",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CalyxPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RANK 4: Clinical Anomaly Monitor with Hint Button
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        showAnomalySheet = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Anomaly Check",
                            tint = CalyxSecondary,
                            modifier = Modifier.size(18.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "Anomaly Monitor: Normal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            showAnomalySheet = true
                        },
                        modifier = Modifier.size(28.dp).testTag("anomaly_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.info_hint),
                            tint = CalyxPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal popup sheet for complete Trigger Correlation analysis
    if (showAnalysisSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAnalysisSheet = false },
            containerColor = CalyxSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CalyxPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRIGGER CORRELATION REPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CalyxPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = aiAnalysisResult,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { showAnalysisSheet = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close), color = CalyxPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal popup sheet for complete AI Consultation answer
    if (showAnswerSheet && customAiAnswer != null) {
        ModalBottomSheet(
            onDismissRequest = { showAnswerSheet = false },
            containerColor = CalyxSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "AI Clinical Consultation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CalyxPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = customAiAnswer ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { showAnswerSheet = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close), color = CalyxPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal popup sheet for Anomaly Monitor explanation
    if (showAnomalySheet) {
        ModalBottomSheet(
            onDismissRequest = { showAnomalySheet = false },
            containerColor = CalyxSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CalyxSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clinical Anomaly Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Current cycle length (28 days) and flow variance compared with 12-month baseline. Standard deviation is within normal parameters (< 1.2d). No clinical flags or irregular cycle markers detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { showAnomalySheet = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close), color = CalyxPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
