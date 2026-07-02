package com.example.respmonitor.processing

import com.example.respmonitor.util.FloatCircularBuffer
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.*

class SignalAnalyzer {
    fun findRespiratoryRate(signalBuffer: FloatCircularBuffer, samplingRate: Float, minBpm: Float = 6f, maxBpm: Float = 40f): Float? {
        val signal = signalBuffer.toFloatArray()
        val n = signal.size

        if (n < 2) return null

        val mean = signal.average().toFloat()
        val detrended = FloatArray(n) { signal[it] - mean }

        val windowed = hanningWindow(detrended)
        val fftSize = nextPowerOfTwo(n)
        val fftInput = FloatArray(fftSize) { if (it < n) windowed[it] else 0f }

        val fft = FloatFFT_1D(fftSize.toLong())
        fft.realForward(fftInput)
        val powerSpectrum = FloatArray(fftSize / 2 + 1)

        powerSpectrum[0] = fftInput[0] * fftInput[0]

        if (fftSize % 2 == 0) {
            powerSpectrum[fftSize / 2] = fftInput[1] * fftInput[1]
        }

        for (i in 1 until fftSize / 2) {
            val real = fftInput[2 * i]
            val imag = fftInput[2 * i + 1]
            powerSpectrum[i] = real * real + imag * imag
        }

        val minFreq = minBpm / 60f
        val maxFreq = maxBpm / 60f

        val minIndex = (minFreq * fftSize / samplingRate).toInt().coerceAtLeast(1)
        val maxIndex = (maxFreq * fftSize / samplingRate).toInt().coerceAtMost(powerSpectrum.size - 1)

        if (minIndex >= maxIndex) return null

        var maxPower = 0f
        var peakIndex = -1

        for (i in minIndex..maxIndex) {
            if (powerSpectrum[i] > maxPower) {
                maxPower = powerSpectrum[i]
                peakIndex = i
            }
        }

        if (peakIndex == -1) return null

    // Vrh na rubu opsega -> interpolacija nije moguća, vrati sirovi bin
        if (peakIndex <= minIndex || peakIndex >= maxIndex) {
            return peakIndex * samplingRate / fftSize * 60f
        }

    // Parabolička interpolacija vrha na log-power skali.
    // Glavni lob prozorovanog sinusoida je približno parabola u log domenu,
    // pa log-fit daje manju sistematsku grešku od linearnog.
        val alpha = ln(powerSpectrum[peakIndex - 1] + 1e-12f)
        val beta  = ln(powerSpectrum[peakIndex]     + 1e-12f)
        val gamma = ln(powerSpectrum[peakIndex + 1] + 1e-12f)

        val denom = alpha - 2f * beta + gamma
        val delta = if (denom != 0f) 0.5f * (alpha - gamma) / denom else 0f
        // delta je u opsegu [-0.5, 0.5] i pomjera vrh unutar bina

        val interpolatedIndex = peakIndex + delta
        val frequency = interpolatedIndex * samplingRate / fftSize

        return frequency * 60f
    }



    fun calculateEMA(bpm : Float, currentBreaths : Float) : Float{
        val alpha = if (currentBreaths == 0f) {
            1f
        } else {
            val change = abs(bpm - currentBreaths)
            when {
                change <= 3f -> 0.5f
                change <= 6f -> 0.3f
                change <= 10f -> 0.15f
                else -> 0.05f
            }
        }

        val breathsPerMinute = alpha * bpm + (1f - alpha) * currentBreaths
        return breathsPerMinute

    }


    private fun hanningWindow(signal: FloatArray): FloatArray {
        val n = signal.size
        return FloatArray(n) { i ->
            val window = 0.5f * (1f - cos(2f * PI.toFloat() * i / (n - 1)))
            signal[i] * window
        }
    }

    private fun nextPowerOfTwo(n: Int): Int {
        var power = 1
        while (power < n) {
            power = power shl 1
        }
        return power
    }

}