package dts.dtengine.eventsystem.service.event

import dts.dtsystem.datastructures.DataProbe
import dtengine.eventsystem.abstractevents.DTEvent

class FrontEndDataRequestEvent(source: Any?, content: DataProbe?) : DTEvent(source) {
    constructor(): this("", null)
}