
The provided example is relatively straightforward. It involves a mock weather station, backed by a weather API, acting as a CPS. Additionally, a CSV file serves as the model in this scenario.

### Core classes

In this example, the core classes are implementations of the abstract classes within the architecture, along with the main class. The main class is the focal point where all instances are created, and it houses the main method.

#### Main class

All classes of the instances created in this example can be found further below. Exceptions include classes like the DTE, which are neither abstract classes of the architecture nor specific helper classes of the example.

```
class MainKt {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //create the engine
            val engine = DigitalTwinEngine()

            //create the gateway
            val weatherStation = WeatherStation("Stuttgart")
            val weatherGateway = DTWeatherGateway(weatherStation)
            weatherGateway.addListener(GatewayObserver(engine))

            //create the model manager
            val csvModelManager = CSVModelManager()
            csvModelManager.addObserver(ModelObserver(engine))



            //create the mapping
            val csvMapping = Mapping(
                3000,
                mapOf("Temperature" to "temp", "Humidity" to "humidity", "Pressure" to "pressure", "Location" to "location"),
                SynchronizationDirection.GATEWAY_TO_DT,
                "csvWeatherConnection",
                csvModelManager.modelManagerID,
                weatherGateway.gatewayID
            )

            csvMapping.addListener(MappingObserver(engine))


            val demoService = DemoUIService()
            demoService.addListener(ServiceObserver(engine))
            val highestTempInCSVFileService = HighestTempInCSVFileService()
            highestTempInCSVFileService.addListener(ServiceObserver(engine))




            //configure the engine
            engine.configure()
            engine.addModelManager(csvModelManager)
            engine.addMapping(csvMapping)
            engine.addGateway(weatherGateway)
            engine.addService(demoService)
            engine.addService(highestTempInCSVFileService)
            engine.addObserver(EngineWFObserver(engine, setOf(highestTempInCSVFileService)))
        }
    }
}
```

#### Gateway

This is an exemplary implementation of the AbstractGateway class. Note that, besides small helper methods, no "new" method has to be implemented. Only the specific implementation of the inherited methods is necessary to achieve the desired functionality.
```
class DTWeatherGateway(var weatherStation : WeatherStation) : AbstractGateway(propertyIDs = setOf("temp", "humidity", "pressure", "location"),
    gatewayID = "weatherGateway"
) {

    override val lastPropertyUpdateMap: HashMap<String, Long> = HashMap()

    override fun listen() {
        thread.start()
    }

    private val thread = Thread(Runnable {
        while (true) {
            createNewDataPointEvent()
            Thread.sleep(40000)
        }

    })

    init {
        listen()
    }

    override fun sendError(error: DTError) {
        for (gatewayListener: IGatewayObserver in observer) {
            val event = ErrorEvent(this)
            event.sourceID = error.source.toString()
            event.timestamp = LocalDateTime.now()
            event.message = error.message
            gatewayListener.handleError(event)
        }
    }

    fun createNewDataPointEvent() {
        for(gatewayListener: IGatewayObserver in observer){
            // setup the event
            val dataPointEvent = NewDataPointEvent(this)
            dataPointEvent.sourceID= "weatherGateway"
            dataPointEvent.timestamp = LocalDateTime.now()
            dataPointEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT

            // let the observer handle it
            gatewayListener.handleDatapointEvent(dataPointEvent)
        }
    }

    override fun getDataFromSource() {
        return
    }

    override fun getValue(id: String): Any {
        if (id == "location") {
            lastPropertyUpdateMap["location"] = System.currentTimeMillis()
            return weatherStation.location
        }
        lastPropertyUpdateMap[id] = System.currentTimeMillis()
        return weatherStation.getValue(id)
    }

    override fun getAllValues(): Map<String, Any> {
        TODO("Not yet implemented")
    }

    override fun setValue(id: String, value: Any) {
        if (id == "location") {
            weatherStation.location = value.toString()
        } else {
            throw IllegalArgumentException("Cannot set property $id to $value")
        }
    }

    /**
     * This function is used to move the weather station to a new location.
     * It is used in the test to simulate a change in location of the weather station.
     */
    fun moveStationTo(location: String) {
        lastPropertyUpdateMap["location"] = System.currentTimeMillis()
        weatherStation.location = location
    }

}
```

