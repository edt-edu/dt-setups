import ctypes
import logging

import revpimodio2
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing

class MachineController:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        pass

    def write(self):
        pass

    def reset(self):
        pass


class Island3Controller(RevPiPyMachineController):
    def __init__(self, configurationFile: str = ""):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """

        super().__init__(configurationFile)

        self.rpi = revpimodio2.RevPiModIO(autorefresh=True)

        self.controllers = list()

        self.addController(MultiProcessingController(MultiProcessing("I3MultiProcessing02"), self.rpi))

    def addController(self, controller: MachineController):
        self.controllers.append(controller)
        self.machines.append(controller.machine)
        self.currentlyExecuting[controller.machine] = None
        self.machineFeedback[controller.machine] = None

    def read(self):
        for c in self.controllers:
            c.read()

    def write(self):
        for c in self.controllers:
            c.write()

    def reset(self) -> None:
        for c in self.controllers:
            c.reset()

class MultiProcessingController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.multiProcessingSensTurntablePosVacuum = self.rpi.io.I_1_i03.value
        self.machine.multiProcessingSensTurntablePosBelt = self.rpi.io.I_2_i03.value
        self.machine.multiProcessingSensEndConveyor = self.rpi.io.I_3_i03.value
        self.machine.multiProcessingSensTurntablePosSaw = self.rpi.io.I_4_i03.value
        self.machine.multiProcessingSensVacuumGripperAtTurntable = self.rpi.io.I_5_i03.value
        self.machine.multiProcessingSensOvenFeederIn = self.rpi.io.I_6_i03.value
        self.machine.multiProcessingSensOvenFeederOut = self.rpi.io.I_7_i03.value
        self.machine.multiProcessingSensVacuumGripperAtOven = self.rpi.io.I_8_i03.value
        self.machine.multiProcessingSensOven = self.rpi.io.I_9_i03.value

    def write(self):
        self.rpi.io.O_1_i03.value = self.machine.multiProcessingActRotClockwise
        self.rpi.io.O_2_i03.value = self.machine.multiProcessingActRotCounterclockwise
        self.rpi.io.O_3_i03.value = self.machine.multiProcessingActConveyorForward
        self.rpi.io.O_4_i03.value = self.machine.multiProcessingActSaw
        self.rpi.io.O_5_i03.value = self.machine.multiProcessingActOvenInward
        self.rpi.io.O_6_i03.value = self.machine.multiProcessingActOvenOutward
        self.rpi.io.O_7_i03.value = self.machine.multiProcessingActGripperToOven
        self.rpi.io.O_8_i03.value = self.machine.multiProcessingActGripperToTurntable
        self.rpi.io.O_9_i03.value = self.machine.multiProcessingOvenLight
        self.rpi.io.O_10_i03.value = self.machine.multiProcessingCompressor
        self.rpi.io.O_11_i03.value = self.machine.multiProcessingValveVacuum
        self.rpi.io.O_12_i03.value = self.machine.multiProcessingActLowerValve
        self.rpi.io.O_13_i03.value = self.machine.multiProcessingValveOvenDoor
        self.rpi.io.O_14_i03.value = self.machine.multiProcessingValveFeeder
        # PWM
        self.rpi.io.PWM_13_i04.value = self.machine.pwmTurntable
        self.rpi.io.PWM_14_i04.value = self.machine.pwmHorizontal

    def reset(self) -> None:
        pass

if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s',
                        level=logging.INFO,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler("logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)

    # Start communication threads and main control loop
    Island3Controller(configurationFile="/home/pi/rppmcontroller/rppmcontroller/example/island3_config.yml").start()
