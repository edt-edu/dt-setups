import multiprocessing

import paho.mqtt.client as mqtt
import queue
from multiprocessing import Queue


class MqttSubscriber:

    def __init__(self, broker_address, port, topic, clientId):
        self.broker_address = broker_address
        self.port = port
        self.topic = topic

        self.input_buffer = multiprocessing.Queue()

        self.client = mqtt.Client(clientId)
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

    def on_message(self, client, userdata, message):
        msg = str(message.payload.decode("utf-8"))
        self.input_buffer.put(msg)
        print("Message received: ", msg)
        print("Message topic: ", message.topic)

    def on_connect(self, client, userdata, flags, rc):
        print("Connected to MQTT Broker: " + self.broker_address)
        client.subscribe(self.topic)

    def get_oldest_message(self):
        return self.input_buffer.get(block=False)

    def get_is_empty(self):
        return self.input_buffer.empty()

    def run(self):
        self.client.connect(self.broker_address, self.port)
        self.client.loop_start()