#### Model Manager

The Model Manager necessitates more helper methods compared to the Gateway.

A good example illustrates the purpose of dedicated `writeToModel()` and `readFromModel()` methods. This design allows for the utilization of the generalized `setValue()` and `readValue()` methods while concurrently writing completely use case-dependent objects, such as the `CSVWeatherValues`, to the model.

```
private val logger = KotlinLogging.logger {}
class CSVModelManager : AbstractModelManager<CSVWeatherValues>(

    modelManagerID = "CSVWeatherModelManager",
    propertyIDs = setOf("Timestamp", "Temperature", "Humidity", "Pressure", "Location", "CSVWeatherValues")) {
    override val lastPropertyUpdateMap: HashMap<String, Long> = HashMap()

    private val millsToDateMap = mutableMapOf<Long, String>()
    private val localCSVWeatherValues = CSVWeatherValues("", 0.0f, 0.0f, 0.0f, "")
    private var intervalCounter = 0
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
    private val fileName = "CPS.csv"
    private val csvReaderWriter = CSVReaderWriter<CSVWeatherValues>()

    init {
        csvReaderWriter.initWriter(fileName)
        logger.info { "$this started" }
    }

    /**
     * Returns the pathname of the csv File
     */
    override fun getModel(): List<CSVWeatherValues> {
        csvReaderWriter.initReader(fileName, CSVWeatherValues::class.java)
        return csvReaderWriter.doRead()!!
    }

    /**
     * Writes the values of the csvWeatherValues object with the current time stamp to the csv file
     */
    override fun writeToModel(property: String, csvWeatherValues: CSVWeatherValues) {
        try {
            val time = LocalDateTime.now()
            saveTime(time)
            csvWeatherValues.timeStamp = time.format(formatter)
            csvReaderWriter.doWrite(csvWeatherValues)
            logger.debug { this.modelManagerID + " write to model: " + csvWeatherValues.asString() }
        } catch (e: Exception) {
            val error = DTError(message = e.message!!, source = modelManagerID)
            sendError(error, SynchronizationDirection.GATEWAY_TO_DT)
        }
    }

    /**
     * stores the data locally until all properties have been collected and then writes them to the csv file
     */
    override fun setValue(id: String, value: Any) {
        storeValuesLocally(id, value)
        intervalCounter++
        if (intervalCounter == 4) {
            writeToModel(id, localCSVWeatherValues)
            intervalCounter = 0
        }
    }

    /**
     * Returns a csvWeatherValues object with the data from the given time stamp
     */
    override fun readFromModel(timeStamp: String): CSVWeatherValues {
        try {
            csvReaderWriter.initReader(fileName, CSVWeatherValues::class.java)
            val csvTimestamp= millsToDateMap[timeStamp.toLong()]
            lateinit var concreteCSVWeatherValues : CSVWeatherValues
            /*
             * This loop is used to find the csvWeatherValues object with the given time stamp
             */
            csvReaderWriter.doRead()?.forEach {
                if (it.timeStamp == csvTimestamp) {
                    concreteCSVWeatherValues = it
                }
            }
            return concreteCSVWeatherValues
        } catch (e: Exception) {
            val error = DTError(message = e.message!!, source = modelManagerID)
            sendError(error, SynchronizationDirection.DT_TO_GATEWAY)
            return CSVWeatherValues("", 0.0f, 0.0f, 0.0f, "")
        }

    }

    /**
     * Returns the latest value of the given property
     */
    override fun getValue(id: String): String {
        val lastUpdate = getLastUpdate(id)
        return if (lastUpdate == "0") {
            lastUpdate
        } else {
            val csvValue = readFromModel(lastUpdate)
            when (id) {
                "Temperature" -> csvValue.temperature.toString()
                "Humidity" -> csvValue.humidity.toString()
                "Pressure" -> csvValue.pressure.toString()
                "Location" -> csvValue.location
                else -> "No value found"
            }
        }
    }

    override fun getAllValues(): Map<String, Any> {
        return emptyMap()
    }

    override fun getValueAtIndex(index: String, property: String): Any {
        val csvValue = readFromModel(index)
        return when (property) {
            "Temperature" -> csvValue.temperature
            "Humidity" -> csvValue.humidity
            "Pressure" -> csvValue.pressure
            "Location" -> csvValue.location
            else -> "No value found"
        }
    }

    /**
     * Stores the values locally in the csvWeatherValues object
     */
    private fun storeValuesLocally(property: String, value: Any) {
        when (property) {
            "Temperature" -> localCSVWeatherValues.temperature = value.toString().toFloat()
            "Humidity" -> localCSVWeatherValues.humidity = value.toString().toFloat()
            "Pressure" -> localCSVWeatherValues.pressure = value.toString().toFloat()
            "Location" -> localCSVWeatherValues.location = value.toString()
        }
    }

    /**
     * Saves the time stamp of the last update of the properties as they are updated together
     */
    private fun saveTime(time : LocalDateTime) {
        propertyIDs.forEach { lastPropertyUpdateMap[it] = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        millsToDateMap[time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()] = time.format(formatter)

    }


}
```

