### Overview

The fundamental concept is to implement an observer pattern where every class can act as both an observer and observable. The definition of an observer can be found in the section below, titled "Event Throwing". Details on the event processing within the Digital Twin Engine (DTE), where the majority of events are ultimately handled, are available in the last section titled "Event Handling in DTE."

---- 

### Observer 

Technically speaking, no instance of the basic classes in the Digital Twin System (MM, GW, DTE, ...) serves as an observer itself. Instead, this is achieved through dedicated helper classes. These specific observer classes hold the instance that wishes to be an observer as an attribute and provide `handle` methods through wich a desired functionality is achieved. Here is an example of the Mapping observer class. 

```
class MappingObserver(private val dtEngine: DigitalTwinEngine) :IMappingObserver {

    override fun handleTimedEvent(dtEvent: DTEvent) {

        if (dtEvent is NewDataPointEvent) {
            dtEngine.registerTimedSyncEvent(dtEvent)
        }
    }

    override fun handleError(error: ErrorEvent) {
        dtEngine.handleError(error)
    }
}
```


It's noteworthy that this class implements the IMappingObserver interface, enabling instances of different classes to act as observers of the Mappings while maintaining a consistent structure. For instance, if a specific Gateway were interested in the given mapping, we might have a class named something like `GatewayMappingObserver`. This class would implement the `handleTimedEvent()` and `handleError()` methods inherited from the IMappingObserver interface in a manner that is meaningful for the context of a Gateway observing a Mapping.


----


#### Event throwing

Events are thrown by various classes, with any class able to initiate events if it meets specific requirements. This flexibility allows for decentralized and adaptable event-driven communication within the system.  

At a minimum, these classes require a list containing all Observers, a method for creating new events, and ideally, their own thread dedicated to creating and passing these events to the listeners. Given the substantial similarity in implementation across various use cases, let's examine how it is implemented in the Mapping class. 

The desired functionality to be implemented with the event system involves each mapping initiating its own synchronization event at a predetermined interval, referred to as the synchronizeInterval. In this way, the Synchronizer doesn't need to check whether mappings require an update. Instead, each mapping informs the Digital Twin Engine (DTE) and, consequently, the Synchronizer that it desires synchronization. This communication is facilitated through the `createNewSyncEvent` method within the mapping class.

The challenge that emerges is how to create a streamlined version of events that retains sufficient information for most use cases. Through exploration, it has been determined that the most crucial information includes the timestamp indicating when the event was thrown, the source as a reference to the instance, a sourceID for cases where the source may generate numerous events and further differentiation is needed, a synchronization direction (as in the current example), and a message field for additional instructions or to convey error codes. This comprehensive set of information ensures that the event system is versatile and can cater to a wide range of scenarios within the Digital Twin System.


```` 
    private fun createNewSyncEvent() {
        logger.debug { this.connectionID + ": create new sync event" }
        for(wfListener: IMappingObserver in listeners){
            val event = NewDataPointEvent(this)
            event.sourceID= connectionID
            event.timestamp = LocalDateTime.now()
            event.synchronizationDirection = synchronisationDirection
            wfListener.handleTimedEvent(event)
        }
    }
`````


The "createNewEvent" methods are invoked within a thread that either checks a specific condition or, in this case, executes at predefined intervals. For example, in a Gateway, this process might involve checking for updates from the Cyber-Physical System (CPS) every x seconds.

````
    private val thread = Thread(Runnable {
        while (true) {
            createNewSyncEvent()
            Thread.sleep(synchronizeInterval)
        }

    })
````

----


#### Event handling in DTE

Upon being thrown, the events destined for the DTE register themselves in one of three lists that store waiting events: the `serviceEventList`, `timedSyncEventList`, and `triggeredSyncEventList`.  The events in these lists are each processed by its respective thread. 

The three threads responsible for concurrently processing (aka handling) events within the DTE are implemented in a similar fashion. The example provided below, illustrating the `triggeredSyncsThread`, offers insight on how this functionality operates.


````
    private val triggeredSyncsThread = Thread(Runnable {
        while (true) {
            Thread.sleep(10)
            if (triggeredSyncEventList.isNotEmpty() && triggeredSyncLock.tryLock()) {
               executeTriggeredSync()
            }
        }
    })
````

At intervals of ten milliseconds, we assess the presence of events in the query and whether the collection, where the events are stored, is currently modifiable. If both conditions are met, an `execute` command is initiated.   
The implementation of the `execute` command is similar across all threads, with the `triggeredSync` serving as a representative example:

```
    private fun executeTriggeredSync() {
        try {
            triggeredSyncEventList.forEach { event ->
                if (event.synchronizationDirection == SynchronizationDirection.GATEWAY_TO_DT){
                    val gateway = event.source as AbstractGateway
                    synchronizer.mappingSet.forEach { mapping ->
                        if (checkForPropertiesInMapping(mapping, gateway.propertyIDs)) {
                            logger.debug { "Triggered Sync triggered from:" + event.sourceID }
                            synchronizer.synchronize(mapping)
                        }
                    }
                }
                else if (event.synchronizationDirection == SynchronizationDirection.DT_TO_GATEWAY){
                    val modelManager = event.source as AbstractModelManager<*>
                    synchronizer.mappingSet.forEach { mapping ->
                        if (checkForPropertiesInMapping(mapping, modelManager.propertyIDs)) {
                            logger.debug { "Triggered Sync triggered from:" + event.sourceID }
                            synchronizer.synchronize(mapping)
                        }
                    }
                }
            }
        } finally {
            triggeredSyncEventList.clear()
            triggeredSyncLock.unlock()
        }
    }
```