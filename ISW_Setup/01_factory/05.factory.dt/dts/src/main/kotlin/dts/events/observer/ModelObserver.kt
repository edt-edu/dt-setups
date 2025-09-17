package dts.events.observer

import dts.DigitalTwinEngine
import dts.events.DTEvent
import dts.events.ErrorEvent
import dts.events.ModelUpdateErrorEvent
import dts.events.NewDataPointEvent

/**
 * if we encounter any problems when:
 * a) single models cannot be written?
 * b) composing models?
 * c) database has problems - faulty query?
 * d) composing databases?
 * e) ?
 */
class ModelObserver(private val dtEngine: DigitalTwinEngine): IModelObserver {
    override fun handleError(dtEvent: ErrorEvent) {
        dtEngine.handleError(dtEvent)
    }

    override fun modelUpdateError(event: ModelUpdateErrorEvent) {
        dtEngine.handleModelUpdateError(event)
    }

    override fun handleModelDatapointEvent(dtEvent: DTEvent) {
        if (dtEvent is NewDataPointEvent)
            dtEngine.registerTriggeredSyncEvent(dtEvent)

    }

}