"""Run the RevPi control code"""

import time
from contextlib import contextmanager
import json
import logging
import threading
from enum import Enum
from typing import Any, Callable, Dict, Union
from paho.mqtt.client import topic_matches_sub
from threading import Thread

from .machines import BaseMachine, UnknownMachineType, get_machine_from_description
from .machines.base_machine import OperationInProgress
from .mqtt import (
    byte_array_parser,
    machine_control_topic,
    machine_status_topic,
    machine_lifecycle_topic,
    get_wildcard_topic
)
from .mqtt.mqtt_logger import MQTTHandler


class MessageType(Enum):
    JSON = 0
    ByteArray = 1


class IslandLifecycle(Enum):
    ONLINE = "online"
    SHUTTING_DOWN = "shutting down"


class MachineLifecycle(Enum):
    ONLINE = "online"
    ALREADY_CREATED = "already created"
    DESTROYED = "destroyed"
    FAILURE = "failed"
    UNKNOWN_TYPE = "unknown type"
    DESTROY_ABORTED = "destroy aborted"


class Main:

    def __init__(self, name,
                 rpi,
                 mqtt_client,
                 qos=0,
                 logger=None,
                 cycle_time=0.2,
                 stop_timeout=10,
                 message_type: MessageType = MessageType.JSON):
        """Control revpi execution

        Expects `rpi` and `mqtt_client` to be already configured instances
        of RevPiModIO and mqtt.Client.
        This class expects to have full control over these instances.
        `cycle_time` is the number of seconds between cycles
        `qos` TBD
        """
        self._name = name
        self._rpi = rpi
        self._client = mqtt_client
        self._qos = qos
        if logger is not None:
            self._logger = logger
        else:
            self._logger = logging.getLogger(__name__)
        self._lock = threading.Lock()
        self._lock_timeout = 0.5

        self._encoder: Callable[[Dict[str, Any]], Union[str, bytearray]]
        self._decoder: Callable[[bytearray], Any]
        if message_type == MessageType.ByteArray:
            self._encoder = byte_array_parser.encode
            self._decoder = byte_array_parser.decode
        elif message_type == MessageType.JSON:
            self._encoder = lambda payload: json.dumps(payload, indent=2)
            self._decoder = lambda payload: json.loads(payload.decode())
        self._send_all_status_messages = False

        self._cycle_time = cycle_time
        self._stop_timeout = stop_timeout
        self._stop_event = threading.Event()
        "Signal abort from loop and cleanup"
        self._machines: Dict[str, BaseMachine] = dict()
        "Machines controlled by this instance"

        self._island_status = f"{name}/status"
        self._island_error = f"{name}/errors"
        self._stop_topic = f"{name}/stop"
        self._create_topics = f"{name}/create/+"
        self._destroy_topics = f"{name}/destroy/+"
        self._query_all_topic = f"{self._name}/QueryAll"
        self._is_running = False

    @property
    def stop(self):
        return self._stop_event

    def on_message(self, client, userdata, message):
        if self._stop_event.is_set():
            return
        topic = message.topic
        with self._lock:
            if topic_matches_sub(self._stop_topic, topic):
                self._stop_event.set()
            elif topic_matches_sub(self._create_topics, topic):
                name = get_wildcard_topic(self._create_topics, topic)[0]
                self._add_machine(name, json.loads(message.payload.decode()))
            elif topic_matches_sub(self._destroy_topics, topic):
                name = get_wildcard_topic(self._destroy_topics, topic)[0]
                self._destroy_machine(name)
            elif topic_matches_sub(self._query_all_topic, topic):
                self._send_all_status_messages = True
            elif topic_matches_sub("+/control", topic):
                name = get_wildcard_topic("+/control", topic)[0]
                try:
                    machine = self._machines[name]
                except KeyError:
                    self._logger.info("Unknown machine %s", name)
                try:
                    control = self._decoder(message.payload)
                except ValueError as error:
                    self._logger.warning("Could not decode control message send to %s. Ignoring message", topic,
                                         exc_info=error)
                    return
                self._logger.info(control)
                try:
                    machine.handle_control(control)
                except Exception as error:
                    self._logger.error("Control handler failed for machine %s", name, exc_info=error)
            else:
                self._logger.info("Unknown mqtt topic '%s'", topic)

    def on_disconnect(self, client, userdata, dc_flag, rc, props):
        if rc.is_failure:
            self._logger.error("MQTT disconnected (%s), stopping revpi", rc.getName())
            self._stop_event.set()

    def signal_stop(self):
        self._stop_event.set()

    def _subscribe_machine_control(self, name):
        self._client.subscribe(machine_control_topic(name))

    def _unsubscribe_machine_control(self, name):
        self._client.unsubscribe(machine_control_topic(name))

    def configure_machines(self, machine_descriptions: Dict[str, dict]):
        """For specifying an initial set of machines

        Should be called at most once during initialisation or never at all.
        """
        with self._lock:
            if self._machines:
                raise Exception("Machines already initialised")
            self._logger.info("Starting batch machine initialisation")
            for name, description in machine_descriptions.items():
                self._add_machine(name, description)

    def _add_machine(self, name, description):
        """Initialise and add a machine

        Machine creation might take some time and is done within a new thread.
        The calling method must hold the lock.
        """
        self._logger.debug("Adding machine %s", name)
        if name in self._machines:
            self._send_machine_lifecycle(name, MachineLifecycle.ALREADY_CREATED)
            self._logger.warning("Machine %s is already added", name)
            return
        self._machines[name] = None
        creator = Thread(target=self._create_machine_thread, args=(name, description), daemon=True)
        creator.start()

    def _create_machine_thread(self, name, description):
        """Thread for creating machines"""
        try:
            machine = get_machine_from_description(name, description, self._logger, self._lock, self._rpi.io)
        except UnknownMachineType as unknown_type:
            self._logger.error(unknown_type)
            self._send_machine_lifecycle(name, MachineLifecycle.UNKNOWN_TYPE)
            with self._lock:
                del self._machines[name]
        except Exception as error:
            self._logger.error("Could not create machine %s", name, exc_info=error)
            self._send_machine_lifecycle(name, MachineLifecycle.FAILURE)
            with self._lock:
                del self._machines[name]
        else:
            self._logger.info("Created machine %s", name)
            with self._lock:
                self._machines[name] = machine
            self._subscribe_machine_control(name)
            self._send_machine_lifecycle(name, MachineLifecycle.ONLINE)

    def _send_machine_lifecycle(self, name: str, status: MachineLifecycle):
        """Send the machine lifecycle"""
        return self._client.publish(machine_lifecycle_topic(self._name, name), status.value, retain=True)

    def _send_island_lifecycle(self, status: IslandLifecycle):
        """Send the island lifecycle message"""
        return self._client.publish(self._island_status, status.value, retain=True)

    def _cleanup_machines(self):
        """Cleanup all machines

        This method is only called once no new machines can be created or commands received.
        Therefor, machines with a pending initialisation are skipped.
        NOTE: This might be a problem if the island is stopped and started within the same
        python instance multiple times.

        This method might block.
        The calling method must not hold the lock.
        """
        self._logger.info("Cleanup machines")
        for name, machine in self._machines.items():
            if machine is None:
                self._logger.warning("Skipping cleanup of not initialized machine %s", name)
                continue
            self._unsubscribe_machine_control(name)
            machine.cleanup()
            self._send_machine_lifecycle(name, MachineLifecycle.DESTROYED)
        with self._lock:
            self._machines = dict()

    def _destroy_machine(self, name):
        """Destroy a machine

        This method should never block.
        The calling method must hold the lock.
        """
        self._logger.info("Destroying machine %s", name)
        machine = self._machines[name]
        if machine is None:
            self._logger.warning("Machine %s is not initialised, aborting destroy", name)
            return
        try:
            machine.destroy()
        except OperationInProgress:
            self._logger.warning("Machine %s could not be destroyed", name)
            self._send_machine_lifecycle(name, MachineLifecycle.DESTROY_ABORTED)
        else:
            del self._machines[name]
            self._unsubscribe_machine_control(name)
            self._send_machine_lifecycle(name, MachineLifecycle.DESTROYED)

    def send_changed_machine_status(self):
        """Send all machine status messages for machines with a changed status

        The calling method must hold the lock.
        """
        force_send = self._send_all_status_messages
        for name, machine in self._machines.items():
            if machine is None:
                continue
            if force_send or machine.state_changed():
                status = self._encoder(machine.get_status())
                self._client.publish(machine_status_topic(name), status)
        self._send_all_status_messages = False

    @contextmanager
    def _mqtt_client_loop(self):
        """Context manager for using the MQTT client loop

        This manages the loop and all island specific subscriptions.
        This adds log handler to send logs via MQTT.
        """
        mqtt_handler = MQTTHandler(self._client, self._island_error)
        mqtt_handler.setLevel(logging.WARNING)
        try:
            self._logger.info("Starting the MQTT loop")
            logging.root.addHandler(mqtt_handler)
            self._client.loop_start()
            self._client.subscribe(self._stop_topic)
            self._client.subscribe(self._create_topics)
            self._client.subscribe(self._destroy_topics)
            self._client.subscribe(self._query_all_topic)
            self._client.on_message = self.on_message
            self._client.on_disconnect = self.on_disconnect
            yield self._client
        finally:
            self._logger.info("Stopping the MQTT loop")
            self._client.on_message = None
            self._client.on_disconnect = None
            self._client.unsubscribe(self._stop_topic)
            self._client.unsubscribe(self._create_topics)
            self._client.unsubscribe(self._destroy_topics)
            self._client.unsubscribe(self._query_all_topic)
            self._client.loop_stop()
            logging.root.removeHandler(mqtt_handler)

    @contextmanager
    def _revpi_loop(self):
        try:
            self._logger.info("Starting the RevPi loop")
            self._rpi.mainloop(blocking=False)
            yield self._rpi
        finally:
            self._logger.info("Stopping RevPi")
            self._rpi.exit()

    def run(self):
        if self._is_running:
            raise Exception('Already running')
        self._stop_event.clear()
        self._is_running = True

        with self._mqtt_client_loop(), self._revpi_loop():
            self._logger.info("Configuration complete, starting loop")
            self._send_island_lifecycle(IslandLifecycle.ONLINE)

            while not self._stop_event.is_set():
                locked = self._lock.acquire(timeout=self._lock_timeout)
                if not locked:
                    self._logger.critical("Could not acquire global revpi lock (waited for %f s)", self._lock_timeout)
                    continue
                try:
                    self.send_changed_machine_status()
                finally:
                    self._lock.release()
                self._stop_event.wait(self._cycle_time)
            self._logger.info("Received shutdown signal")

            self._send_island_lifecycle(IslandLifecycle.SHUTTING_DOWN)

            self._cleanup_machines()

            # TODO Wait until all MQTT messages are sent/acknowledged.
            # Probably related to QoS setting requiring acknowledgement,
            # so the wait should help a little.
            time.sleep(0.5)

        self._is_running = False
