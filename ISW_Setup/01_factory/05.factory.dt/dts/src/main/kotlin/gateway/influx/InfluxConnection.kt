package dts.gateway.influx

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.WriteApi
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import java.time.Instant

class InfluxConnection(private val config: InfluxConfig) {
    private val client: InfluxDBClient =
        InfluxDBClientFactory.create(config.url, config.token.toCharArray(), config.org, config.bucket)

    private val writeApi: WriteApi = client.writeApi

    fun writeMeasurement(
        measurement: String,
        fields: Map<String, Any>,
        tags: Map<String, String> = emptyMap()
    ) {
        val point = Point.measurement(measurement).time(Instant.now(), WritePrecision.MS)
        tags.forEach { (k, v) -> point.addTag(k, v) }
        fields.forEach { (k, v) ->
            when (v) {
                is Int -> point.addField(k, v.toLong())
                is Long -> point.addField(k, v)
                is Double -> point.addField(k, v)
                is Float -> point.addField(k, v.toDouble())
                is Boolean -> point.addField(k, v)
                is String -> point.addField(k, v)
                else -> point.addField(k, v.toString())
            }
        }
        writeApi.writePoint(point)
    }

    fun close() {
        writeApi.flush()
        client.close()
    }
}
