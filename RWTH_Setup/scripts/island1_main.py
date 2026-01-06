import ctypes
import logging

import revpimodio2
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController
from rppmcontroller.machine.conveyorbelt.ConveyorBelt import ConveyorBelt
from rppmcontroller.machine.highbay.HighBay import HighBay
from rppmcontroller.machine.indexedline.IndexedLine import IndexedLine
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
from rppmcontroller.machine.punchingmachine.PunchingMachine import PunchingMachine
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine
from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper


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


class Island1Controller(RevPiPyMachineController):
    def __init__(self, configurationFile: str = ""):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """

        super().__init__(configurationFile)

        self.rpi = revpimodio2.RevPiModIO(autorefresh=True)

        self.controllers = list()

        self.addController(VacuumGripperController(VacuumGripper("I1VacuumGripper01"), self.rpi))
        self.addController(VacuumGripperController2(VacuumGripper("I1VacuumGripper02"), self.rpi))
        self.addController(ConveyorBeltController(ConveyorBelt("I1ConveyorBelt01"), self.rpi))
        self.addController(MultiProcessingController(MultiProcessing("I1MultiProcessing01"), self.rpi))
        self.addController(SortingLineController(SortingLine("I1SortingLine01", delay_offsets=(0, -0.2, -0.25), mockAnalogSensor=True), self.rpi))
        self.addController(HighBayController(HighBay("I1HighBay01", column_offset=(-30, -60, -90, -130)), self.rpi))
        # machines which are usually not connected to island 1
        # self.addController(PunchingMachineController(PunchingMachine("I1PunchingMachine01"), self.rpi))
        # self.addController(IndexedLineController(IndexedLine("I1IndexedLine01"), self.rpi))

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


class VacuumGripperController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

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
        if self.machine.vertical_reset_helper.must_reset():
            self.rpi.io.Counter_5.reset()
        if self.machine.arm_reset_helper.must_reset():
            self.rpi.io.Counter_7.reset()
        if self.machine.rot_reset_helper.must_reset():
            self.rpi.io.Counter_9.reset()

class VacuumGripperController2(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.vacuumSensVerticalEndUp = self.rpi.io.I_1_i03.value
        self.machine.vacuumSensArmEndIn = self.rpi.io.I_2_i03.value
        self.machine.vacuumSensRotEnd = self.rpi.io.I_3_i03.value
        self.machine.vacuumSensVerticalEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_5_i03.value).value
        self.machine.vacuumSensArmEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_7_i03.value).value
        self.machine.vacuumSensRotEncoderCounter = ctypes.c_int32(self.rpi.io.Counter_9_i03.value).value

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
        if self.machine.vertical_reset_helper.must_reset():
            self.rpi.io.Counter_5_i03.reset()
        if self.machine.arm_reset_helper.must_reset():
            self.rpi.io.Counter_7_i03.reset()
        if self.machine.rot_reset_helper.must_reset():
            self.rpi.io.Counter_9_i03.reset()

class ConveyorBeltController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.conveyorSensFeed = self.rpi.io.I_1_i04.value
        self.machine.conveyorSensSwap = self.rpi.io.I_2_i04.value
        self.machine.conveyorSensImpulse = self.rpi.io.I_3_i04.value

    def write(self):
        self.rpi.io.O_1_i04.value = self.machine.conveyorActForward
        self.rpi.io.O_2_i04.value = self.machine.conveyorActBackward

    def reset(self) -> None:
        pass

class MultiProcessingController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

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

class SortingLineController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

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

class HighBayController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.highbaySensHorizontal = self.rpi.io.I_1_i05.value
        self.machine.highbaySensInside = self.rpi.io.I_2_i05.value
        self.machine.highbaySensOutside = self.rpi.io.I_3_i05.value
        self.machine.highbaySensVertical = self.rpi.io.I_4_i05.value
        # no trail sensors
        self.machine.highbaySensHorizontalEncoderCounter = -ctypes.c_int32(self.rpi.io.Counter_7_i05.value).value
        self.machine.highbaySensVerticalEncoderCounter = ctypes.c_int32(self.rpi.io.Counter_9_i05.value).value
        self.machine.highbaySensCantileverFront = self.rpi.io.I_11_i05.value
        self.machine.highbaySensCantileverBack = self.rpi.io.I_12_i05.value

    def write(self):
        self.rpi.io.O_1_i05.value = self.machine.highbayActConveyorForward
        self.rpi.io.O_2_i05.value = self.machine.highbayActConveyorBackward
        self.rpi.io.O_3_i05.value = self.machine.highbayActHorizontalToRack
        self.rpi.io.O_4_i05.value = self.machine.highbayActHorizontalToConveyor
        self.rpi.io.O_5_i05.value = self.machine.highbayActDown
        self.rpi.io.O_6_i05.value = self.machine.highbayActUp
        self.rpi.io.O_7_i05.value = self.machine.highbayActCantileverForward
        self.rpi.io.O_8_i05.value = self.machine.highbayActCantileverBackward

    def reset(self):
        if self.machine.horizontal_reset_helper.must_reset():
            self.rpi.io.Counter_7_i05.reset()
        if self.machine.vertical_reset_helper.must_reset():
            self.rpi.io.Counter_9_i05.reset()

class PunchingMachineController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.punchingMachineSensGoods = self.rpi.io.I_1_i08.value
        self.machine.punchingMachineSensMachine = self.rpi.io.I_2_i08.value
        self.machine.punchingMachineSensUp = self.rpi.io.I_3_i08.value
        self.machine.punchingMachineSensDown = self.rpi.io.I_4_i08.value

    def write(self):
        self.rpi.io.O_1_i08.value = self.machine.punchingMachineActConveyorForward
        self.rpi.io.O_2_i08.value = self.machine.punchingMachineActConveyorBackward
        self.rpi.io.O_3_i08.value = self.machine.punchingMachineActUp
        self.rpi.io.O_4_i08.value = self.machine.punchingMachineActDown

    def reset(self):
        pass


class IndexedLineController(MachineController):
    def __init__(self, machine, rpi):
        super().__init__(machine, rpi)

    def read(self):
        self.machine.indexedLineSensSlider1Front = self.rpi.io.I_1_i08.value
        self.machine.indexedLineSensSlider1Rear = self.rpi.io.I_2_i08.value
        self.machine.indexedLineSensSlider2Front = self.rpi.io.I_3_i08.value
        self.machine.indexedLineSensSlider2Rear = self.rpi.io.I_4_i08.value
        self.machine.indexedLineSensSlider1 = self.rpi.io.I_5_i08.value
        self.machine.indexedLineSensMilling = self.rpi.io.I_6_i08.value
        self.machine.indexedLineSensLoading = self.rpi.io.I_7_i08.value
        self.machine.indexedLineSensDrilling = self.rpi.io.I_8_i08.value
        self.machine.indexedLineSensSwap = self.rpi.io.I_9_i08.value

    def write(self):
        self.rpi.io.O_1_i08.value = self.machine.indexedLineActSlider1Forward
        self.rpi.io.O_2_i08.value = self.machine.indexedLineActSlider1Backward
        self.rpi.io.O_3_i08.value = self.machine.indexedLineActSlider2Forward
        self.rpi.io.O_4_i08.value = self.machine.indexedLineActSlider2Backward
        self.rpi.io.O_5_i08.value = self.machine.indexedLineActFeedConveyor
        self.rpi.io.O_6_i08.value = self.machine.indexedLineActMillingConveyor
        self.rpi.io.O_7_i08.value = self.machine.indexedLineActMilling
        self.rpi.io.O_8_i08.value = self.machine.indexedLineActDrillingConveyor
        self.rpi.io.O_9_i08.value = self.machine.indexedLineActDrilling
        self.rpi.io.O_10_i08.value = self.machine.indexedLineActSwapConveyor

    def reset(self):
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
    Island1Controller(configurationFile="/home/pi/rppmcontroller/rppmcontroller/example/island1_config.yml").start()
