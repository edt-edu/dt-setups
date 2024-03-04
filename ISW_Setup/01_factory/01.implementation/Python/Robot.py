from MovingMachine import MovingMachine
from ReachedDirection import ReachedDirection
from RequestedParameter import RequestedParameter
from Axis import AxisType, Axis
from ThreeDRobotConfig import ThreeDRobotConfig
from Position import Position
import logging


class Robot(MovingMachine):

    def generateTransferMoveList(self, numPickup: Position, numPlace: Position):
        logging.debug('called generateTransferMoveList')
        offset = 700
        if numPickup.vertical >= offset:
            self.pickVert = numPickup.vertical - offset
        if numPlace.vertical >= offset:
            self.placeVert = numPlace.vertical - offset
        # Greifer öffnen
        robotPickConf0 = ThreeDRobotConfig(self.robotSensVerticalEncoderCounter, self.robotSensRotEncoderCounter,
                                           self.__axisArm.counterValueCurrent, 6, False, False, False, False)
        # Arm einfahren
        robotPickConf1 = ThreeDRobotConfig(self.robotSensVerticalEncoderCounter, self.robotSensRotEncoderCounter, 0, 6,
                                           False, False, True, False)
        # an Objekt heranfahren (Rot, Vertical)
        robotPickConf2 = ThreeDRobotConfig(self.pickVert, numPickup.rot, 0, 6, False, False, True, False)
        # Arm ausfahen
        robotPickConf3 = ThreeDRobotConfig(self.pickVert, numPickup.rot, numPickup.horizontal, 6, False,
                                           False, False,
                                           False)
        # absenken
        robotPickConf4 = ThreeDRobotConfig(numPickup.vertical, numPickup.rot, numPickup.horizontal, 6, False, False,
                                           False, False)
        # Greifer schließen
        robotPickConf5 = ThreeDRobotConfig(numPickup.vertical, numPickup.rot, numPickup.horizontal, 14, False, False,
                                           False, False)
        # anheben
        robotPickConf6 = ThreeDRobotConfig(self.pickVert, numPickup.rot, numPickup.horizontal, 14, False,
                                           False, False,
                                           False)
        # Arm einfahren
        robotPlaceConf0 = ThreeDRobotConfig(self.pickVert, numPickup.rot, 0, 14, False,
                                            False, True,
                                            False)
        # bewegen
        robotPlaceConf1 = ThreeDRobotConfig(self.placeVert, numPlace.rot, 0, 14, False,
                                            False, True, False)
        # Arm ausfahren
        robotPlaceConf2 = ThreeDRobotConfig(self.placeVert, numPlace.rot, numPlace.horizontal, 14, False,
                                            False, False, False)
        # absenken
        robotPlaceConf3 = ThreeDRobotConfig(numPlace.vertical, numPlace.rot, numPlace.horizontal, 14, False, False,
                                            False, False)
        # Greifer öffnen
        robotPlaceConf4 = ThreeDRobotConfig(numPlace.vertical, numPlace.rot, numPlace.horizontal, 6, False, False,
                                            False, True)
        # anheben
        robotPlaceConf5 = ThreeDRobotConfig(self.placeVert, numPlace.rot, numPlace.horizontal, 6, False,
                                            False, True, False)
        # Arm einfahren?

        return [robotPickConf0,
                robotPickConf1,
                robotPickConf2,
                robotPickConf3,
                robotPickConf4,
                robotPickConf5,
                robotPickConf6,
                robotPlaceConf0,
                robotPlaceConf1,
                robotPlaceConf2,
                robotPlaceConf3,
                robotPlaceConf4,
                robotPlaceConf5]

    @property
    def isExecuting(self) -> bool:
        if self.__robotActRotRight or self.__robotActRotLeft or self.__robotActVerticalUp or self.__robotActVerticalDown or self.__robotActGripperOpen or self.__robotActGripperClose or self.__robotActArmOut or self.__robotActArmIn:
            return True
        else:
            return False

    # @property
    # def fakeIsExecuting(self):
    #     if self.__robotActRotRight or self.__robotActRotLeft or self.__robotActVerticalUp or self.__robotActVerticalDown or self.__robotActGripperOpen or self.__robotActGripperClose or self.__robotActArmOut or self.__robotActArmIn:
    #         self.__isExecutingCount = 0
    #         return True
    #     else:
    #         if self.__isExecutingCount < 10:
    #             self.__isExecutingCount += 1
    #             #logging.debug('fake is executing true')
    #             return True
    #         else:
    #             #logging.debug('is executing false')
    #             return False

    # list containing all critical points on the robots path
    def __init__(self, id1):
        self.__isExecutingCount = 0
        self.__robotSensGripperOpen = self.__robotSensArmEndIn = self.__robotSensVerticalEndUp = self.__robotSensRotEnd = False
        self.__robotActGripperOpen = self.__robotActGripperClose = self.__robotActArmOut = self.__robotActArmIn = self.__robotActVerticalDown = self.__robotActVerticalUp = self.__robotActRotRight = self.__robotActRotLeft = False
        self.__robotSensRotEncoderCounter = self.__robotSensVerticalEncoderCounter = self.__robotSensArmImpulseCounterRaw = self.__robotSensGripperImpulseCounterRaw = 0
        self.__axisArm = Axis(AxisType.Counter, 1)
        self.__axisVertical = Axis(AxisType.Encoder, 20)
        self.__axisRot = Axis(AxisType.Encoder, 20)
        self.__axisGripper = Axis(AxisType.Counter, 1)
        dictMap = {RequestedParameter.REFERNCESWITCHCLAW: self.__robotSensGripperOpen,
                   RequestedParameter.PULSECOUNTERGRIPPER: self.__axisGripper.counterValueCurrent,
                   RequestedParameter.REFERENCESWITCHGRIPARM: self.__robotSensArmEndIn,
                   RequestedParameter.PULSECOUNTERGRIPARM: self.__axisArm.counterValueCurrent,
                   RequestedParameter.REFERENCESWITCHVERTICALAXIS: self.__robotSensVerticalEndUp,
                   RequestedParameter.REFERENCESWITCHROTATE: self.__robotSensRotEnd,
                   RequestedParameter.VERTICALAXISSTEP: self.__robotSensVerticalEncoderCounter,
                   RequestedParameter.ROTATESTEP: self.__robotSensRotEncoderCounter,
                   RequestedParameter.MOTORGRIPPEROPEN: self.__robotActGripperOpen,
                   RequestedParameter.MOTORGRIPPERCLOSE: self.__robotActGripperClose,
                   RequestedParameter.MOTORGRIPARMFORWARD: self.__robotActArmOut,
                   RequestedParameter.MOTORGRIPARMBACKWARD: self.__robotActArmIn,
                   RequestedParameter.MOTORVERTICALAXISDOWN: self.__robotActVerticalDown,
                   RequestedParameter.MOTORVERTICALAXISUP: self.__robotActVerticalUp,
                   RequestedParameter.MOTORROTATECLOCKWISE: self.__robotActRotRight,
                   RequestedParameter.MOTORROTATECOUNTERCLOCKWISE: self.__robotActRotLeft}
        super().__init__(id1, dictMap)

        self.setupFinished = self.setupFinishedHelper = False
        self.__pc = 0
        self.pickVert = 0
        self.placeVert = 0

    @property
    def robotSensGripperOpen(self) -> bool:
        return self.__robotSensGripperOpen

    @robotSensGripperOpen.setter
    def robotSensGripperOpen(self, value):
        self.__robotSensGripperOpen = value

    @property
    def robotSensGripperImpulseCounterRaw(self) -> int:
        return self.__robotSensGripperImpulseCounterRaw

    @robotSensGripperImpulseCounterRaw.setter
    def robotSensGripperImpulseCounterRaw(self, value):
        self.__robotSensGripperImpulseCounterRaw = value

    @property
    def robotSensArmEndIn(self) -> bool:
        return self.__robotSensArmEndIn

    @robotSensArmEndIn.setter
    def robotSensArmEndIn(self, value):
        self.__robotSensArmEndIn = value

    @property
    def robotSensArmImpulseCounterRaw(self) -> int:
        return self.__robotSensArmImpulseCounterRaw

    @robotSensArmImpulseCounterRaw.setter
    def robotSensArmImpulseCounterRaw(self, value):
        self.__robotSensArmImpulseCounterRaw = value

    @property
    def robotSensVerticalEndUp(self) -> bool:
        return self.__robotSensVerticalEndUp

    @robotSensVerticalEndUp.setter
    def robotSensVerticalEndUp(self, value):
        self.__robotSensVerticalEndUp = value

    @property
    def robotSensVerticalEncoderCounter(self):
        return self.__robotSensVerticalEncoderCounter

    @robotSensVerticalEncoderCounter.setter
    def robotSensVerticalEncoderCounter(self, value):
        self.__robotSensVerticalEncoderCounter = value

    @property
    def robotSensRotEnd(self):
        return self.__robotSensRotEnd

    @robotSensRotEnd.setter
    def robotSensRotEnd(self, value):
        self.__robotSensRotEnd = value

    @property
    def robotSensRotEncoderCounter(self):
        return self.__robotSensRotEncoderCounter

    @robotSensRotEncoderCounter.setter
    def robotSensRotEncoderCounter(self, value):
        self.__robotSensRotEncoderCounter = value

    @property
    def robotActGripperOpen(self):
        return self.__robotActGripperOpen

    @robotActGripperOpen.setter
    def robotActGripperOpen(self, value):
        self.__robotActGripperOpen = value

    @property
    def robotActGripperClose(self):
        return self.__robotActGripperClose

    @robotActGripperClose.setter
    def robotActGripperClose(self, value):
        self.__robotActGripperClose = value

    @property
    def robotActArmOut(self):
        return self.__robotActArmOut

    @robotActArmOut.setter
    def robotActArmOut(self, value):
        self.__robotActArmOut = value

    @property
    def robotActArmIn(self):
        return self.__robotActArmIn

    @robotActArmIn.setter
    def robotActArmIn(self, value):
        self.__robotActArmIn = value

    @property
    def robotActVerticalDown(self):
        return self.__robotActVerticalDown

    @robotActVerticalDown.setter
    def robotActVerticalDown(self, value):
        self.__robotActVerticalDown = value

    @property
    def robotActVerticalUp(self):
        return self.__robotActVerticalUp

    @robotActVerticalUp.setter
    def robotActVerticalUp(self, value):
        self.__robotActVerticalUp = value

    @property
    def robotActRotRight(self):
        return self.__robotActRotRight

    @robotActRotRight.setter
    def robotActRotRight(self, value):
        self.__robotActRotRight = value

    @property
    def robotActRotLeft(self):
        return self.__robotActRotLeft

    @robotActRotLeft.setter
    def robotActRotLeft(self, value):
        self.__robotActRotLeft = value

    def executeHelper(self):
        """
        Returns whether each counter can be reset after the setup process

        :returns two boolean values
        :rtype tuple
        """
        if self.setupFinishedHelper:
            self.setupFinishedHelper = False
            return True, True
        else:
            return False, False

    ###################################################################################################################
    # functions below callable by JSON API
    # make sure all method names only consist of lowercase letters

    # TODO not yet successfully callable, missing param type -> also in java
    def gotoconfig(self, config):
        iconfig = config
        if config is None:
            iconfig = ThreeDRobotConfig(0, 0, 0, 0)
        # t1 = t2 = t3 = t4 = False
        # d3 = d4 = None

        self.__axisVertical.update(self.robotSensVerticalEndUp, self.robotSensVerticalEncoderCounter)
        t1 = self.__axisVertical.gotoConfig(iconfig.endVertical, iconfig.counterVertical)
        self.robotActVerticalUp = self.__axisVertical.outputminus
        self.robotActVerticalDown = self.__axisVertical.outputplus
        #logging.debug('verticalEncoder' + str(self.robotSensVerticalEncoderCounter))

        self.__axisRot.update(self.robotSensRotEnd, self.robotSensRotEncoderCounter)
        t2 = self.__axisRot.gotoConfig(iconfig.endRot, iconfig.counterRot)
        self.robotActRotRight = self.__axisRot.outputminus
        self.robotActRotLeft = self.__axisRot.outputplus
        #logging.debug('rotEncoder' + str(self.robotSensRotEncoderCounter))

        self.__axisArm.update(self.robotSensArmEndIn, self.robotSensArmImpulseCounterRaw)
        t3, d3 = self.__axisArm.gotoConfig(iconfig.endArm, iconfig.counterArm)
        self.robotActArmIn = self.__axisArm.outputminus
        self.robotActArmOut = self.__axisArm.outputplus

        self.__axisGripper.update(self.robotSensGripperOpen, self.robotSensGripperImpulseCounterRaw)
        t4, d4 = self.__axisGripper.gotoConfig(iconfig.endGripper, iconfig.counterGripper)
        self.robotActGripperOpen = self.__axisGripper.outputminus
        self.robotActGripperClose = self.__axisGripper.outputplus
        #logging.debug('gripperCounter' + str(self.__axisGripper.counterValueCurrent))
        # TODO figure out return to match pick , place, move
        return t1 and t2 and t3 and t4
        # return ReachedDirection(t1 and t2 and t3 and t4, d3, d4)

    # referenzfahrt des Roboters um alle counter korrekt zu setzen - setzen der counter an anderer Stelle, hier aber in Referenzposition
    # von execute bereits berücksichtigt

    def setup(self):
        logging.debug("setup")
        t1 = t2 = t3 = t4 = False
        if self.robotSensArmEndIn:
            self.robotActArmIn = False
            t3 = True
        else:
            self.robotActArmIn = True
        if self.robotSensVerticalEndUp:
            self.robotActVerticalUp = False
            t1 = True
        elif t3:
            self.robotActVerticalUp = True

        if self.robotSensRotEnd:
            self.robotActRotRight = False
            t2 = True
        elif t3:
            self.robotActRotRight = True

        if self.robotSensGripperOpen:
            self.robotActGripperOpen = False
            t4 = True
        elif t3:
            self.robotActGripperOpen = True
        self.setupFinished = (t1 and t2 and t3 and t4)
        if not self.setupFinished:
            #self.setFakeExecuting = True
            self.setupFirst = True
        else:
            self.setupFinishedHelper = True
        if self.setupFirst:
            logging.debug("setup first True")
            self.setupFirst = False
            self.setFakeExecuting = True
        logging.debug('setup finished ' + str(self.setupFinished))
        return lambda: self.setup() #überschreibt eigentlich auszuführende fkt? nein doch

    def move(self, startPos: Position, endPos: Position):
        self.pc = 0
        self.setupFirst = True
        self.setFakeExecuting = True
        """
        The robot picks an item at the start position and drops it at the end position
        :param startPos: the position to pick the item
        :param endPos: the position to place the item
        """
        print("move")
        moveList = self.generateTransferMoveList(startPos, endPos)
        print(moveList)
        # TODO make this execution function be called multiple times in the background until the execution is finished
        # or do this in JSONProcessingIntegrationMain by keeping all currently executing functions in a list and call them from there (detecting change probably more easily)
        # should already be implemented in exLoop -> siehe JSONProcessingIntegrationMain
        return lambda: self.execute(startPos, endPos, moveList)

    def pick(self, startPos: Position):
        """
        The robot picks the object at the start position and keeps it in its gripper until place is called
        :param startPos: the position the object is being picked up at
        """
        logging.debug('called pick with: %s', startPos)
        self.setupFirst = True
        self.setFakeExecuting = True
        moveList = self.generateTransferMoveList(startPos, startPos)
        moveList = moveList[:7]
        print(moveList)
        # TODO make this execution function be called multiple times in the background until the execution is finished

        return lambda: self.execute(startPos, startPos, moveList)

    def place(self, endPos):
        """
        Used to place an object at the endPos, that the robot is already holding
        :param endPos: the position to drop the object at
        """
        moveList = self.generateTransferMoveList(endPos, endPos)
        moveList = moveList[7:]
        self.setupFirst = True
        self.setFakeExecuting = True
        print(moveList)
        # TODO make this execution function be called multiple times in the background until the execution is finished
        return lambda: self.execute(endPos, endPos, moveList)

    def stop(self):
        self.robotActRotLeft = self.robotActRotRight = self.robotActVerticalUp = self.robotActVerticalDown = self.robotActArmIn = self.robotActArmOut = self.robotActGripperClose = self.robotActGripperOpen = False
        return None
