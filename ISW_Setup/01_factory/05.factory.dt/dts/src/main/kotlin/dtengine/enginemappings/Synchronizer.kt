package dts.dtsystem.enginemappings

import dts.dtengine.enginemappings.SynchronizationDirection
import dts.dtsystem.mappingutils.MappingConsolidation
import dts.gateway.AbstractGateway
import dts.modelmanager.AbstractDBAdapter
import dts.modelmanager.AbstractModelRepoAdapter

class Synchronizer(
    var mappings: MutableList<Mapping>,
    var gateways: MutableSet<AbstractGateway>,
    var dbAdapters: MutableSet<AbstractDBAdapter>,
    var modelRepoAdapters: MutableSet<AbstractModelRepoAdapter>, //<-- probably graphs
) {

    constructor(): this(
        mutableListOf(), mutableSetOf(), mutableSetOf(), mutableSetOf()
    )

    fun addMapping(mapping: Mapping) {
        this.mappings.add(mapping)
    }

    fun removeMapping(id: String) {
        this.mappings.removeIf { x -> x.id.equals(id) }
    }

    fun sync(currentLoopCounter: Int): Int {
        // foreach property in the gateway get the value and post the value to ALL related DBs
        for (mapping in mappings) {
            if (mapping.interval > 0 && currentLoopCounter >= mapping.interval) {
                doSync(mapping)
            } else if (mapping.interval <= 0) {
                doSync(mapping)
            }
        }
        return 0
    }
    fun doSync(mapping: Mapping){
            // Gateway to Modelmanager
            if (mapping.synchronisationDirection.equals(SynchronizationDirection.GATEWAY_TO_DB)) {
                //combine all values
                var inputs = mutableListOf<Pair<String, Any?>>()
                // Update a value in a certain database
                var targets = mapping.mappings.second

                //fill inputs
                for (gw in gateways) {
                    inputs.addAll(getContainedInputs(mapping.mappings.first, gw))
                }
                //consolidate the values into one single value for writing
                var value = MappingConsolidation().consolidate(mapping.consolidationfunctionPath, inputs)

                //write the value to targets
                for (db in dbAdapters) {
                    if (value != null) {
                        writeValueToDBAdapter(value, targets, db)
                    }
                }
                for (mod in modelRepoAdapters) {
                    if (value != null) {
                        writeValueToModelAdapter(value, targets, mod)
                    }
                }

            }
            if (mapping.synchronisationDirection.equals(SynchronizationDirection.DB_TO_GATEWAY)) {
                //combine all values
                var inputs = mutableListOf<Pair<String, Any?>>()
                // Update a value in a certain database
                var targets = mapping.mappings.second

                //fill the inputs
                for (db in dbAdapters) {
                    inputs.addAll(getContainedDBCommands(mapping.mappings.first, db))
                }
                for (mod in modelRepoAdapters) {
                    inputs.addAll(getContainedModelCommands(mapping.mappings.first, mod))
                }

                //consolidate the values into one single value for writing
                var value = MappingConsolidation().consolidate(mapping.consolidationfunctionPath, inputs)

                //write the value to targets
                for (gw in gateways) {
                    if (value != null) {
                        writeValueToGWAdapter(value, targets, gw)
                    }
                }
            }


        }
        // foreach command in the DB get the command and post the command to ALL related GWs

    fun writeValueToDBAdapter(value: Any, targets: MutableList<Pair<String, String>>, db: AbstractDBAdapter) {
        for (target in targets) {
            if (target.first.equals(db.id)) {
                if (db.properties.contains(target.second)) {
                    db.writeValue(target.second, value)
                }
            }
        }
    }

    fun writeValueToModelAdapter(
        value: Any,
        targets: MutableList<Pair<String, String>>,
        db: AbstractModelRepoAdapter
    ) {
        for (target in targets) {
            if (target.first.equals(db.id)) {
                if (db.properties.contains(target.second)) {
                    db.writeValue(target.second, value)
                }
            }
        }
    }

    fun writeValueToGWAdapter(value: Any, targets: MutableList<Pair<String, String>>, gw: AbstractGateway) {
        for (target in targets) {
            if (target.first.equals(gw.id)) {
                if (gw.properties.contains(target.second)) {
                    gw.writeValue(target.second, value)
                }
            }
        }
    }

    fun getContainedInputs(
        list: MutableList<Pair<String, String>>,
        gw: AbstractGateway
    ): MutableList<Pair<String, Any?>> {
        var output = mutableListOf<Pair<String, Any?>>()
        for (element in list) {
            if (element.first.equals(gw.id)) {
                if (gw.properties.contains(element.second)) {
                    output.add(Pair(gw.id + "." + element.second, gw.getValueByPropertyID(element.second)))
                }
            }
        }
        return output
    }

    fun getContainedModelCommands(
        list: MutableList<Pair<String, String>>,
        mod: AbstractModelRepoAdapter
    ): MutableList<Pair<String, Any>> {
        var output = mutableListOf<Pair<String, Any>>()
        for (element in list) {
            if (element.first.equals(mod.id)) {
                if (mod.properties.contains(element.second)) {
                    output.add(Pair(mod.id + "." + element.second, mod.getValueByPropertyID(element.second)))
                }
            }
        }
        return output
    }

    fun getContainedDBCommands(
        list: MutableList<Pair<String, String>>,
        db: AbstractDBAdapter
    ): MutableList<Pair<String, Any?>> {
        var output = mutableListOf<Pair<String, Any?>>()
        for (element in list) {
            if (element.first.equals(db.id)) {
                if (db.properties.contains(element.second)) {
                    output.add(Pair(db.id + "." + element.second, db.getValueByPropertyID(element.second)))
                }
            }
        }
        return output
    }
}