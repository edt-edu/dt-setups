package dts.dtsystem.datastructures

import java.time.LocalDateTime

class ExampleUsage {
}

// Example usage
fun main() {
    // Create a new DataProbe instance with providers
    val probe = DataProbe(
        idd = "12345" ,
        attributeProvider = "temperature" ,
        sourceProvider =  "sensor_A" ,
        typeProvider =  "numeric" ,
        functionNameProvider =  "readTemperature" ,
        timestampProvider =  LocalDateTime.now()
    )

    // Display details of the DataProbe
    probe.displayDetails()
}