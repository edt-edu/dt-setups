package dts.dtsystem.datastructures
import java.time.LocalDateTime

// DataProbe class definition with properties resolved into function calls
class DataProbe(
     val idd: String,
     val attributeProvider: String,
     val sourceProvider: String,
     val typeProvider: String,
     val functionNameProvider: String,
     val timestampProvider: LocalDateTime
) {
    // Getter functions to resolve properties
    fun getId(): String = idd
    fun getAttribute(): String = attributeProvider
    fun getSource(): String = sourceProvider
    fun getType(): String = typeProvider
    fun getFunctionName(): String = functionNameProvider
    fun getTimestamp(): LocalDateTime = timestampProvider

    // Method to display details of the DataProbe
    fun displayDetails() {
        println("DataProbe Details:")
        println("ID: ${getId()}")
        println("Attribute: ${getAttribute()}")
        println("Source: ${getSource()}")
        println("Type: ${getType()}")
        println("Function Name: ${getFunctionName()}")
        println("Timestamp: ${getTimestamp()}")
    }
}

