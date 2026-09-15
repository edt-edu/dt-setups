package dts.events

import dts.connection.SynchronizationDirection
import java.time.LocalDateTime
import java.util.*

abstract class DTEvent(source: Any?) : EventObject(source) {
    // timestamp for workflow optimization
    lateinit var timestamp: LocalDateTime
    // from where?
    lateinit var sourceID: String
    // to where?
    lateinit var synchronizationDirection:SynchronizationDirection
    // for what?
    lateinit var message : String
}