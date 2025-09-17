package dts.gateway

import dts.dtengine.eventsystem.abstractevents.EventSource
import dts.dtengine.eventsystem.gateway.observer.AbstractGatewayObserver
import dts.dtsystem.datastructures.Functionality

abstract class AbstractGateway(
    var id: String,
    var observers: MutableList<AbstractGatewayObserver>,
    var properties: MutableSet<String>,
    var values: MutableMap<String, Any?>,
    var functions: MutableSet<Functionality>,
    var thread: Thread?
) : EventSource(id) {

    fun addObserver(observer: AbstractGatewayObserver) {
        this.observers.add(observer)
    }

    fun removeObserver(observer: AbstractGatewayObserver) {
        this.observers.remove(observer)
    }

    fun writeValue(key: String, value: Any){
        if (this.values.containsKey(key)) this.values.replace(key, value)
        else this.values.put(key, value)

        updateToGW()
    }

    fun getValueByPropertyID(key:String): Any? {
        return this.values.getValue(key)
    }

    abstract fun findByTimestamp()
    abstract fun updateToGW()
    abstract fun readFromGW()
    fun fillValuesField(){
        for(propid in this.properties)
        this.values.put(propid, null)
    }

    abstract fun start()
}