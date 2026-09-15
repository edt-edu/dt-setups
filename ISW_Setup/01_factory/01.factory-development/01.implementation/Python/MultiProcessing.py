from Machine import Machine
from RequestedParameter import RequestedParameter
import logging


class MultiProcessing(Machine):

    @property
    def isExecuting(self) -> bool:
        if (self.__multiProcessingActRotClockwise or self.__multiProcessingActRotCounterclockwise or self.__multiProcessingActConveyorForward or self.__multiProcessingActSaw
            or self.__multiProcessingActOvenInward or self.__multiProcessingActOvenOutward or self.__multiProcessingActGripperToOven or self.__multiProcessingActGripperToTurntable
            or self.__multiProcessingOvenLight or self.__multiProcessingCompressor or self.__multiProcessingValve or self.__multiProcessingActLowerValve
            or self.__multiProcessingValveOvenDoor or self.__multiProcessingValveFeeder):
            self.__isExecutingCount = 0
            return True
        else:
            if self.__isExecutingCount < 15:
                self.__isExecutingCount += 1
                return True
            else:
                return False


    def __init__(self, id1):
        self.__isExecutingCount = 0
        self.__multiProcessingSensTurntablePosVacuum = self.__multiProcessingSensTurntablePosConveyor = self.__multiProcessingSensDelivery = False
        self.__multiProcessingSensTurntablePosSaw = self.__multiProcessingSensVacuumGripperAtTurntable = False
        self.__multiProcessingSensOvenFeederIn = self.__multiProcessingSensOvenFeederOut = False
        self.__multiProcessingSensVacuumSensGripperAtOven = self.__multiProcessingSensOven = False
        self.__multiProcessingActRotClockwise = self.__multiProcessingActRotCounterclockwise = False
        self.__multiProcessingActConveyorForward = False
        self.__multiProcessingActSaw = False
        self.__multiProcessingActOvenInward = self.__multiProcessingActOvenOutward = False
        self.__multiProcessingActGripperToOven = self.__multiProcessingActGripperToTurntable = False
        self.__multiProcessingOvenLight = False
        self.__multiProcessingCompressor = False
        self.__multiProcessingValve = False
        self.__multiProcessingActLowerValve = False
        self.__multiProcessingValveOvenDoor = False
        self.__multiProcessingValveFeeder = False
        dictMap = {RequestedParameter.REFERENCESWITCHTURNTABLEPOSITOINVACUUM: self.__multiProcessingSensTurntablePosVacuum,
                   RequestedParameter.REFERNCESWITCHTURNTABLEPOSITIONBELT: self.__multiProcessingSensTurntablePosConveyor,
                   RequestedParameter.LIGHTBARRIERENDOFCONVEYORBELT: self.__multiProcessingSensDelivery,
                   RequestedParameter.REFERENCEWITCHTURNTABLEPOSITIONSAW: self.__multiProcessingSensTurntablePosSaw,
                   RequestedParameter.REFERENCESWITCHVACUUMPOSITIONTURNTABLE: self.__multiProcessingSensVacuumGripperAtTurntable,
                   RequestedParameter.REFERENCESWITCHOVENFEEDERINSIDE: self.__multiProcessingSensOvenFeederIn,
                   RequestedParameter.REFERENCESWITCHOVENFEEDEROUTSIDE: self.__multiProcessingSensOvenFeederOut,
                   RequestedParameter.REFERENCESWITCHVACUUMPOSITIONOVEN: self.__multiProcessingSensVacuumSensGripperAtOven,
                   RequestedParameter.LIGHTBARRIEROVEN: self.__multiProcessingSensOven,
                   RequestedParameter.MOTORTURNTABLECLOCKWISE: self.__multiProcessingActRotClockwise,
                   RequestedParameter.MOTORROTATECOUNTERCLOCKWISE: self.__multiProcessingActRotCounterclockwise,
                   RequestedParameter.MOTORCONVEYORBELTFORWARD: self.__multiProcessingActConveyorForward,
                   RequestedParameter.MOTORSAW: self.__multiProcessingActSaw,
                   RequestedParameter.MOTOROVENFEEDERRETRACT: self.__multiProcessingSensOvenFeederIn,
                   RequestedParameter.MOTOROVENFEEDEREXTEND: self.__multiProcessingSensOvenFeederOut,
                   RequestedParameter.MOTORVACUUMTOWARDSOVEN: self.__multiProcessingActGripperToOven,
                   RequestedParameter.MOTORVACUUMTOWARDSTURNTABLE: self.__multiProcessingActGripperToTurntable,
                   RequestedParameter.LIGHTOVEN: self.__multiProcessingOvenLight,
                   RequestedParameter.VALVEVACUUM: self.__multiProcessingValve,
                   RequestedParameter.VALVELOWERING: self.__multiProcessingActLowerValve,
                   RequestedParameter.VALVEOVENDOOR: self.__multiProcessingValveOvenDoor,
                   RequestedParameter.VALVEFEEDER: self.__multiProcessingValveFeeder}
        super().__init__(id1, dictMap)

        self.setupFinished = False
        self.sawCount = 0
        self.ovenCount = 0
        self.vacuumCount = 0
        self.deliveryCount = 0
        self.ovenReady = False
        self.toVac = False
        self.delivered = False

    @property
    def multiProcessingSensTurntablePosVacuum(self) -> bool:
        return self.__multiProcessingSensTurntablePosVacuum

    @multiProcessingSensTurntablePosVacuum.setter
    def multiProcessingSensTurntablePosVacuum(self, value: bool):
        self.__multiProcessingSensTurntablePosVacuum = value

    @property
    def multiProcessingSensTurntablePosSaw(self) -> bool:
        return self.__multiProcessingSensTurntablePosSaw

    @multiProcessingSensTurntablePosSaw.setter
    def multiProcessingSensTurntablePosSaw(self, value: bool):
        self.__multiProcessingSensTurntablePosSaw = value

    @property
    def multiProcessingSensTurntablePosConveyor(self) -> bool:
        return self.__multiProcessingSensTurntablePosConveyor

    @multiProcessingSensTurntablePosConveyor.setter
    def multiProcessingSensTurntablePosConveyor(self, value: bool):
        self.__multiProcessingSensTurntablePosConveyor = value

    @property
    def multiProcessingSensDelivery(self) -> bool:
        return self.__multiProcessingSensDelivery

    @multiProcessingSensDelivery.setter
    def multiProcessingSensDelivery(self, value: bool):
        self.__multiProcessingSensDelivery = value

    @property
    def multiProcessingSensOven(self) -> bool:
        return self.__multiProcessingSensOven

    @multiProcessingSensOven.setter
    def multiProcessingSensOven(self, value: bool):
        self.__multiProcessingSensOven = value

    @property
    def multiProcessingSensVacuumGripperAtOven(self) -> bool:
        return self.__multiProcessingSensVacuumSensGripperAtOven

    @multiProcessingSensVacuumGripperAtOven.setter
    def multiProcessingSensVacuumGripperAtOven(self, value: bool):
        self.__multiProcessingSensVacuumSensGripperAtOven = value

    @property
    def multiProcessingSensVacuumGripperAtTurntable(self) -> bool:
        return self.__multiProcessingSensVacuumGripperAtTurntable

    @multiProcessingSensVacuumGripperAtTurntable.setter
    def multiProcessingSensVacuumGripperAtTurntable(self, value: bool):
        self.__multiProcessingSensVacuumGripperAtTurntable = value

    @property
    def multiProcessingSensOvenFeederOut(self) -> bool:
        return self.__multiProcessingSensOvenFeederOut

    @multiProcessingSensOvenFeederOut.setter
    def multiProcessingSensOvenFeederOut(self, value: bool):
        self.__multiProcessingSensOvenFeederOut = value

    @property
    def multiProcessingSensOvenFeederIn(self) -> bool:
        return self.__multiProcessingSensOvenFeederIn

    @multiProcessingSensOvenFeederIn.setter
    def multiProcessingSensOvenFeederIn(self, value: bool):
        self.__multiProcessingSensOvenFeederIn = value

    @property
    def multiProcessingActRotClockwise(self) -> bool:
        return self.__multiProcessingActRotClockwise

    @multiProcessingActRotClockwise.setter
    def multiProcessingActRotClockwise(self, value: bool):
        self.__multiProcessingActRotClockwise = value

    @property
    def multiProcessingActRotCounterclockwise(self) -> bool:
        return self.__multiProcessingActRotCounterclockwise

    @multiProcessingActRotCounterclockwise.setter
    def multiProcessingActRotCounterclockwise(self, value: bool):
        self.__multiProcessingActRotCounterclockwise = value

    @property
    def multiProcessingActConveyorForward(self) -> bool:
        return self.__multiProcessingActConveyorForward

    @multiProcessingActConveyorForward.setter
    def multiProcessingActConveyorForward(self, value: bool):
        self.__multiProcessingActConveyorForward = value

    @property
    def multiProcessingActSaw(self) -> bool:
        return self.__multiProcessingActSaw

    @multiProcessingActSaw.setter
    def multiProcessingActSaw(self, value: bool):
        self.__multiProcessingActSaw = value

    @property
    def multiProcessingActOvenInward(self) -> bool:
        return self.__multiProcessingActOvenInward

    @multiProcessingActOvenInward.setter
    def multiProcessingActOvenInward(self, value: bool):
        self.__multiProcessingActOvenInward = value

    @property
    def multiProcessingActOvenOutward(self) -> bool:
        return self.__multiProcessingActOvenOutward

    @multiProcessingActOvenOutward.setter
    def multiProcessingActOvenOutward(self, value: bool):
        self.__multiProcessingActOvenOutward = value

    @property
    def multiProcessingActGripperToOven(self) -> bool:
        return self.__multiProcessingActGripperToOven

    @multiProcessingActGripperToOven.setter
    def multiProcessingActGripperToOven(self, value: bool):
        self.__multiProcessingActGripperToOven = value

    @property
    def multiProcessingActGripperToTurntable(self) -> bool:
        return self.__multiProcessingActGripperToTurntable

    @multiProcessingActGripperToTurntable.setter
    def multiProcessingActGripperToTurntable(self, value: bool):
        self.__multiProcessingActGripperToTurntable = value

    @property
    def multiProcessingOvenLight(self) -> bool:
        return self.__multiProcessingOvenLight

    @multiProcessingOvenLight.setter
    def multiProcessingOvenLight(self, value: bool):
        self.__multiProcessingOvenLight = value

    @property
    def multiProcessingCompressor(self) -> bool:
        return self.__multiProcessingCompressor

    @multiProcessingCompressor.setter
    def multiProcessingCompressor(self, value: bool):
        self.__multiProcessingCompressor = value

    @property
    def multiProcessingValve(self) -> bool:
        return self.__multiProcessingValve

    @multiProcessingValve.setter
    def multiProcessingValve(self, value: bool):
        self.__multiProcessingValve = value

    @property
    def multiProcessingActLowerValve(self) -> bool:
        return self.__multiProcessingActLowerValve

    @multiProcessingActLowerValve.setter
    def multiProcessingActLowerValve(self, value: bool):
        self.__multiProcessingActLowerValve = value

    @property
    def multiProcessingValveOvenDoor(self) -> bool:
        return self.__multiProcessingValveOvenDoor

    @multiProcessingValveOvenDoor.setter
    def multiProcessingValveOvenDoor(self, value: bool):
        self.__multiProcessingValveOvenDoor = value

    @property
    def multiProcessingValveFeeder(self) -> bool:
        return self.__multiProcessingValveFeeder

    @multiProcessingValveFeeder.setter
    def multiProcessingValveFeeder(self, value: bool):
        self.__multiProcessingValveFeeder = value

    def startPosition(self):
        """Starting position is as follows:
        The feeder is out, the oven door is closed, the vacuum gripper is at the turntable.
        Turntable is pointing at the vacuum"""
        self.vacuumToTurntable()
        self.turntableToVacuum()
        self.moveFeederOut()


    def turntableToSaw(self):
        logging.debug("turntableToSaw")
        """Rotates the turntable to the saw.
        Does not rotate if the turntable is at the conveyor."""
        if not self.__multiProcessingSensTurntablePosSaw and not self.__multiProcessingSensTurntablePosConveyor:
            self.__multiProcessingActRotClockwise = True
            self.__multiProcessingActRotCounterclockwise = False
        else:
            self.__multiProcessingActRotClockwise = False

    def turntableToConveyor(self):
        logging.debug("turntableToConveyor")
        """Rotates the turntable to conveyor."""
        if not self.__multiProcessingSensTurntablePosConveyor:
            self.__multiProcessingActRotClockwise = True
            self.__multiProcessingActRotCounterclockwise = False
        else:
            self.__multiProcessingActRotClockwise = False

    def turntableToVacuum(self):
        """Rotates the turntable to the vacuum."""
        if not self.__multiProcessingSensTurntablePosVacuum and not self.toVac:
            self.toVac = True
            self.__multiProcessingActRotCounterclockwise = True
            self.__multiProcessingActRotClockwise = False
        else:
            self.__multiProcessingActRotCounterclockwise = False

    def useSaw(self):
        """Uses the saw on the package. sawCount >= 20 is an artbitrary number."""
        if self.__multiProcessingSensTurntablePosSaw and not self.sawCount >= 20:
            self.__multiProcessingActSaw  = True
            self.sawCount += 1
        else:
            self.__multiProcessingActSaw  = False

    def vacuumToOven(self):
        """Moves the vacuum gripper to the oven."""
        if not self.__multiProcessingSensVacuumSensGripperAtOven:
            self.__multiProcessingActGripperToOven = True
        else:
            self.__multiProcessingActGripperToOven = False
            print("vac to oven act false")

    def vacuumToTurntable(self):
        """Moves the vacuum gripper to the turntable."""
        if not self.__multiProcessingSensVacuumGripperAtTurntable:
            self.__multiProcessingActGripperToTurntable = True
        else:
            self.__multiProcessingActGripperToTurntable = False

    def startOven(self):
        """Brings the feeder inside the oven and initiates the cooking process.
        self.ovenCount >= 30 is an arbitrary number."""
        if not self.ovenReady  and self.__multiProcessingSensVacuumSensGripperAtOven:
            if not self.__multiProcessingSensOvenFeederIn:
                self.__multiProcessingCompressor = True
                self.__multiProcessingValveOvenDoor = True
                self.__multiProcessingActOvenInward = True
            else:
                self.__multiProcessingActOvenInward = False
                self.__multiProcessingCompressor = False
                self.__multiProcessingValveOvenDoor = False

                #haha, flashing lights go brrrr
                if self.ovenCount % 2 == 1:
                    self.__multiProcessingOvenLight = True
                else:
                    self.__multiProcessingOvenLight = False

                self.ovenCount += 1

            if self.ovenCount >= 30:
                self.__multiProcessingOvenLight = False
                self.ovenReady = True
                self.ovenCount = 0

    def endOven(self):
        """Brings the feeder outside to the vacuum gripper along with the readied product.
        Requires the product to have been in the oven before via startOven()."""
        if self.ovenReady:
            self.moveFeederOut()

    def moveFeederIn(self):
        """Moves the feeder inside the oven."""
        if not self.__multiProcessingSensOvenFeederIn:
            self.__multiProcessingCompressor = True
            self.__multiProcessingValveOvenDoor = True
            self.__multiProcessingActOvenInward = True
        else:
            self.__multiProcessingActOvenInward = False
            self.__multiProcessingOvenLight = True
            self.__multiProcessingCompressor = False
            self.__multiProcessingValveOvenDoor = False

    def moveFeederOut(self):
        """Moves the feeder outside of the oven."""
        if not self.__multiProcessingSensOvenFeederOut:
            self.__multiProcessingCompressor = True
            self.__multiProcessingValveOvenDoor = True
            self.__multiProcessingActOvenOutward = True
        else:
            self.__multiProcessingActOvenOutward = False
            self.__multiProcessingCompressor = False
            self.__multiProcessingValveOvenDoor = False

    def gripProduct(self):
        """Takes the product with the vacuum gripper.
        The vacuum gripper should be at oven and the product must have been inside of it.
        It is intended to use this operation with moveProductToTurntable()"""
        if self.__multiProcessingSensOvenFeederOut and self.ovenReady and self.__multiProcessingSensVacuumSensGripperAtOven and self.vacuumCount < 20:
            self.__multiProcessingCompressor = True
            self.__multiProcessingActLowerValve = True
            self.vacuumCount += 1

    def moveProductToTurntable(self):
        """The gripper brings the product to Turntable.
        The time required to grip it is stimulated
        with the self.vacuumCount count. +50"""
        if self.vacuumCount >= 20 and self.vacuumCount < 30:
            self.__multiProcessingValve = True
            self.vacuumCount += 1
        elif self.vacuumCount >= 30 and self.vacuumCount < 50:
            self.__multiProcessingActLowerValve = False
            self.vacuumCount += 1
        elif self.vacuumCount >= 50 and self.vacuumCount < 60:
                self.__multiProcessingCompressor = False
                self.vacuumCount += 1
        elif self.vacuumCount >= 30:
            self.vacuumToTurntable()

    def deliverProduct(self):
        logging.debug("deliverProduct")
        """Brings product from vacuum to saw, uses it and then brings it to the conveyor.
        After it hits the sensor, brings the turntable back to the vacuum.
        Requires the product to have been picked up by the vacuum before.
        sawCount >= x comes from the useSaw() operation.
        Note: this operation does not turn off the compressor."""
        logging.debug('vacuum count ' + str(self.vacuumCount))
        if (self.__multiProcessingSensDelivery and self.vacuumCount >= 60 and self.__multiProcessingSensVacuumGripperAtTurntable and not self.delivered):
            if self.deliveryCount < 30:
                self.__multiProcessingCompressor = True
                self.__multiProcessingActLowerValve = True
                self.deliveryCount += 1
            elif self.deliveryCount < 50 and self.deliveryCount >= 30:
                self.__multiProcessingValve = False
                self.deliveryCount += 1
            elif self.deliveryCount < 70 and self.deliveryCount >= 50:
                self.__multiProcessingCompressor = False
                self.__multiProcessingActLowerValve = False
                self.deliveryCount += 1
            else:
                if self.sawCount == 0:
                    self.turntableToSaw()
                self.useSaw()
                if self.sawCount >= 20:
                    self.turntableToConveyor()
                if self.__multiProcessingSensTurntablePosConveyor:
                    self.delivered = True
                    self.__multiProcessingCompressor = True
                    self.__multiProcessingValveFeeder = True
                    self.__multiProcessingActConveyorForward = True


    def resetStation(self):
        logging.debug("resetStation")
        """Sets all valuables and the ovenReady flag to the starting values."""
        self.sawCount = 0
        self.ovenCount = 0
        self.vacuumCount = 0
        self.deliveryCount = 0
        self.toVac = False
        self.ovenReady = False
        self.delivered = False

    def processProduct(self):
        logging.debug("processProduct")
        """Brings the vacuum over to to the oven. The product goes inside and outside. Then it is brought to the turntable.
        It is operated upon by the saw and finally is brought to the conveyor.
        The machine resets once the product reaches the sensor."""
        self.__isExecutingCount = 0
        if self.__multiProcessingSensDelivery:
            if not self.ovenReady:
                self.vacuumToOven()
            self.startOven()
            self.endOven()
            self.gripProduct()
            self.moveProductToTurntable()
            self.deliverProduct()
        else:
            self.__multiProcessingCompressor = False
            self.__multiProcessingValveFeeder = False
            self.__multiProcessingActConveyorForward = False
            self.turntableToVacuum()
            if self.__multiProcessingSensTurntablePosVacuum:
                #self.resetStation()
                print("noreset")

    #TODO check whether this runs without duration and return lambda setup; wäre analog zu robot und co
    def setup(self, duration: int):
        self.setupFinished = self.__multiProcessingSensOvenFeederOut and self.__multiProcessingSensTurntablePosVacuum and self.__multiProcessingSensVacuumGripperAtTurntable
        self.__isExecutingCount = 0
        self.resetStation()
        self.startPosition()
        self.resetStation()
        self.setupFinished = self.__multiProcessingSensOvenFeederOut and self.__multiProcessingSensTurntablePosVacuum and self.__multiProcessingSensVacuumGripperAtTurntable
        return lambda: self.freeze(duration)

    def freeze(self, duration: int):
        self.__isExecutingCount = 0
        if not self.setupFinished:
            self.setup(duration)
        else:
            self.processProduct()
        if not self.isExecuting:
            self.resetStation()
        return lambda: self.freeze(duration)

    def stop(self):
        pass
