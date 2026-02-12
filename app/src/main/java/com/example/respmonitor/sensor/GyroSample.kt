import com.example.respmonitor.sensor.SensorSample

data class GyroSample(
    override val timestampNs: Long,
    val pitch: Float,
    val roll: Float,
    val yaw: Float
) : SensorSample(timestampNs, pitch, roll, yaw)
