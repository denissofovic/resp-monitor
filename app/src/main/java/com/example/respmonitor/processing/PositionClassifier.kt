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

    init {
        val model = loadModelFile(context)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)


    }

    fun classify(sensorData: FloatArray): ClassificationResult {
        val fixedData = when {
            sensorData.size > 600 -> sensorData.copyOfRange(0, 600)
            sensorData.size < 600 -> FloatArray(600).apply { sensorData.copyInto(this) }
            else -> sensorData
        }

        val inputTensor = interpreter.getInputTensor(0)
        val inScale = inputTensor.quantizationParams().scale
        val inZeroPoint = inputTensor.quantizationParams().zeroPoint

        val inputBuffer = ByteBuffer.allocateDirect(600).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        for (value in fixedData) {
            val quantized = Math.round(value / inScale) + inZeroPoint
            val clamped = quantized.coerceIn(-128, 127)
            inputBuffer.put(clamped.toByte())
        }
        inputBuffer.rewind()

        val outputTensor = interpreter.getOutputTensor(0)
        val outScale = outputTensor.quantizationParams().scale
        val outZeroPoint = outputTensor.quantizationParams().zeroPoint

        val outputBuffer = ByteBuffer.allocateDirect(2).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        try {
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            return ClassificationResult("error", 0f, 0f, 0f)
        }

        outputBuffer.rewind()
        val rawInvalid = outputBuffer.get().toInt()
        val rawValid = outputBuffer.get().toInt()
        val invalidScore = (rawInvalid - outZeroPoint) * outScale
        val validScore = (rawValid - outZeroPoint) * outScale
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