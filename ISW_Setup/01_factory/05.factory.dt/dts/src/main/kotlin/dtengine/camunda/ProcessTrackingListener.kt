package dts.dtengine.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener


class ProcessTrackingListener : ExecutionListener {
    override fun notify(execution: DelegateExecution?) {
        val event = execution?.eventName
        val activityId = execution?.currentActivityId
        val activityName = execution?.bpmnModelElementInstance?.name
        println("Process Event: $event at Activity ID: $activityId, Name: $activityName")
    }
}