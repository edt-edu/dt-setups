package dts.dtengine.eventsystem.abstractevents

import dtengine.eventsystem.abstractevents.DTEvent
import java.util.*

interface IDTObserver : EventListener {
    fun handleEvent(event: DTEvent)
}