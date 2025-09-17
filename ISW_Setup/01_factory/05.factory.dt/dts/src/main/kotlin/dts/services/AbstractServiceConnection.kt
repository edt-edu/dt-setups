package dts.services

import dts.DigitalTwinEngine
import dts.events.observer.IServiceObserver
import dts.logger

/**
 * This class is the abstract class for all connections to services
 * @param live: This parameter is there to indicate whether the service can be contacted or not
 * @param serviceID: This parameter is there to identify the service, like for events
 */
abstract class AbstractServiceConnection(var live: Boolean = true, val serviceID: String) {

    val observer = mutableListOf<IServiceObserver>()

    fun addObserver(serviceObserver: IServiceObserver){
        observer.add(serviceObserver)
        logger.info { "Observer $serviceObserver added to $this"  }
    }

    /**
     * This method is there to execute a task (like retrieving data from a model or gateway), which was requested by the service
     */
    abstract fun executeTask(taskToExecute: String, digitalTwinEngine: DigitalTwinEngine)

    /**
     * This method is there to contact the service without being tasked by it prior e.g. to push data to it
     */
    abstract fun serviceUpdate(digitalTwinEngine: DigitalTwinEngine, observedProperty: String)

    /**
     * This method runs in the background and listens to the Service for incoming requests
     */
    abstract fun listen()
}