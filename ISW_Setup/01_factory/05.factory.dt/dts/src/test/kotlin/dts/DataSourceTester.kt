package dts

import org.junit.jupiter.api.Assertions.*

/**
 * Helper class for testing an `IDatasource`.
 */
abstract class DataSourceTester<T: IDataSource> {
    /** DataSource under test */
    abstract var dataSource: T

    /**
     * Assert that the `dataSource` contains the property with the given value.
     * It optionally tests the last updated time.
     *
     * @param property The property to access
     * @param value The expected value of the property
     * @param lastUpdatedBefore If not null, the upper bound for the last update
     */
    open fun assertGetProperty(property: String, value: Any?) {
        assertEquals(value, dataSource.getValue(property))
    }

    /**
     * Assert that multiple properties have a given value.
     *
     * @param values Map of the propertyIDs to values that should be present in the data source.
     */
    open fun assertGetProperties(values: Map<String, Any?>) {
        values.forEach { (property, value) -> assertGetProperty(property, value) }
    }

    /**
     * Assert that the given property can be set to the give value.
     *
     * @param property The property to test
     * @param value The target value
     */
    open fun assertSetProperty(property: String, value: Any?) {
        dataSource.setValue(property, value)
    }

    /**
     * Assert that all given properties can be set to the given values.
     *
     * @param values Map of the propertyIDs to values to set
     */
    open fun assertSetProperties(values: Map<String, Any?>) {
        values.forEach { (property, value) -> assertSetProperty(property, value) }
    }

    /**
     * Assert that the data source is responsible for the given property.
     *
     * @param property The property to test
     */
    open fun assertResponsibleFor(property: String) {
        assertTrue(dataSource.responsibleForID(property))
    }

    /**
     * Assert that the data source is responsible for the given properties.
     *
     * @param properties The properties to test
     */
    open fun assertResponsibleFor(properties: Set<String>) {
        properties.forEach { value -> assertResponsibleFor(value)}
    }

    /**
     * Assert that the data source is not responsible for the given property.
     *
     * @param property The property to test
     */
    open fun assertNotResponsibleFor(property: String) {
        assertFalse(dataSource.responsibleForID(property))
    }
}