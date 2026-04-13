import logging
from logging.handlers import RotatingFileHandler

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
        self.machines = [
            self.vacuumGripperMachine
        ]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: None
        }

        self.machineFeedback = {
            self.vacuumGripperMachine: None
        }

        self.commandFeedback = {
            self.vacuumGripperMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)


    def read(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.updateValueRead("VacuumGripper",0,"vacuumSensVerticalEndUp","dio1_I_1","ReferenceSwitchVerticalAxis")
        self.updateValueRead("VacuumGripper",0,"vacuumSensArmEndIn","dio1_I_2","ReferenceSwitchHorizontalAxis")
        self.updateValueRead("VacuumGripper",0,"vacuumSensRotEnd","dio1_I_3","ReferenceSwitchRotate")
        # use signed int32 to deal with possible negative values of the encoders
        self.updateValueRead("VacuumGripper",0,"vacuumSensVerticalEncoderCounter","dio1_Counter_5","VerticalAxisStep",Type.POSITIVEINT32)
        self.updateValueRead("VacuumGripper",0,"vacuumSensArmEncoderCounter","dio1_Counter_7","HorizontalAxisStep",Type.POSITIVEINT32)
        # note: the rotation encoder counts in negative when going counterclockwise
        self.updateValueRead("VacuumGripper",0,"vacuumSensRotEncoderCounter","dio1_Counter_9","RotateStep",Type.NEGATIVEINT32)



    def write(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.updateValueWrite("VacuumGripper",0,"vacuumActVerticalUp","dio1_O_1","MotorVerticalAxisUp")
        self.updateValueWrite("VacuumGripper",0,"vacuumActVerticalDown","dio1_O_2","MotorVerticalAxisDown")
        self.updateValueWrite("VacuumGripper",0,"vacuumActArmIn","dio1_O_3","MotorHorizontalAxisBackward")
        self.updateValueWrite("VacuumGripper",0,"vacuumActArmOut","dio1_O_4","MotorHorizontalAxisForward")
        self.updateValueWrite("VacuumGripper",0,"vacuumActRotRight","dio1_O_5","MotorRotateClockwise")
        self.updateValueWrite("VacuumGripper",0,"vacuumActRotLeft","dio1_O_6","MotorRotateCounterClockwise")
        self.updateValueWrite("VacuumGripper",0,"vacuumActCompressorOn","dio1_O_7","Compressor")
        self.updateValueWrite("VacuumGripper",0,"vacuumActValve","dio1_O_8","ValveVacuum")



    
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
    # maxBytes: 15MB (5 * 1024 * 1024)
    # backupCount: Keep the last 5 log files
    handler = RotatingFileHandler(
        "logfile.log", 
        maxBytes=15*1024*1024, 
        backupCount=5
    )
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)
    # Start app
    root = RevPi_Controller(configurationFile="config.yml")
    # start communication threads and main control loop
    root.start()