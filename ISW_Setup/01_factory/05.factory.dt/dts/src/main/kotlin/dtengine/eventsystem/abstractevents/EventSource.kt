package dts.dtengine.eventsystem.abstractevents

import dtengine.eventsystem.abstractevents.DTEvent

abstract class EventSource(id:String) {
    val listeners = mutableListOf<IDTObserver>()

    // Add a listener
    fun addListener(listener: IDTObserver) {
        listeners.add(listener)
    }

    // Remove a listener
    fun removeListener(listener: IDTObserver) {
        listeners.remove(listener)
    }

    // Notify all listeners of an event
    fun notifyListeners(event: DTEvent) {
        for (listener in listeners) {
            listener.handleEvent(event)
        }
    }
}