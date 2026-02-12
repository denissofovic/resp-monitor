import android.content.Context
import android.util.Log
import com.example.respmonitor.sensor.SensorSample
import java.io.File


class CsvLogger(private val context: Context, private val sensorName : String) {
    private val unfilteredSamples = mutableListOf<SensorSample>()
    private val filteredSamples = mutableListOf<SensorSample>()
    private var isRecording = false
    private var startTime = 0L

    fun startRecording() {
        unfilteredSamples.clear()
        filteredSamples.clear()
        isRecording = true
        startTime = System.nanoTime()
    }

    fun addSample(unfiltered: SensorSample, filtered: SensorSample) {
        if (isRecording) {
            unfilteredSamples.add(unfiltered)
            filteredSamples.add(filtered)

            if (unfilteredSamples.size >= 4000) {
                stopAndSave()
            }
        }
    }

    fun stopAndSave() {
        if (!isRecording) return
        isRecording = false

        saveToFile("${sensorName}_unfiltered_data.csv", unfilteredSamples)
        saveToFile("${sensorName}_filtered_data.csv", filteredSamples)
    }

    private fun saveToFile(fileName: String, samples: List<SensorSample>) {
        val file = File(context.getExternalFilesDir(null), fileName)

        file.bufferedWriter().use { writer ->
            writer.write("time,x,y,z\n")

            samples.forEach { sample ->
                val time = (sample.timestampNs - startTime) / 1_000_000_000.0
                writer.write("$time,${sample.v1},${sample.v2},${sample.v3}\n")
            }
        }

        Log.d("CsvLogger", "File saved: ${file.absolutePath}")
    }
}