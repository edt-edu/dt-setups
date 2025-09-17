import threading

import revpimodio2
import time
import MultiProcessingMappings
import paho.mqtt.client as mqtt
import json

if __name__ == '__main__':
    rpi = revpimodio2.RevPiModIO(autorefresh=True)

    #get
    getValue = rpi.io.I_1.value
    #set
    #String
    rpiMappings = MultiProcessingMappings.MPMapper(rpi)

    motor_clockwise = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_clockwise']]]
    motor_counterclockwise = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_counterclockwise']]]
    motor_conveyor_forward = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_conveyor_forward']]]
    motor_saw = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_saw']]]
    motor_oven_in = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_oven_in']]]
    motor_oven_out = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_oven_out']]]
    motor_vacuum_to_oven = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_vacuum_to_oven']]]
    motor_vacuum_to_turntable = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['motor_vacuum_to_turntable']]]
    light_oven = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['light_oven']]]
    compressor = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['compressor']]]
    valve_vacuum = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['valve_vacuum']]]
    valve_lowering = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['valve_lowering']]]
    valve_oven_door = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['valve_oven_door']]]
    valve_feeder = rpiMappings.RPIO[rpiMappings.RevPi_output_mapping[rpiMappings.FT_output_mapping['valve_feeder']]]

    #plus_power_supply_actuators = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['plus_power_supply_actuators']]]
    #minus_power_supply_actuators = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['minus_power_supply_actuators']]]
    #plus_power_supply_sensors = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['plus_power_supply_sensors']]]
    #minus_power_supply_sensors = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['minus_power_supply_sensors']]]
    reference_switch_turntable_pos_vacuum = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_turntable_pos_vacuum']]]
    reference_switch_vacuum_pos_belt = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_vacuum_pos_belt']]]
    light_barrier_end_conv_belt = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['light_barrier_end_conv_belt']]]
    reference_switch_turntable_pos_saw = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_turntable_pos_saw']]]
    reference_switch_vacuum_pos_turntable = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_vacuum_pos_turntable']]]
    reference_switch_oven_feeder_inside = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_oven_feeder_inside']]]
    reference_switch_oven_feeder_outside = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_oven_feeder_outside']]]
    reference_switch_vacuum_pos_oven = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['reference_switch_vacuum_pos_oven']]]
    light_barrier_oven = rpiMappings.RPII[rpiMappings.RevPi_input_mapping[rpiMappings.FT_input_mapping['light_barrier_oven']]]


def MQTTtrigger(skill, topic, expected_value, broker_ip="host.docker.internal", broker_port=1884):
    # Callback-Funktion, die aufgerufen wird, wenn eine Nachricht empfangen wird
    def on_message(client, userdata, message):
        print(f"Message received: {message.payload.decode()} on topic {message.topic}")
        try:
            payload = json.loads(message.payload.decode())
            if payload.get("value") == expected_value:
                print("Trigger message received. Executing skill...")
                skill()  # Aufruf der übergebenen Skill-Funktion
        except json.JSONDecodeError:
            print("Failed to decode JSON message")

    # MQTT-Client initialisieren
    client = mqtt.Client()

    # Callback-Funktion zuweisen
    client.on_message = on_message

    # Verbindung zum Broker herstellen
    client.connect(broker_ip, broker_port)

    # Topic abonnieren
    client.subscribe(topic)

    # Schleife starten, um auf Nachrichten zu warten
    client.loop_forever()

def MQTTget(topic, broker_ip="host.docker.internal", broker_port=1884):
    global global_variables

    def on_message(client, userdata, message):
        print(f"Property update message received: {message.payload.decode()} on topic {message.topic}")
        try:
            payload = json.loads(message.payload.decode())
            property_name = payload.get("idShort")
            property_value = payload.get("value")
            if property_name and property_value:
                # Update global variable
                global_variables[property_name] = int(property_value)
                print(f"Updated {property_name} to: {global_variables[property_name]} seconds")
        except json.JSONDecodeError:
            print("Failed to decode JSON message")

    # MQTT-Client initialisieren
    client = mqtt.Client()

    # Callback-Funktion zuweisen
    client.on_message = on_message

    # Verbindung zum Broker herstellen
    client.connect(broker_ip, broker_port)

    # Topic abonnieren
    client.subscribe(topic)

    # Schleife starten, um auf Nachrichten zu warten
    client.loop_forever()

