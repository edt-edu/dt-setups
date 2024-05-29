from typing import Union
from paho.mqtt import client
import os

# TODO: extract Gateway, MqttGateway
class VacuumGripperMqttGateway:

    def __init__(self, mqtt_id: Union[str, int]):
        self.mqtt_id = mqtt_id
        if os.getenv("MQTT_ENABLED") == "true":
          self.client = self.connect_mqtt()
        else:
          print("MQTT is disabled. Set environment variable MQTT_ENABLED=true to enable")
          self.client = None

    def connect_mqtt(self) -> client.Client:
        def on_connect(client, userdata, flags, rc, properties):
            if rc == 0:
                print("Connected to MQTT Broker!")
            else:
                raise Exception("Failed to connect, return code %d\n", rc)
        
        c = client.Client(client_id=str(self.mqtt_id), callback_api_version=client.CallbackAPIVersion.VERSION2)
        print("Creating gateway")
        c.on_connect = on_connect
        c.connect(os.getenv("MQTT_HOST", "localhost"), 1883) # TODO: same as server?
        return c

    # send the input port values 
    def reportInputs(self, verticalUp: bool,
                      verticalDown: bool,
                      horizontalForward: bool,
                      horizontalBack: bool,
                      rotationClockwise: bool,
                      rotationCounterclockwise: bool,
                    ):
        if self.client is None:
          return

        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/verticalUp", verticalUp)
        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/verticalDown", verticalDown)
        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/horizontalForward", horizontalForward)
        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/horizontalBack", horizontalBack)
        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/rotationClockwise", rotationClockwise)
        self.client.publish("/vacuumGripper/" + self.mqtt_id + "/rotationCounterclockwise", rotationCounterclockwise)

    def reportVerticalUp(self, verticalUp:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/verticalUp", verticalUp)

    def reportVerticalDown(self, verticalDown:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/verticalDown", verticalDown)

    def reportHorizontalForward(self, horizontalForward:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/horizontalForward", horizontalForward)

    def reportHorizontalBack(self, horizontalBack:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/horizontalBack", horizontalBack)

    def reportRotationClockwise(self, rotationClockwise:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/rotationClockwise", rotationClockwise)

    def reportRotationCounterclockwise(self, rotationCounterclockwise:bool):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/rotationCounterclockwise", rotationCounterclockwise)

    def reportCounterHorizontal(self, counter):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/counterHorizontal", counter)

    def reportCounterVertical(self, counter):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/counterVertical", counter)

    def reportCounterRotation(self, counter):
      if self.client is None:
        return
      self.client.publish("/vacuum-gripper/" + self.mqtt_id + "/counterRotation", counter)