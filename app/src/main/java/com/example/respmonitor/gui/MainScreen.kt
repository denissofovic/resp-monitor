package com.example.respmonitor.gui

import CsvLogger
import GyroSample
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.sensor.AccelSample
import com.example.respmonitor.sensor.AccelerometerManager
import com.example.respmonitor.sensor.GyroscopeManager

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
    filterZ: ButterworthFilter
) {
    val context = LocalContext.current
    val gyroLogger = remember { CsvLogger(context, "gyro") }
    val accelLogger = remember { CsvLogger(context, "accel") }


    var unfilteredGyroSample by remember { mutableStateOf<GyroSample?>(null) }
    var filteredGyroSample by remember { mutableStateOf<GyroSample?>(null) }
    var unfilteredAccelSample by remember { mutableStateOf<AccelSample?>(null) }
    var filteredAccelSample by remember { mutableStateOf<AccelSample?>(null) }
    var isRecording by remember { mutableStateOf(false) }

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


            unfilteredGyroSample = unfilteredGyro
            filteredGyroSample = filteredGyro

            if (isRecording) gyroLogger.addSample(unfilteredGyro, filteredGyro)


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

        Spacer(modifier = Modifier.height(16.dp))


        Button(onClick = {
            if (!isRecording) {
                gyroLogger.startRecording()
                accelLogger.startRecording()
                isRecording = true
            } else {
                gyroLogger.stopAndSave()
                accelLogger.stopAndSave()
                isRecording = false
            }
        }) {
            Text(if (isRecording) "Stop " else "Start ")
        }


    }
}