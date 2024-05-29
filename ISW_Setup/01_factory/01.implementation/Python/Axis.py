import logging
from enum import Enum
from PlusMinusStop import PlusMinusStop
from ImpulseCounter import ImpulseCounter
from Counter import Counter


class AxisType(Enum):
    """used to differentiate between axis using impulse counters and encoders"""
    Counter = 1
    Encoder = 2

#TODO ensure no negative values are accepted for counter goal
class Axis:

    def __init__(self, typ: AxisType, tolerance, counter_callback = None):
        """constructor creates ImpulseCounter object if necessary"""
        self.__type = typ
        self.counter_callback = counter_callback
        if typ == AxisType.Counter:
            self.__counter = ImpulseCounter()
        else:
            self.__counter = Counter()
        self.__tolerance = tolerance
        self.__first = True
        #variables need to be manually updated/written
        self.__endpos = 0
        self.__counterinput = 0
        self.__outputplus = 0
        self.__outputminus = 0
        if typ == AxisType.Encoder:
            self.play = 10
        else:
            self.play = 2

    @property
    def counterValueCurrent(self):
        return self.__counter.counter

    @counterValueCurrent.setter
    def counterValueCurrent(self, value):
        self.__counter.counter = value
        if self.counter_callback is not None:
            self.counter_callback(value)

    @property
    def endpos(self):
        return self.__endpos

    @property
    def counterinput(self):
        return self.__counterinput

    @property
    def outputplus(self):
        return self.__outputplus

    @property
    def outputminus(self):
        return self.__outputminus

    def update(self, endpos, counterinput):
        self.__endpos = endpos
        self.__counterinput = counterinput

    def howtoCounterPos(self, counterGoal, counterCurrent, tolerance):
        """method to determine which way the axis needs to rotate"""
        #TODO probably set a different play for impulse counters(vllt 2) vs encoder counters (vllt 10)
        #"handle" overflow
        #assume overflow if counter greater 4 millions
        if counterGoal < 0:
            counterGoal = 0
        if counterGoal > counterCurrent + tolerance:
            return PlusMinusStop.PLUS
        elif counterGoal < counterCurrent - tolerance - self.play and counterCurrent > 4000000:
            logging.debug('handeled overflow')
            return PlusMinusStop.PLUS
        elif counterGoal < counterCurrent - tolerance - self.play:
            return PlusMinusStop.MINUS
        else:
            return PlusMinusStop.STOP


    def gotoConfig(self, endpos, counterGoal):
        """method to set outputs to reach the wanted config goal for that axis"""
        t = False
        d = None
        #wenn endschalter gewünscht immer nutzen anstellen von counterGoal
        if endpos:
            if not self.__endpos:
                self.__outputminus = True
                if isinstance(self.__counter, ImpulseCounter):
                    self.counterValueCurrent = self.__counter.compute(self.__counterinput, PlusMinusStop.MINUS)
                    print(self.counterValueCurrent)
                d = PlusMinusStop.MINUS
            else:
                self.__outputminus = False
                t = True
        else:
            #calls compute methods for axis with impulse counters based on (previous) motor direction, not necessary for encoder
            if isinstance(self.__counter, ImpulseCounter):
                if self.outputplus:
                    self.counterValueCurrent = self.__counter.compute(self.__counterinput, PlusMinusStop.PLUS)
                    print(self.counterValueCurrent)
                    #print("compute counter")
                    if self.__first or self.endpos:
                        self.__first = False
                        print("Reset arm counter here here here here here here")
                        self.counterValueCurrent = 0
                elif self.outputminus:
                    self.counterValueCurrent = self.__counter.compute(self.__counterinput, PlusMinusStop.MINUS)
                    print(self.counterValueCurrent)
                    if self.__first or self.endpos:
                        self.__first = False
                        print("Reset arm counter here here here here here here")
                        self.counterValueCurrent = 0

            else:
                self.counterValueCurrent = self.__counterinput
            if self.howtoCounterPos(counterGoal, self.counterValueCurrent, self.__tolerance) == PlusMinusStop.PLUS:
                self.__outputminus = False
                self.__outputplus = True
                d = PlusMinusStop.PLUS
            elif self.howtoCounterPos(counterGoal, self.counterValueCurrent, self.__tolerance) == PlusMinusStop.MINUS:
                self.__outputminus = True
                self.__outputplus = False
                d = PlusMinusStop.MINUS
            elif self.howtoCounterPos(counterGoal, self.counterValueCurrent, self.__tolerance) == PlusMinusStop.STOP:
                self.__outputminus = False
                self.__outputplus = False
                t = True
        if isinstance(self.__counter, ImpulseCounter):
            return t, d
        return t
