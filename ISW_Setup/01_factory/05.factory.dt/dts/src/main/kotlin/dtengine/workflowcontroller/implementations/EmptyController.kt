package dts.dtsystem.workflowcontroller.implementations

import dts.DigitalTwinEngineV2_0
import dts.dtengine.eventsystem.service.event.FrontEndDataRequestEvent
import dts.dtsystem.workflowcontroller.DigitalTwinEngineWorkflowController
import dts.events.neweventsystem.gateway.events.GatewayNewDatapointEvent

class EmptyController(dtengine: DigitalTwinEngineV2_0) :
    DigitalTwinEngineWorkflowController(dtengine) {

    var tickrate = 1
    var thread = Thread {
        this.execute()
    }

    override fun handle() {
        TODO("Not yet implemented")
    }

    override fun execute() {
        var curtime = 0

        while (true) {
            //see synchorizer diagram. Here is just a simple cutout of the diagram
            if (!dtengine.gatewayEventQueue.isEmpty() || !dtengine.dbEventQueue.isEmpty()) {
                println("Controller: handling number of events: " + dtengine.gatewayEventQueue.queue.size)
                for (event in dtengine.gatewayEventQueue.getAllEvents()) {
                    if (event is GatewayNewDatapointEvent) {
                        println("Controller: handling NewDataPointEvent from"+event.sourceID + " with content: " + event.content.typeProvider)
                        for(map in dtengine.synchronizer.mappings)
                        if (map.mappings.first.contains(Pair(event.sourceID, event.content.attributeProvider)))
                            curtime = this.dtengine.synchronizer.sync(curtime)
                    }
                }
            }
            if (!dtengine.serviceRequestEventQueue.isEmpty())
                handleServiceRequestEvents()

            if (curtime >= tickrate) curtime = 0
            //check for service requests
//            foreach (service in dtengine)
//            {
//                call it? if exists

                // if servicecall === shudwodn and reload servuce GW the
            // dtenginge.servgw.stop()
            // reload configs
            // servgw.restart()

            }

            //check for gateway events

            //check for mm events
            curtime += 1
            Thread.sleep(1)

        }



    override fun handleServiceRequestEvents() {
        for (event in this.dtengine.serviceRequestEventQueue.getAllEvents()) {
            if (event is FrontEndDataRequestEvent) {
                //find the service
                var requester =
                    this.dtengine.requestingServices.filter { service -> service.id.equals(event.sourceID) }.first()

                // understand the request
                var probe = event.content

                var requestkind1 = probe.getFunctionName().equals("getCurrentData")
                if (requestkind1) {
                    var collectedData = mutableListOf<MutableMap<String, Any?>>()
                    var databases = this.dtengine.dbAdapter
                    for (db in databases) collectedData.add(db.values)
                    requester.post(collectedData)
                }


                var requestkind2 = probe.getFunctionName().equals("getAllData")
                if (requestkind2) {
                    var collectedData = mutableListOf<MutableMap<String, Any>>()
                    var databases = this.dtengine.dbAdapter
                    for (db in databases) collectedData.addAll(db.getAllHistory())
                    requester.post(collectedData)
                }
            }
        }
    }


}