package dts.modelmanager.models

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.write.Point
import kotlinx.coroutines.runBlocking
import java.time.Instant


class ConveyorModel(
    modelID: String,
    val modelProperties: Set<String> = setOf("state","reason","target","leftSensor","rightSensor","position"),
    client: InfluxDBClientKotlin,
    island: String,
) : AbstractModel(modelID, client = client, island = island) {



    init {
        runBlocking {
            for (property in modelProperties) {
                modelData[property] = getLastValue(property)
            }
        }
    }

    override fun getModel(): MutableMap<String, String> {
        return modelData
    }

    override fun getValue(id: String): String {
        return modelData[id] ?: ""
    }

    override suspend fun writeCommandHist(property: String, value: String) {
        val writeApi = client.getWriteKotlinApi()
        val point = Point.measurement("$modelID/command")
            .addTags(mutableMapOf("Machine Type" to "Conveyor", "Machine ID" to modelID,"Island" to island))
            .addField(property,value).time(Instant.now().toEpochMilli(), WritePrecision.MS)
        writeApi.writePoint(point)
        logger.info { "Property $property of $modelID set to $value" }
    }


    override suspend fun writeStatus(properties: Map<String,Any?>) {
        if( properties.keys.all { it in modelProperties }) {
            val writeApi = client.getWriteKotlinApi()
            val points = Point.measurement("$modelID/status").
            addTags(mutableMapOf("Machine Type" to "Conveyor", "Machine ID" to modelID,"Island" to island)).
            addFields(properties).time(Instant.now().toEpochMilli(), WritePrecision.MS)
            for (property in properties) {
                modelData[property.key] = property.value.toString()
                logger.info { "Property ${property.key} of $modelID set to ${property.value}" }
            }
            writeApi.writePoint(points)
        }
        else {
            logger.error { "some properties of: $properties do not exist for the model $modelID abort writing data to model" }
        }
    }

    override fun getControl(): Map<String, Any?> {
        TODO("Not yet implemented")
    }

}