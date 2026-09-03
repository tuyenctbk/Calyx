package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.CalyxDarkBg
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxRose
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.util.HapticUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinEntryOverlay(
    onPinEntered: (String) -> Boolean,
    onBiometricUnlock: (() -> Unit)? = null,
    titleText: String = stringResource(R.string.pin_lock_title),
    subtitleText: String = stringResource(R.string.pin_lock_subtitle)
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val defaultErrorMessage = stringResource(R.string.pin_error_incorrect)
    val deleteContentDescription = stringResource(R.string.pin_delete)
    val bioContentDescription = stringResource(R.string.pin_biometric_unlock)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalyxDarkBg)
            .clickable(enabled = false) {}, // Intercept all clicks to prevent background touches
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = CalyxSurface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isError) CalyxRose.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isError) CalyxRose.copy(alpha = 0.2f) else CalyxPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Lock Shield",
                        tint = if (isError) CalyxRose else CalyxPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = if (isError && errorMessage.isNotEmpty()) errorMessage else subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) CalyxRose else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Indicator Dots with Error Animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..4) {
                        val isFilled = enteredPin.length >= i
                        val dotColor by animateColorAsState(
                            targetValue = when {
                                isError -> CalyxRose
                                isFilled -> CalyxPrimary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            label = "dot_color"
                        )

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isError) CalyxRose else if (isFilled) CalyxPrimary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Numeric Keypad
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "DEL")

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in 0..3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0..2) {
                                val key = keys[row * 3 + col]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (key == "BIO") CalyxPrimary.copy(alpha = 0.15f)
                                            else CalyxSurfaceVariant
                                        )
                                        .testTag("pin_key_$key")
                                        .clickable {
                                            HapticUtil.performHeartbeat(context)
                                            isError = false
                                            when (key) {
                                                "BIO" -> {
                                                    onBiometricUnlock?.invoke()
                                                }
                                                "DEL" -> {
                                                    if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                }
                                                else -> {
                                                    if (enteredPin.length < 4) {
                                                        val newPin = enteredPin + key
                                                        enteredPin = newPin
                                                        if (newPin.length == 4) {
                                                            val success = onPinEntered(newPin)
                                                            if (!success) {
                                                                isError = true
                                                                errorMessage = defaultErrorMessage
                                                                HapticUtil.performHeartbeat(context)
                                                                scope.launch {
                                                                    delay(600)
                                                                    enteredPin = ""
                                                                    isError = false
                                                                }
                                                            } else {
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "DEL" -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = deleteContentDescription,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        "BIO" -> {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = bioContentDescription,
                                                tint = CalyxPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SeedPhraseDialog(
    seedPhrase: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val words = seedPhrase.split(" ")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Zero Knowledge Vault",
                    tint = CalyxPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "24-Word Recovery Phrase")
            }
        },
        text = {
            Column {
                Text(
                    text = "This 24-word phrase is your private Zero-Knowledge account key. Calyx does not store this on any server. Keep it safe to restore your data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(words) { index, word ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CalyxPrimary,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(seedPhrase))
                    copied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalyxPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
                Text(text = if (copied) "Copied!" else "Copy Seed Phrase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close Vault")
            }
        }
    )
}
