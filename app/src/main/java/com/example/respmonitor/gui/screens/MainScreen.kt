package com.example.respmonitor.gui.screens

import GyroSample
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(gyroManager: GyroscopeManager, accelManager: AccelerometerManager, gyroInterpolator: Interpolator<GyroSample>, accelInterpolator: Interpolator<AccelSample>, filterPitch: ButterworthFilter, signalAnalyzer: SignalAnalyzer) {
    var hasStarted by remember { mutableStateOf(false) }

    if (!hasStarted) {
        WelcomeScreen(onStartClick = { hasStarted = true })
    } else {
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
        val INVALID_THRESHOLD = 3

        DisposableEffect(gyroManager, accelManager) {
            accelManager.onAccelSample = { sample ->
                lastAccel = sample
                accelInterpolator.onNewSample(sample)
            }

            gyroManager.onGyroSample = { gyroInterpolator.onNewSample(it) }

            gyroInterpolator.onInterpolatedSample = { g ->
                val filteredPitch = filterPitch.process(g.pitch)
                mlBuffer.add(g.pitch)
                mlBuffer.add(g.roll)
                mlBuffer.add(g.yaw)
                mlBuffer.add(lastAccel.x)
                mlBuffer.add(lastAccel.y)
                mlBuffer.add(lastAccel.z)

                if (isPositionValid) {
                    signalBuffer.add(filteredPitch)
                }
            }

            accelManager.start()
            gyroManager.start()

            onDispose {
                gyroManager.stop()
                accelManager.stop()
                mlClassifier.close()
            }
        }

        LaunchedEffect(Unit) {
            delay(1000)
            while (isActive) {
                if (mlBuffer.isFull() && !isCalculating) {
                    isCalculating = true

                    val mlResult = withContext(Dispatchers.Default) {
                        try {
                            val data = mlBuffer.toFloatArray()
                            mlClassifier.classify(data)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    mlResult?.let { result ->
                        val wasValid = isPositionValid
                        val currentMlValid = result.label == "valid" && result.confidence > 0.7f

                        if (currentMlValid) {
                            invalidCounter = 0
                            isPositionValid = true
                            statusMessage = "Position correct - Measuring..."

                        } else {
                            invalidCounter++
                            if (invalidCounter >= INVALID_THRESHOLD) {
                                if (wasValid) { HapticFeedback.vibrateDouble(context) }
                                isPositionValid = false
                                signalBuffer.clear()
                                breathsPerMinute = null
                            }

                            statusMessage = "Hold phone to your chest now"
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
                        try {
                            signalAnalyzer.findRespiratoryRate(signalBuffer, 100f, 6f, 40f)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    result?.let { bpm ->
                        breathsPerMinute = signalAnalyzer.calculateEMA(bpm, breathsPerMinute ?: 0f)
                    }
                    isCalculating = false
                }
                delay(200)
            }
        }

        MonitoringScreen(
            isPositionValid = isPositionValid,
            breathsPerMinute = breathsPerMinute,
            statusMessage = statusMessage,
            onBackClick = { hasStarted = false }
        )

    }


}

