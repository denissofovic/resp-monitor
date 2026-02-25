package com.example.respmonitor

import GyroSample
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.respmonitor.gui.MainScreen
import com.example.respmonitor.processing.ButterworthFilter
import com.example.respmonitor.processing.Interpolator
import com.example.respmonitor.processing.SignalAnalyzer
import com.example.respmonitor.sensor.GyroscopeManager


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gyroscopeManager = GyroscopeManager(this)
        val filterPitch = ButterworthFilter(0.4f,100f)
        val filterRoll = ButterworthFilter(0.4f,100f)
        val filterYaw = ButterworthFilter(0.4f,100f)
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


        setContent {
            MainScreen(gyroscopeManager,
                                    gyroInterpolator,
                                    filterPitch,
                                    filterRoll,
                                    filterYaw,
                                    signalAnalyzer
            )}
    }


}








