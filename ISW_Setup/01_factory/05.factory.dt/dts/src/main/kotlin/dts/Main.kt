package dts

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import com.xenomachina.argparser.default
import dts.events.observer.*
import dts.gateway.IslandGateway
import dts.gateway.LifecycleGateway
import dts.services.DemoSequenceService
import dts.modelmanager.ModelManager
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence

class DTArgs(parser: ArgParser) {
    val broker: String by parser.storing("--mqtt", help="MQTT broker address")
        .default("tcp://127.0.0.1:1883")
    val island: String by parser.storing("--island", help="Island name")
        .default("island1")
    val influxDB: String by parser.storing("--influx-db", help="Influx DB server address")
        .default("http://localhost:8086")
    val influxToken: String by parser.storing("--influx-token", help="Influx API key")
        .default("uiGYbpIcfLiFkWIUqoNGGtKCxrnOqFu2E52Sj7spBPNEtdv3mxN9adfVpcr3p52OoIThlVk8wEbnbSixu0yguw==")
    val influxOrga: String by parser.storing("--influx-orga", help="Influx Organisation Name")
        .default("Stupro")
    val influxBucket: String by parser.storing("--influx-bucket", help="Influx Bucket Name")
        .default("Island1")
}

class Main {
    companion object {
        @JvmStatic fun main(rawArgs: Array<String>) = mainBody {
            val args = ArgParser(rawArgs).parseInto(::DTArgs)

            val machines: Set<String> = setOf(
                "1-1-conveyor",
                "1-2-clawGripper",
                "1-3-conveyor",
                "1-4-sortingLine",
                "1-5-warehouse",
                "1-6-vacuumGripper",
                "1-7-indexedLine",
                "1-8-clawGripper",
            )

            // create the engine
            val influxDBClient = InfluxDBClientKotlinFactory
                .create(args.influxDB,
                    args.influxToken.toCharArray(),
                    args.influxOrga,
                    args.influxBucket)
            val modelManager = ModelManager(setupName = "Island1", influxDBClient = influxDBClient)
            val engine = DigitalTwinEngine(modelManager =  modelManager)

            val persistence = MemoryPersistence()
            val connOpts = MqttConnectionOptions()
            val client = MqttAsyncClient(args.broker, "lifecycle_gateway", persistence)
            connOpts.isCleanStart = false
            val token = client.connect(connOpts)
            logger.info { "Connecting lifecycleGateway MQTT client" }
            token.waitForCompletion()
            val lifecycleGateway = LifecycleGateway("lifecycleGateway", args.island, client,
                machines=machines,
                autoAddMachines=false)
            lifecycleGateway.addObserver(GatewayObserver(engine))

            val machineGateway = IslandGateway("island1Gateway", client,
                machines=machines)
            machineGateway.addObserver(GatewayObserver(engine))

            val demoService = DemoSequenceService(modelManager, machineGateway)
            demoService.addObserver(ServiceObserver(engine))
            machineGateway.addObserver(ServiceGatewayObserver(demoService))

            engine.addGateway(machineGateway)
            engine.addGateway(lifecycleGateway)
        }
    }
}
