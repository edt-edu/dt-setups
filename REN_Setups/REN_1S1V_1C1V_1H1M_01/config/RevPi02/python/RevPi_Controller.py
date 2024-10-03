import logging

from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper
from rppmcontroller.machine.conveyorbelt.ConveyorBelt import ConveyorBelt
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
        self.vacuumGripperMachine = VacuumGripper("VacuumGripper02")
        self.conveyorBeltMachine = ConveyorBelt("ConveyorBelt01")
        self.machines = [self.vacuumGripperMachine,self.conveyorBeltMachine]
        self.currentlyExecuting = {
            self.vacuumGripperMachine: [None, None],
            self.conveyorBeltMachine: [None, None]
        }

        self.feedback = {
            self.vacuumGripperMachine: None,
            self.conveyorBeltMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)
    

    def read(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.updateValueRead("VacuumGripper",0,"vacuumSensVerticalEndUp","dio3_I_1","ReferenceSwitchVerticalAxis")
        self.updateValueRead("VacuumGripper",0,"vacuumSensArmEndIn","dio3_I_2","ReferenceSwitchHorizontalAxis")
        self.updateValueRead("VacuumGripper",0,"vacuumSensRotEnd","dio3_I_3","ReferenceSwitchRotate")
        # use signed int32 to deal with possible negative values of the encoders
        self.updateValueRead("VacuumGripper",0,"vacuumSensVerticalEncoderCounter","dio3_Counter_5","VerticalAxisStep",Type.POSITIVEINT32)
        self.updateValueRead("VacuumGripper",0,"vacuumSensArmEncoderCounter","dio3_Counter_7","HorizontalAxisStep",Type.POSITIVEINT32)
        # note: the rotation encoder counts in negative when going counterclockwise
        self.updateValueRead("VacuumGripper",0,"vacuumSensRotEncoderCounter","dio3_Counter_9","RotateStep",Type.NEGATIVEINT32)

        #______CB______
        self.updateValueRead("ConveyorBelt",1,"conveyorSensFeed","dio4_I_1","LightBarrierFeedStation")
        self.updateValueRead("ConveyorBelt",1,"conveyorSensSwap","dio4_I_2","LightBarrierSwapStation")
        self.updateValueRead("ConveyorBelt",1,"conveyorSensImpulse","dio4_Counter_3","PulseCounter",Type.POSITIVEINT32)

        
    def write(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.updateValueWrite("VacuumGripper",0,"vacuumActVerticalUp","dio3_O_1","MotorVerticalAxisUp")
        self.updateValueWrite("VacuumGripper",0,"vacuumActVerticalDown","dio3_O_2","MotorVerticalAxisDown")
        self.updateValueWrite("VacuumGripper",0,"vacuumActArmIn","dio3_O_3","MotorHorizontalAxisBackward")
        self.updateValueWrite("VacuumGripper",0,"vacuumActArmOut","dio3_O_4","MotorHorizontalAxisForward")
        self.updateValueWrite("VacuumGripper",0,"vacuumActRotRight","dio3_O_5","MotorRotateClockwise")
        self.updateValueWrite("VacuumGripper",0,"vacuumActRotLeft","dio3_O_6","MotorRotateCounterClockwise")
        self.updateValueWrite("VacuumGripper",0,"vacuumActCompressorOn","dio3_O_7","Compressor")
        self.updateValueWrite("VacuumGripper",0,"vacuumActValve","dio3_O_8","ValveVacuum")

        #______CB______
        self.updateValueWrite("ConveyorBelt",1,"conveyorActForward","dio4_O_1","MotorConveyorBeltForward")
        self.updateValueWrite("ConveyorBelt",1,"conveyorActBackward","dio4_O_2","MotorConveyorBeltBackrward")
        
    
    def reset(self) -> None:
        assert self.rpi.io is not None
        vg = self.vacuumGripperMachine.executeHelper()
        if vg[0]:
            self.rpi.io.dio3_Counter_5.reset()
            self.rpi.io.dio3_Counter_7.reset()
            self.rpi.io.dio3_Counter_9.reset()

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