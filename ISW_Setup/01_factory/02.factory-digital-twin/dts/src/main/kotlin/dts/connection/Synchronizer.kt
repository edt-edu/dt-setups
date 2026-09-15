package dts.connection

import dts.DigitalTwinEngine
import dts.IDataSource
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * This class is responsible for synchronizing the properties between the ModelManagers and the Gateways
 *
 */
class Synchronizer {
    private lateinit var dtEngineInstance: DigitalTwinEngine

    /**
     * Configures the Synchronizer with the mappings and the DigitalTwinEngine
     * @param mappingSet: contains every mappings of the DTS
     * @param engine: The instance of the DigitalTwinEngine
     */
    fun configure(engine: DigitalTwinEngine){
        this.dtEngineInstance = engine
    }

    /**
     * Synchronizes properties between the DT and the AbstractGateway
     * depending on the synchronisationDirection of the mapping
     * @param mapping: The mapping with the properties which are supposed to be updated
     */
    fun synchronize(properties: Set<String>, direction: SynchronizationDirection) {
        logger.info{ "Synchronize with Direction: $direction" }
        try {
            when (direction) {
                SynchronizationDirection.GATEWAY_TO_DT -> {
                    updateDT(properties)
                }
                SynchronizationDirection.DT_TO_GATEWAY -> {
                    updateGW(properties)
                }
            }
        } catch (e: Exception) {
            logger.error { "Synchronisation failed\n${e.printStackTrace()}" }
        }
    }

    /**
     * Updates the DT with the values from the AbstractGateway.
     * After getting the properties which are supposed to have an update it gets the corresponding values from
     * the AbstractGateway and then writes the date to the models
     * @param mapping: The mapping with the properties which are supposed to be updated
     */
    private fun updateDT(properties: Set<String>) {
        val updatedValuesFromGateway: Map<String, Any?> = extractValuesFromDatasource(properties, dtEngineInstance.gatewaySet)
        updatedValuesFromGateway.forEach { prop, value -> updateDtProperty(prop, value) }
    }

    /**
     * Updates the property of a ModelManager with the new value, without explicitly knowing the required ModelManager
     * @param property: The property which is supposed to be updated
     * @param newValue: The new value of the property
     */
    private  fun updateDtProperty(property: String, newValue: Any?) {
        logger.debug { "DT update of $property" }
        try {
            val mm = dtEngineInstance.modelManager
            if (mm.responsibleForID(property)) {
                mm.setValue(property, newValue)
            } else {
                logger.info { "unknown DT property $property" }
            }
            //dtEngineInstance.newEngineEvent(dtProperty, newValue, SynchronizationDirection.GATEWAY_TO_DT)
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
        }
    }

    private fun updateGW(properties: Set<String>) {
        val updatedValues = properties.associateWith { dtEngineInstance.modelManager.getValue(it) }
        updatedValues.forEach { prop, value -> updateGatewayProperty(prop, value) }
    }

    /**
     * Updates the properties of a Gateway with the new Value.
     * @param property: The property which is supposed to be updated
     * @param newValue: The new value of the property
     */
    private fun updateGatewayProperty(property: String, newValue: Any?) {
        logger.debug { "GW update of $property" }
        try {
            dtEngineInstance.gatewaySet
                .filter { it.responsibleForID(property) }
                .forEach { it.setValue(property, newValue)}
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
        }
    }

    /**
     * Extracts values from either all ModelManagers or all Gateways available
     * This method allows to extract values from multiple data sources at once
     * @param properties: The properties which are supposed to be extracted
     * @param dataSources: The data sources from which the values are supposed to be extracted
     * @return A map containing the properties as keys and the corresponding values as values
     */
    private fun extractValuesFromDatasource(properties: Set<String>, dataSources: Set<IDataSource>): Map<String, Any?> {
        val returnMap = mutableMapOf<String, Any?>()
        properties.forEach { property ->
            var returnValue: Any? = "No Value"
            dataSources.forEach {
                if (it.responsibleForID(property)) {
                    returnValue = it.getValue(property)
                }
            }
            if (returnValue == "No Value")
                throw Exception("No datasource is responsible for the property $property.")
            returnMap[property] = returnValue
        }
        return returnMap
    }
}