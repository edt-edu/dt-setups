package dts.events.observer

import dts.DigitalTwinEngine
import dts.events.DTEvent
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent

class MappingObserver(private val dtEngine: DigitalTwinEngine) :IMappingObserver {
    /**
     * This function is called when a timed event occurs.
     */
    override fun handleTimedEvent(dtEvent: DTEvent) {

        if (dtEvent is NewDataPointEvent) {
            dtEngine.registerTimedSyncEvent(dtEvent)
        }
    }

    override fun handleError(error: ErrorEvent) {
        dtEngine.handleError(error)
    }
}