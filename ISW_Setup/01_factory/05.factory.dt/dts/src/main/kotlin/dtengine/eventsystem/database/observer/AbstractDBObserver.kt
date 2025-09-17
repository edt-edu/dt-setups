package dts.dtengine.eventsystem.database.observer

import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dtengine.eventsystem.abstractevents.DTEvent

abstract class AbstractDBObserver(relatedDBid:String): AbstractDTObserver() {
    override fun handleEvent(event: DTEvent){
        this.dtengine.gatewayEventQueue.enqueue(event)
    }
}