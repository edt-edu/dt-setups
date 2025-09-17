package dts.dtengine.eventsystem.modelrepo.observer

import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dtengine.eventsystem.abstractevents.DTEvent

abstract class AbstractModelsObserver(relatedModid:String): AbstractDTObserver() {
    override fun handleEvent(event: DTEvent){
        this.dtengine.gatewayEventQueue.enqueue(event)
    }
}