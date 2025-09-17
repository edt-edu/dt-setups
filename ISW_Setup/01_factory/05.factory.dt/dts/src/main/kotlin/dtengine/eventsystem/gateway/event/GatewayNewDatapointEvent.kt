package dts.events.neweventsystem.gateway.events

import dts.dtsystem.datastructures.DataProbe
import dtengine.eventsystem.abstractevents.DTEvent


class GatewayNewDatapointEvent(source: Any?, content:DataProbe?) : DTEvent(source) {
    constructor(): this("", null)
}