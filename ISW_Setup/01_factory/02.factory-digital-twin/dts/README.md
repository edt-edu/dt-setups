# Digital Twin System

## Run the application

1. Run `mvn clean package`
2. Find `target/dts-<VERSION>-jar-with-dependencies.jar`
3. Start required services
   - Ensure that a MQTT broker is running and reachable
   - Ensure that an InfluxDB instance is running
4. Run `java -jar target/dts-<VERSION>-jar-with-dependencies.jar`

Alternative, during development, run `mvn compile exec:java`

## Arguments

- `--help` Show the help message and all available arguments
- `--mqtt` The address of the MQTT broker (default is localhost)
- `--island` The name of the island (default is island1)  
  Note that this does not change any other configuration, e.g. machines
- `--influx-db` The address of the Influx DB server
- `--influx-token` The Influx api token
- `--influx-orga` The Influx organisation name
- `--influx-bucket` The Influx bucket name
