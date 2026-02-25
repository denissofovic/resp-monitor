package com.example.respmonitor.processing

import com.example.respmonitor.util.FloatCircularBuffer
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.*

class SignalAnalyzer {

    /**
     * Pronalazi respiratornu frekvenciju korištenjem FFT analize
     *
     * @param signalBuffer Buffer sa pitch gyroscope podacima
     * @param samplingRate Sampling rate u Hz (npr. 100Hz)
     * @param minBpm Minimalni broj udisaja po minuti (default: 6)
     * @param maxBpm Maksimalni broj udisaja po minuti (default: 40)
     * @return Respiratorna frekvencija u BPM ili null ako nije pronađena
     */
    fun findRespiratoryRate(
        signalBuffer: FloatCircularBuffer,
        samplingRate: Float,
        minBpm: Float = 6f,
        maxBpm: Float = 40f
    ): Float? {
        val signal = signalBuffer.toFloatArray()
        val n = signal.size

        if (n < 2) return null

        // 1. Detrending - ukloni DC komponentu (srednju vrednost)
        val mean = signal.average().toFloat()
        val detrended = FloatArray(n) { signal[it] - mean }

        // 2. Primijeni Hanning window da smanjiš spectral leakage
        val windowed = applyHanningWindow(detrended)

        // 3. Zero-padding do najbližeg power of 2 za efikasnost
        val fftSize = nextPowerOfTwo(n)
        val fftInput = FloatArray(fftSize) { if (it < n) windowed[it] else 0f }

        // 4. Izračunaj FFT korištenjem JTransforms
        val fft = FloatFFT_1D(fftSize.toLong())
        fft.realForward(fftInput)

        // 5. Izračunaj power spectrum (magnitude squared)
        // JTransforms realForward format: [r0, r1, i1, r2, i2, ..., rn/2]
        val powerSpectrum = FloatArray(fftSize / 2 + 1)

        // DC component (index 0)
        powerSpectrum[0] = fftInput[0] * fftInput[0]

        // Nyquist frequency (index n/2)
        if (fftSize % 2 == 0) {
            powerSpectrum[fftSize / 2] = fftInput[1] * fftInput[1]
        }

        // Ostale frekvencije
        for (i in 1 until fftSize / 2) {
            val real = fftInput[2 * i]
            val imag = fftInput[2 * i + 1]
            powerSpectrum[i] = real * real + imag * imag
        }

        // 6. Konvertuj BPM range u Hz
        val minFreq = minBpm / 60f
        val maxFreq = maxBpm / 60f

        // 7. Konvertuj frekvencije u FFT bin indekse
        val minIndex = (minFreq * fftSize / samplingRate).toInt().coerceAtLeast(1)
        val maxIndex = (maxFreq * fftSize / samplingRate).toInt().coerceAtMost(powerSpectrum.size - 1)

        if (minIndex >= maxIndex) return null

        // 8. Pronađi bin sa maksimalnom snagom u validnom rasponu
        var maxPower = 0f
        var peakIndex = -1

        for (i in minIndex..maxIndex) {
            if (powerSpectrum[i] > maxPower) {
                maxPower = powerSpectrum[i]
                peakIndex = i
            }
        }

        if (peakIndex == -1) return null

        // 9. Konvertuj FFT bin index nazad u frekvenciju
        val frequency = peakIndex * samplingRate / fftSize

        // 10. Konvertuj Hz u BPM
        return frequency * 60f
    }

    /**
     * Primjenjuje Hanning window funkciju na signal
     * Smanjuje spectral leakage u FFT analizi
     */
    private fun applyHanningWindow(signal: FloatArray): FloatArray {
        val n = signal.size
        return FloatArray(n) { i ->
            val window = 0.5f * (1f - cos(2f * PI.toFloat() * i / (n - 1)))
            signal[i] * window
        }
    }

    /**
     * Pronalazi najmanji power of 2 koji je veći ili jednak datom broju
     */
    private fun nextPowerOfTwo(n: Int): Int {
        var power = 1
        while (power < n) {
            power = power shl 1 // Ekvivalentno power *= 2
        }
        return power
    }
}