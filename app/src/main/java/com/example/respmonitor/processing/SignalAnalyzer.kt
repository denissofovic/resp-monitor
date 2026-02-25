package com.example.respmonitor.processing

import com.example.respmonitor.util.FloatCircularBuffer

class SignalAnalyzer {

    /**
     * Izračunava normalizovanu autokorelaciju
     * ACF[0] = 1.0, ostale vrednosti su između -1 i 1
     */
    fun calculateAc(signalBuffer: FloatCircularBuffer): FloatArray {
        val signal = signalBuffer.toFloatArray()
        val n = signal.size

        // 1. Detrending - oduzmi srednju vrednost
        val mean = signal.average().toFloat()
        val centered = FloatArray(n) { signal[it] - mean }

        // 2. Autokorelacija
        val result = FloatArray(n)

        // Variance (za normalizaciju)
        var variance = 0f
        for (i in 0 until n) {
            variance += centered[i] * centered[i]
        }

        // Ako nema varijanse, signal je konstanta
        if (variance == 0f) {
            result[0] = 1f
            return result
        }

        // Računaj autokorelaciju
        for (lag in 0 until n) {
            var sum = 0f
            for (i in 0 until n - lag) {
                sum += centered[i] * centered[i + lag]
            }
            // Normalizuj sa variance (ACF[0] će biti 1.0)
            result[lag] = sum / variance
        }

        return result
    }


    fun findPeak( ac: FloatArray,  minLag: Int,  maxLag: Int,  minHeight: Float = 0.3f): Int {
        if (minLag >= ac.size || maxLag >= ac.size) return -1

        var maxVal = Float.MIN_VALUE
        var maxIndex = -1

        for (i in minLag until maxLag.coerceAtMost(ac.size - 1)) {

            val isPeak = (i == minLag || ac[i] > ac[i - 1]) &&
                    (i == ac.size - 1 || ac[i] > ac[i + 1])

            if (isPeak && ac[i] > minHeight && ac[i] > maxVal) {
                maxVal = ac[i]
                maxIndex = i
            }
        }

        return maxIndex
    }



}