package com.example.respmonitor.gui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onSave: (bpm: Float, time: String, date: String, note: String) -> Unit
) {
    var bpmText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val current = LocalDateTime.now()
        dateText = current.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))
        timeText = current.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new entry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = bpmText,
                    onValueChange = { bpmText = it },
                    label = { Text("BPM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date") }
                )
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Time") }
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val bpmVal = bpmText.toFloatOrNull()
                    if (bpmVal != null) {
                        onSave(bpmVal, timeText, dateText, noteText)
                        onDismiss()
                    }
                }
            ) {
                Text("Save", color = Color(0xFF00BCD4))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}