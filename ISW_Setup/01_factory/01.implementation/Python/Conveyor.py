from IllegalValueCombination import IllegalValueCombination
from Machine import Machine
from ImpulseCounter import ImpulseCounter
from PlusMinusStop import PlusMinusStop
from Direction import Direction
from RequestedParameter import RequestedParameter
import logging


class Conveyor(Machine):

    # TODO create reset for self.once from execute

    @property
    def isExecuting(self) -> bool:
        if (self.__conveyorActForward or self.__conveyorActBackward):
            return True
        else:
            if self.__isExecutingCount < 15:
                self.__isExecutingCount += 1
                return True
            else:
                return False


    def __init__(self, id1):
        self.__conveyorSensImpulseCounterRaw = 0
        self.__conveyorSensLeft = self.__conveyorSensRight = True
        self.__conveyorActForward = self.__conveyorActBackward = False
        self.__counter = ImpulseCounter()
        self.current = 0
        self.sensed = False
        dictMap = {RequestedParameter.LIGHTBARRIERFEEDSTATION: self.__conveyorSensRight,
                   RequestedParameter.LIGHTBARRIERSWAPSTATION: self.__conveyorSensLeft,
                   # TODO update self.current to actually count the steps according to the conveyor direction
                   RequestedParameter.PULSECOUNTER: self.current,
                   RequestedParameter.MOTORCONVEYORBELTFORWARD: self.__conveyorActForward,
                   RequestedParameter.MOTORCONVEYORBELTBACKWARD: self.__conveyorActBackward}
        super().__init__(id1, dictMap)

        self.arrived = False
        self.once = True
        self.__isExecutingCount = 0

    @property
    def conveyorSensLeft(self) -> bool:
        return self.__conveyorSensLeft

    @conveyorSensLeft.setter
    def conveyorSensLeft(self, value: bool):
        self.__conveyorSensLeft = value

    @property
    def conveyorSensRight(self) -> bool:
        return self.__conveyorSensRight

    @conveyorSensRight.setter
    def conveyorSensRight(self, value: bool):
        self.__conveyorSensRight = value

    @property
    def conveyorSensImpulse(self):
        return self.__conveyorSensImpulseCounterRaw

    @conveyorSensImpulse.setter
    def conveyorSensImpulse(self, value):
        self.__conveyorSensImpulseCounterRaw = value

    @property
    def conveyorActForward(self) -> bool:
        return self.__conveyorActForward

    @conveyorActForward.setter
    def conveyorActForward(self, value: bool):
        self.__conveyorActForward = value

    @property
    def conveyorActBackward(self) -> bool:
        return self.__conveyorActBackward

    @conveyorActBackward.setter
    def conveyorActBackward(self, value: bool):
        self.__conveyorActBackward = value

    @property
    def conveyorCounterValue(self):
        return self.__counter.counter

    def forward(self):
        """Moves the package from left sensor to right sensor"""
        if not self.__conveyorSensLeft:
            self.__conveyorActForward = True
        if not self.__conveyorSensRight:
            self.__conveyorActForward = False
            return True
        return False

    def backward(self):
        """Moves the package from right sensor to left sensor"""
        if not self.__conveyorSensRight:
            self.__conveyorActBackward = True
        if not self.__conveyorSensLeft:
            self.__conveyorActBackward = False
            return True
        return False

    # TODO Once the package leaves, the conveyor is activated again automatically
    def forwardFromAnywhere(self):
        """Moves the package from any place on the conveyor to the right sensor"""
        # if self.once:
        self.__conveyorActForward = True
        if not self.__conveyorSensRight:
            self.__conveyorActForward = False
            # self.once = False
            return True
        return False

    # TODO Once the package leaves, the conveyor is activated again automatically
    def backwardFromAnywhere(self):
        """Moves the package from any place on the conveyor to the left sensor"""
        # if self.once:
        self.__conveyorActBackward = True
        if not self.__conveyorSensLeft:
            self.__conveyorActBackward = False
            # self.once = False
            return True
        return False

    def forwardLeaveConveyor(self):
        """Moves the package from anywhere on the line to the left, until it leaves the conveyor"""
        # maybe use Cyclic waiter to shutoff conveyor after (50?) cycles of waiting
        # or count steps (maybe 10 extra?) after detection at left sensor
        # maybe use self.forwardFromAnywhere()
        print("self.countSteps " + str(self.countSteps()))
        if not self.arrived:
            self.__conveyorActForward = True
        else:
            if self.countSteps() >= 6:
                self.__conveyorActForward = False
        if not self.__conveyorSensRight:
            self.__counter.counter = 0
            self.arrived = True

    def backwardLeaveConveyor(self):
        """Moves the package from anywhere on the line to the right, until it leaves the conveyor"""
        # maybe use Cyclic waiter to shutoff conveyor after (50?) cycles of waiting
        # or count steps (maybe 10 extra?) after detection at right sensor
        # maybe use self.backwardFromAnywhere()
        print("self.countSteps " + str(self.countSteps()))
        if not self.arrived:
            self.__conveyorActBackward = True
        else:
            if self.countSteps() >= 6:
                self.__conveyorActBackward = False
        if not self.__conveyorSensLeft:
            self.__counter.counter = 0
            self.arrived = True


    def forwardHalfway(self):
        """Moves the package to the middle of the conveyor starting from the left sensor"""
        self.current = self.countSteps()
        if not self.__conveyorSensLeft:
            self.__counter.counter = 0
            self.__conveyorActForward = True
        elif self.current >= 8:
            self.__conveyorActForward = False
            self.current = 0

    def backwardHalfway(self):
        """Moves the package to the middle of the conveyor starting from the right sensor"""
        self.current = self.countSteps()
        if not self.__conveyorSensRight:
            self.__counter.counter = 0
            self.__conveyorActBackward = True
        elif self.current >= 8:
            self.__conveyorActBackward = False
            self.current = 0

    def forwardGoto(self, steps: int):
        self.__conveyorActForward = True
        self.current = self.countSteps()
        if not self.__conveyorSensLeft:
            self.sensed = True
            self.__counter.counter = 0
        elif self.current >= steps and self.sensed:
            self.__conveyorActForward = False
            self.current = 0

    def backwardGoto(self, steps: int):
        self.__conveyorActBackward = True
        self.current = self.countSteps()
        if not self.__conveyorSensRight:
            self.__counter.counter = 0
            self.sensed = True
        elif self.current >= steps and self.sensed:
            self.__conveyorActBackward = False
            self.current = 0

    def countSteps(self):
        steps = self.__counter.compute(self.__conveyorSensImpulseCounterRaw, PlusMinusStop.PLUS)
        print(steps)
        return steps

    def stop(self):
        self.__conveyorActForward = self.__conveyorActBackward = False

    def execute(self):
        self.backwardLeaveConveyor()

    #            self.__conveyorCounterRef = self.__conveyorCounter
    #            self.__conveyorActForward = True
    #        if self.__conveyorCounter - self.__conveyorCounterRef >= 10:
    #            self.__conveyorActForward = False

    def move(self, dir: Direction):
        logging.debug('conveyor.move called')
        self.arrived = False
        if dir == Direction.FORWARD:
            return lambda: self.forwardLeaveConveyor()
        if dir == Direction.BACKWARD:
            return lambda: self.backwardLeaveConveyor()

    def gotoconfig(self, dir: Direction, steps: int):
        logging.debug('conveyor.gotoconfig called')
        self.sensed = False
        if dir == Direction.FORWARD:
            return lambda: self.forwardGoto(steps)
        if dir == Direction.BACKWARD:
            return lambda: self.backwardGoto(steps)

    def movelb(self, dir: Direction):
        if dir == Direction.FORWARD:
            return lambda: self.forwardFromAnywhere()
        if dir == Direction.BACKWARD:
            return lambda: self.backwardFromAnywhere()