def burn_workpiece():
    def wait_with_timeout(check_function, timeout, check_interval=0.1):
        start_time = time.time()
        while not check_function():
            if time.time() - start_time > timeout:
                print("Timeout reached, aborting process.")
                return False
            time.sleep(check_interval)
        return True

    # Wait for light barrier oven
    if not wait_with_timeout(lambda: light_barrier_oven.value == 1, 10):  # timeout of 10 seconds
        print("Process aborted: light barrier oven not activated.")
        return

    # Open oven door
    compressor.value = 1
    valve_oven_door.value = 1
    print(f"Compressor: {compressor.value}, Valve Oven Door: {valve_oven_door.value}")

    # Wait 2 seconds
    time.sleep(2)

    # Move oven feeder in
    compressor.value = 0
    motor_oven_in.value = 1
    print(f"Compressor: {compressor.value}, Motor Oven In: {motor_oven_in.value}")

    # Wait for reference switch oven feeder inside
    if not wait_with_timeout(lambda: reference_switch_oven_feeder_inside.value == 1, 10):  # timeout of 10 seconds
        print("Process aborted: reference switch oven feeder inside not activated. Motor oven in is deactivated.")
        motor_oven_in.value = 0
        return

    # Stop motor and close oven door
    motor_oven_in.value = 0
    valve_oven_door.value = 0
    print(f"Motor Oven In: {motor_oven_in.value}, Valve Oven Door: {valve_oven_door.value}")

    time.sleep(1)

    # Turn on oven light
    light_oven.value = 1
    print(f"Light Oven: {light_oven.value}")

    # wait for duration
    duration = global_variables.get("duration", 0)
    print(f"Waiting for duration: {duration} seconds")
    time.sleep(duration)

    # Open oven door & Turn off oven light
    compressor.value = 1
    valve_oven_door.value = 1
    print(f"Compressor: {compressor.value}, Valve Oven Door: {valve_oven_door.value}")
    light_oven.value = 0
    print(f"Light Oven: {light_oven.value}")

    time.sleep(2)

    # Move oven feeder out
    motor_oven_out.value = 1
    print(f"Motor Oven Out: {motor_oven_out.value}")

    # Wait for reference switch oven feeder outside
    if not wait_with_timeout(lambda: reference_switch_oven_feeder_outside.value == 1, 10):  # timeout of 10 seconds
        print("Process aborted: reference switch oven feeder outside not activated. Motor oven out is deactivated.")
        motor_oven_out.value = 0
        return

    # Stop motor and close oven door
    motor_oven_out.value = 0
    valve_oven_door.value = 0
    print(f"Motor Oven Out: {motor_oven_out.value}, Valve Oven Door: {valve_oven_door.value}")

def emergency_stop():
    # Open oven door
    compressor.value = 1
    valve_oven_door.value = 1
    print(f"Compressor: {compressor.value}, Valve Oven Door: {valve_oven_door.value}")

    # Turn off oven light
    light_oven.value = 0
    print(f"Light Oven: {light_oven.value}")

def simulate_ambient_temperature(broker_ip="host.docker.internal", broker_port=1884):
    ambient_temperature = 18
    client = mqtt.Client()
    client.connect(broker_ip, broker_port)

    while True:
        if light_oven.value == 1:
            # Oven is on, temperature rises
            ambient_temperature += 1
        else:
            # Oven is off, temperature falls
            ambient_temperature -= 1

        # Limit the temperature to a minimum of 18 degrees
        if ambient_temperature < 18:
            ambient_temperature = 18

        # Create the MQTT message
        message = json.dumps({"ambient_temperature": ambient_temperature})

        # Publish the message to the topic "ambient_temperature"
        client.publish("ambient_temperature", message)

        print(f"Ambient Temperature: {ambient_temperature}°C")

        time.sleep(1)  # Update the temperature every second


topic1 = "sm-repository/sm-repo/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQXNzZXRJbnRlcmZhY2VzRGVzY3JpcHRpb24/submodelElements/InterfaceTemplateForMQTT.InteractionMetadata.properties.{property_name}.observable/updated"
topic2 = "sm-repository/sm-repo/submodels/aHR0cHM6Ly9hZG1pbi1zaGVsbC5pby9pZHRhL1N1Ym1vZGVsVGVtcGxhdGUvQXNzZXRJbnRlcmZhY2VzRGVzY3JpcHRpb24/submodelElements/InterfaceTemplateForMQTT.InteractionMetadata.properties.{property_name}.title/updated"
topic3 = "test" #hier richtig einsetzen

# Zwei separate Threads für die MQTT-Clients starten
thread1 = threading.Thread(target=MQTTtrigger, args=(burn_workpiece, topic1, "true"))
thread2 = threading.Thread(target=MQTTtrigger, args=(emergency_stop, topic2, "true"))
thread3 = threading.Thread(target=MQTTget, args=topic3)

thread1.start()
thread2.start()
thread3.start()
simulate_ambient_temperature()
