package dts.events.observer

import dts.DigitalTwinEngine
import dts.connection.SynchronizationDirection
import dts.events.DTEvent
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.services.AbstractServiceConnection

/**
 * This class is the observer for the engine. It is called when a timed event occurs.
 * @param dtEngine instance of the DTE
 * @param serviceSet set of services that are subscribed to the engine
 */

class EngineWFObserver(private val dtEngine: DigitalTwinEngine, private val serviceSet: Set<AbstractServiceConnection> = mutableSetOf()): IEngineWFObserver {
    /**
     * If within the engine something noteworthy for subscribed services happens, this function broadcasts the event to all those services.
     * @param dtEvent the event that occurred
     * @param observedProperty the property that was observed
     */
    override fun handleEngineEvent(dtEvent: DTEvent, observedProperty: String) {
        if (dtEvent is NewDataPointEvent && dtEvent.synchronizationDirection == SynchronizationDirection.GATEWAY_TO_DT) {
            serviceSet.forEach { it.serviceUpdate(dtEngine, observedProperty) }
        }
    }

    override fun handleError(error: ErrorEvent) {
        dtEngine.handleError(error)
    }


}