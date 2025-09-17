package dts.events.genericeventsystem

class ExampleUsage {

}

// Example usage
fun main() {
    // Define a specific observer
    val temperatureObserver = object : IObserver<String> {
        override fun onEvent(event: Event<String>) {
            println("Temperature Observer received event: ${event.name} with data: ${event.data}")
        }
    }

    val sourceObserver = object : IObserver<String> {
        override fun onEvent(event: Event<String>) {
            println("Source Observer received event: ${event.name} with data: ${event.data}")
        }
    }

    // Create an observable
    val observable = Observable<String>()

    // Register observers
    observable.addObserver(temperatureObserver)
    observable.addObserver(sourceObserver)

    // Notify observers of an event
    val temperatureEvent = Event("TemperatureUpdate", "25°C")
    observable.notifyObservers(temperatureEvent)

    val sourceEvent = Event("SourceChange", "Sensor_A")
    observable.notifyObservers(sourceEvent)
}