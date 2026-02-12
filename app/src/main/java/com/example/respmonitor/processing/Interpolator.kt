package com.example.respmonitor.processing

import com.example.respmonitor.sensor.SensorSample

class Interpolator<T : SensorSample>(
    private val targetHz: Float = 100f,
    private val sampleFactory: (timestampNs: Long, v1: Float, v2: Float, v3: Float) -> T
) {

    private val stepNs = (1_000_000_000L / targetHz).toLong()

    private var prev: T? = null
    private var curr: T? = null
    private var nextTime: Long? = null

    var onInterpolatedSample: ((T) -> Unit)? = null

    fun onNewSample(sample: T) {
        prev = curr
        curr = sample

        if (nextTime == null) nextTime = sample.timestampNs

        interpolate()
    }

    private fun interpolate() {
        val s1 = prev ?: return
        val s2 = curr ?: return
        var t = nextTime ?: return

        while (t <= s2.timestampNs) {

            if (t >= s1.timestampNs) {

                val alpha =
                    (t - s1.timestampNs).toFloat() /
                            (s2.timestampNs - s1.timestampNs).toFloat()

                val interpolated = sampleFactory(
                    t,
                    lerp(s1.v1, s2.v1, alpha),
                    lerp(s1.v2, s2.v2, alpha),
                    lerp(s1.v3, s2.v3, alpha)
                )

                onInterpolatedSample?.invoke(interpolated)
            }

            t += stepNs
        }

        nextTime = t
    }

    private fun lerp(a: Float, b: Float, alpha: Float) =
        a + alpha * (b - a)
}