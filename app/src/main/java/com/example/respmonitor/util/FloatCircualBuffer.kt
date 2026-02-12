package com.example.respmonitor.util

import android.text.BoringLayout

class FloatCircularBuffer(private val capacity: Int) {

    private val buffer = FloatArray(capacity)
    private var index = 0
    private var isFull = false

    fun add(value: Float) {
        buffer[index] = value
        index = (index + 1) % capacity
        if (index == 0) isFull = true
    }

    fun toList(): List<Float> {
        return if (!isFull) {
            buffer.slice(0 until index)
        } else {
            buffer.slice(index until capacity) + buffer.slice(0 until index)
        }
    }

    fun isFull() : Boolean {
        return isFull
    }

    fun toFloatArray(): FloatArray = toList().toFloatArray()
}