package dts.events.observer

import dts.events.DTEvent
import dts.events.ErrorEvent
import dts.events.ModelUpdateErrorEvent
import java.util.*

interface IModelObserver: EventListener {

    fun handleError(dtEvent: ErrorEvent)

    fun modelUpdateError(event: ModelUpdateErrorEvent)

    fun handleModelDatapointEvent(dtEvent: DTEvent)
}