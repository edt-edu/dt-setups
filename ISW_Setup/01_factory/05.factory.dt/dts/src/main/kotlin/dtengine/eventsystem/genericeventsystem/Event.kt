package dts.events.genericeventsystem

// Event class representing a generic event
class Event<T>(val name: String, val data: T)