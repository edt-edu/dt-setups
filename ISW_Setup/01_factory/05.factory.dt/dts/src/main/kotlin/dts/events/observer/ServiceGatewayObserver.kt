package dts.events.observer

import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.services.DemoSequenceService
import dts.logger
import kotlin.concurrent.withLock

class ServiceGatewayObserver(val service: DemoSequenceService) : IGatewayObserver {
    override fun handleDatapointEvent(dtEvent: NewDataPointEvent) {
        logger.info { "signaling new data" }
        service.lock.withLock {
            service.condition.signal()
        }
    }

    override fun handleError(error: ErrorEvent) {
        TODO("Not yet implemented")
    }
}