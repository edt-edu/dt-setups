package dts.gateway

import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class LifecycleGatewayTest : GatewayTester<LifecycleGateway>() {
    @BeforeEach
    override fun setUp() {
        client = mock<IMqttAsyncClient>()
        dataSource = LifecycleGateway("test_gateway","test_island", client,
            machines = setOf("1-1-conveyor"))
        val callbackCaptor = ArgumentCaptor.forClass(IMqttMessageListener::class.java)
        verify(client).subscribe(any<Array<MqttSubscription>>(), any(), any(), callbackCaptor.capture(), any())
        callback = callbackCaptor.value
        super.setUp()
    }

    lateinit var client: IMqttAsyncClient
    lateinit var callback: IMqttMessageListener
    override lateinit var dataSource: LifecycleGateway

    @ParameterizedTest
    @CsvSource(
        "test_island/status",
        "test_island/stop",
        "test_island/machines/1-1-conveyor",
        "test_island/create/1-1-conveyor",
        "test_island/destroy/1-1-conveyor"
    )
    fun responsibleForID(id: String) {
        assertResponsibleFor(id)
    }

    @ParameterizedTest
    @CsvSource(
        "test_island/status,up",
        "test_island/status,destroyed",
        "test_island/machines/1-1-conveyor,up",
    )
    fun getReadOnlyValue(property: String, value: String) {
        val message = mock<MqttMessage>()
        `when`(message.toString()).thenReturn(value)

        callback.messageArrived(property, message)

        assertDataPointEvents(1)
        assertGetProperty(property, value)
    }

    @ParameterizedTest
    @CsvSource(
        "test_island/stop,true,destroy",
        "test_island/create/1-1-conveyor,{xyz},{xyz}",
        "test_island/destroy/1-1-conveyor,false,destroy",
    )
    fun setWriteOnlyValue(property: String, value: String, message: String) {
        dataSource.setValue(property, value)

        verify(client, times(1)).publish(property, message.toByteArray(), 2, false)
        assertDataPointEvents(0)
        assertErrorEvents(0)
    }

    @Test
    fun addMachine() {
        dataSource.addMachine("1-2-machine")

        assert(dataSource.hasMachine("1-2-machine"))
        assertResponsibleFor("test_island/machines/1-2-machine")
        assertResponsibleFor("test_island/create/1-2-machine")
        assertResponsibleFor("test_island/destroy/1-2-machine")
    }

    @Test
    fun removeMachine() {
        dataSource.addMachine("1-2-machine")

        dataSource.removeMachine("1-2-machine")

        assertNotResponsibleFor("test_island/machines/1-2-machine")
        assertNotResponsibleFor("test_island/create/1-2-machine")
        assertNotResponsibleFor("test_island/destroy/1-2-machine")
    }

    @Test
    fun autoAddMachine() {
        val status = "machine status"
        val message = mock<MqttMessage>()
        `when`(message.toString()).thenReturn(status)

        callback.messageArrived("test_island/machines/1-6-vg", message)

        assertDataPointEvents(1)
        assertGetProperty("test_island/machines/1-6-vg", status)
        assert(dataSource.hasMachine("1-6-vg"))
        assertResponsibleFor("test_island/machines/1-6-vg")
        assertResponsibleFor("test_island/create/1-6-vg")
        assertResponsibleFor("test_island/destroy/1-6-vg")
    }
}