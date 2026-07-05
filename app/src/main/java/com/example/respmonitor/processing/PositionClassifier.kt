package com.example.respmonitor.processing

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PositionClassifier(context: Context) {

    private val interpreter: Interpreter
    private val TAG = "PositionClassifier"

    init {
        val model = loadModelFile(context)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)

        // --- DIJAGNOSTIKA: tipovi i oblik ulaza/izlaza ---
        val inT = interpreter.getInputTensor(0)
        val outT = interpreter.getOutputTensor(0)
        Log.d(TAG, "INPUT  dataType=${inT.dataType()} shape=${inT.shape().toList()} " +
                "quant(scale=${inT.quantizationParams().scale}, zeroPoint=${inT.quantizationParams().zeroPoint})")
        Log.d(TAG, "OUTPUT dataType=${outT.dataType()} shape=${outT.shape().toList()} " +
                "quant(scale=${outT.quantizationParams().scale}, zeroPoint=${outT.quantizationParams().zeroPoint})")
    }

    fun classify(sensorData: FloatArray): ClassificationResult {
        val fixedData = when {
            sensorData.size > 600 -> sensorData.copyOfRange(0, 600)
            sensorData.size < 600 -> FloatArray(600).apply { sensorData.copyInto(this) }
            else -> sensorData
        }

        // --- ULAZ: float -> int8 (kvantizacija) ---
        val inputTensor = interpreter.getInputTensor(0)
        val inScale = inputTensor.quantizationParams().scale
        val inZeroPoint = inputTensor.quantizationParams().zeroPoint

        val inputBuffer = ByteBuffer.allocateDirect(600).apply {  // 600 bajtova, ne 600*4
            order(ByteOrder.nativeOrder())
            rewind()
        }

        for (value in fixedData) {
            // q = round(real / scale) + zeroPoint, ograniceno na int8 opseg
            val quantized = Math.round(value / inScale) + inZeroPoint
            val clamped = quantized.coerceIn(-128, 127)
            inputBuffer.put(clamped.toByte())
        }
        inputBuffer.rewind()

        // --- IZLAZ: takodje int8, pa ga dekvantizujemo ---
        val outputTensor = interpreter.getOutputTensor(0)
        val outScale = outputTensor.quantizationParams().scale
        val outZeroPoint = outputTensor.quantizationParams().zeroPoint

        val outputBuffer = ByteBuffer.allocateDirect(2).apply {   // 2 klase × 1 bajt
            order(ByteOrder.nativeOrder())
            rewind()
        }

        try {
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            Log.e(TAG, "interpreter.run FAILED: ${e.message}", e)
            return ClassificationResult("error", 0f, 0f, 0f)
        }

        outputBuffer.rewind()
        val rawInvalid = outputBuffer.get().toInt()   // int8 kao signed
        val rawValid = outputBuffer.get().toInt()

        // dekvantizacija: real = (q - zeroPoint) * scale
        val invalidScore = (rawInvalid - outZeroPoint) * outScale
        val validScore = (rawValid - outZeroPoint) * outScale

        Log.d(TAG, "OUT invalid=$invalidScore valid=$validScore")

        val label = if (validScore > invalidScore) "valid" else "invalid"
        val confidence = maxOf(validScore, invalidScore)

        return ClassificationResult(label, confidence, validScore, invalidScore)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("ei-model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        interpreter.close()
    }
}