package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CyclePeriodEntity
import com.example.ui.theme.CalyxPrimary
import com.example.ui.theme.CalyxRose
import com.example.ui.theme.CalyxSecondary
import com.example.ui.theme.CalyxSurface
import com.example.ui.theme.CalyxSurfaceVariant
import com.example.util.HapticUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PeriodLoggingDialog(
    periods: List<CyclePeriodEntity>,
    onDismiss: () -> Unit,
    onSavePeriod: (CyclePeriodEntity) -> Unit,
    onDeletePeriod: (Long) -> Unit
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { sdf.format(Date()) }

    var startDateInput by remember { mutableStateOf(todayStr) }
    var periodDurationDays by remember { mutableIntStateOf(5) }
    var cycleLengthDays by remember { mutableIntStateOf(28) }
    var isNewPeriodMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CalyxRose.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = CalyxRose,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MENSTRUAL CYCLE LOG",
                                style = MaterialTheme.typography.labelSmall,
                                color = CalyxRose,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Period & Cycle Intervals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isNewPeriodMode) {
                    // Quick Action: Add New Period Start Date
                    Button(
                        onClick = {
                            HapticUtil.performHeartbeat(context)
                            isNewPeriodMode = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CalyxRose),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_new_period_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Period Start Date")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Historical Cycle Intervals",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of logged periods
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (periods.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CalyxSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No period history recorded yet. Tap 'Log Period Start Date' above to record your latest cycle.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(periods.sortedByDescending { it.startDate }) { period ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CalyxSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = CalyxRose,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Start: ${period.startDate}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "${period.cycleLengthDays} days cycle • ${period.periodLengthDays} days bleeding",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                HapticUtil.performHeartbeat(context)
                                                onDeletePeriod(period.id)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // New Period Entry Form
                    Text(
                        text = "Period Start Date (YYYY-MM-DD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = { startDateInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("period_start_date_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CalyxRose)
                        }
                    )

                    // Quick Date Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cal = Calendar.getInstance()
                        val today = sdf.format(cal.time)
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterday = sdf.format(cal.time)
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        val twoDaysAgo = sdf.format(cal.time)

                        listOf("Today" to today, "Yesterday" to yesterday, "2 Days Ago" to twoDaysAgo).forEach { (label, date) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (startDateInput == date) CalyxRose.copy(alpha = 0.2f) else CalyxSurfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        HapticUtil.performHeartbeat(context)
                                        startDateInput = date
                                    }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = if (startDateInput == date) CalyxRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Period Bleeding Duration Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bleeding Duration", style = MaterialTheme.typography.labelMedium)
                        Text("$periodDurationDays Days", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CalyxRose)
                    }

                    Slider(
                        value = periodDurationDays.toFloat(),
                        onValueChange = { periodDurationDays = it.toInt() },
                        valueRange = 2f..10f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = CalyxRose,
                            activeTrackColor = CalyxRose
                        )
                    )

                    // Total Cycle Length Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expected Cycle Length", style = MaterialTheme.typography.labelMedium)
                        Text("$cycleLengthDays Days", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CalyxPrimary)
                    }

                    Slider(
                        value = cycleLengthDays.toFloat(),
                        onValueChange = { cycleLengthDays = it.toInt() },
                        valueRange = 20f..40f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = CalyxPrimary,
                            activeTrackColor = CalyxPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isNewPeriodMode = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back", color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = {
                                HapticUtil.performHeartbeat(context)
                                val calEnd = Calendar.getInstance()
                                try {
                                    val parsedStart = sdf.parse(startDateInput)
                                    if (parsedStart != null) calEnd.time = parsedStart
                                } catch (e: Exception) {
                                    // Keep current
                                }
                                calEnd.add(Calendar.DAY_OF_YEAR, periodDurationDays)
                                val calculatedEndDate = sdf.format(calEnd.time)

                                onSavePeriod(
                                    CyclePeriodEntity(
                                        startDate = startDateInput,
                                        endDate = calculatedEndDate,
                                        cycleLengthDays = cycleLengthDays,
                                        periodLengthDays = periodDurationDays
                                    )
                                )
                                isNewPeriodMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CalyxRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_period_entry_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                    }
                }
            }
        },
        containerColor = CalyxSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
