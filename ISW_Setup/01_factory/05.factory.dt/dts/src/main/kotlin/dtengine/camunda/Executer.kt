package dts.dtengine.camunda

import dts.dtengine.eventsystem.abstractevents.DTEventQueue
import dts.services.implementations.GateWayServiceMock
import dts.services.mock.Attribute
import dts.services.mock.Body
import dts.services.mock.Endpoint
import dts.services.mock.TGatewayRequest
import dts.services.mock.TReplyTo
import dts.services.mock.TRequest

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory



class Executer() : JavaDelegate {
    private val logger = LoggerFactory.getLogger(Executer::class.java)
    companion object {
        lateinit var gatewayService: GateWayServiceMock
        lateinit var serviceRequestEventQueue: DTEventQueue
    }



    override fun execute(execution: DelegateExecution?) {
        val variables: Map<String, Any?> = execution!!.variables
        val currentActivityId = execution.currentActivityId
        val processDefinitionId = execution.processDefinitionId
        if(correctFormat(variables, currentActivityId, processDefinitionId)) {
            methodCall(variables)
        }
    }

    private fun correctFormat(
        variables: Map<String, Any?>,
        currentActivityId: String,
        processDefinitionId: String
    ): Boolean {
        var correctFormat = true
        if (!variables.contains("method")) {
            logger.error("BPMN service task $currentActivityId from task $processDefinitionId has no method variable")
            correctFormat=false
        }
        if (!variables.contains("payload")) {
            logger.error("BPMN service task $currentActivityId from task $processDefinitionId has no payload variable")
            correctFormat=false
        }
        if (!variables.contains("serviceName")) {
            logger.error("BPMN service task $currentActivityId from task $processDefinitionId has no serviceName variable")
            correctFormat=false
        }
        return correctFormat
    }



    private fun methodCall(variables: Map<String, Any?>) {
        val method =variables.getValue("method")
        logger.info("call method $method")
        val gatewayRequest = createGatewayRequest(variables)
        callServiceGateWay(gatewayRequest)
    }

    private fun createGatewayRequest(variables: Map<String, Any?>): TGatewayRequest {
        val service = variables["serviceName"] as String
        val method = variables["method"] as String
        val path = "/"
        val payload = variables["payload"] as Map<*, *>
        val payloadTypes = variables["payloadTypes"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val endpoint = Endpoint(
            method = method,
            service = service
        )

        val body = createRequestBody(payloadTypes, payload)

        val request = TRequest(
            endpoint = endpoint,
            body = body
        )

        val replyTo = TReplyTo(
            address = ""
        )

        val gatewayRequest = TGatewayRequest(
            request = request,
            replyTo = replyTo

        )
        return gatewayRequest
    }

    private fun createRequestBody(
        payloadTypes: Map<*, *>,
        payload: Map<*, *>
    ): Body {
        val attributes = payload.map { (key, value) ->
            val declaredType = payloadTypes[key]?.toString()
            val actualType = value?.javaClass?.simpleName ?: "String"
            if (declaredType != null && actualType != declaredType) {
                logger.warn("Type mismatch for key '$key':declared=$declaredType, actual=$actualType using $actualType")
            }
            Attribute(
                key = key.toString(),
                value = value?.toString() ?: "",
                type = declaredType ?: actualType
            )
        }
        return Body(attribute = attributes)
    }

    private fun callServiceGateWay(gatewayRequest: TGatewayRequest) {
        gatewayService.executeRequest(gatewayRequest)
    }
}