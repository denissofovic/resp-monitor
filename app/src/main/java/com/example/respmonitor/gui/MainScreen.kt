package com.example.respmonitor.gui

import CsvLogger
import GyroSample
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    accelerometerManager: AccelerometerManager,
    gyroInterpolator: Interpolator<GyroSample>,
    accelInterpolator: Interpolator<AccelSample>,
    filterPitch: ButterworthFilter,
    filterRoll: ButterworthFilter,
    filterYaw: ButterworthFilter,
    filterX: ButterworthFilter,
    filterY: ButterworthFilter,
    filterZ: ButterworthFilter,
    signalAnalyzer: SignalAnalyzer
) {
    val context = LocalContext.current
    val gyroLogger = remember { CsvLogger(context, "gyro") }
    val accelLogger = remember { CsvLogger(context, "accel") }
    val signalBuffer = remember { FloatCircularBuffer(3000)}
    var unfilteredGyroSample by remember { mutableStateOf<GyroSample?>(null) }
    var filteredGyroSample by remember { mutableStateOf<GyroSample?>(null) }
    var unfilteredAccelSample by remember { mutableStateOf<AccelSample?>(null) }
    var filteredAccelSample by remember { mutableStateOf<AccelSample?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var breathsPerMinute by remember { mutableStateOf<Float?>(null) }
    var isCalculating by remember { mutableStateOf(false) }



    DisposableEffect(gyroManager, gyroInterpolator, accelerometerManager, accelInterpolator) {

        gyroManager.onGyroSample = { sample: GyroSample ->
            gyroInterpolator.onNewSample(sample)
        }


        accelerometerManager.onAccelSample = { accelSample : AccelSample ->
            accelInterpolator.onNewSample(accelSample)
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

            Log.d("GYRO_DATA", "Pitch=${filteredGyro.pitch}, Roll=${filteredGyro.roll}, Yaw=${filteredGyro.yaw}")



            // Dodavanje vrijednosti u signal buffer
            signalBuffer.add(filteredGyro.pitch)

            unfilteredGyroSample = unfilteredGyro
            filteredGyroSample = filteredGyro

            //if (isRecording) gyroLogger.addSample(unfilteredGyro, filteredGyro)

        }


        accelInterpolator.onInterpolatedSample = { a ->

            val unfilteredAccel = AccelSample(
                timestampNs = a.timestampNs,
                x = a.x,
                y = a.y,
                z = a.z
            )

            val filteredAccel = AccelSample(
                timestampNs = a.timestampNs,
                x = filterX.process(a.x),
                y = filterY.process(a.y),
                z = filterZ.process(a.z)
            )

            Log.d("ACCEL_DATA", "X=${filteredAccel.x}, Y=${filteredAccel.y}, Z=${filteredAccel.z}")



            unfilteredAccelSample = unfilteredAccel
            filteredAccelSample = filteredAccel

             if (isRecording) accelLogger.addSample(unfilteredAccel, filteredAccel)


        }

        gyroManager.start()
        accelerometerManager.start()

        onDispose {
            gyroManager.stop()
            accelerometerManager.stop()
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            if (signalBuffer.isFull() && !isCalculating) {
                isCalculating = true

                val result = withContext(Dispatchers.Default) {
                    try {
                        val autoCorrelation = signalAnalyzer.calculateAc(signalBuffer)

                        val peakLag = signalAnalyzer.findPeak(
                            ac = autoCorrelation,
                            minLag = 150,    // 40 bpm
                            maxLag = 1000,   // 6 bpm
                            minHeight = 0.3f // 30% threshold
                             )

                        if (peakLag > 0) {
                            val frequencyHz = 100f / peakLag
                            frequencyHz * 60f
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Error calculating RR: ${e.message}")
                        null
                    }
                }

                breathsPerMinute = result
                isCalculating = false
            }

            delay(2000)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = unfilteredGyroSample?.let {
                "Unfiltered \n Pitch=${it.pitch} \n Roll=${it.roll} \n Yaw=${it.yaw}"
            } ?: ""
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = filteredGyroSample?.let {
                "Filtered \n Pitch=${it.pitch} \n Roll=${it.roll} \n Yaw=${it.yaw}"
            } ?: ""
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = unfilteredAccelSample?.let {
                "Unfiltered \n X=${it.x} \n Y=${it.y} \n Z=${it.z}"
            } ?: ""
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = filteredAccelSample?.let {
                "Filtered \n X=${it.x} \n Y=${it.y} \n Z=${it.z}"
            } ?: ""
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = breathsPerMinute?.let { "Breathing rate: %.1f bpm".format(it) } ?: "Calculating...",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )


        Button(onClick = {
            if (!isRecording) {
                //gyroLogger.startRecording()
                accelLogger.startRecording()
                isRecording = true
            } else {
                //gyroLogger.stopAndSave()
                accelLogger.stopAndSave()
                isRecording = false
            }
        }) {
            Text(if (isRecording) "Stop " else "Start ")
        }


    }
}