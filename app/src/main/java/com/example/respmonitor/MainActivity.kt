package com.example.respmonitor

import GyroSample
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.respmonitor.gui.MainScreen
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.processing.SignalAnalyzer
import com.example.respmonitor.sensor.AccelSample
import com.example.respmonitor.sensor.AccelerometerManager
import com.example.respmonitor.sensor.GyroscopeManager


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gyroscopeManager = GyroscopeManager(this)
        val accelerometerManager = AccelerometerManager(this)
        val filterPitch = ButterworthFilter(0.4f,100f)
        val filterRoll = ButterworthFilter(0.4f,100f)
        val filterYaw = ButterworthFilter(0.4f,100f)
        val filterX = ButterworthFilter(0.4f,100f)
        val filterY = ButterworthFilter(0.4f,100f)
        val filterZ = ButterworthFilter(0.4f,100f)
        val signalAnalyzer = SignalAnalyzer()

        val gyroInterpolator = Interpolator<GyroSample>(
            targetHz = 100f,
            sampleFactory = { timestampNs, v1, v2, v3 ->
                GyroSample(
                    timestampNs = timestampNs,
                    pitch = v1,
                    roll = v2,
                    yaw = v3
                )
            }
        )



        val accelInterpolator = Interpolator<AccelSample>(
            targetHz = 100f,
            sampleFactory = { timestampNs, v1, v2, v3 ->
                AccelSample(
                    timestampNs = timestampNs,
                    x = v1,
                    y = v2,
                    z = v3
                )
            }
        )

        setContent {
            MainScreen(gyroscopeManager,
                                    accelerometerManager,
                                    gyroInterpolator,
                                    accelInterpolator,
                                    filterPitch,
                                    filterRoll,
                                    filterYaw,
                                    filterX,
                                    filterY,
                                    filterZ,
                                    signalAnalyzer
            )}
    }


}








