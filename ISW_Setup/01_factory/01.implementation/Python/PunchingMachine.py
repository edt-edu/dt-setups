from Machine import Machine
from RequestedParameter import RequestedParameter
import logging


class PunchingMachine(Machine):

    def __init__(self, id1):
        self.__punchingSensLeft = False
        self.__punchingSensRight = False
        self.__punchingSensMachineIsUp = False
        self.__punchingSensMachineIsDown = False
        self.__punchingActConveyorForward = False
        self.__punchingActConveyorBackward = False
        self.__punchingActMachineUp = False
        self.__punchingActMachineDown = False
        dictMap = {RequestedParameter.LIGHTBARRIERGOODSINOUT: self.__punchingSensLeft,
                   RequestedParameter.LIGHTBARRIERPUNCHINGMACHINE: self.__punchingSensRight,
                   RequestedParameter.SWITCHPUNCHINGMACHINEUP: self.__punchingSensMachineIsUp,
                   RequestedParameter.SWITCHPUNCHINGMACHINEDOWN: self.__punchingSensMachineIsDown,
                   RequestedParameter.MOTORCONVEYORBELTFORWARD: self.__punchingActConveyorForward,
                   RequestedParameter.MOTORCONVEYORBELTBACKWARD: self.__punchingActConveyorBackward,
                   RequestedParameter.MOTORPUNCHINGMACHINEUP: self.__punchingActMachineUp,
                   RequestedParameter.MOTORPUNCHINGMACHINEDOWN: self.__punchingActMachineDown}
        super().__init__(id1, dictMap)

        self.__packageProcessed = self.__processing= False
        self.__status = "waiting"
        self.__first = True
        self.__firstcounter = 2
        self.__isExecutingCount = 0

    @property
    def isExecuting(self) -> bool:
        """self.__packageProcessed is here as a reminder to reset it for the next run of the punching machine"""
        if (self.__punchingActConveyorForward or self.__punchingActConveyorBackward or self.__punchingActMachineUp or self.__punchingActMachineDown):
            return True
        else:
            if self.__isExecutingCount < 15:
                self.__isExecutingCount += 1
                return True
            else:
                return False

    @property
    def punchingSensLeft(self) -> bool:
        return self.__punchingSensLeft

    @punchingSensLeft.setter
    def punchingSensLeft(self, value: bool):
        self.__punchingSensLeft = value

    @property
    def punchingSensRight(self) -> bool:
        return self.__punchingSensRight

    @punchingSensRight.setter
    def punchingSensRight(self, value: bool):
        self.__punchingSensRight = value
        
    @property
    def punchingMachineSensIsUp(self) -> bool:
        return self.__punchingSensMachineIsUp

    @punchingMachineSensIsUp.setter
    def punchingMachineSensIsUp(self, value: bool):
        self.__punchingSensMachineIsUp = value

    @property
    def punchingMachineSensIsDown(self) -> bool:
        return self.__punchingSensMachineIsDown

    @punchingMachineSensIsDown.setter
    def punchingMachineSensIsDown(self, value: bool):
        self.__punchingSensMachineIsDown = value
        
    @property
    def punchingActForward(self) -> bool:
        return self.__punchingActConveyorForward

    @punchingActForward.setter
    def punchingActForward(self, value: bool):
        self.__punchingActConveyorForward = value

    @property
    def punchingActBackward(self) -> bool:
        return self.__punchingActConveyorBackward

    @punchingActBackward.setter
    def punchingActBackward(self, value: bool):
        self.__punchingActConveyorBackward = value
        
    @property
    def punchingMachineMoveUp(self) -> bool:
        return self.__punchingActMachineUp

    @punchingMachineMoveUp.setter
    def punchingMachineMoveUp(self, value: bool):
        self.__punchingActMachineUp = value

    @property
    def punchingMachineMoveDown(self) -> bool:
        return self.__punchingActMachineDown

    @punchingMachineMoveDown.setter
    def punchingMachineMoveDown(self, value: bool):
        self.__punchingActMachineDown = value
        
    @property
    def packageProcessed(self) -> bool:
        return self.__packageProcessed

    @packageProcessed.setter
    def packageProcessed(self, value: bool):
        self.__packageProcessed = value

    def receive(self):
        """Moves the package from the left sensor to the punching machine"""
        if not self.__packageProcessed:
            if not self.__punchingSensLeft:
                self.__punchingActConveyorForward = True
            if not self.__punchingSensRight:
                self.__punchingActConveyorForward = False

    def deliver(self):
        """Moves the package from the punching machine to the left sensor"""
        if self.__packageProcessed:
            if not self.__punchingSensRight:
                self.__punchingActConveyorBackward = True
            if not self.__punchingSensLeft:
                self.__punchingActConveyorBackward = False

    def process(self):

        """Moves the punching machine down and then up on the package

        Sets the self.__packageProcessed flag to True"""
        if not self.__punchingSensRight or self.__packageProcessed:
            logging.debug("process")
            self.__processing = True
            if not self.__packageProcessed:
                if not self.__punchingSensMachineIsDown:
                    self.__punchingActMachineDown = True
                else:
                    self.__punchingActMachineDown = False
                    self.__packageProcessed = True
            else:
                if not self.__punchingSensMachineIsUp:
                    self.__punchingActMachineUp = True
                else:
                    self.__punchingActMachineUp = False

    def receiveFrom(self, destination):

        """Stops the package at the punching Layout.Machine.
        Use this if you need to activate the conveyor before the package reaches the sensor."""
        if not self.__packageProcessed:# and not self.__processing:
            logging.debug("receive from")
            if not destination:
                self.__punchingActConveyorForward = True
            if not self.__punchingSensRight:
                self.__punchingActConveyorForward = False

    def deliverTo(self, destination):
        """Activates the punching Layout.Machine
        Once the package finishes processing
        Use this if you don't need to stop it upon reaching the right sensor."""
        if self.__packageProcessed:
            self.__processing = False
            if not self.__punchingSensRight:
                self.__punchingActConveyorBackward = True
            if not destination:
                self.__punchingActConveyorBackward = False

