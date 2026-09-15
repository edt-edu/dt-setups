package dts.modelmanager

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfluxDatabaseTest {

    private lateinit var influxDBClient: InfluxDBClient
    private lateinit var influxDatabase: ModelManager.InfluxDatabase

    private val bucket = "isw"
    private val org = "isw"
    private val token = "VJsy5_MZSBzvTklLEXPImHi4rtg4Rb1uPo55DKxFqOuZZ25-HJlfvnZQSLoJP4PEdECYX-20ZI_ahI9zIiBdOA=="
    private val url = "http://localhost:8086"

    @BeforeAll
    fun setUp() {
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)
        influxDatabase = ModelManager.InfluxDatabase(influxDBClient, bucket, org)
    }

    @AfterAll
    fun tearDown() {
        influxDatabase.printDatabase()
        influxDBClient.close()
    }

    @Test
    fun `saveData should write values to InfluxDB`() {
        val modelID = "1-1-conveyor"
        val testData = mapOf("speed" to 100, "status" to "active")

        influxDatabase.saveData(modelID, testData)

        // Direkt nach dem Schreiben eine kurze Verzögerung, damit InfluxDB die Daten verarbeitet
        Thread.sleep(500)

        val storedData = influxDatabase.getData(modelID)
        assertEquals(testData.mapValues { it.value.toString() }, storedData)
    }

    @Test
    fun `getData should retrieve correct values from InfluxDB`() {
        val modelID = "1-2-clawGripper"
        val testData = mapOf("rotor-status" to "active", "arm-position" to "30")

        influxDatabase.saveData(modelID, testData)
        Thread.sleep(500)

        val retrievedData = influxDatabase.getData(modelID)
        assertEquals(testData.mapValues { it.value.toString() }, retrievedData)
    }

    @Test
    fun `deleteData should remove all values for a model in InfluxDB`() {
        val modelID = "1-3-indexedLine"
        val testData = mapOf("state" to "running", "package_at_mill" to "true")

        influxDatabase.saveData(modelID, testData)
        Thread.sleep(500)

        influxDatabase.deleteData(modelID)
        Thread.sleep(500)

        val retrievedData = influxDatabase.getData(modelID)
        assertEquals(emptyMap<String, String>(), retrievedData)
    }

    @Test
    fun `deleteProperty should remove a specific property from a model in InfluxDB`() {
        val modelID = "1-4-sortingLine"
        val testData = mapOf("color" to "red", "speed" to "200")

        influxDatabase.saveData(modelID, testData)
        Thread.sleep(500)

        influxDatabase.deleteProperty(modelID, "color")
        Thread.sleep(500)

        val expectedData = mapOf("speed" to "200")
        assertEquals(expectedData, influxDatabase.getData(modelID))
    }

    @Test
    fun `getValue should return the correct value from InfluxDB`() {
        val modelID = "1-5-vacuumGripper"
        val testData = mapOf("pressure" to "50")

        influxDatabase.saveData(modelID, testData)
        Thread.sleep(500)

        val retrievedValue = influxDatabase.getValue(modelID, "pressure")
        assertEquals("50", retrievedValue)
    }
}