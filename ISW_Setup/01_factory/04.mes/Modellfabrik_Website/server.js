const express = require("express")
const mqtt = require("mqtt")
const bodyParser = require("body-parser")
const app = express()

const client = mqtt.connect("mqtt://localhost:1883")

client.on("connect", () => {
    console.log("Connected!")
})

app.set("view engine", "ejs")
app.use(express.static("public"))
app.use(bodyParser.json())

app.get("/", (req, res) => {
    res.render("index")
})

app.post("/publish", (req, res) => {
    const topic = "test"
    const message = req.body.msg

    client.publish(topic, message, () => {
        console.log("Nachricht: ${message} gesendet an ${topic}")
        res.json({success: true})
    })
})

app.listen(6008)