var mqtt = require("mqtt")

var MQTT_TOPIC = "test"
var MQTT_ADDR = "mqtt://localhost"
var MQTT_PORT = "1883"

export function connect(){
    var client = mqtt.connect("ws://"+MQTT_ADDR+":"+MQTT_PORT)

    client.on("error", (err) => {
        console.log("Error: ", err)
    })

    client.on("connect", () => {
        console.log("Connected")
        client.publish(MQTT_TOPIC, "Test Nachricht")
    })
}