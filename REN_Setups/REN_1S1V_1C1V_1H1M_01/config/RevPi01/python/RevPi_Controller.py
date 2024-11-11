import logging

from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine
from rppmcontroller.RevPiPyModIOMachineController import RevPiPyModIOMachineController
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
        self.vacuumGripperMachine = VacuumGripper("VacuumGripper01")
        self.sortingLineMachine = SortingLine("SortingLine01")
        self.machines = [self.vacuumGripperMachine,self.sortingLineMachine]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: [None, None],
            self.sortingLineMachine: [None, None]
        }

        self.feedback = {
            self.vacuumGripperMachine: None,
            self.sortingLineMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)


    def read(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.vacuumGripperMachine.vacuumSensVerticalEndUp = self.rpi.io.dio1_I_1
        self.vacuumGripperMachine.vacuumSensArmEndIn = self.rpi.io.dio1_I_2
        self.vacuumGripperMachine.vacuumSensRotEnd = self.rpi.io.dio1_I_3
        self.vacuumGripperMachine.vacuumSensVerticalEncoderCounter = self.rpi.io.dio1_Counter_5
        self.vacuumGripperMachine.vacuumSensArmEncoderCounter = self.rpi.io.dio1_Counter_7        
        # note: the rotation encoder counts in negative when going counterclockwise
        self.vacuumGripperMachine.vacuumSensRotEncoderCounter = -self.rpi.io.dio1_Counter_9
        
        #______SL______
        self.sortingLineMachine.sortingLineSensImpulseCounterRaw = self.rpi.io.dio2_Counter_1
        self.sortingLineMachine.sortingLineSensInputLightBarrier = self.rpi.io.dio2_I_2
        self.sortingLineMachine.sortingLineSensMiddleLightBarrier = self.rpi.io.dio2_I_3
        self.sortingLineMachine.sortingLineSensWhiteLightBarrier = self.rpi.io.dio2_I_5
        self.sortingLineMachine.sortingLineSensRedLightBarrier = self.rpi.io.dio2_I_6
        self.sortingLineMachine.sortingLineSensBlueLightBarrier = self.rpi.io.dio2_I_7
        
    def write(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.rpi.io.dio1_O_1 = self.vacuumGripperMachine.vacuumActVerticalUp
        self.rpi.io.dio1_O_2 = self.vacuumGripperMachine.vacuumActVerticalDown
        self.rpi.io.dio1_O_3 = self.vacuumGripperMachine.vacuumActArmIn
        self.rpi.io.dio1_O_4 = self.vacuumGripperMachine.vacuumActArmOut
        self.rpi.io.dio1_O_5 = self.vacuumGripperMachine.vacuumActRotRight
        self.rpi.io.dio1_O_6 = self.vacuumGripperMachine.vacuumActRotLeft
        self.rpi.io.dio1_O_7 = self.vacuumGripperMachine.vacuumActCompressorOn
        self.rpi.io.dio1_O_8 = self.vacuumGripperMachine.vacuumActValve

        #______SL______
        self.rpi.io.dio2_O_1 = self.sortingLineMachine.sortingLineActMotorConveyor
        self.rpi.io.dio2_O_2 = self.sortingLineMachine.sortingLineActMotorConveyor
        self.rpi.io.dio2_O_3 = self.sortingLineMachine.sortingLineActWhiteEjector
        self.rpi.io.dio2_O_4 = self.sortingLineMachine.sortingLineActRedEjector
        self.rpi.io.dio2_O_5 = self.sortingLineMachine.sortingLineActBlueEjector

    
    def reset(self) -> None:
        assert self.rpi.io is not None
        vg = self.vacuumGripperMachine.executeHelper()
        if vg[0]:
            self.rpi.io.dio1_Counter_5.reset()
            self.rpi.io.dio1_Counter_7.reset()
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