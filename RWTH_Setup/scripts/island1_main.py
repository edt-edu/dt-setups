import ctypes
import logging

import revpimodio2
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController
from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper
from rppmcontroller.machine.conveyorbelt.ConveyorBelt import ConveyorBelt
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine


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

        self.conveyorBeltMachine = ConveyorBelt("ConveyorBelt01")
        self.conveyorBeltController = ConveyorBeltController(self.conveyorBeltMachine, self.rpi)

        self.multiProcessingMachine = MultiProcessing("MultiProcessing01")
        self.multiProcessingController = MultiProcessingController(self.multiProcessingMachine, self.rpi)

        self.sortingLineMachine = SortingLine("SortingLine01")
        self.sortingLineController = SortingLineController(self.sortingLineMachine, self.rpi)

        self.machines = [
            self.vacuumGripperMachine,
            self.vacuumGripperMachine2,
            self.conveyorBeltMachine,
            self.multiProcessingMachine,
            self.sortingLineMachine
        ]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: [None, None],
            self.vacuumGripperMachine2: [None, None],
            self.conveyorBeltMachine: [None, None],
            self.multiProcessingMachine: [None, None],
            self.sortingLineMachine: [None, None]
        }

        self.feedback = {
            self.vacuumGripperMachine: None,
            self.vacuumGripperMachine2: None,
            self.conveyorBeltMachine: None,
            self.multiProcessingMachine: None,
            self.sortingLine: None
        }

    def read(self):
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.read()
        self.vacuumGripperController2.read()
        self.conveyorBeltController.read()
        self.multiProcessingController.read()
        self.sortingLineController.read()

    def write(self):
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.write()
        self.vacuumGripperController2.write()
        self.conveyorBeltController.write()
        self.multiProcessingController.write()
        self.sortingLineController.write()

    def reset(self) -> None:
        # TODO find a way to read from a configuration file
        assert self.rpi.io is not None
        self.vacuumGripperController.reset()
        self.vacuumGripperController2.reset()
        self.conveyorBeltController.reset()
        self.multiProcessingController.reset()
        self.sortingLineController.reset()


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

class ConveyorBeltController:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        self.machine.conveyorSensFeed = self.rpi.io.I_1_i04.value
        self.machine.conveyorSensSwap = self.rpi.io.I_2_i04.value
        self.machine.conveyorSensImpulse = self.rpi.io.I_3_i04.value

    def write(self):
        self.rpi.io.O_1_i04.value = self.machine.conveyorActForward
        self.rpi.io.O_2_i04.value = self.machine.conveyorActBackward

    def reset(self) -> None:
        pass

class MultiProcessingController:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        self.machine.multiProcessingSensTurntablePosVacuum = self.rpi.io.I_1_i06.value
        self.machine.multiProcessingSensTurntablePosBelt = self.rpi.io.I_2_i06.value
        self.machine.multiProcessingSensEndConveyor = self.rpi.io.I_3_i06.value
        self.machine.multiProcessingSensTurntablePosSaw = self.rpi.io.I_4_i06.value
        self.machine.multiProcessingSensVacuumGripperAtTurntable = self.rpi.io.I_5_i06.value
        self.machine.multiProcessingSensOvenFeederIn = self.rpi.io.I_6_i06.value
        self.machine.multiProcessingSensOvenFeederOut = self.rpi.io.I_7_i06.value
        self.machine.multiProcessingSensVacuumGripperAtOven = self.rpi.io.I_8_i06.value
        self.machine.multiProcessingSensOven = self.rpi.io.I_9_i06.value

    def write(self):
        self.rpi.io.O_1_i06.value = self.machine.multiProcessingActRotClockwise
        self.rpi.io.O_2_i06.value = self.machine.multiProcessingActRotCounterclockwise
        self.rpi.io.O_3_i06.value = self.machine.multiProcessingActConveyorForward
        self.rpi.io.O_4_i06.value = self.machine.multiProcessingActSaw
        self.rpi.io.O_5_i06.value = self.machine.multiProcessingActOvenInward
        self.rpi.io.O_6_i06.value = self.machine.multiProcessingActOvenOutward
        self.rpi.io.O_7_i06.value = self.machine.multiProcessingActGripperToOven
        self.rpi.io.O_8_i06.value = self.machine.multiProcessingActGripperToTurntable
        self.rpi.io.O_9_i06.value = self.machine.multiProcessingOvenLight
        self.rpi.io.O_10_i06.value = self.machine.multiProcessingCompressor
        self.rpi.io.O_11_i06.value = self.machine.multiProcessingValveVacuum
        self.rpi.io.O_12_i06.value = self.machine.multiProcessingActLowerValve
        self.rpi.io.O_13_i06.value = self.machine.multiProcessingValveOvenDoor
        self.rpi.io.O_14_i06.value = self.machine.multiProcessingValveFeeder

    def reset(self) -> None:
        pass

class SortingLineController:
    def __init__(self, machine, rpi):
        self.machine = machine
        self.rpi = rpi

    def read(self):
        self.machine.sortingLineSensImpulseCounterRaw = self.rpi.io.I_1_i07.value
        self.machine.sortingLineSensInputLightBarrier = self.rpi.io.I_2_i07.value
        self.machine.sortingLineSensMiddleLightBarrier = self.rpi.io.I_3_i07.value
        # color sensor missing 
        self.machine.sortingLineSensWhiteLightBarrier = self.rpi.io.I_5_i07.value
        self.machine.sortingLineSensRedLightBarrier = self.rpi.io.I_6_i07.value
        self.machine.sortingLineSensBlueLightBarrier = self.rpi.io.I_7_i07.value

    def write(self):
        self.rpi.io.O_1_i07.value = self.machine.sortingLineActMotorConveyor
        self.rpi.io.O_2_i07.value = self.machine.sortingLineActCompressorOn
        self.rpi.io.O_3_i07.value = self.machine.sortingLineActWhiteEjector
        self.rpi.io.O_4_i07.value = self.machine.sortingLineActRedEjector
        self.rpi.io.O_5_i07.value = self.machine.sortingLineActBlueEjector

    def reset(self) -> None:
        pass

if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s',
                        level=logging.DEBUG,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler("logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)

    # Start communication threads and main control loop
    Island1Controller(configurationFile="/home/pi/rppmcontroller/rppmcontroller/example/island1_config.yml").start()
