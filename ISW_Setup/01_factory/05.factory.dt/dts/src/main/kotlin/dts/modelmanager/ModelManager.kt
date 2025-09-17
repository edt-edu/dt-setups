package dts.modelmanager

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.write.Point
import com.influxdb.query.FluxTable
import dts.IDataSource
import dts.connection.SynchronizationDirection
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.events.observer.IModelObserver
import dts.modelmanager.models.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger {}

/**
 * The `ModelManager` class is responsible for managing different models in a given setup.
 * It holds a map of model managers and provides methods to interact with the models such as
 * writing data, reading data, and interacting with the InfluxDB client.
 *
 * @property setupName the name of the setup (e.g., "Island1").
 * @property modelManagers a mutable map holding models by their IDs.
 * @property influxDBClient the InfluxDB client initialized with a specific bucket and credentials.
 */
class ModelManager(private val setupName: String, val influxDBClient: InfluxDBClientKotlin) : IDataSource {
    private val modelManagers: MutableMap<String, AbstractModel> = mutableMapOf()
    private val modelStatus: MutableMap<String, String> = mutableMapOf()
    private var islandStatus: String = "shutting down"
    private var statusProperties: Set<String> = mutableSetOf()
    private var islandStatusProperty: String = "islandX/status"
    override val propertyIDs: Set<String>
        get() = setOf(
            *modelManagers.keys.map { "$it/status" }.toTypedArray(),
            *modelManagers.keys.map { "$it/control" }.toTypedArray(),
            *statusProperties.toTypedArray(),
            islandStatusProperty,
        )

    val observer: MutableList<IModelObserver> = mutableListOf()

    /**
     * Adds an observer to the model, allowing it to listen for model-related events.
     *
     * @param modelObserver the observer to add.
     */
    fun addObserver(modelObserver: IModelObserver) {
        observer.add(modelObserver)
        logger.info { "Observer $modelObserver added to $this" }
    }

    /**
     * Removes an observer from the model, preventing it from receiving further updates.
     *
     * @param modelObserver the observer to remove.
     */
    fun removeObserver(modelObserver: IModelObserver) {
        observer.remove(modelObserver)
        logger.info { "Observer $modelObserver removed from $this" }
    }

    override fun getValue(id: String): Any {
        val modelId = id.substringBefore('/')
        val model = modelManagers[modelId]
        if (model == null) {
            throw IllegalArgumentException("Model $modelId does not exist, can't retrieve control")
        }
        return model.getControl()
    }

    override fun getAllValues(): Map<String, Any?> {
        return modelManagers.mapValues { it.key to it.value.getControl() }
    }

    /**
     * Initializes the `ModelHolder` with models if the setup name is "Island1".
     * If the setup name doesn't match, logs an error.
     */
    init {
        if (setupName == "Island1") {
            populateModelMangerMapI1();
        } else {
            logger.error { "there is no modelmanager setup for $setupName " }
        }
    }

    fun size(): Int {
        return modelManagers.size
    }

    /**
     * Populates the `modelManagers` map with models specific to "Island1".
     * Models include conveyors, claw grippers, sorting lines, and more.
     */
    private fun populateModelMangerMapI1() {
        modelStatus["1-1-conveyor"] = "destroyed"
        modelStatus["1-2-clawGripper"] = "destroyed"
        modelStatus["1-3-conveyor"] = "destroyed"
        modelStatus["1-4-sortingLine"] = "destroyed"
        modelStatus["1-5-warehouse"] = "destroyed"
        modelStatus["1-6-vacuumGripper"] = "destroyed"
        modelStatus["1-7-indexedLine"] = "destroyed"
        modelStatus["1-8-clawGripper"] = "destroyed"

        islandStatusProperty = "island1/status"
        statusProperties = mutableSetOf(
            "island1/machines/1-1-conveyor",
            "island1/machines/1-2-clawGripper",
            "island1/machines/1-3-conveyor",
            "island1/machines/1-4-sortingLine",
            "island1/machines/1-5-warehouse",
            "island1/machines/1-6-vacuumGripper",
            "island1/machines/1-7-indexedLine",
            "island1/machines/1-8-clawGripper",
        )

        modelManagers["1-1-conveyor"] = ConveyorModel(
            modelID = "1-1-conveyor",
            client = influxDBClient,
            island = setupName
        );
        modelManagers["1-2-clawGripper"] = ClawGripperModel(
            modelID = "1-2-clawGripper",
            client = influxDBClient,
            island = setupName,
            signalNewData = ::modelDataUpdate,
        );
        modelManagers["1-3-conveyor"] = ConveyorModel(
            modelID = "1-3-conveyor",
            client = influxDBClient,
            island = setupName
        );
        modelManagers["1-4-sortingLine"] = SortingLineModel(
            modelID = "1-4-sortingLine",
            client = influxDBClient,
            island = setupName
        );
        modelManagers["1-5-warehouse"] = WarehouseModel(
            modelID = "1-5-warehouse",
            client = influxDBClient,
            island = setupName
        );
        modelManagers["1-6-vacuumGripper"] = VacuumGripperModel(
            modelID = "1-6-vacuumGripper",
            client = influxDBClient,
            island = setupName,
            signalNewData = ::modelDataUpdate,
        );
        modelManagers["1-7-indexedLine"] = IndexedLineModel(
            modelID = "1-7-indexedLine",
            client = influxDBClient,
            island = setupName
        );
        modelManagers["1-8-clawGripper"] = ClawGripperModel(
            modelID = "1-8-clawGripper",
            client = influxDBClient,
            island = setupName,
            signalNewData = ::modelDataUpdate,
        );

    }

