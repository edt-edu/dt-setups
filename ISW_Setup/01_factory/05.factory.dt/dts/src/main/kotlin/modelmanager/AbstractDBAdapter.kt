package dts.modelmanager

import dts.dtengine.eventsystem.database.observer.AbstractDBObserver
import dts.dtengine.eventsystem.abstractevents.EventSource

abstract class AbstractDBAdapter(
    var id: String,
    var observers: MutableList<AbstractDBObserver>,
    var properties: MutableSet<String>,
    var values: MutableMap<String, Any?>
) : EventSource(id) {

    abstract var tickrate: Int

    fun addObserver(observer: AbstractDBObserver) {
        this.observers.add(observer)
    }

    fun removeObserver(observer: AbstractDBObserver) {
        this.observers.remove(observer)
    }

    fun writeValue(key: String, value: Any) {
        if (this.values.containsKey(key)) this.values.replace(key, value)
        else this.values.put(key, value)

        updateToDB()
    }

    fun getValueByPropertyID(key: String): Any? {
        return this.values.getValue(key)
    }

    abstract fun findByTimestamp()
    abstract fun updateToDB()
    abstract fun readFromDB(): String
    abstract fun getAllHistory(): MutableList<MutableMap<String, Any>>
    fun fillValuesField(){
        for(propid in this.properties)
            this.values.put(propid, null)
    }
}