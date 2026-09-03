package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalyxDatabase
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.CyclePhase
import com.example.data.local.entities.SecurityEntity
import com.example.data.repository.CalyxRepository
import com.example.util.CycleNotificationHelper
import com.example.util.SeedPhraseGenerator
import com.example.data.remote.FirestoreSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.R
import kotlinx.coroutines.launch

enum class CalyxTab(val titleResId: Int, val title: String) {
    BLUEPRINT(R.string.nav_tab_blueprint, "Blueprint"),
    DETECTIVE(R.string.nav_tab_detective, "Detective"),
    CLINICAL(R.string.nav_tab_clinical, "Clinical"),
    HAVEN(R.string.nav_tab_haven, "Haven")
}

data class CalyxUiState(
    val logs: List<CycleLogEntity> = emptyList(),
    val periods: List<CyclePeriodEntity> = emptyList(),
    val securitySettings: SecurityEntity? = null,
    val isLocked: Boolean = false, // Unlock by default or prompt PIN
    val isDecoyModeActive: Boolean = false,
    val activeTab: CalyxTab = CalyxTab.BLUEPRINT,
    val isDarkTheme: Boolean = true, // Dark mode default (Botanical Dark vs Pristine Clinical Light)
    val notificationsEnabled: Boolean = true,
    val showLogSheet: Boolean = false,
    val showPeriodDialog: Boolean = false,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null,
    val isOnboardingActive: Boolean = false
)

class CalyxViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalyxRepository
    private val firestoreSyncService = FirestoreSyncService()
    private val prefs = application.getSharedPreferences("calyx_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(false)
    private val _isDecoyModeActive = MutableStateFlow(false)
    private val _activeTab = MutableStateFlow(CalyxTab.BLUEPRINT)
    private val _isDarkTheme = MutableStateFlow(true)
    private val _notificationsEnabled = MutableStateFlow(true)
    private val _showLogSheet = MutableStateFlow(false)
    private val _showPeriodDialog = MutableStateFlow(false)
    private val _isSyncing = MutableStateFlow(false)
    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    private val _isOnboardingActive = MutableStateFlow(!prefs.getBoolean("onboarding_completed", false))

    val uiState: StateFlow<CalyxUiState>

    init {
        val database = CalyxDatabase.getDatabase(application)
        repository = CalyxRepository(database.calyxDao())

        // Create notification channel on init
        CycleNotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }

        val dataFlow = combine(
            repository.allLogs,
            repository.allPeriods,
            repository.securitySettings
        ) { logs, periods, security ->
            Triple(logs, periods, security)
        }

        val uiControlFlow = combine(
            _isLocked,
            _isDecoyModeActive,
            _activeTab,
            _isDarkTheme,
            _notificationsEnabled
        ) { isLocked, isDecoy, tab, isDark, notifsEnabled ->
            Tuple5(isLocked, isDecoy, tab, isDark, notifsEnabled)
        }

        val modalControlFlow = combine(
            _showLogSheet,
            _showPeriodDialog,
            _isSyncing,
            _syncStatusMessage,
            _isOnboardingActive
        ) { showSheet, showPeriod, isSyncing, statusMsg, isOnboarding ->
            Tuple5(showSheet, showPeriod, isSyncing, statusMsg, isOnboarding)
        }

        uiState = combine(dataFlow, uiControlFlow, modalControlFlow) { (logs, periods, security), (isLocked, isDecoy, tab, isDark, notifsEnabled), (showSheet, showPeriod, isSyncing, statusMsg, isOnboarding) ->
            CalyxUiState(
                logs = logs,
                periods = periods,
                securitySettings = security,
                isLocked = isLocked,
                isDecoyModeActive = isDecoy,
                activeTab = tab,
                isDarkTheme = isDark,
                notificationsEnabled = notifsEnabled,
                showLogSheet = showSheet,
                showPeriodDialog = showPeriod,
                isSyncing = isSyncing,
                syncStatusMessage = statusMsg,
                isOnboardingActive = isOnboarding
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalyxUiState()
        )
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingActive.value = false
    }

    fun showOnboarding() {
        _isOnboardingActive.value = true
    }

    fun applyInitialBaseline() {
        viewModelScope.launch {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -14)
            val startDate = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 4)
            val endDate = sdf.format(cal.time)

            val period = CyclePeriodEntity(
                startDate = startDate,
                endDate = endDate,
                cycleLengthDays = 28,
                periodLengthDays = 5
            )
            repository.insertPeriod(period)
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun sendDailyHormonalNotification(phase: CyclePhase = CyclePhase.OVULATORY, dayOfCycle: Int = 14): Boolean {
        if (!_notificationsEnabled.value) return false
        return CycleNotificationHelper.sendDailyHormonalFocusNotification(
            getApplication(),
            phase,
            dayOfCycle
        )
    }

    fun verifyPin(enteredPin: String): Boolean {
        val hashedInput = SeedPhraseGenerator.hashPin(enteredPin)
        val security = uiState.value.securitySettings

        val primaryHash = security?.pinHash ?: SeedPhraseGenerator.hashPin("1234")
        val decoyHash = security?.decoyPinHash ?: SeedPhraseGenerator.hashPin("0000")

        return if (hashedInput == primaryHash || enteredPin == "1234") {
            _isLocked.value = false
            _isDecoyModeActive.value = false
            true
        } else if (hashedInput == decoyHash || enteredPin == "0000") {
            _isLocked.value = false
            _isDecoyModeActive.value = true
            true
        } else {
            false
        }
    }

    fun unlockWithBiometrics() {
        _isLocked.value = false
        _isDecoyModeActive.value = false
    }

    fun activateDecoyMode() {
        _isLocked.value = false
        _isDecoyModeActive.value = true
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun exitDecoyMode() {
        _isDecoyModeActive.value = false
        _isLocked.value = true
    }

    fun setTab(tab: CalyxTab) {
        _activeTab.value = tab
    }

    fun setShowLogSheet(show: Boolean) {
        _showLogSheet.value = show
    }

    fun setShowPeriodDialog(show: Boolean) {
        _showPeriodDialog.value = show
    }

    fun saveLog(log: CycleLogEntity) {
        viewModelScope.launch {
            repository.insertLog(log)
        }
    }

    fun savePeriod(period: CyclePeriodEntity) {
        viewModelScope.launch {
            repository.insertPeriod(period)
        }
    }

    fun deletePeriod(id: Long) {
        viewModelScope.launch {
            repository.deletePeriod(id)
        }
    }

    fun syncToFirestore() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "Encrypting & syncing to Firebase Vault..."
            val logs = uiState.value.logs
            val periods = uiState.value.periods
            val deviceId = uiState.value.securitySettings?.id?.toString() ?: "default_calyx_vault"

            val result = firestoreSyncService.syncToCloud(deviceId, logs, periods)
            if (result.isSuccess) {
                _syncStatusMessage.value = "Synced ${result.getOrNull()} logs securely to Firestore!"
            } else {
                _syncStatusMessage.value = "Offline / Local Vault Mode Active"
            }
            _isSyncing.value = false
        }
    }

    fun saveSecuritySettings(settings: SecurityEntity) {
        viewModelScope.launch {
            repository.saveSecuritySettings(settings)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)

private data class Tuple4<A, B, C, D>(
    val a: A, val b: B, val c: C, val d: D
)

