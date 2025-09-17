package dts.dtengine.eventsystem.abstractevents

import dts.DigitalTwinEngineV2_0

abstract class AbstractDTObserver(var dtengine: DigitalTwinEngineV2_0, var componentid: String) :
    IDTObserver {

    constructor() : this(DigitalTwinEngineV2_0(), "")

    fun setComponentID(id:String){
        this.componentid = id
    }
}