package dts.modelmanager.models

import com.influxdb.client.kotlin.InfluxDBClientKotlin
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Abstract class representing a model responsible for holding all information about
 * a model and defining how to interact with them. Models extend this class to implement specific
 * behavior for writing, reading, and managing data.
 *
 * @param modelID a unique identifier for the model.
 * @param modelProperties a set of properties associated with the model (defaults to an empty set).
 * @param observer a list of model observers to notify about events or errors.
 * @param client the InfluxDB client instance used for querying the database.
 * @param island the name of the island (bucket) used in database queries.
 * @param modelData a mutable map holding the current data of the model, with properties as keys and values.
 */
abstract class AbstractModel(
    val modelID: String,
    val client: InfluxDBClientKotlin,
    val island: String,
    val modelData: MutableMap<String, String> = mutableMapOf(),
    val signalNewData: (Set<String>, String) -> Unit = {p, m -> },
) {

    val logger = KotlinLogging.logger(this::class.java.name)

    /**
     * Writes multiple properties to the model and records the action in the database with the same
     * timestamp for all properties.
     *
     * @param properties a map of property-value pairs to write.
     */
    abstract suspend fun writeStatus(properties: Map<String,Any?>)

    /**
     * Get the control message for this model.
     */
    abstract fun getControl(): Map<String, Any?>

    /**
     * Retrieves the last known value of a property from the database.
     *
     * @param property the name of the property to query.
     * @return the last value of the property from the database, or an empty string if no value is found.
     */
    fun getLastValue(property: String) : String{
        val fluxQuery = ("from(bucket: $island)\n"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"$modelID/status\" and r[\"_field\"] == \"$property\"))"
                + " |> last()")
        val queryResultChannel = client.getQueryKotlinApi().queryRaw(fluxQuery)
        return queryResultChannel.tryReceive().getOrNull() ?: ""
    }

    /**
     * Retrieves all data for the current model.
     *
     * @return a map containing all property-value pairs for the model.
     */
    abstract fun getModel() : MutableMap<String, String>

    /**
     * Retrieves the value of a specific property from the model's data.
     *
     * @param id the identifier of the property to retrieve.
     * @return the value of the property.
     */
    abstract fun getValue(id: String): String

    /**
     * Writes a command history entry for a specific property of the model. This method is intended
     * to be used when sending an event to the MQTT server.
     *
     * @param property the property to update in the command history.
     * @param value the value to record in the command history.
     */
    abstract suspend fun writeCommandHist(property: String, value: String)

}