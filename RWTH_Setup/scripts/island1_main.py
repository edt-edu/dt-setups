import ctypes
import logging

import revpimodio2
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController
from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper


class Island1Controller(RevPiPyMachineController):
    def __init__(self, simulatedRevPiModIO: bool = False, configurationFile: str = ""):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """

        super().__init__(configurationFile)

        # Instantiate RevPiModIO
        if (not simulatedRevPiModIO):
            self.rpi = revpimodio2.RevPiModIO(autorefresh=True)

        # TODO find a way to read from a configuration file
        # the list of all machines that are connected to this core
        # TODO simplify with arrays and for loops so adding a Machine and controller is as simple as adding the classes below
        self.vacuumGripperMachine = VacuumGripper("VacuumGripper01")
        self.vacuumGripperController = VacuumGripperController(self.vacuumGripperMachine, self.rpi)

        self.vacuumGripperMachine2 = VacuumGripper("VacuumGripper02")
        self.vacuumGripperController2 = VacuumGripperController2(self.vacuumGripperMachine2, self.rpi)

        self.machines = [
            self.vacuumGripperMachine,
            self.vacuumGripperMachine2
        ]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: [None, None],
            self.vacuumGripperMachine2: [None, None]
        }

        self.feedback = {
            self.vacuumGripperMachine: None,
            self.vacuumGripperMachine2: None
        }

    def read(self):
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.read()
        self.vacuumGripperController2.read()

    def write(self):
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.write()
        self.vacuumGripperController2.write()

    def reset(self) -> None:
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.reset()
        self.vacuumGripperController2.reset()


class VacuumGripperController:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        self.machine.vacuumSensVerticalEndUp = self.rpi.io.I_1.value
        self.machine.vacuumSensArmEndIn = self.rpi.io.I_2.value
        self.machine.vacuumSensRotEnd = self.rpi.io.I_3.value
        self.machine.vacuumSensVerticalEncoderCounter = ctypes.c_int32(self.rpi.io.Counter_5.value).value
        self.machine.vacuumSensArmEncoderCounter = ctypes.c_int32(self.rpi.io.Counter_7.value).value
        self.machine.vacuumSensRotEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_9.value).value

    def write(self):
        self.rpi.io.O_1.value = self.machine.vacuumActVerticalUp
        self.rpi.io.O_2.value = self.machine.vacuumActVerticalDown
        self.rpi.io.O_3.value = self.machine.vacuumActArmIn
        self.rpi.io.O_4.value = self.machine.vacuumActArmOut
        self.rpi.io.O_5.value = self.machine.vacuumActRotRight
        self.rpi.io.O_6.value = self.machine.vacuumActRotLeft
        self.rpi.io.O_7.value = self.machine.vacuumActCompressorOn
        self.rpi.io.O_8.value = self.machine.vacuumActValve

    def reset(self) -> None:
        vg = self.machine.executeHelper()
        if vg[0]:
            self.rpi.io.Counter_5.reset()
            self.rpi.io.Counter_7.reset()
            self.rpi.io.Counter_9.reset()


class VacuumGripperController2:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        self.machine.vacuumSensVerticalEndUp = self.rpi.io.I_1_i03.value
        self.machine.vacuumSensArmEndIn = self.rpi.io.I_2_i03.value
        self.machine.vacuumSensRotEnd = self.rpi.io.I_3_i03.value
        self.machine.vacuumSensVerticalEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_5_i03.value).value
        self.machine.vacuumSensArmEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_7_i03.value).value
        self.machine.vacuumSensRotEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_9_i03.value).value
    
    def write(self):
        self.rpi.io.O_1_i03.value = self.machine.vacuumActVerticalUp
        self.rpi.io.O_2_i03.value = self.machine.vacuumActVerticalDown
        self.rpi.io.O_3_i03.value = self.machine.vacuumActArmIn
        self.rpi.io.O_4_i03.value = self.machine.vacuumActArmOut
        self.rpi.io.O_5_i03.value = self.machine.vacuumActRotRight
        self.rpi.io.O_6_i03.value = self.machine.vacuumActRotLeft
        self.rpi.io.O_7_i03.value = self.machine.vacuumActCompressorOn
        self.rpi.io.O_8_i03.value = self.machine.vacuumActValve
    
    def reset(self) -> None:
        vg = self.machine.executeHelper()
        if vg[0]:
            self.rpi.io.Counter_5_i03.reset()
            self.rpi.io.Counter_7_i03.reset()
            self.rpi.io.Counter_9_i03.reset()


if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s',
                        level=logging.DEBUG,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler("logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)

    # Start communication threads and main control loop
    Island1Controller(configurationFile="island1_config.yml").start()