---

### Services

The examples provided in the service section should not be considered as templates for implementing services in general. They merely illustrate how to utilize the various resources provided by the architecture.

##### Demo service

The demo service served as a proof of concept to demonstrate the feasibility of incorporating command-line interface (CLI) functionality within the Digital Twin System (DTS). The CLI is implemented as a REST service, enabling operations such as retrieving datapoints from the CSV file, changing the location of the weather station, or "deactivating" the mapping or other services.

When implemented correctly, much of the functionality currently achieved through switch-case statements should be replaced by differentiating the type of event that was initially thrown. This might necessitate adjustments in the Digital Twin Engine to seamlessly handle the varied types of events.

```
private val logger = KotlinLogging.logger {}
class DemoUIService: AbstractServiceConnection(true, "DemoUIService"),
    IRestService {

    private lateinit var weatherStation : WeatherStation
    private val mockWeatherStation = MockWeatherStation()


    private val thread = Thread(Runnable {
        while (true) {
            if (wantsToExecute() == "true") {
                val task = getTaskToExecute()
                createNewServiceEvent(task!!)
            }
        }
    })

    override fun listen() {
        thread.start()
        logger.info { "$serviceID started" }
    }

    init {
        listen()
    }

    override fun executeTask(taskToExecute: String, digitalTwinEngine: DigitalTwinEngine) {
        logger.info { "Executing task $taskToExecute" }
        when (taskToExecute) {
            "changeLocation" -> digitalTwinEngine.gatewaySet.forEach {
                if (it.responsibleForID("location")) {
                    it as DTWeatherGateway
                    val location = getNewLocation()
                    if (location != null) it.moveStationTo(location)
                    it.createNewDataPointEvent()
                }
            }
            "changeLivelinessStatus" -> digitalTwinEngine.synchronizer.mappingSet.forEach {
                if (it.connectionID == "csvConnection") {
                    it.live = !it.live
                    println("Changed ${it.connectionID} liveliness status to ${it.live}")
                }
                if (it.connectionID == "databaseConnection") {
                    it.live = !it.live
                    println("Changed ${it.connectionID} liveliness status to ${it.live}")
                }
            }
            "serviceLivelinessChange" -> digitalTwinEngine.serviceConnectionSet.forEach {
                if (it.serviceID == "HighestTempInCSVFileService") {
                    it.live = !it.live
                    println("Changed service liveliness status")
                }
            }
            "getValueForDisplay" -> {
                val property = getPropertyForDisplay()
                val value: Any?
                if (property != null) {
                    value = digitalTwinEngine.getResponsibleModelManager(property).getValue(property)
                    postValue(value.toString())
                }
            }
            "changeWeatherStation" -> {
                val gateway = digitalTwinEngine.getResponsibleGateway("location")
                if ((gateway as DTWeatherGateway).weatherStation != mockWeatherStation) {
                    weatherStation = gateway.weatherStation
                    gateway.weatherStation = mockWeatherStation
                } else {
                    gateway.weatherStation = weatherStation
                }
            }
            else -> println("No task to execute")
        }
    }

    override fun serviceUpdate(digitalTwinEngine: DigitalTwinEngine, observedProperty: String) {
        TODO("Not yet implemented")
    }

    private fun createNewServiceEvent(task: String) {
        for(serviceListener in listeners){
            val serviceRequestEvent = ServiceRequestEvent(this)
            serviceRequestEvent.sourceID = serviceID
            serviceRequestEvent.timestamp = LocalDateTime.now()
            serviceRequestEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT
            serviceRequestEvent.message = task
            serviceListener.handleModelGet(serviceRequestEvent)
        }
    }

    private fun wantsToExecute() : String? {
        return makeGetAPICall("getWantsToExecuteDemoUI")
    }

    private fun getNewLocation() : String? {
        return makeGetAPICall("getNewLocationDemoUI")
    }

    private fun getTaskToExecute() : String? {
        return makeGetAPICall("getTaskToExecuteDemoUI")
    }

    private fun getPropertyForDisplay() : String? {
        return makeGetAPICall("getValueForDisplayDemoUI")
    }

    private fun postValue(value : String) : String? {
        return makePostAPICall("postValueDemoUI", value)
    }

}
```

