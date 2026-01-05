import ctypes
import logging

from rppmcontroller.machine.highbay.HighBayParameters import HighBayParameters
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
from rppmcontroller.machine.highbay.HighBay import HighBay
from rppmcontroller.RevPiPyModIOMachineController import RevPiPyModIOMachineController
from rppmcontroller.machine.multiprocessing.MultiProcessingParameters import MultiProcessingParameters
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
        mpsParameters = MultiProcessingParameters(safety_at_oven=True)
        self.multiProcessingMachine = MultiProcessing("MultiProcessing01", mpsParameters)
        highBayParameters = HighBayParameters(vertical_safety_position=0, horizontal_safety_position=0)
        highBayParameters.add_vertical_offset(-70)
        highBayParameters.conveyor_column -= 25
        highBayParameters.right_column -= 65
        highBayParameters.middle_column -= 45
        highBayParameters.left_column -= 80
        self.highBayMachine = HighBay("HighBay01", highBayParameters)
        self.machines = [self.multiProcessingMachine, self.highBayMachine]
        self.currentlyExecuting = {
            self.multiProcessingMachine: None,
            self.highBayMachine: None
        }
        
        self.machineFeedback = {
            self.multiProcessingMachine: None,
            self.highBayMachine: None
        }
        self.commandFeedback = {
            self.multiProcessingMachine: None,
            self.highBayMachine: None
        }

        self.MQTT = MQTTFunctions("mbdo-server.local", 1883, 60)


    def read(self):
        assert self.rpi.io is not None
        #______MP______
        self.multiProcessingMachine.multiProcessingSensTurntablePosVacuum = self.rpi.io.dio5_I_1.value
        self.multiProcessingMachine.multiProcessingSensTurntablePosBelt = self.rpi.io.dio5_I_2.value
        self.multiProcessingMachine.multiProcessingSensEndConveyor = self.rpi.io.dio5_I_3.value
        self.multiProcessingMachine.multiProcessingSensTurntablePosSaw = self.rpi.io.dio5_I_4.value
        self.multiProcessingMachine.multiProcessingSensVacuumGripperAtTurntable = self.rpi.io.dio5_I_5.value
        self.multiProcessingMachine.multiProcessingSensOvenFeederIn = -self.rpi.io.dio5_I_6.value
        self.multiProcessingMachine.multiProcessingSensOvenFeederOut = self.rpi.io.dio5_I_7.value
        self.multiProcessingMachine.multiProcessingSensVacuumGripperAtOven = self.rpi.io.dio5_I_8.value   
        self.multiProcessingMachine.multiProcessingSensOven = self.rpi.io.dio5_I_9.value

        #______HB______
        self.highBayMachine.highbaySensHorizontal = self.rpi.io.dio6_I_1.value
        self.highBayMachine.highbaySensInside = self.rpi.io.dio6_I_2.value
        self.highBayMachine.highbaySensOutside = self.rpi.io.dio6_I_3.value
        self.highBayMachine.highbaySensVertical = self.rpi.io.dio6_I_4.value
        self.highBayMachine.highbaySensCantileverFront = self.rpi.io.dio6_I_11.value
        self.highBayMachine.highbaySensCantileverBack = self.rpi.io.dio6_I_12.value
        self.highBayMachine.highbaySensHorizontalEncoderCounter = -ctypes.c_int32(self.rpi.io.dio6_Counter_7.value).value
        self.highBayMachine.highbaySensVerticalEncoderCounter = ctypes.c_int32(self.rpi.io.dio6_Counter_9.value).value
                
    def write(self):
        assert self.rpi.io is not None
        #______MP______
        self.rpi.io.dio5_O_1.value = self.multiProcessingMachine.multiProcessingActRotClockwise
        self.rpi.io.dio5_O_2.value = self.multiProcessingMachine.multiProcessingActRotCounterclockwise
        self.rpi.io.dio5_O_3.value = self.multiProcessingMachine.multiProcessingActConveyorForward
        self.rpi.io.dio5_O_4.value = self.multiProcessingMachine.multiProcessingActSaw
        self.rpi.io.dio5_O_5.value = self.multiProcessingMachine.multiProcessingActOvenInward
        self.rpi.io.dio5_O_6.value = self.multiProcessingMachine.multiProcessingActOvenOutward
        self.rpi.io.dio5_O_7.value = self.multiProcessingMachine.multiProcessingActGripperToOven
        self.rpi.io.dio5_O_8.value = self.multiProcessingMachine.multiProcessingActGripperToTurntable
        self.rpi.io.dio5_O_9.value = self.multiProcessingMachine.multiProcessingOvenLight
        self.rpi.io.dio5_O_10.value = self.multiProcessingMachine.multiProcessingCompressor
        self.rpi.io.dio5_O_11.value = self.multiProcessingMachine.multiProcessingValveVacuum
        self.rpi.io.dio5_O_12.value = self.multiProcessingMachine.multiProcessingActLowerValve
        self.rpi.io.dio5_O_13.value = self.multiProcessingMachine.multiProcessingValveOvenDoor
        self.rpi.io.dio5_O_14.value = self.multiProcessingMachine.multiProcessingValveFeeder

        #______HB______
        self.rpi.io.dio6_O_1.value = self.highBayMachine.highbayActConveyorForward
        self.rpi.io.dio6_O_2.value = self.highBayMachine.highbayActConveyorBackward
        self.rpi.io.dio6_O_3.value = self.highBayMachine.highbayActHorizontalToRack
        self.rpi.io.dio6_O_4.value = self.highBayMachine.highbayActHorizontalToConveyor
        self.rpi.io.dio6_O_5.value = self.highBayMachine.highbayActDown
        self.rpi.io.dio6_O_6.value = self.highBayMachine.highbayActUp
        self.rpi.io.dio6_O_7.value = self.highBayMachine.highbayActCantileverForward
        self.rpi.io.dio6_O_8.value = self.highBayMachine.highbayActCantileverBackward
        self.rpi.io.dio6_PWM_10.value = self.highBayMachine.pwmHorizontal
        self.rpi.io.dio6_PWM_11.value = self.highBayMachine.pwmVertical


    def reset(self) -> None:
        assert self.rpi.io is not None
        if self.highBayMachine.horizontal_reset_helper.must_reset():
            self.rpi.io.dio6_Counter_7.reset()
        if self.highBayMachine.vertical_reset_helper.must_reset():
            self.rpi.io.dio6_Counter_9.reset()
         
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