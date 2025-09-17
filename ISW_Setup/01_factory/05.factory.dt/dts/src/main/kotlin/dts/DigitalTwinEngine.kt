package dts

import dts.connection.SynchronizationDirection
import dts.connection.Synchronizer
import dts.events.*
import dts.events.observer.EngineWFObserver
import dts.events.observer.ModelObserver
import dts.events.observer.ServiceObserver
import dts.gateway.AbstractGateway
import dts.modelmanager.ModelManager
import dts.modelmanager.models.AbstractModel
import dts.services.AbstractServiceConnection
import dts.services.EngineServiceAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock

val logger = KotlinLogging.logger {}

/**
 * The DigitalTwinEngine is the main class of the DTS. It is responsible for configuring the DTS and synchronizing the
 * properties between the DT and the AbstractGateway.
 * @param serviceConnectionSet: Set of all services that are connected to the DTS
 * @param gatewaySet: Set of all gateways that are connected to the DTS
 * @param modelManagerSet: Set of all modelManagers that are connected to the DTS
 * @param remoteServices: Set of all services that are connected to the DTS via the EngineServiceAPI
 * @param observer: List of all observers that are notified when an event within the DTE occurs
 * @param synchronizer: The synchronizer is responsible for synchronizing the properties between the DT and the AbstractGateway
 * @param serviceRequestLock: This Lock is used to make sure that the threads don't try to access the same lists at the same time
 * @param timedSyncLock: This Lock is used to make sure that the threads don't try to access the same lists at the same time
 * @param triggeredSyncLock: This Lock is used to make sure that the threads don't try to access the same lists at the same time
 * @param serviceEventList: This List stores all the events that are to be executed by the corresponding threads
 * @param timedSyncEventList: This List stores all the events that are to be executed by the corresponding threads
 * @param triggeredSyncEventList: This List stores all the events that are to be executed by the corresponding threads
 * @param serviceRequestThread: This thread is responsible for executing service requests
 * @param timedSyncsThread: This thread is responsible for executing timed syncs
 * @param triggeredSyncsThread: This thread is responsible for executing triggered syncs
 * @param engineServiceAPI: This is the API that is used to connect services to the DTS during runtime
 */