##### Highest Temp in CSV file service

Similar to the DemoUIService, the implementation of this service should not be considered a template. The purpose of the service was to demonstrate the capability of pushing data to a service, handling computations, and notifying the Digital Twin Engine (DTE) once results are available. This was accomplished by informing the service every time a new temperature value was written to the model. This triggered the service to request the history of all temperature values, compute the city with the highest temperature based on the CSV file, and push this information back to the DTE, where it was printed to the console.

```
class HighestTempInCSVFileService : AbstractServiceConnection(true, "HighestTempInCSVFileService"), IRestService {

    var time = System.currentTimeMillis()

    private val thread = Thread(Runnable {
        while (true) {
            if (makeGetAPICall("getRequestStatus") == "finished") {
                for(serviceListener in listeners){
                    val serviceRequestEvent = ServiceRequestEvent(this)
                    serviceRequestEvent.sourceID = serviceID
                    serviceRequestEvent.timestamp = LocalDateTime.now()
                    serviceRequestEvent.synchronizationDirection = SynchronizationDirection.GATEWAY_TO_DT
                    serviceRequestEvent.message = "getHighestTemp"
                    serviceListener.handleModelGet(serviceRequestEvent)
                }
            }
        }
    })

    override fun listen() {
        thread.start()
    }

    init {
        listen()
    }

    override fun executeTask(taskToExecute: String, digitalTwinEngine: DigitalTwinEngine) {
        val highestTemp = makeGetAPICall(taskToExecute)
        println(highestTemp)
    }



    override fun serviceUpdate(digitalTwinEngine: DigitalTwinEngine, observedProperty: String) {

        if (observedProperty == "Temperature" && System.currentTimeMillis() - time > 8000) {
            val csvWeatherValueList = digitalTwinEngine.getResponsibleModelManager("Temperature").getModel()
            postWeatherModel(csvWeatherValueList as ArrayList<CSVWeatherValues>)
            time = System.currentTimeMillis()
        }
    }

    private fun postWeatherModel(csvWeatherValues : ArrayList<CSVWeatherValues>) : String {
        csvWeatherValues.forEach { makePostAPICall("postCSVData", Klaxon().toJsonString(it)) }
        return "CSV file posted"
    }
}
```

