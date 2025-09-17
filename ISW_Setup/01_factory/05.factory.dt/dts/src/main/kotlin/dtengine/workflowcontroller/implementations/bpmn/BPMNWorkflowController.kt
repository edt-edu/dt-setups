package dts.dtsystem.workflowcontroller.implementations.bpmn

import dts.DigitalTwinEngineV2_0
import dts.dtengine.camunda.CamundaKonfig
import dts.dtengine.camunda.ProcessDeployment
import dts.dtengine.camunda.ProcessRunner
import dts.dtengine.camunda.Executer
import dts.dtengine.eventsystem.service.event.FrontEndDataRequestEvent
import dtengine.eventsystem.abstractevents.DTEvent
import dts.dtsystem.workflowcontroller.DigitalTwinEngineWorkflowController
import dts.events.neweventsystem.gateway.events.GatewayNewDatapointEvent
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import kotlin.concurrent.thread

class BPMNWorkflowController(dtengine: DigitalTwinEngineV2_0) :
    DigitalTwinEngineWorkflowController(dtengine) {

    private val logger = LoggerFactory.getLogger(BPMNWorkflowController::class.java)

    var tickrate = 1
    var thread = Thread {
        this.execute()
    }

    private val processEngine: ProcessEngine
    private val deployment: ProcessDeployment
    private val processRunner: ProcessRunner
    private val processDir = "src/main/resources/processdir"
    private val runtimeService: RuntimeService
    private val executer: Executer

    override fun handle() {
        TODO("Not yet implemented")
    }

    init {
        val camundaKonfig = CamundaKonfig()
        processEngine = camundaKonfig.createProcessEngine()
        deployment = ProcessDeployment(processEngine)
        processRunner = ProcessRunner(processEngine)
        runtimeService = processEngine.runtimeService
        executer = Executer(dtengine.serviceRequestEventQueue)
        startFileWatcher()
    }

    private fun startFileWatcher() {
        thread(start = true) {
            val watchService = FileSystems.getDefault().newWatchService()
            val path = Paths.get(processDir)
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE)

            while (true) {
                val key = watchService.take()
                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    val fileName = event.context() as String
                    if (fileName.toString().endsWith(".bpmn")) {
                        logger.info("BPMN file changed: {}. Redeploying process...", fileName)
                        redeployProcesses(fileName)
                    }
                }
                key.reset()
            }
        }
    }

    private fun redeployProcesses(fileName: String) {
        val key = deployment.redeploy(fileName)
        processRunner.startProcessInstance(key)
        val dbAdapter=dtengine.dbAdapter
        dbAdapter.forEach { dbObject ->
            val status = dbObject.readFromDB()
            runtimeService.createMessageCorrelation("ProcessUpdatedMessage")
                .setVariable(dbObject.id +"_status", status)
                .correlateAll()
            logger.info("Sent ProcessUpdatedMessage with variable: $status")
        }
        logger.info("Processes redeployed. Sent message: ProcessUpdatedMessage")
    }

    override fun execute() {
        var curtime = 0
        val keys = deployment.deployBpmnProcessesFromDirectory(processDir)
        processRunner.startMultipleProcessInstances(keys)
        while (true) {
            if (!dtengine.gatewayEventQueue.isEmpty() || !dtengine.dbEventQueue.isEmpty()) {
                curtime = handleGatewayEvents(curtime)
            }
            if (!dtengine.serviceRequestEventQueue.isEmpty())
                handleServiceRequestEvents()

            if (curtime >= tickrate) curtime = 0
            curtime += 1
            Thread.sleep(1)
        }
    }

    private fun handleGatewayEvents(curtime: Int): Int {
        var curtime1 = curtime
        logger.info("Controller: handling number of events: {}", dtengine.gatewayEventQueue.queue.size)
        for (event in dtengine.gatewayEventQueue.getAllEvents()) {
            if (event is GatewayNewDatapointEvent) {
                signalEventReceived(event,"GatewayNewDatapointEvent")
                for (map in dtengine.synchronizer.mappings)
                    if (map.mappings.first.contains(Pair(event.sourceID, event.content.attributeProvider)))
                        curtime1 = this.dtengine.synchronizer.sync(curtime1)
            }
        }
        return curtime1
    }

    private fun signalEventReceived(event: DTEvent, type:String) {
        runtimeService.signalEventReceived(
            type,
            mapOf(
                "sourceID" to event.sourceID,
                "type" to event.content.typeProvider,
                "value" to event.content.getSource()
            )
        )
        logger.info("Received BPMN Signal: $type")
    }

    override fun handleServiceRequestEvents() {
        for (event in this.dtengine.serviceRequestEventQueue.getAllEvents()) {
            if (event is FrontEndDataRequestEvent) {
                val requester = this.dtengine.requestingServices.firstOrNull { it.id == event.sourceID }
                if (requester != null) {
                    val probe = event.content
                    when (probe.getFunctionName()) {
                        "getCurrentData" -> {
                            val collectedData = this.dtengine.dbAdapter.map { it.values }.toMutableList()
                            requester.post(collectedData)
                            logger.info("Processed getCurrentData request for sourceID: {}", event.sourceID)
                        }
                        "getAllData" -> {
                            val collectedData = this.dtengine.dbAdapter.flatMap { it.getAllHistory() }.toMutableList()
                            requester.post(collectedData)
                            logger.info("Processed getAllData request for sourceID: {}", event.sourceID)
                        }
                        else -> logger.warn("Unknown request type: {}", probe.getFunctionName())
                    }
                } else {
                    logger.warn("Requester with ID {} not found", event.sourceID)
                }
            } else {
                signalEventReceived(event,event.javaClass.name)
            }
        }
    }
}