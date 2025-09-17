package dts.dtengine.eventsystem.service.observer


import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dtengine.eventsystem.abstractevents.DTEvent

abstract class AbstractServiceObserver: AbstractDTObserver() {
    override fun handleEvent(event: DTEvent){
        this.dtengine.gatewayEventQueue.enqueue(event)
    }
}