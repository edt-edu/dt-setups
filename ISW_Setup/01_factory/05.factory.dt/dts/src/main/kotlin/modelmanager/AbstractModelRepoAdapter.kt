package dts.modelmanager

import dts.dtengine.eventsystem.modelrepo.observer.AbstractModelsObserver
import dts.dtengine.eventsystem.abstractevents.EventSource

abstract class AbstractModelRepoAdapter(
    var id: String,
    var observers: MutableList<AbstractModelsObserver>,
    var properties: MutableSet<String>,
    var values: MutableMap<String, Any>
) : EventSource(id) {

    fun addObserver(observer: AbstractModelsObserver) {
        this.observers.add(observer)
    }

    fun removeObserver(observer: AbstractModelsObserver) {
        this.observers.remove(observer)
    }

    fun writeValue(key: String, value: Any) {
        if (this.values.containsKey(key)) this.values.replace(key, value)
        else this.values.put(key, value)

        updateToDB()
    }

    fun getValueByPropertyID(key: String): Any {
        return this.values.getValue(key)
    }

    abstract fun findByTimestamp()
    abstract fun updateToDB()
    abstract fun readFromDB()
}