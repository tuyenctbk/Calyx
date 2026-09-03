package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.CyclePhase
import com.example.ui.components.PeriodLoggingDialog
import com.example.ui.components.PinEntryOverlay
import com.example.ui.screens.ClinicalReportScreen
import com.example.ui.screens.DecoyWeatherScreen
import com.example.ui.screens.DetectiveScreen
import com.example.ui.screens.HavenSecurityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogSymptomScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxTheme
import com.example.ui.theme.PhaseFollicularColor
import com.example.ui.theme.PhaseLutealColor
import com.example.ui.theme.PhaseMenstrualColor
import com.example.ui.theme.PhaseOvulatoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalyxApp(
    viewModel: CalyxViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Determine current user hormonal phase for subtle contextual ambient UI transitions
    val currentPhase = remember(uiState.periods, uiState.logs) {
        val latestPeriod = uiState.periods.maxByOrNull { it.startDate }
        if (latestPeriod != null) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val periodDate = runCatching { sdf.parse(latestPeriod.startDate) }.getOrNull()
            if (periodDate != null) {
                val diffDays = ((System.currentTimeMillis() - periodDate.time) / (1000L * 60 * 60 * 24)).toInt()
                val avgLen = if (latestPeriod.cycleLengthDays > 0) latestPeriod.cycleLengthDays else 28
                val cycleDay = if (diffDays >= 0) (diffDays % avgLen) + 1 else 14
                when (cycleDay) {
                    in 1..5 -> CyclePhase.MENSTRUAL
                    in 6..12 -> CyclePhase.FOLLICULAR
                    in 13..16 -> CyclePhase.OVULATORY
                    else -> CyclePhase.LUTEAL
                }
            } else {
                CyclePhase.OVULATORY
            }
        } else {
            uiState.logs.firstOrNull()?.phase?.let { runCatching { CyclePhase.valueOf(it) }.getOrNull() } ?: CyclePhase.OVULATORY
        }
    }

    val phaseAmbientColor by animateColorAsState(
        targetValue = when (currentPhase) {
            CyclePhase.MENSTRUAL -> PhaseMenstrualColor.copy(alpha = 0.08f)
            CyclePhase.FOLLICULAR -> PhaseFollicularColor.copy(alpha = 0.07f)
            CyclePhase.OVULATORY -> PhaseOvulatoryColor.copy(alpha = 0.09f)
            CyclePhase.LUTEAL -> PhaseLutealColor.copy(alpha = 0.08f)
        },
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "phase_ambient_aura"
    )

    CalyxTheme(darkTheme = uiState.isDarkTheme) {
        if (uiState.isOnboardingActive) {
            OnboardingScreen(
                onComplete = { viewModel.completeOnboarding() },
                onApplyBaseline = { viewModel.applyInitialBaseline() }
            )
        } else if (uiState.isDecoyModeActive) {
            DecoyWeatherScreen(onExitDecoyMode = { viewModel.exitDecoyMode() })
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                phaseAmbientColor,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (!uiState.isLocked) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                CalyxTab.entries.forEach { tab ->
                                    val isSelected = uiState.activeTab == tab
                                    val icon = when (tab) {
                                        CalyxTab.BLUEPRINT -> Icons.Default.Spa
                                        CalyxTab.DETECTIVE -> Icons.Default.AutoAwesome
                                        CalyxTab.CLINICAL -> Icons.Default.Description
                                        CalyxTab.HAVEN -> Icons.Default.Shield
                                    }

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.setTab(tab) },
                                        icon = { Icon(imageVector = icon, contentDescription = stringResource(tab.titleResId)) },
                                        label = { Text(text = stringResource(tab.titleResId)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTextColor = CalyxPrimary,
                                            indicatorColor = CalyxPrimary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.activeTab,
                            transitionSpec = {
                                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (slideInHorizontally(
                                    initialOffsetX = { it / 6 * direction },
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { -it / 6 * direction },
                                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                                        ) + fadeOut(animationSpec = tween(280))
                                    )
                            },
                            label = "tab_screen_transition"
                        ) { tab ->
                            when (tab) {
                                CalyxTab.BLUEPRINT -> HomeScreen(
                                    logs = uiState.logs,
                                    periods = uiState.periods,
                                    onSaveLog = { viewModel.saveLog(it) },
                                    onOpenLog = { viewModel.setShowLogSheet(true) },
                                    onOpenPeriodLogger = { viewModel.setShowPeriodDialog(true) },
                                    onEmergencyLock = { viewModel.lockApp() },
                                    onTriggerNotification = { phase, day -> viewModel.sendDailyHormonalNotification(phase, day) }
                                )
                                CalyxTab.DETECTIVE -> DetectiveScreen(
                                    logs = uiState.logs
                                )
                                CalyxTab.CLINICAL -> ClinicalReportScreen(
                                    logs = uiState.logs,
                                    periods = uiState.periods
                                )
                                CalyxTab.HAVEN -> HavenSecurityScreen(
                                    securitySettings = uiState.securitySettings,
                                    isDarkTheme = uiState.isDarkTheme,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    notificationsEnabled = uiState.notificationsEnabled,
                                    onToggleNotifications = { viewModel.toggleNotifications() },
                                    onTestNotification = { viewModel.sendDailyHormonalNotification() },
                                    onSaveSecurity = { viewModel.saveSecuritySettings(it) },
                                    onClearAllData = { viewModel.clearAllData() },
                                    onTestDecoyMode = { viewModel.activateDecoyMode() },
                                    onSyncToFirestore = { viewModel.syncToFirestore() },
                                    isSyncing = uiState.isSyncing,
                                    syncStatusMessage = uiState.syncStatusMessage,
                                    onReplayOnboarding = { viewModel.showOnboarding() }
                                )
                            }
                        }
                    }
                }

                // PIN Lock Overlay Screen
                AnimatedVisibility(
                    visible = uiState.isLocked,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PinEntryOverlay(
                            onPinEntered = { viewModel.verifyPin(it) },
                            onBiometricUnlock = { viewModel.unlockWithBiometrics() }
                        )
                    }
                }

                // Daily Symptom Logger Bottom Sheet
                if (uiState.showLogSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.setShowLogSheet(false) },
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        LogSymptomScreen(
                            onSaveLog = { viewModel.saveLog(it) },
                            onClose = { viewModel.setShowLogSheet(false) }
                        )
                    }
                }

                // Period & Cycle Intervals Logging Dialog
                if (uiState.showPeriodDialog) {
                    PeriodLoggingDialog(
                        periods = uiState.periods,
                        onDismiss = { viewModel.setShowPeriodDialog(false) },
                        onSavePeriod = {
                            viewModel.savePeriod(it)
                            viewModel.setShowPeriodDialog(false)
                        },
                        onDeletePeriod = { viewModel.deletePeriod(it) }
                    )
                }
            }
        }
    }
}

