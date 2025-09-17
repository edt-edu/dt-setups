package dts.dtsystem.workflowcontroller

import dts.DigitalTwinEngineV2_0

abstract class DigitalTwinEngineWorkflowController(var dtengine: DigitalTwinEngineV2_0) {
    abstract fun handle()
    abstract fun execute()
    abstract fun handleServiceRequestEvents()
    //TODO: build this into an abstract class that invokes the BPMN Workflow Controller
}