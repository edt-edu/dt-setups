package dts.events.observer

import dts.events.DTEvent
import dts.events.ErrorEvent
import java.util.*

interface IServiceObserver: EventListener {
    fun handleModelUpdateQuery(dtEvent: DTEvent)
    fun handleModelGet(dtEvent: DTEvent)
    fun handleEngineUpdateQuery(dtEvent: DTEvent)
    fun handleError(error: ErrorEvent)
    fun serviceJoin(dtEvent: DTEvent)
}