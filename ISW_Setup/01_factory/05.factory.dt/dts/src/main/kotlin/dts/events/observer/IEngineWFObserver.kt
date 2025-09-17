package dts.events.observer

import dts.events.DTEvent
import dts.events.ErrorEvent
import java.util.*

/**
 * mainly used by the timed events occurring in the engine loop
 */
interface IEngineWFObserver : EventListener {

    fun handleEngineEvent(dtEvent: DTEvent, observedProperty: String)

    fun handleError(error: ErrorEvent)
}