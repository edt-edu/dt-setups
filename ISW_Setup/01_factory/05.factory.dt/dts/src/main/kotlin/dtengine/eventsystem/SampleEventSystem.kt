package dts.dtengine.eventsystem

import dts.dtengine.eventsystem.abstractevents.IDTObserver
import dtengine.eventsystem.abstractevents.DTEvent
import dts.dtengine.eventsystem.abstractevents.DTEventQueue

class SampleEventSystem {
     var eventSource = SampleEventSource("000")
     var eventQueue = DTEventQueue()

    // Register an event listener
    fun registerListener(listener: IDTObserver) {
        eventSource.addListener(listener)
    }

    // Add an event to the queue
    fun postEvent(event: DTEvent) {
        eventQueue.enqueue(event)
    }

    // Process all events in the queue
    fun processAllEvents() {
        eventQueue.getAllEvents()
    }
}