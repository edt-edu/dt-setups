package dts.services

import dts.connection.SynchronizationDirection
import dts.events.ServiceRequestEvent
import dts.events.observer.IServiceObserver
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.time.LocalDateTime

/**
 * This class is there to allow the addition of new services during runtime
 */
class EngineServiceAPI(private val observer: IServiceObserver) {

    /**
     * This method starts the API. It will start a server on port 10001 and will listen for incoming requests.
     * When a request is received, it will call the corresponding method.
     */
    fun start() {
        val port = 10001;

        val server = embeddedServer(Netty, port) {
            routing {
                post("join") {
                    val serviceId = call.receive<String>()
                    joinEvent(serviceId)
                    call.respondText { "Success" }
                }
                post("executeServiceTask") {
                    data class TaskToExecute(val serviceId: String, val task: String)
                    val taskToExecute = call.receive<TaskToExecute>()
                    createNewServiceEvent(taskToExecute.serviceId, taskToExecute.task)
                    call.respondText { "Success" }
                }
            }
        }
        server.start()
    }

    /**
     * This method is called when a service joins the network. It will create a new event that will be sent to the DTE
     * @param serviceID The ID of the service that joined the network.
     */
    private fun joinEvent(serviceID: String) {
        val serviceRequestEvent = ServiceRequestEvent(this)
        serviceRequestEvent.sourceID = serviceID
        serviceRequestEvent.timestamp = LocalDateTime.now()
        serviceRequestEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT
        serviceRequestEvent.message = serviceID
        observer.serviceJoin(serviceRequestEvent)
    }

    /**
     * This method is called when a service wants to execute a task. It will create a new event that will be sent to the DTE
     * @param serviceID The ID of the service that wants to execute a task.
     * @param task The task that the service wants to be executed.
     */
    private fun createNewServiceEvent(task: String, serviceID: String) {
            val serviceRequestEvent = ServiceRequestEvent(this)
            serviceRequestEvent.sourceID = serviceID
            serviceRequestEvent.timestamp = LocalDateTime.now()
            serviceRequestEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT
            serviceRequestEvent.message = task
            observer.handleModelGet(serviceRequestEvent)
    }
}