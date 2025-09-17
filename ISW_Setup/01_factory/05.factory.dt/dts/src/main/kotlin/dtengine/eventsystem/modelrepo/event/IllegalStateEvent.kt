package dts.dtengine.eventsystem.modelrepo.event

import dts.dtsystem.datastructures.DataProbe
import dtengine.eventsystem.abstractevents.DTEvent


class IllegalStateEvent(source: Any?, content:DataProbe?) : DTEvent(source) {
    constructor(): this("", null)
}