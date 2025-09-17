package dts.dtengine.camunda
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
class CamundaKonfig {

        fun createProcessEngine(): ProcessEngine {
            return ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource("camunda.cfg.xml")
                .buildProcessEngine()
    }
}