package dts.dtengine.eventsystem.database.event

import dts.dtsystem.datastructures.DataProbe
import dtengine.eventsystem.abstractevents.DTEvent


class IllegalDatapointFormatEvent(source: Any?, content:DataProbe?) : DTEvent(source) {
    constructor(): this("", null)
}