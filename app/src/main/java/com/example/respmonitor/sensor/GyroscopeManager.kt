package com.example.respmonitor.sensor

import GyroSample
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class GyroscopeManager(
    context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


    var onGyroSample: ((GyroSample) -> Unit)? = null

    fun start() {
        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        onGyroSample?.invoke(
            GyroSample(
                timestampNs = event.timestamp,
                pitch = event.values[0],
                roll = event.values[1],
                yaw = event.values[2]
            )
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
