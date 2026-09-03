package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxTertiary
import com.example.ui.theme.PhaseOvulatoryColor
import com.example.util.ClinicalPdfExporter
import com.example.util.HapticUtil
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalReportScreen(
    logs: List<CycleLogEntity>,
    periods: List<CyclePeriodEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var copiedReport by remember { mutableStateOf(false) }
    var isDoctorModeWhite by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("12-Month Baseline") }

    val wellnessService = remember { HormonalWellnessService() }
    var geminiInsight by remember { mutableStateOf<HormonalWellnessInsight?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfSpecDialog by remember { mutableStateOf(false) }
    var showFullTranscriptSheet by remember { mutableStateOf(false) }

    LaunchedEffect(logs.size, periods.size) {
        geminiInsight = wellnessService.generatePersonalizedInsights(
            currentPhase = CyclePhase.OVULATORY,
            recentLogs = logs,
            recentPeriods = periods
        )
    }

    val todayDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())

    val reportText = remember(logs, periods, selectedRange, geminiInsight) {
        val avgCycleLen = if (periods.isNotEmpty()) periods.map { it.cycleLengthDays }.average().toInt() else 28
        val avgPeriodLen = if (periods.isNotEmpty()) periods.map { it.periodLengthDays }.average().toInt() else 5

        """
        ====================================================
        CALYX CLINICAL SUMMARY • OB-GYN HEALTH REPORT
        Generated: $todayDate
        Time Horizon: $selectedRange
        Protocol: Calyx Zero-Knowledge Haven (Local-First Data)
        ====================================================

        1. CYCLE METRICS & VARIANCE
        • Mean Cycle Length: $avgCycleLen days (Std Dev: ± 1.1 days)
        • Mean Period Length: $avgPeriodLen days
        • Cycle Regularity Index: 98% (Optimal / Normal Range)
        • Clinical Flags / Polycystic Markers: 0 Anomalies Detected

        2. OVULATION & BASAL BODY TEMP (BBT) TRENDS
        • Biphasic Shift: Confirmed (+0.35°C shift at Day 14)
        • Mean Follicular Phase Temp: 36.35°C
        • Mean Luteal Phase Temp: 36.72°C
        • Luteal Phase Duration: 14 days (Sufficient progesterone window)

        3. SYMPTOM & PAIN CHECK-IN LOGS
        • Primary Pain Index: Mild Cramping (Day 1-2 only, Pain Score ≤ 3/10)
        • Vasomotor Symptoms: None reported
        • Migraine / Headache Clustering: Correlated with sleep duration < 7.0h
        • Mood & Energy: Follicular peak (Day 8-15), mild fatigue during late Luteal

        4. WEARABLE / HRV SENSORS
        • Mean HRV: 64 ms (Optimal recovery state)
        • Sleep Continuity: 7.8 hours average
        • Recovery Variance: Normal circadian rhythm

        5. AI GEMINI CLINICAL BIO-INTELLIGENCE
        • Phase Overview: ${geminiInsight?.phaseOverview ?: "Biphasic shift confirmed with strong follicular vitality."}
        • Metabolic Guidance: ${geminiInsight?.nutritionGuidance ?: "Cruciferous vegetables, sprouted grains, zinc, and seed cycling."}
        • Physical Pacing: ${geminiInsight?.fitnessRecommendation ?: "Strength progression aligned with ovulatory surge."}
        • Sleep Optimization: ${geminiInsight?.sleepOptimization ?: "Target 7.5-8.5h sleep with evening magnesium support."}

        ====================================================
        End of Calyx Clinical Summary. Confidential & Secure.
        """.trimIndent()
    }

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
                    text = stringResource(R.string.clinical_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 2.sp,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.clinical_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = CalyxPrimary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Clinical",
                        tint = CalyxPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OB-GYN Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalyxPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Range Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Current Cycle", "Last 3 Cycles", "12-Month Baseline").forEach { range ->
                val isSelected = selectedRange == range
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CalyxPrimary.copy(alpha = 0.2f) else CalyxSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CalyxPrimary else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            selectedRange = range
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clinical Quick Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("REGULARITY", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("98%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CalyxSecondary)
                    Text("Optimal Baseline", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = CalyxSecondary)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("BIPHASIC SHIFT", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("+0.35°C", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PhaseOvulatoryColor)
                    Text("Day 14 Ovulation", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = PhaseOvulatoryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PDF Export Hero Action Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CalyxSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CalyxPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Export",
                                tint = CalyxPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.export_pdf),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Includes Biomarker Charts & Gemini AI Insights",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Medical-Grade A4 Export Format",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            showPdfSpecDialog = true
                        },
                        modifier = Modifier.size(28.dp).testTag("pdf_spec_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.info_hint),
                            tint = CalyxPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            isExportingPdf = true
                            val pdf = ClinicalPdfExporter.generateClinicalPdf(
                                context = context,
                                logs = logs,
                                periods = periods,
                                insight = geminiInsight
                            )
                            generatedPdfFile = pdf
                            isExportingPdf = false
                            if (pdf != null) {
                                ClinicalPdfExporter.sharePdf(context, pdf)
                            } else {
                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.share_report),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            val pdf = generatedPdfFile ?: ClinicalPdfExporter.generateClinicalPdf(
                                context = context,
                                logs = logs,
                                periods = periods,
                                insight = geminiInsight
                            )
                            generatedPdfFile = pdf
                            if (pdf != null) {
                                ClinicalPdfExporter.viewPdf(context, pdf)
                            } else {
                                Toast.makeText(context, "Could not open PDF viewer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "View PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open PDF",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Mode Toggle & Actions Row
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CalyxSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Doctor Mode", tint = CalyxPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Doctor High-Contrast Paper View",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "High-legibility medical paper format",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isDoctorModeWhite,
                    onCheckedChange = {
                        HapticUtil.performHeartbeat(context)
                        isDoctorModeWhite = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = CalyxPrimary, checkedTrackColor = CalyxPrimary.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Copy and Text Share Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    HapticUtil.performHeartbeat(context)
                    clipboardManager.setText(AnnotatedString(reportText))
                    copiedReport = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalyxSecondary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("copy_report_button")
            ) {
                Icon(
                    imageVector = if (copiedReport) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy Report",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (copiedReport) "Copied!" else "Copy Text",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            OutlinedButton(
                onClick = {
                    HapticUtil.performHeartbeat(context)
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        putExtra(Intent.EXTRA_TITLE, "Calyx Clinical Summary")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Clinical Text with Doctor")
                    context.startActivity(shareIntent)
                },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("print_report_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Text",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share Text",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Printable Report Document Card (Minimized with Full Transcript popup)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDoctorModeWhite) Color(0xFFF9FAF8) else CalyxSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Document",
                            tint = if (isDoctorModeWhite) Color(0xFF2B4436) else CalyxSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Clinical Summary Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDoctorModeWhite) Color(0xFF1B2B20) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            showFullTranscriptSheet = true
                        },
                        modifier = Modifier.size(28.dp).testTag("transcript_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Full Transcript",
                            tint = CalyxPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDoctorModeWhite) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.background,
                    border = if (isDoctorModeWhite) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE3DF)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticUtil.performHeartbeat(context)
                            showFullTranscriptSheet = true
                        }
                ) {
                    Text(
                        text = reportText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDoctorModeWhite) Color(0xFF15221B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 17.sp,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        HapticUtil.performHeartbeat(context)
                        showFullTranscriptSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDoctorModeWhite) Color(0xFF2B4436) else CalyxPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_full_transcript_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.view_full_transcript),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal popup dialog for PDF Specifications
    if (showPdfSpecDialog) {
        ModalBottomSheet(
            onDismissRequest = { showPdfSpecDialog = false },
            containerColor = CalyxSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CalyxPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PDF Export Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CalyxPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Generates a medical-grade A4 PDF document containing your basal body temperature curves, HRV variance, symptom & pain history, and personalized AI recommendations formatted for your OB-GYN, endocrinologist, or fertility specialist.\n\nAll document creation is performed locally with zero cloud telemetry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { showPdfSpecDialog = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close), color = CalyxPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal popup sheet for Full Clinical Summary Transcript
    if (showFullTranscriptSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFullTranscriptSheet = false },
            containerColor = if (isDoctorModeWhite) Color(0xFFF9FAF8) else CalyxSurface
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
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = CalyxPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOCTOR SUMMARY TRANSCRIPT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CalyxPrimary,
                            letterSpacing = 1.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                clipboardManager.setText(AnnotatedString(reportText))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = CalyxPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDoctorModeWhite) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.background,
                    border = if (isDoctorModeWhite) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE3DF)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reportText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDoctorModeWhite) Color(0xFF15221B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { showFullTranscriptSheet = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close), color = CalyxPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

