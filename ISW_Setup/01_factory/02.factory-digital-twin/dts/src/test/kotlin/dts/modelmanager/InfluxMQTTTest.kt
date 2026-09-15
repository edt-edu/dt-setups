package dts.modelmanager

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import dts.mqtt.MqttHandler
import org.eclipse.paho.mqttv5.client.MqttClient
import org.junit.jupiter.api.*
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfluxMQTTTest {

    private lateinit var influxDBClient: InfluxDBClient
    private lateinit var influxDatabase: ModelManager.InfluxDatabase
    private lateinit var mqttHandler: MqttHandler

    // InfluxDB Konfiguration
    private val bucket = "isw"
    private val org = "isw"
    private val token = "VJsy5_MZSBzvTklLEXPImHi4rtg4Rb1uPo55DKxFqOuZZ25-HJlfvnZQSLoJP4PEdECYX-20ZI_ahI9zIiBdOA=="
    private val url = "http://localhost:8086"

    // MQTT Konfiguration
    private val brokerUrl = "tcp://192.168.1.104:1883"
    private val islandName = "island1"

    @BeforeAll
    fun setUp() {
        // InfluxDB Client initialisieren
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)
        influxDatabase = ModelManager.InfluxDatabase(influxDBClient, bucket, org)

        // MQTT Client initialisieren
        val clientId = "test-client-id"
        mqttHandler = MqttHandler()
        mqttHandler.connect(brokerUrl, clientId)
    }

    @Test
    fun publish() {
        mqttHandler.publish("1-1-conveyor/control", "{ \"motor\": \"right\" }")
    }

    @Test
    fun subscribe() {

    }


    @AfterAll
    fun tearDown() {
        influxDatabase.printDatabase()
        influxDBClient.close()
        mqttHandler.disconnect()
    }
}