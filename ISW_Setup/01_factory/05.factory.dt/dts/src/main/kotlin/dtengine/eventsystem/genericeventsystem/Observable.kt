package dts.events.genericeventsystem

class Observable<T> {
    private val observers = mutableListOf<IObserver<T>>()

    // Add an observer
    fun addObserver(observer: IObserver<T>) {
        observers.add(observer)
    }

    // Remove an observer
    fun removeObserver(observer: IObserver<T>) {
        observers.remove(observer)
    }

    // Notify all observers of an event
    fun notifyObservers(event: Event<T>) {
        for (observer in observers) {
            observer.onEvent(event)
        }
    }
}