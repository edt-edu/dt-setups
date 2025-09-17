package dts

/**
 * This interface is the blueprint for all data access classes.
 */
interface IDataSource {

    /**
     * This function returns the value for the given property.
     * This function should only be called if the data source is responsible for the given property.
     * @param id The id of the desired property
     * @return The value as a nullable Any
     */
    fun getValue(id: String): Any?

    /**
     * This function returns the values for the given properties.
     * This function should only be called if the data source is responsible for the given properties.
     * @param idSet The set of ids of the desired properties
     * @return The values as a Map<String, Any?>
     */
    fun getValues(idSet: Set<String>): Map<String, Any?> {
        val values = mutableMapOf<String, Any?>()
        idSet.forEach { property ->
            values[property] = getValue(property)
        }
        return values
    }

    fun getAllValues(): Map<String, Any?>


    /**
     * This function sets the value for the given property.
     * This function should only be called if the data source is responsible for the given property.
     * @param id The id of the desired property
     * @param value The value to be set
     */
    fun setValue(id: String, value: Any?)

    /**
     * This function sets the values for the given properties.
     * This function should only be called if the data source is responsible for the given properties.
     * @param values The values to be set as a Map<String, Any> with the id as key and the value as value
     */
    fun setValues(values: Map<String, Any>) {
        values.keys.forEach { id ->
            setValue(id, values[id])
        }
    }

    /**
     * This function returns true if the data source is responsible for the given property.
     * @param id The id of the property
     * @return True if the data source is responsible for the given property, false otherwise
     */
    fun responsibleForID(id: String): Boolean {
        return propertyIDs.contains(id)
    }

    /**
     * This Set contains all IDs that are available on the data source, meaning the properties for which the data source is responsible.
     */
    val propertyIDs: Set<String>

}