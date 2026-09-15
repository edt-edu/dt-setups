package dts.mqtt

import dts.DigitalTwinEngine
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.events.observer.GatewayObserver
import dts.events.observer.IGatewayObserver
import dts.gateway.IslandGateway
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttAsyncClient

class GatewayImplementation(brokerUrl: String, clientId: String, gatewayID: String, dts: DigitalTwinEngine) : IGatewayObserver {

    private val gateway: IslandGateway
    private val observer: GatewayObserver = GatewayObserver(dts)
    private var propertyMap = HashMap<String, Any?>()

    init {
        val client = MqttAsyncClient(brokerUrl, clientId)
        val options = MqttConnectionOptions()
        options.isAutomaticReconnect = true
        options.isCleanStart = true
        client.connect(options).waitForCompletion()

        gateway = IslandGateway(gatewayID, client)
        gateway.addObserver(observer)

        println("GatewayManager initialized and listening!")
    }

    fun getPropertyValue(propertyID: String): Any? {
        return gateway.getValue(propertyID)
    }

    fun setControlValue(propertyID: String, value: Map<String, Any?>) {
        gateway.setValue(propertyID, value)
    }

    fun getAllValues() {
        propertyMap = gateway.getAllValues() as HashMap<String, Any?>
    }

    override fun handleDatapointEvent(dtEvent: NewDataPointEvent) {
        println("Neue Daten empfangen für ${dtEvent.sourceID}:")
        val value = gateway.getValue(dtEvent.sourceID)
        println("→ Wert: $value")
    }

    override fun handleError(error: ErrorEvent) {
        println("Fehler empfangen von ${error.sourceID}: ${error.message}")
    }
}