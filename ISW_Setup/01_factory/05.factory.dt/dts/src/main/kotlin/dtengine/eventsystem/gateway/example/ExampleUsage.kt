package dts.dtsystem.eventsystem.neweventsystem.gateway.example

import dts.dtengine.eventsystem.SampleEventSystem
import dts.dtsystem.datastructures.DataProbe
import dts.dtengine.eventsystem.gateway.observer.GatewayObserverImpl
import dts.events.neweventsystem.gateway.events.GatewayNewDatapointEvent
import java.time.LocalDateTime

class ExampleUsage {
}

// Example usage
fun main() {
    // dataprobe example
    val dataprobe1 = DataProbe("probeID1", "none", "gatewayID1", "String", "none", LocalDateTime.now())
    val dataprobe2 = DataProbe("probeID2", "none", "gatewayID2", "String", "none", LocalDateTime.now())

    // Define specific event listeners
    /**
     //long definition
    val temperatureListener = object : GatewayObserver {
        override fun handleEvent(event: DTEvent) {
            println("Temperature Listener received event with data: ${event.content}")
        }
    }

    val sourceListener = object : GatewayObserver {
        override fun handleEvent(event: DTEvent) {
            println("Source Listener received event with data: ${event.content}")
        }
    }*/

    val temperatureListener= GatewayObserverImpl("000")
    val sourceListener = GatewayObserverImpl("000")

    // Create the event system, this is for example purpose, in production the usage of the engine is necessary
    val eventSystem = SampleEventSystem()

    // Register listeners
    eventSystem.registerListener(temperatureListener)
    eventSystem.registerListener(sourceListener)

    // Post events to the queue
    eventSystem.postEvent(GatewayNewDatapointEvent(eventSystem, dataprobe1))
    eventSystem.postEvent(GatewayNewDatapointEvent(eventSystem, dataprobe2))

    // Process all events
    eventSystem.processAllEvents()
}