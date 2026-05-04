package com.example.respmonitor.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.respmonitor.database.JournalEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyStatsCard(
    entries: List<JournalEntry>,
    modifier: Modifier = Modifier
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    fun parseEntryDate(dateObj: Any?): java.util.Date? {
        if (dateObj == null) return null
        return when (dateObj) {
            is java.util.Date -> dateObj
            is Long -> java.util.Date(dateObj)
            is String -> {
                val formats = listOf(
                    SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                )
                var parsedDate: java.util.Date? = null
                for (format in formats) {
                    try {
                        parsedDate = format.parse(dateObj)
                        if (parsedDate != null) break
                    } catch (e: Exception) {
                    }
                }
                parsedDate
            }
            else -> null
        }
    }

    val weeklyData = remember(entries) {
        val calendar = Calendar.getInstance()
        val dailySums = FloatArray(7) { 0f }
        val dailyCounts = IntArray(7) { 0 }

        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diff = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2
        cal.add(Calendar.DAY_OF_MONTH, -diff)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeekMillis = cal.timeInMillis

        cal.add(Calendar.DAY_OF_MONTH, 7)
        val endOfWeekMillis = cal.timeInMillis

        entries.forEach { entry ->
            try {
                val entryDate = parseEntryDate(entry.date)

                if (entryDate != null) {
                    val entryTime = entryDate.time

                    if (entryTime >= startOfWeekMillis && entryTime < endOfWeekMillis) {
                        calendar.time = entryDate
                        val entryDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val index = if (entryDayOfWeek == Calendar.SUNDAY) 6 else entryDayOfWeek - 2

                        if (index in 0..6) {
                            dailySums[index] += entry.breathsPerMinute.toFloat()
                            dailyCounts[index]++
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        days.mapIndexed { index, day ->
            val count = dailyCounts[index]
            val average = if (count > 0) dailySums[index] / count else null
            Pair(day, average)
        }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Kalendar",
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Weekly Average",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .height(100.dp)
                        .padding(end = 12.dp, bottom = 26.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "30",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "15",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "0",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { (day, avgValue) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.height(100.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (avgValue != null) {
                                    val maxHeight = 100.dp
                                    val fraction = (avgValue / 30f).coerceIn(0f, 1f)

                                    val barHeight = if (avgValue > 0f) {
                                        maxHeight * fraction
                                    } else {
                                        4.dp
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height(barHeight)
                                            .background(
                                                color = Color(0xFF00BCD4),
                                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}