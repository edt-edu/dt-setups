import argparse
import importlib
import logging
import re
import signal
from threading import Event

import paho.mqtt.client as mqtt
import revpimodio2

from .main import Main, MessageType

logging.basicConfig(
    level=logging.WARNING,
    format="%(levelname)s:%(asctime)s:%(name)s:%(message)s")
_logger = logging.root

_ADDRESS_REGEX = r'(?P<ip>([0-9]{1,3}\.){3}[0-9]{1,3})(:(?P<port>[0-9]{1,5}))?'
_RE_ADDRESS = re.compile(_ADDRESS_REGEX)

_MQTT_QOS = 2

stop_event = Event()
_CYCLE_TIME = 0.2


def mqtt_address_validation(address):
    if _RE_ADDRESS.fullmatch(address) is None:
        raise ValueError()
    return address


parser = argparse.ArgumentParser(
    description='RevPi controller',
)
parser.add_argument(
    "-v",
    "--verbose",
    action='count',
    default=0,
    help="Show detailed status output",
)
parser.add_argument(
    "--island",
    action='store',
    type=str,
    default='island1',
    help="Select island layout",
)
parser.add_argument(
    "--mqtt",
    action='store',
    default='127.0.0.1',
    type=mqtt_address_validation,
    help="MQTT broker address"
)
parser.add_argument(
    "--json",
    action='store_true',
    help="Use JSON for message encoding"
)

args = parser.parse_args()

debug = False
if args.verbose == 1:
    _logger.setLevel(logging.INFO)
elif args.verbose > 1:
    debug = True
    _logger.setLevel(logging.DEBUG)

if args.island == 'island1':
    _logger.info("Selected configuration for island1")
    machine_descriptions = importlib.import_module(".islands.island1", package='revpi').machines
elif args.island == 'island2':
    _logger.info("Selected configuration for island2")
    machine_descriptions = importlib.import_module(".islands.island2", package='revpi').machines
elif args.island == 'island3':
    _logger.info("Selected configuration for island3")
    machine_descriptions = importlib.import_module(".islands.island3", package='revpi').machines
elif args.island == 'island4':
    _logger.info("Selected configuration for island4")
    machine_descriptions = importlib.import_module(".islands.island4", package='revpi').machines
else:
    machine_descriptions = []

message_type = MessageType.ByteArray
if args.json:
    _logger.info("Use JSON message format")
    message_type = MessageType.JSON

try:
    rpi = revpimodio2.RevPiModIO(autorefresh=True, debug=debug)

    _match = _RE_ADDRESS.fullmatch(args.mqtt).groupdict()  # type: ignore [union-attr]
    broker_ip = _match['ip']
    broker_port = 1883
    if _match['port'] is not None:
        broker_port = int(_match['port'])

    _logger.info("Start mqtt client")
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.will_set(f"{args.island}/status", "failed", retain=True)
    client.connect(broker_ip, broker_port)

    main = Main(args.island, rpi, client,
                cycle_time=_CYCLE_TIME,
                qos=_MQTT_QOS,
                logger=_logger,
                message_type=message_type)

    main.configure_machines(machine_descriptions)

    def sigint_handler(sig, frame):
        main.signal_stop()
        # Reregister original sigint handler to help aborting serious errors
        signal.signal(signal.SIGINT, signal.default_int_handler)
    _logger.debug("Registering abort signal handler")
    signal.signal(signal.SIGINT, sigint_handler)

    _logger.info("Base setup complete, starting main")

    main.run()

    client.disconnect()
    _logger.info("Shutting down...")
except Exception as error:
    _logger.critical("Unknown error", exc_info=error)
    exit(1)
