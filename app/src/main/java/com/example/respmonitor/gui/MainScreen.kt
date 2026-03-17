package com.example.respmonitor.gui

import GyroSample
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.processing.PositionClassifier
import com.example.respmonitor.processing.SignalAnalyzer
import com.example.respmonitor.sensor.AccelSample
import com.example.respmonitor.sensor.AccelerometerManager
import com.example.respmonitor.sensor.GyroscopeManager
import com.example.respmonitor.util.FloatCircularBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(
    gyroManager: GyroscopeManager,
    accelManager: AccelerometerManager,
    gyroInterpolator: Interpolator<GyroSample>,
    accelInterpolator: Interpolator<AccelSample>,
    filterPitch: ButterworthFilter,
    signalAnalyzer: SignalAnalyzer
) {
    val context = LocalContext.current

    val mlClassifier = remember { PositionClassifier(context) }
    val mlBuffer = remember { FloatCircularBuffer(600) }
    val signalBuffer = remember { FloatCircularBuffer(3000) }

    var lastAccel by remember { mutableStateOf(AccelSample (timestampNs = 0L, x = 0f, y = 0f, z = 0f)) }

    var isPositionValid by remember { mutableStateOf(false) }
    var positionConfidence by remember { mutableFloatStateOf(0f) }
    var breathsPerMinute by remember { mutableStateOf<Float?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Initializing...") }


    DisposableEffect(gyroManager, accelManager) {
        Log.d("MainScreen", "═══ Setting up sensors ═══")

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
                        Log.e("MainScreen", "ML Error: ${e.message}", e)
                        null
                    }
                }

                mlResult?.let { result ->
                    val wasValid = isPositionValid
                    isPositionValid = result.label == "valid" && result.confidence > 0.7f
                    positionConfidence = result.confidence

                    Log.d("MainScreen", "═══════════════════════════════════════")
                    Log.d("MainScreen", "ML RESULT:")
                    Log.d("MainScreen", "  Label: ${result.label}")
                    Log.d("MainScreen", "  Confidence: ${(result.confidence * 100).toInt()}%")
                    Log.d("MainScreen", "  Valid score: ${String.format("%.4f", result.validScore)}")
                    Log.d("MainScreen", "  Invalid score: ${String.format("%.4f", result.invalidScore)}")
                    Log.d("MainScreen", "  Position Valid: $isPositionValid")
                    Log.d("MainScreen", "═══════════════════════════════════════")

                    statusMessage = if (isPositionValid) {
                        "Position correct - Measuring..."
                    } else {
                        "Please place phone on chest"
                    }

                    if (wasValid && !isPositionValid) {
                        signalBuffer.clear()
                        breathsPerMinute = null
                    }
                }

                isCalculating = false
            }

            delay(2000)
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
                        Log.e("MainScreen", "BPM Error: ${e.message}", e)
                        null
                    }
                }
                result?.let { bpm ->
                    breathsPerMinute = signalAnalyzer.calculateEMA(bpm, breathsPerMinute ?: 0f)
                }
                isCalculating = false
            }
            delay(500)
        }
    }


    DrawGui(isPositionValid, positionConfidence, breathsPerMinute, statusMessage)
}


@Composable
fun DrawGui(
    isPositionValid: Boolean,
    positionConfidence: Float,
    breathsPerMinute: Float?,
    statusMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPositionValid)
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                else
                    Color(0xFFF44336).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isPositionValid) "✓ POSITION CORRECT" else "✗ INCORRECT POSITION",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositionValid) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Confidence: ${(positionConfidence * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isPositionValid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = breathsPerMinute?.let { "%.1f".format(it) } ?: "--",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "breaths per minute",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "📱 How to Position:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text("1. Lie down or sit comfortably", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Place phone flat on your chest", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. Screen facing up", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4. Keep still and breathe normally", fontSize = 14.sp)
                }
            }
        }
    }
}