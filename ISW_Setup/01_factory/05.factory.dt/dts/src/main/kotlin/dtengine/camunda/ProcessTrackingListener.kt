package dts.dtengine.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.slf4j.LoggerFactory

class ProcessTrackingListener : ExecutionListener {
    private val logger = LoggerFactory.getLogger(ProcessTrackingListener::class.java)
    override fun notify(execution: DelegateExecution?) {
        val event = execution?.eventName
        val activityId = execution?.currentActivityId
        val activityName = execution?.bpmnModelElementInstance?.name
        logger.info("Process Event: $event at Activity ID: $activityId, Name: $activityName")

        // Alle Prozessvariablen loggen
        val variables = execution?.variables
        if (variables != null && variables.isNotEmpty()) {
            logger.info("Process Variables:")
            for ((key, value) in variables) {
                logger.info("  $key = $value")
            }
        } else {
            logger.info("Keine Prozessvariablen vorhanden.")
        }
    }
}
