package com.example.respmonitor.processing

import android.content.Context
import org.tensorflow.lite.Interpreter
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
        val inputBuffer = ByteBuffer.allocateDirect(600 * 4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val fixedData = when {
            sensorData.size > 600 -> sensorData.copyOfRange(0, 600)
            sensorData.size < 600 -> FloatArray(600).apply { sensorData.copyInto(this) }
            else -> sensorData
        }

        inputBuffer.asFloatBuffer().put(fixedData)
        inputBuffer.rewind()
        val outputBuffer = Array(1) { FloatArray(2) }

        try {
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            return ClassificationResult("error", 0f, 0f, 0f)
        }


        val invalidScore = outputBuffer[0][0] // Changed indexes, seems right
        val validScore = outputBuffer[0][1] // Changed indexes, seems right



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