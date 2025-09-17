# Rahmenbedingungen
- Nur POST Request
- Input Variablen als Request Body
- Output Variables as Response Body
- No Query Parameters

- Use OpenAPI

define base Schemata with field
__schema__: string

- only possible if there is a central gateway not one for each DT. Otherwise dependencies are hard to track and schemata are not applyable efficiently

# Input

# Weather Service (Read Only)

## input variables
```ts
    - location: LocationType
    - date: DateType
```   
## output variables
```ts
    - temperature: TemperatureType
    - humidity: HumidityType
    - wind_speed: WindSpeedType
    - precipitation: PrecipitationType
```

## OpenAPI Schemata
```yaml
openapi: 3.0.0
info:
  title: Weather Service
  version: 1.0.0
servers:
    - url: "{protocol}://{host}:{port}/{basePath}"
        description: The service for weather information
        variables:
        protocol:
            default: "http"
            enum: ["http", "https"]
        host:
            default: "localhost"
        port:
            default: 8080
        basePath:
            default: "/api/v1"
components:
    schemas:
        LocationType:
            type: object
            properties:
                __schema__:
                    type: string
                    description: The schema of the location object.
                name:
                    type: string
                    description: The name of the location.
                coordinates:
                    type: object
                    properties:
                        latitude:
                            type: number
                            description: The latitude of the location.
                        longitude:
                            type: number
                            description: The longitude of the location.
        DateType:
            type: string
            format: date
            description: The date for which to get the weather information.
        TemperatureType:
            type: object
            properties:
                __schema__:
                    type: string
                    description: The schema of the temperature object.
                value:
                    type: number
                    description: The temperature value.
                unit:
                    type: string
                    enum: ["Celsius", "Fahrenheit"]
                    description: The unit of the temperature.
            description: The temperature in Celsius.
        HumidityType:
            type: object
            properties:
                __schema__:
                    type: string
                    description: The schema of the humidity object.
                value:
                    type: number
                    description: The humidity value.
                unit:
                    type: string
                    enum: ["Percentage"]
                    description: The unit of the humidity.
            description: The humidity percentage.
        WindSpeedType:
            type: object
            properties:
                __schema__:
                    type: string
                    description: The schema of the wind speed object.
                value:
                    type: number
                    description: The wind speed value.
                unit:
                    type: string
                    enum: ["km/h", "m/s"]
                    description: The unit of the wind speed.
            description: The wind speed in km/h.
        PrecipitationType:
            type: object
            properties:
                __schema__:
                    type: string
                    description: The schema of the precipitation object.
                value:
                    type: number
                    description: The precipitation value.
                unit:
                    type: string
                    enum: ["mm", "inches"]
                    description: The unit of the precipitation.
            description: The amount of precipitation in mm.
paths:
    /get/weather:
        post:
        summary: Get weather information
        requestBody:
            required: true
            content:
            application/json:
                schema:
                type: object
                properties:
                    location:
                    type: object
                    properties:                        
                        date:
                            $ref: "#/components/schemas/DateType"
                        coordinates:
                            $ref: "#/components/schemas/LocationType"
                    date:
                    type: string
                    format: date
                    description: The date for which to get the weather information.
        responses:
            '200':
            description: Successful response with weather information
            content:
                application/json:
                schema:
                    type: object
                    properties:
                    temperature:
                        $ref: "#/components/schemas/TemperatureType"
                    humidity:
                        $ref: "#/components/schemas/HumidityType"
                    wind_speed:
                        $ref: "#/components/schemas/WindSpeedType"
                    precipitation:
                        $ref: "#/components/schemas/PrecipitationType"
    ```