class DigitalTwinEngine(
    val serviceConnectionSet: MutableSet<AbstractServiceConnection> = mutableSetOf(),
    val gatewaySet: MutableSet<AbstractGateway> = mutableSetOf(),
    var modelManager: ModelManager,
    private var remoteServices: MutableSet<String> = mutableSetOf(),
    var triggeredSyncTime: Long = 100,
    var timedSyncTime: Long = 100,
    var serviceRequestTime: Long = 100,
) {

    private val observer = mutableListOf<EngineWFObserver>()
    val synchronizer = Synchronizer()
    private val engineServiceAPI = EngineServiceAPI(ServiceObserver(this))

    /**
     * These Locks are used to make sure that the threads don't try to access the same lists at the same time
     */
    private val serviceRequestLock =  ReentrantLock()
    private val timedSyncLock =  ReentrantLock()
    private val triggeredSyncLock =  ReentrantLock()

    /**
     * These Lists store all the events that are to be executed by the corresponding threads
     */
    private val serviceEventList = mutableListOf<DTEvent>()
    private val timedSyncEventList = mutableListOf<DTEvent>()
    private val triggeredSyncEventList = mutableListOf<NewDataPointEvent>()

    //Store Errors in memory for easy retrieval
    private val errors = mutableListOf<ErrorEvent>()

    private val serviceRequestThread = Thread(Runnable {
        while (true) {
            Thread.sleep(serviceRequestTime)
            serviceRequestLock.lock()
            try {
                executeServiceRequest()
            } finally {
                serviceEventList.clear()
                serviceRequestLock.unlock()
            }
        }
    })

    private val timedSyncsThread = Thread(Runnable {
        while (true) {
            Thread.sleep(timedSyncTime)
            timedSyncLock.lock()
            try {
                executeTimedSync()
            } finally {
                timedSyncEventList.clear()
                timedSyncLock.unlock()
            }
        }
    })

    private val triggeredSyncsThread = Thread(Runnable {
        while (true) {
            Thread.sleep(triggeredSyncTime)
            executeTriggeredSync()
        }
    })

    init {
        this.synchronizer.configure(this)
        timedSyncsThread.start()
        serviceRequestThread.start()
        triggeredSyncsThread.start()
        engineServiceAPI.start()
        logger.info { "Engine started." }
        modelManager.addObserver(ModelObserver(this))
    }

    /**
     * Adds a new data point event to the list of events that are to be executed
     */
    fun registerTriggeredSyncEvent(dataPointEvent: NewDataPointEvent) {
        logger.info { "Trigger sync event received" }
        //if (triggeredSyncLock.tryLock()) {
        triggeredSyncLock.lock()
        try {
            triggeredSyncEventList.add(dataPointEvent)
        } finally {
            triggeredSyncLock.unlock()
        }
    }

    /**
     * Adds a new data point event to the list of events that are to be executed
     */
    fun registerTimedSyncEvent(dataPointEvent: NewDataPointEvent) {
        logger.info { "Timed sync event received" }
        if (timedSyncLock.tryLock()) {
            try {
                timedSyncEventList.add(dataPointEvent)
            } finally {
                timedSyncLock.unlock()
            }
        }
    }

    /**
     * This method executes a service task like retrieving data from a model or gateway
     */
    fun registerServiceEvent(dtEvent: DTEvent) {
        if (dtEvent is NewDataPointEvent) {
            println("NewDataPointEvent")
        }
        if (dtEvent is ServiceRequestEvent && serviceRequestLock.tryLock()) {
            try {
                serviceEventList.add(dtEvent)
            } finally {
                serviceRequestLock.unlock()
            }
        }
    }

    fun newEngineEvent(observedProperty: String, newValue: Any?, syncDirection: SynchronizationDirection) {
        observer.forEach { listener ->
            val event = NewDataPointEvent(this, observedProperty)
            event.sourceID = "engine"
            event.timestamp = LocalDateTime.now()
            event.synchronizationDirection = syncDirection
            event.message = newValue.toString()
            listener.handleEngineEvent(event, observedProperty)
        }
    }

    /**
     * This method is called when an error occurs. It is supposed to either log the error and forward it to the UI.
     */
    fun handleError(error: ErrorEvent) {
        logger.error { "'${error.message}' from ${error.sourceID}" }
        errors.add(error)
    }

    fun handleModelUpdateError(error: ModelUpdateErrorEvent) {
        logger.error { "'${error.message}' from ${error.sourceID}" }
        errors.add(error)
        //Retry or forget
    }

    /**
     * This method checks which gateway is responsible for a given property
     * @param property: The property that is checked
     * @return the gateway that is responsible for the property
     * @throws Exception if no gateway is responsible for the property
     */
    fun getResponsibleGateway(property: String): AbstractGateway {
        var responsibleGateway: AbstractGateway? = null
        gatewaySet.forEach {
            if (it.responsibleForID(property)) {
                responsibleGateway = it
            }
        }
        if (responsibleGateway != null)
            return responsibleGateway!!
        else
            throw Exception("No gateway is responsible for the property $property.")
    }

    /**
     * This method checks which modelManager is responsible for a given property
     * @param property: The property that is checked
     * @return the modelManager that is responsible for the property
     * @throws Exception if no modelManager is responsible for the property
     */
    fun getResponsibleModelManager(modelManagerId: String): AbstractModel {
        val responsibleModelManager= modelManager.returnAbstractModel(modelManagerId)
        if (responsibleModelManager != null)
            return responsibleModelManager
        else
            throw Exception("No model is responsible for the property $modelManagerId.")
    }

    /**
     * This method executes a triggered sync triggered from a gateway or model.
     *
     * Props are in general only send in one direction.
     * This combines properties with the same direction.
     */
    private fun executeTriggeredSync() {
        val propsFromGW: MutableSet<String> = mutableSetOf()
        val propsFromDT: MutableSet<String> = mutableSetOf()
        triggeredSyncLock.lock()
        try {
            triggeredSyncEventList.forEach { event ->
                when (event.synchronizationDirection) {
                    SynchronizationDirection.DT_TO_GATEWAY -> propsFromDT.addAll(event.properties)
                    SynchronizationDirection.GATEWAY_TO_DT -> propsFromGW.addAll(event.properties)
                }
            }
        } finally {
            triggeredSyncEventList.clear()
            triggeredSyncLock.unlock()
        }
        if (propsFromDT.isNotEmpty()) {
            synchronizer.synchronize(propsFromDT, SynchronizationDirection.DT_TO_GATEWAY)
        }
        if (propsFromGW.isNotEmpty()) {
            synchronizer.synchronize(propsFromGW, SynchronizationDirection.GATEWAY_TO_DT)
        }
    }

    /**
     * This method executes a timed sync triggered from a mapping
     */
    private fun executeTimedSync() {
        timedSyncEventList.forEach { event ->
            logger.warn { "${event.source} requested a timed sync, timed sync unavailable" }
        }
    }

    /**
     * This method executes a service request like retrieving data from a model or gateway
     */
    private fun executeServiceRequest() {
        serviceEventList.forEach { event ->
            serviceConnectionSet.forEach { service ->
                if (service == event.source) {
                    logger.debug { "Execute Service Request from:" + event.sourceID }
                    service.executeTask(event.message, this)
                }
            }
        }
    }

    fun addObserver(dteObserver: EngineWFObserver){
        observer.add(dteObserver)
        logger.info { "Observer $dteObserver added to $this"  }
    }

    fun removeObserver(dteObserver: EngineWFObserver){
        observer.remove(dteObserver)
        logger.info { "Observer $dteObserver removed from $this"  }
    }

    fun serviceJoin(dtEvent: DTEvent) {
        this.addRemoteService(dtEvent.message)
    }

    fun addGateway(gatewayListener: AbstractGateway) {
        this.gatewaySet.add(gatewayListener)
        logger.info { "Gateway added: " + gatewayListener.gatewayID }
    }

    fun removeGateway(gatewayListener: AbstractGateway) {
        this.gatewaySet.remove(gatewayListener)
        logger.info { "Gateway removed: " + gatewayListener.gatewayID }
    }
    fun addService(serviceListener: AbstractServiceConnection) {
        this.serviceConnectionSet.add(serviceListener)
        logger.info { "Service added: " + serviceListener.serviceID }
    }
    fun addRemoteService(serviceId: String) {
        this.remoteServices.add(serviceId)
        logger.info { "Remote service added: $serviceId" }
    }
    fun removeService(serviceListener: AbstractServiceConnection) {
        this.serviceConnectionSet.remove(serviceListener)
        logger.info { "Service removed: " + serviceListener.serviceID }
    }
    fun removeRemoteService(serviceId: String) {
        this.remoteServices.remove(serviceId)
        logger.info { "Remote service removed: $serviceId" }
    }
}