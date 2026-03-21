package com.example.respmonitor.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedback {


    fun vibrateShort(context: Context) {
        vibrate(context, 50) // 50ms
    }


    fun vibrateMedium(context: Context) {
        vibrate(context, 150) // 150ms
    }


    fun vibrateLong(context: Context) {
        vibrate(context, 600) // 600ms
    }


    fun vibrateDouble(context: Context) {
        val pattern = longArrayOf(
            0,
            300,
            200,
            300
        )
        vibratePattern(context, pattern)
    }


    fun vibrateTriple(context: Context) {
        val pattern = longArrayOf(
            0,    // Start immediately
            80,   // Vibrate 80ms
            80,   // Pause 80ms
            80,   // Vibrate 80ms
            80,   // Pause 80ms
            80    // Vibrate 80ms
        )
        vibratePattern(context, pattern)
    }


    fun vibrateWarning(context: Context) {
        val pattern = longArrayOf(
            0,     // Start
            200,   // Long vibrate
            100,   // Short pause
            200    // Long vibrate
        )
        vibratePattern(context, pattern)
    }


    private fun vibrate(context: Context, durationMs: Long) {
        val vibrator = getVibrator(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }

    private fun vibratePattern(context: Context, pattern: LongArray) {
        val vibrator = getVibrator(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}