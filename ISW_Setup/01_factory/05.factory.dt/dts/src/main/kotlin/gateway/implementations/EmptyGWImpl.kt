package dts.gateway.implementations

import dts.dtengine.eventsystem.gateway.observer.AbstractGatewayObserver
import dts.dtsystem.datastructures.DataProbe
import dts.dtsystem.datastructures.Functionality
import dts.events.neweventsystem.gateway.events.GatewayNewDatapointEvent
import dts.gateway.AbstractGateway
import java.time.LocalDateTime
import java.util.UUID

class EmptyGWImpl(
    id: String, observers: MutableList<AbstractGatewayObserver>, properties: MutableSet<String>,
    values: MutableMap<String, Any?>, functions: MutableSet<Functionality>, thread: Thread?
) : AbstractGateway(
    id, observers,
    properties, values, functions, thread
) {
    constructor() : this("", mutableListOf(), mutableSetOf(), mutableMapOf(), mutableSetOf(), null)
    var tickrate = 2

    override fun start() {
        this.thread = Thread {
            var time = 0
            // Code to execute in the new thread
            while (true) {
                if (time == tickrate) {
                    println("GW: read value at "+ time)
                    readFromGW()
                    time = 0
                }
                time += 1
                Thread.sleep(1)
            }

        }
        this.thread!!.start() // Starts the thread
    }

    override fun findByTimestamp() {
        println(LocalDateTime.now())
    }

    override fun updateToGW() {
        println("available:" + this.functions)
    }

    override fun readFromGW() {
        for (value in this.values) {
            val rnds = (0..10).random()
            this.values.set(value.key, rnds)

            var gwe = GatewayNewDatapointEvent()
            gwe.sourceID = this.id
            var dataprobe = DataProbe(UUID.randomUUID().toString(), value.key, rnds.toString(), value.key,"",
                LocalDateTime.now())
            gwe.timestamp = LocalDateTime.now()
            gwe.content = dataprobe
            gwe.message = "New Datapoint(" + value.key + ") received at " + LocalDateTime.now().toString()

            for (obs in this.observers) obs.handleEvent(gwe)
        }
    }
}