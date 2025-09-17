package dts.dtengine.eventsystem.gateway.observer

import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dtengine.eventsystem.abstractevents.DTEvent

abstract class AbstractGatewayObserver(relatedGWid:String): AbstractDTObserver() {
    override fun handleEvent(event: DTEvent){
        println("GW observer: adding Event to Engine " + event.message)
        this.dtengine.gatewayEventQueue.enqueue(event)
    }
}