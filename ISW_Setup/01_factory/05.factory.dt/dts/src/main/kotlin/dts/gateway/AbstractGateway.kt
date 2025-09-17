package dts.gateway

import dts.IDataSource
import dts.events.ErrorEvent
import dts.events.observer.IGatewayObserver
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * This class represents any form of data source on the side that is not the DT e.g. an CPS
 * @param propertyIDs: The IDs of the properties that are available on the gateway
 * @param gatewayID: The ID of the gateway
 * @param observer: The list of observers that are registered to this gateway
 */
abstract class AbstractGateway(override val propertyIDs: Set<String>,
                               val gatewayID : String,
                               val observer : MutableList<IGatewayObserver> = mutableListOf()
): IDataSource {

    val logger = KotlinLogging.logger(this::class.java.name)
    /**
     * This adds a gateway listener to the gateway
     */
    fun addObserver(gatewayObserver: IGatewayObserver) {
        observer.add(gatewayObserver)
        logger.info { "Observer $gatewayObserver added to $this"  }
    }

    /**
     * This removes a gateway Listener form the gateway
     */
    fun removeObserver(gatewayObserver: IGatewayObserver) {
        observer.remove(gatewayObserver)
        logger.info { "Observer $gatewayObserver removed from $this"  }
    }


    /**
     * This method runs in the background and listens for data from external data source e.g. CPS
     */
    abstract fun listen()

    /**
     * Alternatively, this method is used to directly ask the source for data
     */
    abstract fun getDataFromSource()

    /**
     * This method is used to send an error to the DT
     */
    abstract fun sendError(error: ErrorEvent)

}