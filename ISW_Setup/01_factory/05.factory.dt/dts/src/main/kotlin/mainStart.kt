package dts

import dts.dtengine.camunda.CamundaKonfig
import dts.dtengine.enginemappings.SynchronizationDirection
import dts.dtengine.eventsystem.abstractevents.AbstractDTObserver
import dts.dtengine.eventsystem.abstractevents.EventSource
import dts.dtengine.eventsystem.database.observer.DBObserverImpl
import dts.dtengine.eventsystem.gateway.observer.GatewayObserverImpl
import dts.dtsystem.datastructures.Functionality
import dts.dtsystem.enginemappings.Mapping
import dts.dtsystem.workflowcontroller.implementations.EmptyController
import dts.gateway.implementations.EmptyGWImpl
import dts.modelmanager.implementations.EmptyDBImpl
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import services.dtservices.Runner
import services.dtservices.configs.Config
import java.io.IOException
import java.util.*

class mainStart {
}

fun main() {
        var de: DigitalTwinEngineV2_0 = DigitalTwinEngineV2_0()

        var componentsMap = mutableListOf<Pair<EventSource, AbstractDTObserver>>()
        val camundaKonfig = CamundaKonfig()
        //val processEngine = camundaKonfig.createProcessEngine()
        val temperatureListener = GatewayObserverImpl(
            relatedGWid = "001-GW-Listener"
        )
        temperatureListener.dtengine = de

        //setup Gateway
        val gwImpl = EmptyGWImpl()
        //identifier of GW
        gwImpl.id = "001-GW"
        gwImpl.properties = mutableSetOf("temp", "time", "speed", "wear")
        gwImpl.fillValuesField()
        //functions of GW
        var functionality: Functionality = Functionality()
        functionality.skillId = "drill"
        gwImpl.functions.add(functionality)
        gwImpl.tickrate = 1000

        //add to initial setup
        componentsMap.add(Pair(gwImpl, temperatureListener))


        // sample db
        val dbImpl = EmptyDBImpl()
        dbImpl.id = "001-DB"
        dbImpl.properties = mutableSetOf("temp")
        dbImpl.fillValuesField()
        dbImpl.tickrate = 200

        val dbListener = DBObserverImpl(relatedDBid = "001-DB")
        dbListener.dtengine = de
        componentsMap.add(Pair(dbImpl, dbListener))

        // add the observer to the GWImpl
        de.registerComponents(componentsMap)


        // TODO edd example mappings
        var map = Mapping()
        map.id = UUID.randomUUID()
        map.synchronisationDirection = SynchronizationDirection.GATEWAY_TO_DB
        map.mappings = Pair(mutableListOf(Pair("001-GW", "temp")), mutableListOf(Pair("001-DB", "temp")))
        map.interval = 100
        val args: String = "--spring.profiles.active=gateway --startup.properties=application-gateway.properties"
        val runner = Runner()
        runner.execute()
        //SpringApplicationBuilder(services.dtservices.Runner::class.java)
        //        .web(WebApplicationType.NONE)
        //        .run(args)

        //could add more of course
        de.synchronizer.mappings = mutableListOf(map)
        for (gw in de.gatewayAdapter) {
            gw.start()
        }

        val emp = EmptyController(de)
        emp.tickrate = 10
        emp.thread.start()
        emp.thread.join()
        


        // init service manamenemt



        //val serviceGWthread = Thread(serviceRunner)
        //serviceGWthread.start()
        //serviceGWthread.join()


//        val dtEngineWFController = BPMNWorkflowController(de)
//        de.dtEngineWFController = dtEngineWFController
//
//        (de.dtEngineWFController as BPMNWorkflowController).tickrate = 1000
//
//
//        (dtEngineWFController).thread.start()
//        (dtEngineWFController).thread.join()


}