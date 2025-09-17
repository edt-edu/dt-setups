package dts.dtengine.camunda

import dts.dtengine.eventsystem.abstractevents.DTEventQueue
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory

class Executer(serviceRequestEventQueue: DTEventQueue) : JavaDelegate {
    private val logger = LoggerFactory.getLogger(Executer::class.java)

    override fun execute(execution: DelegateExecution?) {
        val variables: Map<String, Any?> = execution!!.variables

        if (variables.contains("method")) {
            methodCall(variables)
        } else {
            val currentActivityId = execution.currentActivityId
            val processDefinitionId = execution.processDefinitionId
            logger.error("BPMN service task $currentActivityId from task $processDefinitionId has no method variable")
        }
    }

    private fun methodCall(variables: Map<String, Any?>) {
        when(variables["method"]){
            "createModel" -> println("createModel()")
            "editModel" -> println("editModel()")
            "deleteModel" -> println("deleteModel()")
            "modelFailure" -> println("modelFailure()")
            else -> println("no correct method")
        }
    }
}