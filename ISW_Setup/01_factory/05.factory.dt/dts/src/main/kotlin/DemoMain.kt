package dts

import dts.gateway.implementations.mqttGateway.MqttClientImplementation
import dts.gateway.implementations.mqttGateway.MqttConfig
import dts.gateway.influx.InfluxConfig
import dts.gateway.influx.InfluxConnection
import org.json.JSONObject
import kotlin.math.sin

object DemoMain {
    private val idleEnergy = mapOf(
        "conveyor" to 1.3,
        "clawGripper" to 1.4,
        "vacuumGripper" to 1.4,
        "sortingLine" to 1.2,
        "warehouse" to 1.5,
        "indexedLine" to 1.6
    )

    private fun fourier(x: Double): Double {
        var sum = 0.0
        for (n in 1..4) {
            sum += sin(n * x) / n
        }
        return sum
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val mqttBroker = args.getOrElse(0) { "tcp://localhost:1883" }
        val influxUrl = args.getOrElse(1) { "http://localhost:8086" }
        val influxToken =
            args.getOrElse(2) { "Lg7L-2pjeUTClrKAzqAdHXigzb5vUP1i6P1wRrx6hl0Rco0DtHdXnNQtP4Za8ZWXEsI95fojVhD8YNFt-VSiuQ==" }
        val influxOrg = args.getOrElse(3) { "isw" }
        val influxBucket = args.getOrElse(4) { "insel1" }

        val topics = listOf(
            "1-1-conveyor/status",
            "1-2-clawGripper/status",
            "1-3-conveyor/status",
            "1-4-sortingLine/status",
            "1-5-warehouse/status",
            "1-6-vacuumGripper/status",
            "1-7-indexedLine/status",
            "1-8-clawGripper/status"
        )

        val subscribeMap = topics.associateWith { 0 }

        val mqttConfig = MqttConfig(
            configId = "demo",
            brokerUrl = mqttBroker,
            topicsToSubscribe = subscribeMap
        )

        val mqttClient = MqttClientImplementation(mqttConfig)
        val influxConnection = InfluxConnection(
            InfluxConfig(
                url = influxUrl,
                token = influxToken,
                org = influxOrg,
                bucket = influxBucket
            )
        )


        try {
            mqttClient.connect(mqttConfig.toConnectOptions())
        } catch (e: Exception) {
            println("Failed to connect to MQTT broker: ${e.message}")
        }

        println("Demo started!")
        try {
            while (true) {
                var sum = 0.0
                topics.forEach { topic ->
                    mqttClient.subscribe(topic) { t, message ->
                        println("Empfange Nachricht auf Topic: $t")
                        val payload = String(message)
                        println("Payload empfangen: $payload")

                        val machine = t.substringBefore("/status")

                        influxConnection.writeMeasurement(
                            measurement = "machine_status",
                            fields = mapOf("payload" to payload),
                            tags = mapOf("machine" to machine)
                        )
                        println("machine_status in Influx geschrieben für $machine")

                        val json = try {
                            JSONObject(payload)
                        } catch (e: Exception) {
                            println("Fehler beim Parsen von JSON: ${e.message}")
                            JSONObject()
                        }

                        val machineType = machine.split("-").drop(2).joinToString("-")
                        val idle = idleEnergy[machineType] ?: 1.0
                        println("Maschinentyp: $machineType, Idle Energy: $idle")

                        var active = false
                        val numericValues = mutableListOf<Double>()
                        json.keys().forEach { key ->
                            val value = json.opt(key)
                            when (value) {
                                is Number -> numericValues += value.toDouble()
                                is String -> {
                                    val v = value.toLowerCase()
                                    val k = key.toLowerCase()
                                    val activeKeywords = listOf(
                                        "moving", "rotating", "sorting", "retrieving", "storing",
                                        "targeting", "processing", "running"
                                    )
                                    val inactiveKeywords = listOf(
                                        "ready", "stopped", "failure", "failed", "waiting", "at_pickup"
                                    )

                                    if (k.contains("state") || k.contains("status")) {
                                        if (inactiveKeywords.any { v.contains(it) }) {
                                            active = false
                                        } else if (activeKeywords.any { v.contains(it) }) {
                                            active = true
                                        }
                                    }
                                }
                            }
                        }
                        val machineValue = if (numericValues.isNotEmpty()) numericValues.average() else 0.0
                        val energy = if (active) fourier(machineValue) + idle else idle

                        println("Berechnete Energie: $energy (Machine Value: $machineValue, Active: $active)")

                        influxConnection.writeMeasurement(
                            measurement = "energy",
                            fields = mapOf("value" to energy),
                            tags = mapOf("machine" to machine)
                        )
                        sum += energy
                        println("Energiewert in Influx geschrieben für $machine\n")
                    }
                }

                influxConnection.writeMeasurement(
                    measurement = "energy",
                    fields = mapOf("value" to sum),
                    tags = mapOf("insel" to "1-insel/status")
                )
                sum = 0.0

                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            println(e.message)
        } finally {
            mqttClient.disconnect()
            influxConnection.close()
        }
    }
}