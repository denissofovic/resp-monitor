package com.example.respmonitor.sensor

abstract class SensorSample(
    open val timestampNs: Long,
    open val v1: Float,
    open val v2: Float,
    open val v3: Float
)
