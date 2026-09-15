package dts.events.observer

import dts.DigitalTwinEngine
import dts.events.DTEvent
import dts.events.ErrorEvent

class ServiceObserver(private val dtEngine: DigitalTwinEngine) :IServiceObserver {
    override fun handleModelUpdateQuery(dtEvent: DTEvent) {
        TODO("Not yet implemented")
    }

    override fun handleModelGet(dtEvent: DTEvent) {
        dtEngine.registerServiceEvent(dtEvent)
    }

    override fun handleEngineUpdateQuery(dtEvent: DTEvent) {
        TODO("Not yet implemented")
    }

    override fun handleError(error: ErrorEvent) {
        dtEngine.handleError(error)
    }

    override fun serviceJoin(dtEvent: DTEvent) {
        dtEngine.serviceJoin(dtEvent)
    }
}