---

### helper classes

These are the classes used to implement all the desired functionality. They are not necessary for the core architecture but are included here for reference in the implementation examples.

#### CSV weather values

An object containing all properties of a given datapoint. Each attribute corresponds to a column of the CSV file.

```
class CSVWeatherValues (
    @CsvBindByName(column = "TIMESTAMP")
    var timeStamp: String,
    @CsvBindByName(column = "TEMPERATURE")
    var temperature: Float,
    @CsvBindByName(column = "HUMIDITY")
    var humidity: Float,
    @CsvBindByName(column = "PRESSURE")
    var pressure: Float,
    @CsvBindByName(column = "LOCATION")
    var location: String
) {

    //DO NOT Delete empty constructor. Needed for beans
    constructor() : this("", 0.0f, 0.0f, 0.0f, "")

    fun asString(): String {
        return "$timeStamp, $temperature, $humidity, $pressure, $location"
    }

}
```

##### Weather station

The weather station that simulated a Cyber-Physical System (CPS) in the example. It utilizes the OpenWeather API to obtain data that can be used in the Digital Twin Engine (DTE).

```
open class WeatherStation(var location : String) {

    /**
     * Creates a singleton instance of the OkHttpClient class
     */
    private object OkHttpClient {
        val instance = OkHttpClient()
    }

    /**
     * Makes a call to the OpenWeatherMap API (using okhttp) and returns the response as a json string.
     */
    private fun makeAPICall() : String {
        val url = "https://api.openweathermap.org/data/2.5/forecast?${getGeoLocationFromJSON()}&appid=16a25a823d5722d74a7d93e94062c614"
        val request = Request.Builder()
            .url(url)
            .build()
        OkHttpClient.instance.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body!!.string()
        }
    }

    private fun getGeoLocationFromJSON() : String {
        val response = makeCallToGeocodingAPI(location)
        val jsonObject = JSONObject("{list: $response}")
        val locationData = jsonObject.getJSONArray("list").getJSONObject(0)
        return "lat=${locationData.get("lat")}&lon=${locationData.get("lon")}"
    }

    private fun makeCallToGeocodingAPI(cityName : String) : String {
        val url = "http://api.openweathermap.org/geo/1.0/direct?q=$cityName,276&limit=1&appid=16a25a823d5722d74a7d93e94062c614"
        val request = Request.Builder()
            .url(url)
            .build()
        OkHttpClient.instance.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body!!.string()
        }
    }

    /**
     * Returns the value of the given property from the OpenWeatherMap API response
     */
    private fun getPropertyFromAPIResponse(property: String): Any {
        val jsonObject = JSONObject(this.makeAPICall())
        return jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("main").get(property)
    }

    open fun getValue(property: String) : Any {
        return getPropertyFromAPIResponse(property)
    }
}
```

##### IRestService

A template for a REST client implementation for making `GET` and `POST` API calls. This was used consistently throughout the example implementation.

````
interface IRestService {
    object OkHttpClient {
        val instance = OkHttpClient()
    }

    /**
     * This function makes a get request to the demo Service.
     * @param command: The command for the get mapping without the /.
     */
    fun makeGetAPICall(command: String) : String? {
        val url = "http://localhost:8080/$command"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            OkHttpClient.instance.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                return response.body!!.string()
            }
        } catch (e: Exception) {
            return null
        }

    }

    /**
     * This function makes a post request to the demo Service.
     * @param command: The command for the post mapping without the /.
     * @param json: The json that should be posted.
     */
    fun makePostAPICall(command: String, json: String) : String? {
        val url = "http://localhost:8080/$command"
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody())
            .build()
        try {
            OkHttpClient.instance.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                return response.body!!.string()
            }
        } catch (e: Exception) {
            return null
        }
    }
}
````