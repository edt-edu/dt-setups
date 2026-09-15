package dts.events.observer

import dts.events.DTEvent
import dts.events.ErrorEvent
import java.util.*

interface IMappingObserver : EventListener {
    fun handleTimedEvent(dtEvent: DTEvent)

    fun handleError(error: ErrorEvent)
}