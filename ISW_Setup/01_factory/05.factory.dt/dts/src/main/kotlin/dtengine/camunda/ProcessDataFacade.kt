package dts.dtengine.camunda

import dts.modelmanager.AbstractDBAdapter
import dts.services.implementations.GateWayServiceMock

class ProcessDataFacade {
    companion object {
        lateinit var DBAdapter: AbstractDBAdapter
    }

    fun getModel(id: String): Map<String, Any> {
        var map = DBAdapter.getEntry(id)
        if (map.isNullOrEmpty()){
            map = mutableMapOf("exists" to "false")
        } else {
            map.put("exists","true")
        }
        return map
    }

    

}