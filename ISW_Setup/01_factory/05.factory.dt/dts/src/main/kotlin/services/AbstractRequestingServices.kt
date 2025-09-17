package dts.services

import dts.dtengine.eventsystem.abstractevents.EventSource
import dts.dtengine.eventsystem.gateway.observer.AbstractGatewayObserver
import dts.dtengine.eventsystem.service.observer.AbstractServiceObserver

abstract class AbstractRequestingServices(
    var id: String,
    var observers: MutableList<AbstractServiceObserver>,
    var properties: MutableSet<String>,
    var values: MutableMap<String, Any>
) : EventSource(id) {

    fun addObserver(observer: AbstractServiceObserver) {
        this.observers.add(observer)
    }

    fun removeObserver(observer: AbstractServiceObserver) {
        this.observers.remove(observer)
    }

    fun post(data: Any){
        println(data)
    }
}