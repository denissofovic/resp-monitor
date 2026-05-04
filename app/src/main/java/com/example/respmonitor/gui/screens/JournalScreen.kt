package com.example.respmonitor.gui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.respmonitor.database.JournalEntry
import com.example.respmonitor.gui.components.AddEntryDialog
import com.example.respmonitor.gui.components.JournalEntryCard
import com.example.respmonitor.gui.viewmodel.JournalViewModel
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import java.time.Instant
import java.time.ZoneId


@OptIn(ExperimentalTime::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalScreen(viewModel: JournalViewModel) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        selectedDate = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF00BCD4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showDatePicker = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Choose Date",
                        tint = Color(0xFF00BCD4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    val currentDateForDay = java.time.LocalDate.now()
                        .minusDays(java.time.LocalDate.now().dayOfWeek.value.toLong() - 1)
                        .plusDays(index.toLong())

                    val dateString = currentDateForDay.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))

                    DayItem(
                        day = day,
                        date = currentDateForDay.dayOfMonth.toString(),
                        isSelected = selectedDate == dateString,
                        onClick = { selectedDate = dateString }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MEASUREMENTS FOR $selectedDate",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredEntries = entries.filter { it.date == selectedDate }

                if (filteredEntries.isEmpty()) {
                    item {
                        Text(
                            text = "No measurements for this day.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filteredEntries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onDelete = { selectedEntry ->
                                viewModel.deleteEntry(selectedEntry)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEntryDialog(
            onDismiss = { showDialog = false },
            onSave = { bpm, time, date, note ->
                viewModel.addEntry(
                    JournalEntry(
                        breathsPerMinute = bpm,
                        time = time,
                        date = date,
                        note = note.ifBlank { "Manual entry" }
                    )
                )
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePicker@ {
                            datePickerState.selectedDateMillis?.let {
                                val selectedDateObj = Instant.ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                selectedDate = selectedDateObj.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))
                            }
                        }()
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm", color = Color(0xFF00BCD4))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



@Composable
fun DayItem(
    day: String,
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                color = if (isSelected) Color(0xFF00BCD4).copy(alpha = 0.1f) else Color.Transparent
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF00BCD4) else Color.Gray
        )
        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF00BCD4) else MaterialTheme.colorScheme.onBackground
        )
    }
}

