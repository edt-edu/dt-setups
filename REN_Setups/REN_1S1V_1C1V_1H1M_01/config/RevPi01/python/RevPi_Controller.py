import ctypes
import logging

from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine
from rppmcontroller.RevPiPyModIOMachineController import RevPiPyModIOMachineController
from rppmcontroller.machine.vacuumgripper.VacuumGripperParameters import VacuumGripperParameters
from rppmcontroller.protocol.MQTTFunctions import MQTTFunctions
from rppmcontroller.machine.Type import Type


class RevPi_Controller(RevPiPyModIOMachineController):
    """
    Class allowing to stream commands to and from  a vacuum gripper and a Conveyor belt  variant 01
    """

    def __init__(self, configurationFile : str = ""):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """

        super().__init__(configurationFile)

        #dict, which keys are the machines, than there is a tuple holding the function currently executed ([0]) and the id it was sent with ([1])
        self.currentlyExecuting = {}
        vacuumGripperParameters = VacuumGripperParameters(rotational_safety_position=875, horizontal_safety_position=0, vertical_safety_position=0)
        self.vacuumGripperMachine = VacuumGripper("VacuumGripper01", vacuumGripperParameters)
        self.sortingLineMachine = SortingLine("SortingLine01")
        self.machines = [self.vacuumGripperMachine,self.sortingLineMachine]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: None,
            self.sortingLineMachine: None
        }

        self.machineFeedback = {
            self.vacuumGripperMachine: None,
            self.sortingLineMachine: None
        }
        self.commandFeedback = {
            self.vacuumGripperMachine: None,
            self.sortingLineMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)


    def read(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.vacuumGripperMachine.vacuumSensVerticalEndUp = self.rpi.io.dio1_I_1.value
        self.vacuumGripperMachine.vacuumSensArmEndIn = self.rpi.io.dio1_I_2.value
        self.vacuumGripperMachine.vacuumSensRotEnd = self.rpi.io.dio1_I_3.value
        self.vacuumGripperMachine.vacuumSensVerticalEncoderCounter = ctypes.c_int32(self.rpi.io.dio1_Counter_5.value).value
        self.vacuumGripperMachine.vacuumSensArmEncoderCounter = ctypes.c_int32(self.rpi.io.dio1_Counter_7.value).value        
        # note: the rotation encoder counts in negative when going counterclockwise
        self.vacuumGripperMachine.vacuumSensRotEncoderCounter = -ctypes.c_int32(self.rpi.io.dio1_Counter_9.value).value
        
        #______SL______
        self.sortingLineMachine.sortingLineSensImpulseCounterRaw = ctypes.c_int32(self.rpi.io.dio2_Counter_1.value).value
        self.sortingLineMachine.sortingLineSensInputLightBarrier = self.rpi.io.dio2_I_2.value
        self.sortingLineMachine.sortingLineSensMiddleLightBarrier = self.rpi.io.dio2_I_3.value
        self.sortingLineMachine.sortingLineSensWhiteLightBarrier = self.rpi.io.dio2_I_5.value
        self.sortingLineMachine.sortingLineSensRedLightBarrier = self.rpi.io.dio2_I_6.value
        self.sortingLineMachine.sortingLineSensBlueLightBarrier = self.rpi.io.dio2_I_7.value
        
    def write(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.rpi.io.dio1_O_1.value = self.vacuumGripperMachine.vacuumActVerticalUp
        self.rpi.io.dio1_O_2.value = self.vacuumGripperMachine.vacuumActVerticalDown
        self.rpi.io.dio1_O_3.value = self.vacuumGripperMachine.vacuumActArmIn
        self.rpi.io.dio1_O_4.value = self.vacuumGripperMachine.vacuumActArmOut
        self.rpi.io.dio1_O_5.value = self.vacuumGripperMachine.vacuumActRotRight
        self.rpi.io.dio1_O_6.value = self.vacuumGripperMachine.vacuumActRotLeft
        self.rpi.io.dio1_O_7.value = self.vacuumGripperMachine.vacuumActCompressorOn
        self.rpi.io.dio1_O_8.value = self.vacuumGripperMachine.vacuumActValve

        #______SL______
        self.rpi.io.dio2_O_1.value = self.sortingLineMachine.sortingLineActMotorConveyor
        self.rpi.io.dio2_O_2.value = self.sortingLineMachine.sortingLineActCompressorOn
        self.rpi.io.dio2_O_3.value = self.sortingLineMachine.sortingLineActWhiteEjector
        self.rpi.io.dio2_O_4.value = self.sortingLineMachine.sortingLineActRedEjector
        self.rpi.io.dio2_O_5.value = self.sortingLineMachine.sortingLineActBlueEjector

    
    def reset(self) -> None:
        assert self.rpi.io is not None
        if self.vacuumGripperMachine.vertical_reset_helper.must_reset():
            self.rpi.io.dio1_Counter_5.reset()
        if self.vacuumGripperMachine.arm_reset_helper.must_reset():
            self.rpi.io.dio1_Counter_7.reset()
        if self.vacuumGripperMachine.rot_reset_helper.must_reset():
            self.rpi.io.dio1_Counter_9.reset()

if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s', 
                        level=logging.DEBUG,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler("logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)
    # Start app
    root = RevPi_Controller(configurationFile="config.yml")
    # start communication threads and main control loop
    root.start()