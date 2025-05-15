import ctypes
import logging
import sys
from typing import List

import revpimodio2
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController
from rppmcontroller.machine.conveyorbelt.ConveyorBelt import ConveyorBelt
from rppmcontroller.machine.highbay.HighBay import HighBay
from rppmcontroller.machine.indexedline.IndexedLine import IndexedLine
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
from rppmcontroller.machine.punchingmachine.PunchingMachine import PunchingMachine
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine
from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper


class IslandController(RevPiPyMachineController):
    """Controls the machines of an island"""

    def __init__(self, simulatedRevPiModIO: bool = False, configurationFile: str = ""):
        """
        Creates all machines specified in the configuration of the island
        :param simulatedRevPiModIO: Whether this controls a simulation
        :param configurationFile: Path to the config file
        """

        super().__init__(configurationFile)

        self.machine_controllers: List[MachineController] = []
        """A list with all machine controllers for all machines on this"""

        # Instantiate RevPiModIO
        if not simulatedRevPiModIO:
            self.rpi = revpimodio2.RevPiModIO(autorefresh=True)

        # Create machines as specified in config
        for machine_cfg in self.controller_config.get('machines', []):
            name = machine_cfg['name']
            machine_type = machine_cfg['type']
            if machine_type == "VACUUM":
                machine = VacuumGripper(name)
            # elif machine_type == "GRIPPER":
            #     machine = Gripper(name)
            elif machine_type == "WAREHOUSE":
                machine = HighBay(name)
            elif machine_type == "SORTING":
                machine = SortingLine(name)
            elif machine_type == "INDEXEDLINE":
                machine = IndexedLine(name)
            elif machine_type == "MULTIPROCESSING":
                machine = MultiProcessing(name)
            elif machine_type == "CONVEYOR":
                machine = ConveyorBelt(name)
            elif machine_type == "PUNCHING":
                machine = PunchingMachine(name)
            else:
                error = f"Unknown machine type: {machine_type}"
                logging.error(error)
                raise ValueError(error)

            # register machine on controller
            self.machines.append(machine)
            self.currentlyExecuting[machine] = None
            self.feedback[machine] = None

            # generate controller for machine
            inputs = []
            for binding_cfg in machine_cfg.get('inputs', []):
                field = binding_cfg['field']
                port = binding_cfg['port']
                if 'counter' in binding_cfg and binding_cfg['counter']:
                    inverted = 'inverted' in binding_cfg and binding_cfg['inverted'] is True
                    binding = CounterBinding(field, port, inverted)
                else:
                    binding = Binding(field, port)
                inputs.append(binding)
            outputs = []
            for binding_cfg in machine_cfg.get('outputs', []):
                field = binding_cfg['field']
                port = binding_cfg['port']
                binding = Binding(field, port)
                outputs.append(binding)
            self.machine_controllers.append(MachineController(machine, self.rpi, inputs, outputs))


    def read(self):
        for controller in self.machine_controllers:
            controller.read()

    def write(self):
        for controller in self.machine_controllers:
            controller.write()

    def reset(self):
        for controller in self.machine_controllers:
            controller.reset()


class Binding:
    def __init__(self, field: str, port: str):
        self.field: str = field
        """The name of the field/attribute to bind, e.g. vacuumActVerticalUp"""
        self.port: str = port
        """The id of the port to bind, e.g. O_1_i07"""


class CounterBinding(Binding):
    def __init__(self, field: str, port: str, inverted: bool = False):
        super().__init__(field, port)
        self.inverted: bool = inverted
        """Whether this counter value should be read inverted"""


class MachineController:
    def __init__(self, machine, rpi, inputs: List[Binding], outputs: List[Binding]):
        self.machine = machine
        self.rpi = rpi
        self.inputs = inputs
        self.outputs = outputs

    def read(self):
        for binding in self.inputs:
            if isinstance(binding, CounterBinding):
                value = ctypes.c_int32(getattr(self.rpi.io, binding.port).value).value
                if binding.inverted:
                    value = -value
            else:
                value = getattr(self.rpi.io, binding.port).value
            setattr(self.machine, binding.field, value)


    def write(self):
        for binding in self.outputs:
            getattr(self.rpi.io, binding.port).value = getattr(self.machine, binding.field)

    def reset(self):
        if self.machine.isInitialized:
            for binding in self.inputs:
                if isinstance(binding, CounterBinding):
                    getattr(self.rpi.io, binding.port).reset()

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s',
                        level=logging.DEBUG,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler("logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)

    if len(sys.argv) != 1:
        logging.error("Expected the island number to be the first argument")
        sys.exit(1)

    island_number = sys.argv[0]

    # Start communication threads and main control loop
    IslandController(configurationFile=f"/home/pi/rppmcontroller/rppmcontroller/example/island{island_number}_config.yml").start()
