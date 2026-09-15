import logging

from MovingMachine import MovingMachine
from WarehouseConfig import WarehouseConfig
from Axis import AxisType, Axis
from BoxNumber import BoxNumber
from Position import Position
from RequestedParameter import RequestedParameter


class Warehouse(MovingMachine):

    def generateTransferMoveList(self, numPickup: int, numPlace: int):
        offset = 50
        # (sicherheitshalber) Arm einfahren
        warehousePickConf0 = WarehouseConfig(self.warehouseSensEncoderVertical, self.warehouseSensEncoderHorizontal,True,False,False,False)
        # mit eingefahrenem Arm in Ladepos
        warehousePickConf1 = WarehouseConfig(self.placeList[numPickup][0]+offset, self.placeList[numPickup][1],True,False,False,False)
        # Arm ausfahren
        warehousePickConf2 = WarehouseConfig(self.placeList[numPickup][0]+offset, self.placeList[numPickup][1],False,True,False,False)
        # Förderband aktivieren
        if numPickup == 0:
            warehousePickConf3 = WarehouseConfig(self.placeList[numPickup][0]+offset, self.placeList[numPickup][1],False,True,True,False)
        else:
            warehousePickConf3 = warehousePickConf2
        # anheben
        warehousePickConf4 = WarehouseConfig(self.placeList[numPickup][0]-offset, self.placeList[numPickup][1],False,True,False,False)
        # Arm einfahren
        warehousePickConf5 = WarehouseConfig(self.placeList[numPickup][0]-offset, self.placeList[numPickup][1],True,False,False,False)
        # move
        warehousePlaceConf0 = WarehouseConfig(self.placeList[numPlace][0]-offset, self.placeList[numPlace][1],True,False,False,False)
        # Arm ausfahren
        warehousePlaceConf1 = WarehouseConfig(self.placeList[numPlace][0]-offset, self.placeList[numPlace][1],False,True,False,False)
        # absenken
        warehousePlaceConf2 = WarehouseConfig(self.placeList[numPlace][0]+offset, self.placeList[numPlace][1],False,True,False,False)
        #Förderband aktivieren
        if numPlace == 0:
            warehousePlaceConf3 = WarehouseConfig(self.placeList[numPlace][0]+offset, self.placeList[numPlace][1],False,True,False,True)
        else:
            warehousePlaceConf3 = warehousePlaceConf2
        # Arm einfahren
        warehousePlaceConf4 = WarehouseConfig(self.placeList[numPlace][0]+offset, self.placeList[numPlace][1],True,False,False,False)

        return [warehousePickConf0,
                warehousePickConf1,
                warehousePickConf2,
                warehousePickConf3,
                warehousePickConf4,
                warehousePickConf5,
                warehousePlaceConf0,
                warehousePlaceConf1,
                warehousePlaceConf2,
                warehousePlaceConf3,
                warehousePlaceConf4]

    @property
    def isExecuting(self) -> bool:
        if (self.__warehouseActConveyorIn or self.__warehouseActConveyorOut or self.__warehouseActHorizontalToRack or self.__warehouseActHorizontalToConveyor or self.__warehouseActVerticalUp or self.__warehouseActVerticalDown or self.__warehouseActArmIn or self.__warehouseActArmOut):
            return True
        else:
            if self.__isExecutingCount < 15:
                self.__isExecutingCount += 1
                return True
            else:
                return False

    def __init__(self, id1):
        self.__isExecutingCount = 0
        self.__warehouseSensHorizontalEnd = self.__warehouseSensLightBarrierIn = self.__warehouseSensLightBarrierOut = self.__warehouseSensVerticalEnd = self.__warehouseSensArmIn = self.__warehouseSensArmOut = False
        self.__warehouseSensEncoderHorizontal = self.__warehouseSensEncoderVertical = 0
        self.__warehouseActConveyorIn = self.__warehouseActConveyorOut = self.__warehouseActHorizontalToRack = self.__warehouseActHorizontalToConveyor = self.__warehouseActVerticalUp = self.__warehouseActVerticalDown = self.__warehouseActArmIn = self.__warehouseActArmOut = False
        self.__axisVertical = Axis(AxisType.Encoder, 13)
        self.__axisHorizontal = Axis(AxisType.Encoder, 13)
        dictMap = {RequestedParameter.REFERENCESWITCHHORIZONTALAXIS: self.__warehouseSensHorizontalEnd,
                   RequestedParameter.LIGHTBARRIERINSIDE: self.__warehouseSensLightBarrierIn,
                   RequestedParameter.LIGHTBARRIEROUTSIDE: self.__warehouseSensLightBarrierOut,
                   RequestedParameter.REFERENCESWITCHVERTICALAXIS: self.__warehouseSensVerticalEnd,
                   RequestedParameter.HORIZONTALAXISSTEP: self.__axisHorizontal.counterValueCurrent,
                   RequestedParameter.VERTICALAXISSTEP: self.__axisVertical.counterValueCurrent,
                   RequestedParameter.REFERENCESWITCHCANTILEVERFRONT: self.__warehouseSensArmOut,
                   RequestedParameter.REFERENCESWITCHCANTILEVERBACK: self.__warehouseSensArmIn,
                   RequestedParameter.MOTORCONVEYORBELTFORWARD: self.__warehouseActConveyorOut,
                   RequestedParameter.MOTORCONVEYORBELTBACKWARD: self.__warehouseActConveyorIn,
                   RequestedParameter.MOTORVERTICALAXISDOWNWARD: self.__warehouseActVerticalDown,
                   RequestedParameter.MOTORVERTICALAXISUPWARD: self.__warehouseActVerticalUp,
                   RequestedParameter.MOTORCANTILEVERFORWARD: self.__warehouseActArmOut,
                   RequestedParameter.MOTORCANTILEVERBACKWARD: self.__warehouseActArmIn}
        conveyor = [1380, 40]
        box1 = [150, 1460]
        box2 = [150, 2620]
        box3 = [150, 3780]
        box4 = [850, 1460]
        box5 = [850, 2620]
        box6 = [850, 3780]
        box7 = [1630, 1460]
        box8 = [1630, 2620]
        box9 = [1630, 3780]
        super().__init__(id1, dictMap)
        self.placeList = [conveyor,box1,box2,box3,box4,box5,box6,box7,box8,box9]



    @property
    def warehouseSensHorizontalEnd(self) -> bool:
        return self.__warehouseSensHorizontalEnd

    @warehouseSensHorizontalEnd.setter
    def warehouseSensHorizontalEnd(self, value: bool):
        self.__warehouseSensHorizontalEnd = value

    @property
    def warehouseSensLightBarrierIn(self):
        return self.__warehouseSensLightBarrierIn

    @warehouseSensLightBarrierIn.setter
    def warehouseSensLightBarrierIn(self, value):
        self.__warehouseSensLightBarrierIn = value

    @property
    def warehouseSensLightBarrierOut(self):
        return self.__warehouseSensLightBarrierOut

    @warehouseSensLightBarrierOut.setter
    def warehouseSensLightBarrierOut(self, value):
        self.__warehouseSensLightBarrierOut = value

    @property
    def warehouseSensVerticalEnd(self):
        return self.__warehouseSensVerticalEnd

    @warehouseSensVerticalEnd.setter
    def warehouseSensVerticalEnd(self, value):
        self.__warehouseSensVerticalEnd = value

    @property
    def warehouseSensArmIn(self):
        return self.__warehouseSensArmIn

    @warehouseSensArmIn.setter
    def warehouseSensArmIn(self, value):
        self.__warehouseSensArmIn = value

    @property
    def warehouseSensArmOut(self):
        return self.__warehouseSensArmOut

    @warehouseSensArmOut.setter
    def warehouseSensArmOut(self, value):
        self.__warehouseSensArmOut = value

    @property
    def warehouseSensEncoderHorizontal(self):
        return self.__warehouseSensEncoderHorizontal

    @warehouseSensEncoderHorizontal.setter
    def warehouseSensEncoderHorizontal(self, value):
        self.__warehouseSensEncoderHorizontal = value

    @property
    def warehouseSensEncoderVertical(self):
        return self.__warehouseSensEncoderVertical

    @warehouseSensEncoderVertical.setter
    def warehouseSensEncoderVertical(self, value):
        self.__warehouseSensEncoderVertical = value

    @property
    def warehouseActConveyorIn(self):
        return self.__warehouseActConveyorIn

    @warehouseActConveyorIn.setter
    def warehouseActConveyorIn(self, value):
        self.__warehouseActConveyorIn = value

    @property
    def warehouseActConveyorOut(self):
        return self.__warehouseActConveyorOut

    @warehouseActConveyorOut.setter
    def warehouseActConveyorOut(self, value):
        self.__warehouseActConveyorOut = value

    @property
    def warehouseActHorizontalToRack(self):
        return self.__warehouseActHorizontalToRack

    @warehouseActHorizontalToRack.setter
    def warehouseActHorizontalToRack(self, value):
        self.__warehouseActHorizontalToRack = value

    @property
    def warehouseActHorizontalToConveyor(self):
        return self.__warehouseActHorizontalToConveyor

    @warehouseActHorizontalToConveyor.setter
    def warehouseActHorizontalToConveyor(self, value):
        self.__warehouseActHorizontalToConveyor = value

    @property
    def warehouseActVerticalDown(self):
        return self.__warehouseActVerticalDown

    @warehouseActVerticalDown.setter
    def warehouseActVerticalDown(self, value):
        self.__warehouseActVerticalDown = value

    @property
    def warehouseActVerticalUp(self):
        return self.__warehouseActVerticalUp

    @warehouseActVerticalUp.setter
    def warehouseActVerticalUp(self, value):
        self.__warehouseActVerticalUp = value

    @property
    def warehouseActArmIn(self):
        return self.__warehouseActArmIn

    @warehouseActArmIn.setter
    def warehouseActArmIn(self, value):
        self.__warehouseActArmIn = value

    @property
    def warehouseActArmOut(self):
        return self.__warehouseActArmOut

    @warehouseActArmOut.setter
    def warehouseActArmOut(self, value):
        self.__warehouseActArmOut = value

    #TODO execute-Logik

    #TODO speichermechanismus für Belegung des Regals

    def gotoconfig(self, config):
        iconfig = config
        if config is None:
            iconfig = WarehouseConfig(0, 0, True)
        t1 = t2 = t3 = t4 = False
        self.__axisVertical.update(self.warehouseSensVerticalEnd, self.warehouseSensEncoderVertical)
        t1 = self.__axisVertical.gotoConfig(iconfig.verticalEnd, iconfig.counterVertical)
        self.warehouseActVerticalUp = self.__axisVertical.outputminus
        self.warehouseActVerticalDown = self.__axisVertical.outputplus
        self.__axisHorizontal.update(self.warehouseSensHorizontalEnd, self.warehouseSensEncoderHorizontal)
        t2 = self.__axisHorizontal.gotoConfig(iconfig.horizontalEnd, iconfig.counterHorizontal)
        self.warehouseActHorizontalToConveyor = self.__axisHorizontal.outputminus
        self.warehouseActHorizontalToRack = self.__axisHorizontal.outputplus

        if iconfig.armEndIn:
            if not self.warehouseSensArmIn:
                self.warehouseActArmIn = True
            else:
                self.warehouseActArmIn = False
                t3 = True
        if iconfig.armEndOut:
            if not self.warehouseSensArmOut:
                self.warehouseActArmOut = True
            else:
                self.warehouseActArmOut = False
                t3 = True
        if iconfig.conveyorIn:
            self.warehouseActConveyorIn = True
            if not self.warehouseSensLightBarrierIn:
                t4 = True
        else:
            self.warehouseActConveyorIn = False
        if iconfig.conveyorOut:
            self.warehouseActConveyorOut = True
            if not self.warehouseSensLightBarrierOut:
                t4 = True
        else:
            self.warehouseActConveyorOut = False
        if not iconfig.conveyorOut and not iconfig.conveyorIn:
            t4 = True
        return t1 and t2 and t3 and t4

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

    @staticmethod
    def boxnrtoint(boxnr: BoxNumber) -> int:
        if boxnr == BoxNumber.BOX1:
            place = 1
        elif boxnr == BoxNumber.BOX2:
            place = 2
        elif boxnr == BoxNumber.BOX3:
            place = 3
        elif boxnr == BoxNumber.BOX4:
            place = 4
        elif boxnr == BoxNumber.BOX5:
            place = 5
        elif boxnr == BoxNumber.BOX6:
            place = 6
        elif boxnr == BoxNumber.BOX7:
            place = 7
        elif boxnr == BoxNumber.BOX8:
            place = 8
        elif boxnr == BoxNumber.BOX9:
            place = 9
        return place

    def setup(self):
        t1 = t2 = t3 = False
        if self.warehouseSensArmIn:
            self.warehouseActArmIn = False
            t1 = True
        else:
            self.warehouseActArmIn = True

        if self.warehouseSensVerticalEnd:
            self.warehouseActVerticalUp = False
            t2 = True
        elif t1: #only move after arm is in secure position
            self.warehouseActVerticalUp = True

        if self.warehouseSensHorizontalEnd:
            self.warehouseActHorizontalToConveyor = False
            t3 = True
        elif t1:
            self.warehouseActHorizontalToConveyor = True
        self.setupFinished = (t1 and t2 and t3)
        if not self.setupFinished:
            self.setupFirst = True
        else:
            self.setupFinishedHelper = True
        if self.setupFirst:
            logging.debug("setup first True")
            self.setupFirst = False
        logging.debug('setup finished ' + str(self.setupFinished))
        return lambda: self.setup()

    def store(self, boxnr):
        self.setupFirst = True
        print("store warehouse")
        place = self.boxnrtoint(boxnr)
        moveList = self.generateTransferMoveList(0, place)
        placePos = Position("END", self.placeList[place][0], 0, self.placeList[place][1])
        return lambda: self.execute(placePos, placePos, moveList)

    def get(self, boxnr):
        self.setupFirst = True
        print("pick warehouse")
        pick = self.boxnrtoint(boxnr)
        moveList = self.generateTransferMoveList(pick, 0)
        #self.execute(pick, pick, moveList)
        pickPos = Position("START", self.placeList[pick][0], 0, self.placeList[pick][1])
        return lambda: self.execute(pickPos, pickPos, moveList)

    def inout(self, boxnrin, boxnrout):
        self.setupFirst = True
        print("inout warehouse")
        place = self.boxnrtoint(boxnrin)
        pick = self.boxnrtoint(boxnrout)
        moveListIn = self.generateTransferMoveList(0, place)
        moveListOut = self.generateTransferMoveList(pick, 0)
        moveList = moveListIn
        moveList.extend(moveListOut)
        print(moveList)
        placePos = Position("END", self.placeList[place][0], 0, self.placeList[place][1])
        pickPos = Position("START", self.placeList[pick][0], 0, self.placeList[pick][1])
        return lambda: self.execute(placePos, pickPos, moveList)

    def stop(self):
        self.__warehouseActConveyorIn = self.__warehouseActConveyorOut = self.__warehouseActHorizontalToRack = self.__warehouseActHorizontalToConveyor = self.__warehouseActVerticalUp = self.__warehouseActVerticalDown = self.__warehouseActArmIn = self.__warehouseActArmOut = False
        return None
