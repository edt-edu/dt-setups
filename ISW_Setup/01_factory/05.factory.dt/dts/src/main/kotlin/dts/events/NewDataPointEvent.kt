package dts.events

class NewDataPointEvent(source: Any?, val properties: Set<String>) : DTEvent(source) {

    constructor(source: Any?, property: String) : this(source, setOf(property)) {
    }
}