package dts.dtsystem.enginemappings

import dts.dtengine.enginemappings.SynchronizationDirection
import java.util.UUID

class Mapping(

    //ID pairmap from multiple sources to one single target

    // fromID - fromPropertyID - toID - toPropertyID
    var mappings: Pair<MutableList<Pair<String, String>>, MutableList<Pair<String, String>>>,
    //direction
    var synchronisationDirection: SynchronizationDirection,
    //mappingID
    var id: UUID = UUID.randomUUID(),
    //timing
    var interval: Long = -1,
    //function to shadow on the fromIDProperties
    var consolidationfunctionPath: String = ""
) {

    constructor(): this(
        Pair(mutableListOf(), mutableListOf()), SynchronizationDirection.BOTH
    )
}