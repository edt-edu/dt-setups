package dts.dtengine.camunda

import dts.dtsystem.workflowcontroller.implementations.bpmn.BPMNWorkflowController
import org.camunda.bpm.engine.ProcessEngine
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import java.io.File
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.thread

class ProcessDeployment (private val processEngine: ProcessEngine) {
    private val logger = LoggerFactory.getLogger(ProcessDeployment::class.java)
    fun deployBpmnProcess(bpmnFilePath: String) {
        val file = File(bpmnFilePath)
        if (!file.exists()) {
            logger.error("BPMN file not found: $bpmnFilePath")
        }

        val inputStream = file.inputStream()
        val repositoryService = processEngine.repositoryService
        val deployment = repositoryService.createDeployment()
            .addInputStream(file.name, inputStream)
            .name("Single BPMN Deployment")
            .deploy()

        logger.info("BPMN deployed: ${deployment.id}")
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.id)
            .singleResult()

        if (processDefinition != null) {
            logger.info(" Process Definition Deployed: ${processDefinition.id}, Key: ${processDefinition.key}")
        } else {
            logger.warn("No process definition found in this deployment!")
        }
    }
    fun deployBpmnProcessesFromDirectory(directoryPath: String): List<String> {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            logger.error("Invalid directory path: $directoryPath")
        }

        val repositoryService = processEngine.repositoryService
        val deploymentBuilder = repositoryService.createDeployment().name("Batch BPMN Deployment")

        val bpmnFiles = directory.listFiles { file -> file.extension == "bpmn" }
        if (bpmnFiles.isNullOrEmpty()) {
            throw RuntimeException("No BPMN files found in directory: $directoryPath")
        }

        for (file in bpmnFiles) {
            logger.info("Adding BPMN file to deployment: ${file.name}")
            val inputStream = file.inputStream()
            deploymentBuilder.addInputStream(file.name, inputStream)
        }

        val deployment = deploymentBuilder.deploy()
        logger.info("BPMN deployment completed: ${deployment.id}")


        val processDefinitions = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.id)
            .list()

        val processKeys = processDefinitions.map { it.key }

        if (processKeys.isNotEmpty()) {
            logger.info("Deployed Process Definitions:")
            processKeys.forEach { key -> logger.info(" - Process Key: $key") }
        } else {
            logger.warn("No process definitions found in this deployment!")
        }

        return processKeys
    }

    fun redeploy(fileName: String){
        thread(name = "RedeployThread") {
            try {
                val processDefinitionKey = extractProcessDefinitionKeyFromFile(fileName)

                waitUntilInstanceStoppable(processDefinitionKey)
                deleteStoppableInstance(processDefinitionKey)
                deployBpmnProcess(fileName)

                logger.info("Successfully redeployed $fileName with process key $processDefinitionKey")
            } catch (e: Exception) {
                logger.error("Redeployment failed for $fileName: ${e.message}", e)
            }
        }
    }

    private fun extractProcessDefinitionKeyFromFile(fileName: String): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(fileName))

        val processElements = doc.getElementsByTagName("bpmn:process")
        if (processElements.length == 0) throw IllegalArgumentException("No process element found")

        val processElement = processElements.item(0) as Element
        return processElement.getAttribute("id")
    }

    private fun waitUntilInstanceStoppable(
        processDefinitionKey: String,
        checkIntervalMillis: Long = 5000
    ) {
        val runtimeService = processEngine.runtimeService

        while (true) {
            val nonStoppableCount = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .variableValueEquals("stoppable", false)
                .count()

            if (nonStoppableCount == 0L) break

            logger.info("Waiting for $nonStoppableCount instances to become stoppable...")
            Thread.sleep(checkIntervalMillis)
        }
    }

    private fun deleteStoppableInstance(processDefinitionKey: String) {
        val runtimeService = processEngine.runtimeService

        val stoppableInstances = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .variableValueEquals("stoppable", true)
            .list()

        for (instance in stoppableInstances) {
            runtimeService.deleteProcessInstance(instance.id, "Redeploy cleanup")
            logger.info("Deleted instance ${instance.id}")
        }
    }
}