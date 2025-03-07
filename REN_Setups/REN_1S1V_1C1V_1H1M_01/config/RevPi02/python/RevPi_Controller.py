import ctypes
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

        self.commandFeedback = {
            self.vacuumGripperMachine: None,
            self.conveyorBeltMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)
    

    def read(self):
        assert self.rpi.io is not None

        #_____VGR______

        self.vacuumGripperMachine.vacuumSensVerticalEndUp = self.rpi.io.dio3_I_1.value
        self.vacuumGripperMachine.vacuumSensArmEndIn = self.rpi.io.dio3_I_2.value
        self.vacuumGripperMachine.vacuumSensRotEnd = self.rpi.io.dio3_I_3.value
        self.vacuumGripperMachine.vacuumSensVerticalEncoderCounter = ctypes.c_int32(self.rpi.io.dio3_Counter_5.value).value
        self.vacuumGripperMachine.vacuumSensArmEncoderCounter = ctypes.c_int32(self.rpi.io.dio3_Counter_7.value).value
        # note: the rotation encoder counts in negative when going counterclockwise
        self.vacuumGripperMachine.vacuumSensRotEncoderCounter = -ctypes.c_int32(self.rpi.io.dio3_Counter_9.value).value
        
        #______CB______
        self.conveyorBeltMachine.conveyorSensFeed = self.rpi.io.dio4_I_1.value
        self.conveyorBeltMachine.conveyorSensSwap = self.rpi.io.dio4_I_2.value
        self.conveyorBeltMachine.conveyorSensImpulse = ctypes.c_int32(self.rpi.io.dio4_Counter_3.value).value
        
    def write(self):
        assert self.rpi.io is not None

        #_____VGR______
        self.rpi.io.dio3_O_1.value = self.vacuumGripperMachine.vacuumActVerticalUp
        self.rpi.io.dio3_O_2.value = self.vacuumGripperMachine.vacuumActVerticalDown
        self.rpi.io.dio3_O_3.value = self.vacuumGripperMachine.vacuumActArmIn
        self.rpi.io.dio3_O_4.value = self.vacuumGripperMachine.vacuumActArmOut
        self.rpi.io.dio3_O_5.value = self.vacuumGripperMachine.vacuumActRotRight
        self.rpi.io.dio3_O_6.value = self.vacuumGripperMachine.vacuumActRotLeft
        self.rpi.io.dio3_O_7.value = self.vacuumGripperMachine.vacuumActCompressorOn
        self.rpi.io.dio3_O_8.value = self.vacuumGripperMachine.vacuumActValve

        #______CB______
        self.rpi.io.dio4_O_1.value = self.conveyorBeltMachine.conveyorActForward
        self.rpi.io.dio4_O_2.value = self.conveyorBeltMachine.conveyorActBackward
        
    
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