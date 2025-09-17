package dts.modelmanager.implementations

import dts.dtengine.eventsystem.database.observer.AbstractDBObserver
import dts.modelmanager.AbstractDBAdapter

class EmptyDBImpl(id: String, observers: MutableList<AbstractDBObserver>, properties: MutableSet<String>,
                  values: MutableMap<String, Any?>, override var tickrate: Int
) : AbstractDBAdapter(id, observers,
    properties, values
) {

    val thread = Thread{

    }
    init {

    }

    constructor(): this("", mutableListOf(), mutableSetOf(), mutableMapOf(), 1000)

    override fun findByTimestamp() {
        TODO("Not yet implemented")
    }

    override fun updateToDB() {
        println("DB: upated Value to DB:: "+ this.values)
    }

    override fun readFromDB(): String {
        return "test"
    }

    override fun getAllHistory(): MutableList<MutableMap<String, Any>> {
        TODO("Not yet implemented")
    }

}