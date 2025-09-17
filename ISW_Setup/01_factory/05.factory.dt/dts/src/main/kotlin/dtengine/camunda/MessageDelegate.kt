package dts.dtengine.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate


class MessageDelegate : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val messageName = execution.getVariable("messageName") as String?
        execution.processEngineServices.runtimeService
            .createMessageCorrelation(messageName)
            .correlate()
    }
}