import argparse
import logging
import re
import signal

import paho.mqtt.client as mqtt

from revpi.islands.island1 import machines
from revpi.main import Main, MessageType
from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput
from tests.simulation.islands.island1 import Island1
from tests.simulation.simulator import Simulator


def get_simulation_ports(machines):
    ports = dict()
    re_input = re.compile("dio[0-9]_I_[0-9]+")
    re_output = re.compile("dio[0-9]_O_[0-9]+")
    re_counter = re.compile("dio[0-9]_Counter_[0-9]+")
    for _, machine in machines.items():
        for _, port in machine['ports'].items():
            if re_input.fullmatch(port):
                ports[port] = MockInput(port, False, blocking=True)
            elif re_output.fullmatch(port):
                ports[port] = MockOutput(port, False, blocking=True)
            elif re_counter.fullmatch(port):
                ports[port] = MockCounter(port, 4321, blocking=True)
            else:
                raise Exception("Unknown port type")
    return ports


ports = get_simulation_ports(machines)

logging.basicConfig(
    level=logging.WARNING,
    format="%(levelname)s:%(asctime)s:%(name)s:%(message)s")
_sim_logger = logging.getLogger("simulator")
_sim_logger.setLevel(logging.INFO)
_tests_logger = logging.getLogger("tests")
_tests_logger.setLevel(logging.INFO)

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
# parser.add_argument(
#     "--island",
#     action='store',
#     choices=['island1', 'island2'],
#     default='island1',
#     help="Select island layout",
# )
parser.add_argument(
    "--mqtt",
    action='store',
    default='127.0.0.1',
    type=str,
    help="MQTT broker address"
)
parser.add_argument(
    "--json",
    action='store_true',
    help="Use JSON for message encoding"
)

if __name__ == "__main__":
    args = parser.parse_args()

    if args.verbose == 1:
        logging.root.setLevel(logging.INFO)
    elif args.verbose > 1:
        logging.root.setLevel(logging.DEBUG)

    sim = Simulator(0.1, logger=logging.getLogger("simulator.main"))
    island1 = Island1(ports, logging.getLogger("simulator"))
    sim.add_item(island1)

    rpi = revpimock.RevPiModIO(ports)
    sim.add_step_function(rpi.mock_cycle)
    sim.start()

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.will_set("island1/status", "failed", retain=True)
    client.connect(args.mqtt)

    message_type = MessageType.ByteArray
    if args.json:
        message_type = MessageType.JSON
    main = Main("island1", rpi, client, message_type=message_type)

    main.configure_machines(machines)

    def sigint_handler(sig, frame):
        main.signal_stop()
        # Reregister original sigint handler to help aborting serious errors
        signal.signal(signal.SIGINT, signal.default_int_handler)
    signal.signal(signal.SIGINT, sigint_handler)

    client.publish("island1/status", "setup complete", retain=True)

    main.run()

    client.disconnect()

    sim.stop()
