package de.unistuttgart.isw.examples.weather;

import de.unistuttgart.isw.annotations.ExportService;
import de.unistuttgart.isw.annotations.ExportServiceFunction;
import de.unistuttgart.isw.annotations.ExportServiceType;
import de.unistuttgart.isw.annotations.ExportServiceTypeField;

@ExportService(
    serviceName = "WeatherService",
    identifier = "weatherService"
)
public class WeatherService {

    
    @ExportServiceType(
        typeName = "RequestWeatherData",
        identifier = "RequestWeatherData"
    )
    public static class WeatherDataRequest {
        public CoordinatesType coordinates;        
    }

    @ExportServiceFunction(
        inputType = WeatherDataRequest.class,
        outputType = WeatherData.class,
        serviceNameString = "WeatherService",
        identifier = "getWeatherData"
    )
    public WeatherData getWetherData(WeatherDataRequest request) {
        // Simulate fetching weather data
        CoordinatesType coordinates = request.coordinates;
        TemperatureType temperature = new TemperatureType(25.0, "Celsius");
        WindType wind = new WindType(10.0, "North", "km/h");
        HumidityType humidity = new HumidityType(60.0, "%");
        PrecipitationType precipitation = new PrecipitationType(5.0, "mm");

        return new WeatherData(coordinates, temperature, wind, humidity, precipitation);
    }


    @ExportServiceType(
        typeName = "WeatherData",
        identifier = "WeatherData"
    )
    public static class WeatherData {

        @ExportServiceTypeField(
            name = "coordinates",
            type=CoordinatesType.class
        )
        public CoordinatesType coordinates;

        @ExportServiceTypeField(
            name = "temperature",
            type=TemperatureType.class
        )
        public TemperatureType temperature;
        @ExportServiceTypeField(
            name = "wind",
            type=WindType.class
        )
        public WindType wind;
        @ExportServiceTypeField(
            name = "humidity",
            type=HumidityType.class
        )
        public HumidityType humidity;
        @ExportServiceTypeField(
            name = "precipitation",
            type=PrecipitationType.class
        )
        public PrecipitationType precipitation;

        public WeatherData(CoordinatesType coordinates, TemperatureType temperature, WindType wind, HumidityType humidity, PrecipitationType precipitation) {
            this.coordinates = coordinates;
            this.temperature = temperature;
            this.wind = wind;
            this.humidity = humidity;
            this.precipitation = precipitation;
        }
       
    }

    @ExportServiceType(
        typeName = "Coordinates",
        identifier = "Coordinates"
    )
    public static class CoordinatesType {
        @ExportServiceTypeField(
            name = "latitude",
            type=Double.class
        )
        public double latitude;
        @ExportServiceTypeField(
            name = "longitude",
            type=Double.class
        )
        public double longitude;
        public CoordinatesType(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @ExportServiceType(
        typeName = "Temperature",
        identifier = "Temperature"
    )
    public static class TemperatureType {
        @ExportServiceTypeField(
            name = "value",
            type=Double.class
        )
        public double value;
        @ExportServiceTypeField(
            name = "unit",
            type=String.class,
            isEnum=true,
            onlyValues={"Celsius", "Fahrenheit", "Kelvin"}
        )
        public String unit;
        public TemperatureType(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    @ExportServiceType(
        typeName = "Wind",
        identifier = "Wind"
    )
    public static class WindType {
        @ExportServiceTypeField(
            name = "speed",
            type=Double.class
        )
        public double speed;
        @ExportServiceTypeField(
            name = "direction",
            type=String.class
        )
        public String direction;
        @ExportServiceTypeField(
            name = "speedUnit",
            type=String.class,
            isEnum=true,
            onlyValues={"km/h", "m/s", "mph"}
        )
        public String speedUnit;
        public WindType(double speed, String direction, String speedUnit) {
            this.speed = speed;
            this.direction = direction;
            this.speedUnit = speedUnit;
        }
    }

    @ExportServiceType(
        typeName = "Humidity",
        identifier = "Humidity"
    )
    public static class HumidityType {
        @ExportServiceTypeField(
            name = "value",
            type=Double.class
        )
        public double value;
        @ExportServiceTypeField(
            name = "unit",
            type=String.class,
            isEnum=true,
            onlyValues={"%"}
        )
        public String unit;        
        public HumidityType(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }
    @ExportServiceType(
        typeName = "Precipitation",
        identifier = "Precipitation"
    )
    public static class PrecipitationType {
        @ExportServiceTypeField(
            name = "amount",
            type=Double.class
        )
        public double amount;
        @ExportServiceTypeField(
            name = "unit",
            type=String.class,
            isEnum=true,
            onlyValues={"mm", "cm", "inches"}
        )
        public String unit;
        public PrecipitationType(double amount, String unit) {
            this.amount = amount;
            this.unit = unit;
        }
    }


}



