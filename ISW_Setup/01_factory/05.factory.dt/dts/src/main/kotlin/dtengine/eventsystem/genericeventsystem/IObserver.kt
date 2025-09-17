package dts.events.genericeventsystem

// Observer interface
interface IObserver<T> {
    fun onEvent(event: Event<T>)
}