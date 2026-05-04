package com.example.respmonitor.gui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class MonitoringViewModel : ViewModel() {
    var lastMeasurement by mutableStateOf<Float?>(null)
        private set

    fun updateLastMeasurement(value: Float) {
        lastMeasurement = value
    }

    fun clearMeasurement() {
        lastMeasurement = null
    }
}