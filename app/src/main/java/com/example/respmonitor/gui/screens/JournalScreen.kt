package com.example.respmonitor.gui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class JournalEntry(
    val id: Int,
    val bpm: Float,
    val time: String,
    val date: String, // yyyy-mm-dd
    val note: String = ""
)
@Composable
fun JournalScreen() {
    val entries = remember {
        mutableStateListOf(
            JournalEntry(1, 14.5f, "08:30", "2026-05-04", "Morning session"),
            JournalEntry(2, 16.2f, "22:15", "2026-05-04", "Before sleep"),
            JournalEntry(3, 15.0f, "09:00", "2026-05-03")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Ovdje bi išao dijalog za ručni unos */ },
                containerColor = Color(0xFF00BCD4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "May 2026",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    DayItem(day = day, date = (index + 1).toString(), isSelected = index == 3)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "TODAY'S READINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries.filter { it.date == "2026-05-04" }) { entry ->
                    JournalEntryCard(entry)
                }
            }
        }
    }
}



@Composable
fun DayItem(day: String, date: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(45.dp)
            .background(
                color = if (isSelected) Color(0xFF00BCD4).copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        Text(text = day, fontSize = 12.sp, color = if (isSelected) Color(0xFF00BCD4) else Color.Gray)
        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF00BCD4) else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFF00BCD4).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00BCD4), modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${entry.bpm} BPM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.time,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (entry.note.isNotEmpty()) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
            }
        }
    }
}