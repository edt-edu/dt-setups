import paho.mqtt.client as mqtt


class MqttPublisher:

    def __init__(self, broker_address, port, qos, topic, clientId):
        self.broker_address = broker_address
        self.port = port
        self.qos = qos
        self.topic = topic

        self.client = mqtt.Client(clientId)

    def run(self, message):
        self.client.connect(self.broker_address, self.port)
        print("Connected to MQTT Broker: " + self.broker_address)
        print("Send Message: " + message)
        self.client.publish(self.topic, message, self.qos)
        self.client.loop()