#
    def processPackage(self):
        """Moves the package to the punching machine.
        The machine then processes the package.
        After that the package is returned to the starting position at the conveyor."""
        self.receive()
        self.process()
        self.deliver()

#
    def processPackageFrom(self, destination):
        """Use this operation if the punching machine is connected to another one e.g. a conveyor"""
        self.receiveFrom(destination)
        self.process()
        self.deliverTo(destination)

    def processPackageFromTo(self, incoming, outgoing):
        """Use this operation if the incoming and outgoing destinations are different"""
        self.receiveFrom(incoming)
        self.process()
        self.deliverTo(outgoing)
        
#
    def resetMachine(self):
        """ Resets the packageProcessed flag to False
        Since it is set to True after the punching machine does the processing."""
        self.__packageProcessed = False

    def stop(self):
        self.__punchingActConveyorForward = self.__punchingActConveyorBackward = self.__punchingActMachineUp = self.__punchingActMachineDown = False
        return None

    def press(self):
        logging.debug("press")
        logging.debug("status " + str(self.__status))
        if self.__firstcounter > 0:
            logging.debug("first counter " + str(self.__firstcounter))
            self.__firstcounter -= 1
        else:
            if self.__status == "waiting":
                logging.debug("waiting")
                self.__punchingActConveyorForward = True
            if (not self.__punchingSensRight) and self.__status == "waiting":
                self.__status = "pressingStart"
                self.__punchingActConveyorForward = False
            if self.__punchingSensMachineIsDown and self.__status == "pressingMid":
                logging.debug("mid")
                self.__punchingActMachineDown = False
                self.__punchingActMachineUp = True
                self.__status = "pressingEnd"
            if (not self.__punchingSensMachineIsDown) and self.__status == "pressingStart":
                logging.debug("start")
                self.__punchingActMachineDown = True
                self.__status = "pressingMid"
            if self.__punchingSensMachineIsUp and self.__status == "pressingEnd":
                logging.debug("end")
                self.__punchingActMachineUp = False
                self.__punchingActConveyorBackward = True
        logging.debug("sens right " + str(self.__punchingSensRight))
        logging.debug("sens left " + str(self.__punchingSensLeft))
        logging.debug("isup " + str(self.__punchingSensMachineIsUp))
        logging.debug("isdown " + str(self.__punchingSensMachineIsDown))
        logging.debug("forward " + str(self.__punchingActConveyorForward))
        logging.debug("backward " +  str(self.__punchingActConveyorBackward))
        logging.debug("down " + str(self.__punchingActMachineDown))
        logging.debug("up " + str(self.__punchingActMachineUp))

        #self.processPackageFrom(False)
        return lambda: self.press()
