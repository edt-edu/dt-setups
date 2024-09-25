import logging

from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
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
        self.multiProcessingMachine = MultiProcessing("MultiProcessing01")
        self.machines = [self.multiProcessingMachine]
        self.currentlyExecuting = {
            self.multiProcessingMachine: [None, None]
        }
        
        self.feedback = {
            self.multiProcessingMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)


    def read(self):
        assert self.rpi.io is not None
        #______MP______
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensTurntablePosVacuum","dio6_I_1","ReferenceSwitchTurntablePositionVacuum")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensTurntablePosBelt","dio6_I_2","ReferenceSwitchTurntablePositionBelt")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensEndConveyor","dio6_I_3","LightBarrierEndOfConveyorBelt")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensTurntablePosSaw","dio6_I_4","ReferenceSwitchTurntablePositionSaw")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensVacuumGripperAtTurntable","dio6_I_5","ReferenceSwitchVacuumPositionTurntable")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensOvenFeederIn","dio6_I_6","ReferenceSwitchOvenFeederInside")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensOvenFeederOut","dio6_I_7","ReferenceSwitchOvenFeederOutside")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensVacuumGripperAtOven","dio6_I_8","ReferenceSwitchVacuumPositionOven")
        self.updateValueRead("MultiProcessing",0,"multiProcessingSensOven","dio6_I_9","LightBarrierOven")
        
        
    def write(self):
        assert self.rpi.io is not None
        #______MP______
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActRotClockwise","dio6_O_1","MotorTurntableClockwise")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActRotCounterclockwise","dio6_O_2","MotorTurntableCounterClockwise")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActConveyorForward","dio6_O_3","MotorConveyorBelt")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActSaw","dio6_O_4","MotorSaw")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActOvenInward","dio6_O_5","MotorOvenFeederRetract")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActOvenOutward","dio6_O_6","MotorOvenFeederExtend")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActGripperToOven","dio6_O_7","MotorVacuumTowardsOven")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActGripperToTurntable","dio6_O_8","MotorVacuumTowardsTurntable")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingOvenLight","dio6_O_9","LightOven")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingCompressor","dio6_O_10","Compressor")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingValveVacuum","dio6_O_11","ValveVacuum")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingActLowerValve","dio6_O_12","ValveLowering")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingValveOvenDoor","dio6_O_13","ValveOvenDoor")
        self.updateValueWrite("MultiProcessing",0,"multiProcessingValveFeeder","dio6_O_14","ValveFeeder")

         
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