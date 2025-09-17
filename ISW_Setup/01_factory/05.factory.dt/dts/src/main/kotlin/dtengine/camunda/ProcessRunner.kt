package dts.dtengine.camunda

import org.camunda.bpm.engine.ProcessEngine

class ProcessRunner(private val processEngine: ProcessEngine) {
    fun startProcessInstance(processKey: String) {
        val repositoryService = processEngine.repositoryService
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processKey)
            .latestVersion()
            .singleResult()

        if (processDefinition == null) {
            throw RuntimeException(" No deployed process found with key: $processKey")
        }

        val runtimeService = processEngine.runtimeService
        val processInstance = runtimeService.startProcessInstanceByKey(processKey)

        println(" Started BPMN process with ID: ${processInstance.id}")
    }

    fun startMultipleProcessInstances(processKeys: List<String>){
        val runtimeService = processEngine.runtimeService

                if (processKeys.isEmpty()) {
                    println("No process keys provided, nothing to start.")
                    return
                }

        for (processKey in processKeys) {
            val processInstance = runtimeService.startProcessInstanceByKey(processKey)
            println("Started BPMN process with ID: ${processInstance.id}, Key: $processKey")
        }
    }
}