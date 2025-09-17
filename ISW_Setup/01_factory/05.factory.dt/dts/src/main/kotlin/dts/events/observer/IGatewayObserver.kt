package dts.events.observer

import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import java.util.*

interface IGatewayObserver : EventListener{
    fun handleDatapointEvent(dtEvent: NewDataPointEvent)

    fun handleError(error: ErrorEvent)
}