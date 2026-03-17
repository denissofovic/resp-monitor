package com.example.respmonitor.sensor

data class AccelSample(
    override val timestampNs: Long,
    val x: Float,
    val y: Float,
    val z: Float
): SensorSample(timestampNs, x, y, z)