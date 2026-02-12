package com.example.respmonitor.processing

import kotlin.math.sqrt
import kotlin.math.tan

class ButterworthFilter(fg: Float, fs:Float = 100f) {
    private val b0: Float
    private val b1: Float
    private val b2: Float
    private val a1: Float
    private val a2: Float

    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f


    init {
        val omega = tan(Math.PI * fg / fs).toFloat()
        val sqrt2 = sqrt(2.0).toFloat()

        val c = 1f + sqrt2 * omega + omega * omega

        b0 = (omega * omega) / c
        b1 = 2f * b0
        b2 = b0

        a1 = 2f * (omega * omega - 1f) / c
        a2 = (1f - sqrt2 * omega + omega * omega) / c
    }

    fun process(x: Float): Float {
        val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1
        x1 = x
        y2 = y1
        y1 = y

        return y
    }


}