    /**
     * Returns an abstract model from modeMangers
     *
     * @param modelId the ID of the model
     * @return the abstract model with given ID
     */
    fun returnAbstractModel(modelId: String): AbstractModel? {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            return tmpModel
        } else {
            logger.error { "model $modelId does not exist" }
            return null
        }

    }

    override fun setValue(id: String, value: Any?) {
        if (id == islandStatusProperty) {
            islandStatus = value as String
        } else if (statusProperties.contains(id)) {
            val machine = id.substringAfterLast('/')
            modelStatus[machine] = value as String
        } else {
            val modelId = id.substringBefore('/')
            val model = modelManagers[modelId]
            if (model == null) {
                throw IllegalArgumentException("Model $modelId does not exist, can't retrieve control")
            }
            runBlocking {
                model.writeStatus(value as Map<String, Any?>)
            }
        }
    }

    /**
     * Writes multiple properties to the specified model.
     *
     * @param modelId the ID of the model to write to.
     * @param properties a map of property-value pairs to write to the model.
     */
    suspend fun writeMultipleToModel(modelId: String, properties: Map<String, Any?>) {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            tmpModel.writeStatus(properties)
        } else {
            logger.error { "model $modelId does not exist" }
        }
    }


    /**
     * Retrieves the last known value for a given property from the specified model from the database.
     *
     * @param modelId the ID of the model to query.
     * @param property the property to retrieve the last value for.
     * @return the last known value of the property, or an empty string if the model does not exist.
     */
    fun getLastValue(modelId: String, property: String): String {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            return tmpModel.getLastValue(property)
        } else {
            logger.error { "model $modelId does not exist return empty string" }
            return ""
        }
    }

    /**
     * Retrieves the current state of all data for the specified model.
     *
     * @param modelId the ID of the model to query.
     * @return a map of all the model's data, or an empty map if the model does not exist.
     */

    fun getModelData(modelId: String): Map<String, String> {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            return tmpModel.getModel()
        } else {
            logger.error { "model $modelId does not exist return empty map" }
            return mapOf()
        }
    }


    /**
     * Retrieves the value of a specified property from the model.
     *
     * @param modelId the unique identifier of the model from which the property is being retrieved.
     * @param property the name of the property whose value needs to be retrieved.
     * @return the value of the property if the model exists, or an empty string if the model does not exist.
     */
    fun getModelValue(modelId: String, property: String): String {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            return tmpModel.getValue(property)
        } else {
            logger.error { "model $modelId does not exist return empty String" }
            return ""
        }
    }

    /**
     * Writes a command history entry to a model for a specific property.
     *
     * @param modelId the unique identifier of the model to which the command history is being written.
     * @param property the name of the property for which the command history entry is being written.
     * @param value the value to write as part of the command history for the specified property.
     */
    suspend fun writeCommandHist(modelId: String, property: String, value: String) {
        val tmpModel = modelManagers[modelId]
        if (tmpModel != null) {
            tmpModel.writeCommandHist(property, value)
        } else {
            logger.error { "model $modelId does not exist" }

        }
    }

    /**
     * Function for closing the influxdbClient connection should be used after shutting down program
     */
    fun closeClient() {
        influxDBClient.close()
    }

    fun islandIsUp(): Boolean {
        return islandStatus == "online"
    }

    fun modelIsUp(modelId: String): Boolean {
        val status = getModelLifecycleStatus(modelId)
        return status == "online" || status == "already created" || status == "destroy aborted"
    }

    fun getModelLifecycleStatus(modelId: String): String {
        val status = modelStatus[modelId]
        if (status == null) {
            throw IllegalArgumentException("Unknown model $modelId")
        }
        return status
    }

    fun allModelsUp(): Boolean {
        if (!islandIsUp()) {
            return false
        }
        for (ms in modelStatus) {
            if (!modelIsUp(ms.key)) {
                return false
            }
        }
        return true
    }

    /**
     * Notify observers that this ModelManager wants to synchronise data with the gateway.
     */
    fun modelDataUpdate(properties: Set<String>, message: String = "") {
        for (listener: IModelObserver in observer) {
            // Set up the event
            val dataPointEvent = NewDataPointEvent(this, properties)
            dataPointEvent.sourceID = setupName
            dataPointEvent.timestamp = LocalDateTime.now()
            dataPointEvent.synchronizationDirection = SynchronizationDirection.DT_TO_GATEWAY
            dataPointEvent.message = message

            // let the observer handle it
            listener.handleModelDatapointEvent(dataPointEvent)
        }
    }

    /**
     * Sends an error event to all registered observers, including the source of the error and the
     * synchronization direction.
     *
     * @param error the error event to send.
     * @param syncDirection the synchronization direction in which the error occurred.
     */
    fun sendError(error: ErrorEvent, syncDirection: SynchronizationDirection) {
        for (modelListener in observer) {
            val event = ErrorEvent(this)
            event.sourceID = error.sourceID
            event.timestamp = LocalDateTime.now()
            event.message = error.message
            event.synchronizationDirection = syncDirection
            modelListener.handleError(event)
        }
    }

    class InfluxDatabase(
        private val influxDBClient: InfluxDBClient,
        private val bucket: String,
        private val org: String
    ) {
        private val logger = KotlinLogging.logger {}

        /** Speichert Werte für ein bestimmtes Modell in InfluxDB. */
        fun saveData(modelId: String, values: Map<String, Any?>) {
            val writeApi = influxDBClient.writeApiBlocking

            values.forEach { (property, value) ->
                val point = Point.measurement("model_data")
                    .addTag("modelId", modelId)
                    .addTag("property", property)
                    .addField("value", value.toString())
                    .time(Instant.now(), com.influxdb.client.domain.WritePrecision.MS)

                writeApi.writePoint(bucket, org, point)
            }
            logger.info { "[InfluxDB] Gespeicherte Daten für Modell '$modelId': $values" }
        }

        /** Ruft die zuletzt gespeicherten Werte eines Modells aus InfluxDB ab. */
        fun getData(modelId: String): Map<String, String> {
            val queryApi = influxDBClient.queryApi
            val fluxQuery = """
            from(bucket: "$bucket")
                |> range(start: -30d)
                |> filter(fn: (r) => r["_measurement"] == "model_data" and r["modelId"] == "$modelId")
                |> last()
        """.trimIndent()

            val tables: List<FluxTable> = queryApi.query(fluxQuery, org)
            val result = mutableMapOf<String, String>()

            tables.forEach { table ->
                table.records.forEach { record ->
                    val property = record.getValueByKey("property") as String
                    val value = record.getValueByKey("_value") as String
                    result[property] = value
                }
            }

            logger.info { "[InfluxDB] Abgerufene Daten für Modell '$modelId': $result" }
            return result
        }

        /** Holt den letzten Wert einer bestimmten Eigenschaft eines Modells aus InfluxDB. */
        fun getValue(modelId: String, property: String): String? {
            val queryApi = influxDBClient.queryApi
            val fluxQuery = """
            from(bucket: "$bucket")
                |> range(start: -30d)
                |> filter(fn: (r) => r["_measurement"] == "model_data" and r["modelId"] == "$modelId" and r["property"] == "$property")
                |> last()
        """.trimIndent()

            val tables: List<FluxTable> = queryApi.query(fluxQuery, org)
            val record = tables.firstOrNull()?.records?.firstOrNull()
            val value = record?.getValueByKey("_value") as? String

            logger.info { "[InfluxDB] Letzter Wert für '$property' in Modell '$modelId': $value" }
            return value
        }

        /** Löscht alle gespeicherten Werte eines Modells in InfluxDB (setzt Tags auf NULL). */
        fun deleteData(modelId: String) {
            val deleteApi = influxDBClient.deleteApi
            deleteApi.delete(
                OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC),
                "_measurement=\"model_data\" AND modelId=\"$modelId\"",
                bucket,
                org
            )
            logger.info { "[InfluxDB] Gelöschte Daten für Modell '$modelId'." }
        }

        /** Löscht einen bestimmten Wert einer Eigenschaft eines Modells. */
        fun deleteProperty(modelId: String, property: String) {
            val deleteApi = influxDBClient.deleteApi
            deleteApi.delete(
                OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC),
                "_measurement=\"model_data\" AND modelId=\"$modelId\" AND property=\"$property\"",
                bucket,
                org
            )
            logger.info { "[InfluxDB] Gelöschte Eigenschaft '$property' für Modell '$modelId'." }
        }

        fun printDatabase() {
            val queryApi = influxDBClient.queryApi
            // Abfrage: Alle Daten aus dem Bucket ab dem frühestmöglichen Zeitpunkt
            val fluxQuery = """
            from(bucket: "$bucket")
                |> range(start: 0)
        """.trimIndent()

            try {
                val tables: List<FluxTable> = queryApi.query(fluxQuery, org)
                val output = StringBuilder()
                tables.forEach { table ->
                    table.records.forEach { record ->
                        output.append("Record: ")
                        output.append(record.values.toString())
                        output.append("\n")
                    }
                }
                logger.info { "[InfluxDB] Vollständiger Datenbankinhalt:\n$output" }
            } catch (e: Exception) {
                logger.error { "[InfluxDB] Fehler beim Abfragen der Datenbank: ${e.message}" }
            }
        }
    }

}