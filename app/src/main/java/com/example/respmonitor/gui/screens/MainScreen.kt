package com.example.respmonitor.gui.screens

import GyroSample
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.respmonitor.database.JournalEntry
import com.example.respmonitor.database.RespDatabase
import com.example.respmonitor.gui.navigation.Screen
import com.example.respmonitor.gui.viewmodel.JournalViewModel
import com.example.respmonitor.gui.viewmodel.MonitoringViewModel
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.processing.PositionClassifier
import com.example.respmonitor.processing.SignalAnalyzer
import com.example.respmonitor.sensor.AccelSample
import com.example.respmonitor.sensor.AccelerometerManager
import com.example.respmonitor.sensor.GyroscopeManager
import com.example.respmonitor.util.FloatCircularBuffer
import com.example.respmonitor.util.HapticFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    gyroManager: GyroscopeManager,
    accelManager: AccelerometerManager,
    gyroInterpolator: Interpolator<GyroSample>,
    accelInterpolator: Interpolator<AccelSample>,
    filterPitch: ButterworthFilter,
    signalAnalyzer: SignalAnalyzer,
    viewModel: MonitoringViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(Screen.Home.route) }
    var isMonitoring by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = remember { RespDatabase.getDatabase(context) }
    val dao = database.journalDao()

    val journalViewModel: JournalViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return JournalViewModel(dao) as T
            }
        }
    )

    val entries by journalViewModel.entries.collectAsStateWithLifecycle()

    if (isMonitoring) {
        val context = LocalContext.current
        val mlClassifier = remember { PositionClassifier(context) }
        val mlBuffer = remember { FloatCircularBuffer(600) }
        val signalBuffer = remember { FloatCircularBuffer(1500) }

        var lastAccel by remember { mutableStateOf(AccelSample(0L, 0f, 0f, 0f)) }
        var isPositionValid by remember { mutableStateOf(false) }
        var breathsPerMinute by remember { mutableStateOf<Float?>(null) }
        var isCalculating by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("Validating position...") }
        var invalidCounter by remember { mutableIntStateOf(0) }
        val invalidThreshold = 3


        DisposableEffect(gyroManager, accelManager) {
            accelManager.onAccelSample = { sample ->
                lastAccel = sample
                accelInterpolator.onNewSample(sample)
            }
            gyroManager.onGyroSample = { gyroInterpolator.onNewSample(it) }
            gyroInterpolator.onInterpolatedSample = { g ->
                val filteredPitch = filterPitch.process(g.pitch)
                mlBuffer.add(g.pitch); mlBuffer.add(g.roll); mlBuffer.add(g.yaw)
                mlBuffer.add(lastAccel.x); mlBuffer.add(lastAccel.y); mlBuffer.add(lastAccel.z)
                if (isPositionValid) { signalBuffer.add(filteredPitch) }
            }
            accelManager.start(); gyroManager.start()
            onDispose {
                gyroManager.stop(); accelManager.stop(); mlClassifier.close()
            }
        }

        LaunchedEffect(Unit) {
            delay(1000)
            while (isActive) {
                if (mlBuffer.isFull() && !isCalculating) {
                    isCalculating = true
                    val mlResult = withContext(Dispatchers.Default) {
                        try { mlClassifier.classify(mlBuffer.toFloatArray()) } catch (e: Exception) { null }
                    }
                    mlResult?.let { result ->
                        val wasValid = isPositionValid
                        val currentMlValid = result.label == "valid" && result.confidence > 0.7f
                        if (currentMlValid) {
                            invalidCounter = 0; isPositionValid = true
                            statusMessage = "Position correct"
                        } else {
                            invalidCounter++
                            if (invalidCounter >= invalidThreshold) {
                                if (wasValid) { HapticFeedback.vibrateDouble(context) }
                                isPositionValid = false
                                signalBuffer.clear()
                                breathsPerMinute = null
                            }
                            statusMessage = "Please hold your phone correctly"
                        }
                    }
                    isCalculating = false
                }
                delay(500)
            }
        }

        LaunchedEffect(isPositionValid) {
            while (isActive) {
                if (isPositionValid && signalBuffer.isFull() && !isCalculating) {
                    isCalculating = true
                    val result = withContext(Dispatchers.Default) {
                        try { signalAnalyzer.findRespiratoryRate(signalBuffer, 100f, 6f, 40f) } catch (e: Exception) { null }
                    }
                    result?.let { bpm -> breathsPerMinute = signalAnalyzer.calculateEMA(bpm, breathsPerMinute ?: 0f) }
                    isCalculating = false
                }
                delay(200)
            }
        }

        MonitoringScreen(
            isPositionValid = isPositionValid,
            breathsPerMinute = breathsPerMinute,
            statusMessage = statusMessage,
            onBackClick = {
                breathsPerMinute?.let { viewModel.updateLastMeasurement(it) }
                isMonitoring = false
            },
            viewModel = viewModel
        )

    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(Screen.Home, Screen.Journal)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = selectedTab == screen.route,
                            onClick = { selectedTab = screen.route },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00BCD4),
                                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    Screen.Home.route -> {
                        val context = LocalContext.current

                        WelcomeScreen(
                            onStartClick = { isMonitoring = true },
                            lastBreathsPerMinute = viewModel.lastMeasurement,
                            onSaveToJournal = { customNote ->
                                val current = java.time.LocalDateTime.now()
                                val formatterDate = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy.")
                                val formatterTime = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

                                val dateNow = current.format(formatterDate)
                                val timeNow = current.format(formatterTime)

                                val entry = JournalEntry(
                                    date = dateNow,
                                    time = timeNow,
                                    breathsPerMinute = viewModel.lastMeasurement ?: 0f,
                                    note = customNote.ifBlank { "I was out of breath to think of anything" }
                                )

                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try {
                                        dao.insertEntry(entry)
                                        Log.d("DATABASE", "Saved to database: $entry")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                viewModel.clearMeasurement()
                            },
                            onDismiss = {
                                viewModel.clearMeasurement()
                            },
                            onSeeAllClick = {
                                selectedTab = Screen.Journal.route
                            },
                            entries = entries
                        )
                    }

                    Screen.Journal.route -> {
                        val context = LocalContext.current
                        val database = remember { RespDatabase.getDatabase(context) }
                        val dao = database.journalDao()

                        val journalViewModel: JournalViewModel = viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return JournalViewModel(dao) as T
                                }
                            }
                        )

                        JournalScreen(viewModel = journalViewModel)
                    }

                }
            }
        }
    }
}