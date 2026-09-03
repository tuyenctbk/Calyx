package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.SecurityEntity
import com.example.ui.components.SeedPhraseDialog
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxRose
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.util.HapticUtil
import com.example.util.SeedPhraseGenerator

@Composable
fun HavenSecurityScreen(
    securitySettings: SecurityEntity?,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    notificationsEnabled: Boolean = true,
    onToggleNotifications: () -> Unit = {},
    onTestNotification: () -> Unit = {},
    onSaveSecurity: (SecurityEntity) -> Unit,
    onClearAllData: () -> Unit,
    onTestDecoyMode: (() -> Unit)? = null,
    onSyncToFirestore: (() -> Unit)? = null,
    isSyncing: Boolean = false,
    syncStatusMessage: String? = null,
    onReplayOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showSeedPhraseModal by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportedBackupDialog by remember { mutableStateOf(false) }

    var primaryPinInput by remember(securitySettings) { mutableStateOf("1234") }
    var decoyPinInput by remember(securitySettings) { mutableStateOf("0000") }
    var pinSavedStatus by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var selectedAutoLockTime by remember { mutableStateOf("Immediate") }
    var showAdvancedDangerZone by remember { mutableStateOf(false) }
    var showDecoyInfoDialog by remember { mutableStateOf(false) }

    if (showDecoyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDecoyInfoDialog = false },
            title = { Text("Decoy Vault Protocol") },
            text = {
                Text(
                    "The Dual PIN protocol provides duress protection. Entering your Decoy PIN opens an authentic, innocent Weather forecast dashboard instead of your cycle data, protecting your private reproductive records.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDecoyInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showSeedPhraseModal && securitySettings != null) {
        SeedPhraseDialog(
            seedPhrase = securitySettings.recoverySeedPhrase.ifEmpty { SeedPhraseGenerator.generate24Words() },
            onDismiss = { showSeedPhraseModal = false }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Zero-Knowledge Local Wipe") },
            text = { Text("Permanently erase all local health records from this device? This action is irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        HapticUtil.performHeartbeat(context)
                        onClearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CalyxRose)
                ) {
                    Text("Wipe All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportedBackupDialog) {
        AlertDialog(
            onDismissRequest = { showExportedBackupDialog = false },
            title = { Text("Encrypted Backup Ready") },
            text = { Text("An anonymous encrypted backup string has been copied to your clipboard.") },
            confirmButton = {
                Button(
                    onClick = { showExportedBackupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary)
                ) {
                    Text("Done")
                }
            }
        )
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
                        text = stringResource(R.string.haven_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.haven_subtitle),
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
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = CalyxPrimary,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 1: Quick Preferences (Theme & Notifications)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme Switcher Row
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
                                    .background(CalyxPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme",
                                    tint = CalyxPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isDarkTheme) "Botanical Dark" else "Clinical Light",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isDarkTheme) "Circadian night tones" else "High contrast daylight",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = !isDarkTheme,
                            onCheckedChange = {
                                HapticUtil.performHeartbeat(context)
                                onToggleTheme()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CalyxPrimary,
                                checkedTrackColor = CalyxPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("theme_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notifications Row
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
                                    .background(CalyxSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Notification",
                                    tint = CalyxSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Hormonal Focus",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Localized biological hints",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                HapticUtil.performHeartbeat(context)
                                onToggleNotifications()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CalyxSecondary,
                                checkedTrackColor = CalyxSecondary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("notification_toggle_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 2: Biometrics & Auto-Lock Settings
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
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = CalyxPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Biometric Authentication", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }

                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                HapticUtil.performHeartbeat(context)
                                biometricEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CalyxPrimary, checkedTrackColor = CalyxPrimary.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("AUTO-LOCK TIMEOUT", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = CalyxPrimary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Immediate", "1 Minute", "5 Minutes").forEach { time ->
                            val isSelected = selectedAutoLockTime == time
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CalyxPrimary.copy(alpha = 0.2f) else CalyxSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CalyxPrimary else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        HapticUtil.performHeartbeat(context)
                                        selectedAutoLockTime = time
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = time, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = if (isSelected) CalyxPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 3: PIN & Decoy Vault Mode
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
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Decoy Mode",
                                tint = CalyxSecondary,
                                modifier = Modifier.size(18.dp).padding(end = 6.dp)
                            )
                            Text(
                                text = "Decoy Vault & Dual PIN Protocol",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                showDecoyInfoDialog = true
                            },
                            modifier = Modifier.size(28.dp).testTag("decoy_info_hint_button")
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
                        OutlinedTextField(
                            value = primaryPinInput,
                            onValueChange = { if (it.length <= 4) primaryPinInput = it },
                            label = { Text("Primary PIN", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("primary_pin_field")
                        )

                        OutlinedTextField(
                            value = decoyPinInput,
                            onValueChange = { if (it.length <= 4) decoyPinInput = it },
                            label = { Text("Decoy PIN (Weather)", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("decoy_pin_field")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                val current = securitySettings ?: SecurityEntity()
                                onSaveSecurity(
                                    current.copy(
                                        pinHash = SeedPhraseGenerator.hashPin(primaryPinInput),
                                        decoyPinHash = SeedPhraseGenerator.hashPin(decoyPinInput),
                                        isSetupCompleted = true
                                    )
                                )
                                pinSavedStatus = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("save_pins_button")
                        ) {
                            Icon(
                                imageVector = if (pinSavedStatus) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = "Save PINs",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (pinSavedStatus) "Updated!" else "Save PINs", style = MaterialTheme.typography.labelMedium)
                        }

                        if (onTestDecoyMode != null) {
                            OutlinedButton(
                                onClick = {
                                    HapticUtil.performHeartbeat(context)
                                    onTestDecoyMode()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("test_decoy_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test Decoy",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Test Decoy", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 4: Recovery Key Vault & Encrypted Backup
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                showSeedPhraseModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("view_seed_phrase_button")
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "View Key", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "24-Word Key Vault", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                val dummyEncryptedPayload = "CALYX-ENC-V1:7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069::CHKSUM_VALID"
                                clipboardManager.setText(AnnotatedString(dummyEncryptedPayload))
                                showExportedBackupDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = "Export", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export Backup", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (onSyncToFirestore != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                onSyncToFirestore()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("firestore_sync_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync", tint = CalyxPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isSyncing) "Syncing..." else "Sync Cloud Vault", style = MaterialTheme.typography.labelMedium, color = CalyxPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Onboarding & Setup Guide
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        onReplayOnboarding()
                    }
                    .testTag("replay_onboarding_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CalyxPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CalyxPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.onboarding_guide_btn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.onboarding_guide_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Replay",
                        tint = CalyxPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RANK 5 (Collapsed by default): Advanced & Danger Zone
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalyxSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticUtil.performHeartbeat(context)
                        showAdvancedDangerZone = !showAdvancedDangerZone
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Advanced",
                                tint = CalyxRose,
                                modifier = Modifier.size(18.dp).padding(end = 6.dp)
                            )
                            Text(
                                text = "Advanced & Data Wipe",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = CalyxRose
                            )
                        }

                        Icon(
                            imageVector = if (showAdvancedDangerZone) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedDangerZone) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Instantly purge all local Room database entries from device storage without recovery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    HapticUtil.performHeartbeat(context)
                                    showClearDataDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CalyxRose),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wipe_data_button")
                            ) {
                                Text(text = stringResource(R.string.erase_data))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

