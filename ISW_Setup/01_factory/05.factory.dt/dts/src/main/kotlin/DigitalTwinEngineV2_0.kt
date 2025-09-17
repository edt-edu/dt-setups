package dts

import dts.dtengine.camunda.CamundaKonfig
import dts.dtengine.enginemappings.SynchronizationDirection
import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dts.dtengine.eventsystem.abstractevents.DTEventQueue
import dts.dtengine.eventsystem.abstractevents.EventSource
import dts.dtengine.eventsystem.database.observer.AbstractDBObserver
import dts.dtengine.eventsystem.database.observer.DBObserverImpl
import dts.dtengine.eventsystem.gateway.observer.AbstractGatewayObserver
import dts.dtengine.eventsystem.gateway.observer.GatewayObserverImpl
import dts.dtengine.eventsystem.modelrepo.observer.AbstractModelsObserver
import dts.dtengine.eventsystem.service.observer.AbstractServiceObserver
import dts.dtsystem.datastructures.Functionality
import dts.dtsystem.enginemappings.Mapping
import dts.dtsystem.enginemappings.Synchronizer
import dts.dtsystem.workflowcontroller.DigitalTwinEngineWorkflowController
import dts.dtsystem.workflowcontroller.implementations.bpmn.BPMNWorkflowController
import dts.gateway.AbstractGateway
import dts.gateway.implementations.EmptyGWImpl
import dts.modelmanager.AbstractDBAdapter
import dts.modelmanager.AbstractModelRepoAdapter
import dts.modelmanager.implementations.EmptyDBImpl
import dts.services.AbstractRequestingServices
import dts.services.AbstractUsedServices
import java.util.*

class DigitalTwinEngineV2_0(
    val requestingServices: MutableSet<AbstractRequestingServices> = mutableSetOf(),
    val usedServices: MutableSet<AbstractUsedServices> = mutableSetOf(),
    val dbAdapter: MutableSet<AbstractDBAdapter> = mutableSetOf(),
    val modelsAdapter: MutableSet<AbstractModelRepoAdapter> = mutableSetOf(),
    val gatewayAdapter: MutableSet<AbstractGateway> = mutableSetOf(),

    var serviceRequestEventQueue: DTEventQueue = DTEventQueue(),
    var serviceAnswerEventQueue: DTEventQueue = DTEventQueue(),
    var dbEventQueue: DTEventQueue = DTEventQueue(),
    var modelsRepoQueue: DTEventQueue = DTEventQueue(),
    var gatewayEventQueue: DTEventQueue = DTEventQueue(),

    var observerList: MutableList<AbstractDTObserver> = mutableListOf(),

    var dtEngineWFController: DigitalTwinEngineWorkflowController?,
    var synchronizer: Synchronizer
) {

    constructor() : this(
        mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf(),
        DTEventQueue(), DTEventQueue(), DTEventQueue(), DTEventQueue(), DTEventQueue(), mutableListOf(),
        null,
        Synchronizer()
    )

    private var gwsetChanged: Boolean = false
    private var dbsetChanged: Boolean = false
    private var mappingchanged: Boolean = false

    fun run() {

    }

    fun addSynchonizerMappings(
        from_id: String,
        from_property: String,
        to_id: String,
        to_property: String,
        direction: String
    ) {
        this.synchronizer.addMapping(
            Mapping(
                mappings = Pair(
                    mutableListOf(Pair(from_id, from_property)),
                    mutableListOf(Pair(to_id, to_property))
                ),
                synchronisationDirection = SynchronizationDirection.valueOf(direction)
            )
        )
    }

    fun removeSynchonizerMappings(id: String) {
        this.synchronizer.removeMapping(id)
    }

    fun registerComponent(es: EventSource, observer: AbstractDTObserver) {
        if (es is AbstractGateway) if (observer is AbstractGatewayObserver) {
            observer.componentid = es.id
            es.addObserver(observer)
            this.gatewayAdapter.add(es)
            this.observerList.add(observer)
            this.synchronizer.gateways.add(es)
        }
        if (es is AbstractDBAdapter) if (observer is AbstractDBObserver) {
            observer.componentid = es.id
            es.addObserver(observer)
            this.dbAdapter.add(es)
            this.observerList.add(observer)
            this.synchronizer.dbAdapters.add(es)
        }
        if (es is AbstractModelRepoAdapter) if (observer is AbstractModelsObserver) {
            observer.componentid = es.id
            es.addObserver(observer)
            this.modelsAdapter.add(es)
            this.observerList.add(observer)
            this.synchronizer.modelRepoAdapters.add(es)
        }
        if (es is AbstractUsedServices) if (observer is AbstractServiceObserver) {
            observer.componentid = es.id
            es.addObserver(observer)
            this.usedServices.add(es)
            this.observerList.add(observer)
        }
        if (es is AbstractRequestingServices) if (observer is AbstractServiceObserver) {
            observer.componentid = es.id
            es.addObserver(observer)
            this.requestingServices.add(es)
            this.observerList.add(observer)
        }
    }

    fun registerComponents(eslist: MutableList<Pair<EventSource, AbstractDTObserver>>) {
        for (x in eslist) registerComponent(x.first, x.second)
    }

    fun removeComponent(es: String) {
        this.gatewayAdapter.removeIf { x -> x.id.equals(es) }
        this.dbAdapter.removeIf { x -> x.id.equals(es) }
        this.modelsAdapter.removeIf { x -> x.id.equals(es) }
        this.usedServices.removeIf { x -> x.id.equals(es) }
        this.requestingServices.removeIf { x -> x.id.equals(es) }

        this.observerList.removeIf { x -> x.componentid.equals(es) }
    }
}

