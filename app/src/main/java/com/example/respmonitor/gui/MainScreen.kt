package com.example.respmonitor.gui

import GyroSample
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.processing.SignalAnalyzer
import com.example.respmonitor.sensor.GyroscopeManager
import com.example.respmonitor.util.FloatCircularBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun MainScreen(
    gyroManager: GyroscopeManager,
    gyroInterpolator: Interpolator<GyroSample>,
    filterPitch: ButterworthFilter,
    filterRoll: ButterworthFilter,
    filterYaw: ButterworthFilter,
    signalAnalyzer: SignalAnalyzer
) {

    val signalBuffer = remember { FloatCircularBuffer(3000)}
    var breathsPerMinute by remember { mutableStateOf<Float?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var lastValidBpm by remember { mutableFloatStateOf(0f) }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }



    DisposableEffect(gyroManager, gyroInterpolator) {

        gyroManager.onGyroSample = { sample: GyroSample ->
            gyroInterpolator.onNewSample(sample)
        }


        gyroInterpolator.onInterpolatedSample = { g ->

            val unfilteredGyro = GyroSample(
                timestampNs = g.timestampNs,
                pitch = g.pitch,
                roll = g.roll,
                yaw = g.yaw
            )

            val filteredGyro = GyroSample(
                timestampNs = g.timestampNs,
                pitch = filterPitch.process(g.pitch),
                roll = filterRoll.process(g.roll),
                yaw = filterYaw.process(g.yaw)
            )

            // Dodavanje vrijednosti u signal buffer
            signalBuffer.add(filteredGyro.pitch)
        }

        gyroManager.start()

        onDispose {
            gyroManager.stop()
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            if (signalBuffer.isFull() && !isCalculating) {
                isCalculating = true

                val result = withContext(Dispatchers.Default) {
                    try {
                        signalAnalyzer.findRespiratoryRate(
                            signalBuffer = signalBuffer,
                            samplingRate = 100f,
                            minBpm = 6f,
                            maxBpm = 40f
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                result?.let { bpm ->
                    if (signalAnalyzer.validateChange(lastValidBpm, lastUpdateTime, bpm)) {
                        breathsPerMinute = bpm
                        lastValidBpm = bpm
                        lastUpdateTime = System.currentTimeMillis()
                    }
                }

            }
                isCalculating = false
            }

            delay(500)
    }


    // ISCRTAVANJE GUI-A
    DrawGui(breathsPerMinute)


}

@Composable
fun DrawGui(breathsPerMinute : Float?){

    Column(modifier = Modifier.padding(16.dp)) {

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = breathsPerMinute?.let { "Breathing rate: %.1f bpm".format(it) } ?: "Calculating...",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

    }

}