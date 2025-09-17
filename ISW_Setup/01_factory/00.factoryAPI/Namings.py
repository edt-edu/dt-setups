import revpimodio2

self.rpi = revpimodio2.RevPiModIO(autorefresh=True)

"""read functions that set the internal machine parameters according to the RevPi inputs"""
def read1(self):
        self.conveyor11.conveyorSensLeft = self.rpi.io.dio1_I_1.value
        self.conveyor11.conveyorSensRight = self.rpi.io.dio1_I_2.value
        self.conveyor11.conveyorSensImpulse = self.rpi.io.dio1_Counter_3.value

        self.robot12.robotSensGripperOpen = self.rpi.io.dio1_I_5.value
        self.robot12.robotSensGripperImpulseCounterRaw = self.rpi.io.dio1_Counter_6.value
        self.robot12.robotSensArmEndIn = self.rpi.io.dio1_I_7.value
        self.robot12.robotSensArmImpulseCounterRaw = self.rpi.io.dio1_Counter_8.value
        self.robot12.robotSensVerticalEndUp = self.rpi.io.dio1_I_9.value
        self.robot12.robotSensRotEnd = self.rpi.io.dio1_I_10.value
        self.robot12.robotSensVerticalEncoderCounter = self.rpi.io.dio1_Counter_11.value
        self.robot12.robotSensRotEncoderCounter = self.rpi.io.dio1_Counter_13.value

        self.conveyor13.conveyorSensLeft = self.rpi.io.dio2_I_1.value
        self.conveyor13.conveyorSensRight = self.rpi.io.dio2_I_2.value
        self.conveyor13.conveyorSensImpulse = self.rpi.io.dio2_Counter_3.value

        self.sortingLine14.sortingLineSensImpulseCounterRaw = self.rpi.io.dio2_Counter_4.value
        self.sortingLine14.sortingLineSensInputLightBarrier = self.rpi.io.dio2_I_5.value
        self.sortingLine14.sortingLineSensMiddleLightBarrier = self.rpi.io.dio2_I_6.value
        self.sortingLine14.sortingLineSensWhiteLightBarrier = self.rpi.io.dio2_I_7.value
        self.sortingLine14.sortingLineSensRedLightBarrier = self.rpi.io.dio2_I_8.value
        self.sortingLine14.sortingLineSensBlueLightBarrier = self.rpi.io.dio2_I_9.value

        self.warehouse15.warehouseSensHorizontalEnd = self.rpi.io.dio3_I_1.value
        self.warehouse15.warehouseSensLightBarrierIn = self.rpi.io.dio3_I_2.value
        self.warehouse15.warehouseSensLightBarrierOut = self.rpi.io.dio3_I_3.value
        self.warehouse15.warehouseSensVerticalEnd = self.rpi.io.dio3_I_4.value
        self.warehouse15.warehouseSensEncoderHorizontal = self.rpi.io.dio3_Counter_5.value
        #logging.debug('warehouseHorizontal ' + str(self.warehouse15.warehouseSensEncoderHorizontal))
        self.warehouse15.warehouseSensEncoderVertical = self.rpi.io.dio3_Counter_7.value
        #logging.debug('warehouseVertical ' + str(self.warehouse15.warehouseSensEncoderVertical))
        self.warehouse15.warehouseSensArmOut = self.rpi.io.dio3_I_9.value
        self.warehouse15.warehouseSensArmIn = self.rpi.io.dio3_I_10.value

        self.vacuumGripper16.vacuumSensVerticalEndUp = self.rpi.io.dio4_I_1.value
        self.vacuumGripper16.vacuumSensArmEndIn = self.rpi.io.dio4_I_2.value
        self.vacuumGripper16.vacuumSensRotEnd = self.rpi.io.dio4_I_3.value
        self.vacuumGripper16.vacuumSensVerticalEncoderCounter = self.rpi.io.dio4_Counter_5.value
        self.vacuumGripper16.vacuumSensArmEncoderCounter = self.rpi.io.dio4_Counter_7.value
        self.vacuumGripper16.vacuumSensRotEncoderCounter = self.rpi.io.dio4_Counter_9.value

        self.indexedLine17.indexSensPushButton1Front = self.rpi.io.dio4_I_4.value
        self.indexedLine17.indexSensPushButton1Back = self.rpi.io.dio4_I_11.value
        self.indexedLine17.indexSensPushButton2Front = self.rpi.io.dio4_I_12.value
        self.indexedLine17.indexSensPushButton2Back = self.rpi.io.dio4_I_13.value
        self.indexedLine17.indexSensSlider1 = self.rpi.io.dio4_I_14.value
        self.indexedLine17.indexSensMilling = self.rpi.io.dio5_I_1.value
        self.indexedLine17.indexSensLoading = self.rpi.io.dio5_I_2.value
        self.indexedLine17.indexSensDrilling = self.rpi.io.dio5_I_3.value
        self.indexedLine17.indexSensConveyorSwap = self.rpi.io.dio5_I_4.value

        self.robot18.robotSensGripperOpen = self.rpi.io.dio5_I_5.value
        self.robot18.robotSensGripperImpulseCounterRaw = self.rpi.io.dio5_Counter_6.value
        self.robot18.robotSensArmEndIn = self.rpi.io.dio5_I_7.value
        self.robot18.robotSensArmImpulseCounterRaw = self.rpi.io.dio5_Counter_8.value
        self.robot18.robotSensVerticalEndUp = self.rpi.io.dio5_I_9.value
        self.robot18.robotSensRotEnd = self.rpi.io.dio5_I_10.value
        self.robot18.robotSensVerticalEncoderCounter = self.rpi.io.dio5_Counter_11.value
        self.robot18.robotSensRotEncoderCounter = self.rpi.io.dio5_Counter_13.value

def write(self):
        self.rpi.io.dio1_O_1.value = self.conveyor11.conveyorActForward
        self.rpi.io.dio1_O_2.value = self.conveyor11.conveyorActBackward

        self.rpi.io.dio1_O_3.value = self.robot12.robotActGripperOpen
        self.rpi.io.dio1_O_4.value = self.robot12.robotActGripperClose
        self.rpi.io.dio1_O_5.value = self.robot12.robotActArmOut
        self.rpi.io.dio1_O_6.value = self.robot12.robotActArmIn
        self.rpi.io.dio1_O_7.value = self.robot12.robotActVerticalDown
        self.rpi.io.dio1_O_8.value = self.robot12.robotActVerticalUp
        self.rpi.io.dio1_O_9.value = self.robot12.robotActRotRight
        self.rpi.io.dio1_O_10.value = self.robot12.robotActRotLeft

        self.rpi.io.dio2_O_1.value = self.conveyor13.conveyorActForward
        self.rpi.io.dio2_O_2.value = self.conveyor13.conveyorActBackward

        self.rpi.io.dio2_O_3.value = self.sortingLine14.sortingLineActMotorConveyor
        self.rpi.io.dio2_O_4.value = self.sortingLine14.sortingLineActCompressorOn
        self.rpi.io.dio2_O_5.value = self.sortingLine14.sortingLineActWhiteEjector
        self.rpi.io.dio2_O_6.value = self.sortingLine14.sortingLineActRedEjector
        self.rpi.io.dio2_O_7.value = self.sortingLine14.sortingLineActBlueEjector

        self.rpi.io.dio3_O_1.value = self.warehouse15.warehouseActConveyorOut
        self.rpi.io.dio3_O_2.value = self.warehouse15.warehouseActConveyorIn
        self.rpi.io.dio3_O_3.value = self.warehouse15.warehouseActHorizontalToRack
        self.rpi.io.dio3_O_4.value = self.warehouse15.warehouseActHorizontalToConveyor
        self.rpi.io.dio3_O_5.value = self.warehouse15.warehouseActVerticalDown
        self.rpi.io.dio3_O_6.value = self.warehouse15.warehouseActVerticalUp
        self.rpi.io.dio3_O_7.value = self.warehouse15.warehouseActArmOut
        self.rpi.io.dio3_O_8.value = self.warehouse15.warehouseActArmIn

        self.rpi.io.dio4_O_1.value = self.vacuumGripper16.vacuumActVerticalUp
        self.rpi.io.dio4_O_2.value = self.vacuumGripper16.vacuumActVerticalDown
        self.rpi.io.dio4_O_3.value = self.vacuumGripper16.vacuumActArmIn
        self.rpi.io.dio4_O_4.value = self.vacuumGripper16.vacuumActArmOut
        self.rpi.io.dio4_O_5.value = self.vacuumGripper16.vacuumActRotRight
        self.rpi.io.dio4_O_6.value = self.vacuumGripper16.vacuumActRotLeft
        self.rpi.io.dio4_O_7.value = self.vacuumGripper16.vacuumActCompressorOn
        self.rpi.io.dio4_O_8.value = self.vacuumGripper16.vacuumActValve

        self.rpi.io.dio4_O_10.value = self.indexedLine17.motorSlider1Backward
        self.rpi.io.dio4_O_9.value = self.indexedLine17.motorSlider1Forward
        self.rpi.io.dio4_O_12.value = self.indexedLine17.motorSlider2Backward
        self.rpi.io.dio4_O_11.value = self.indexedLine17.motorSlider2Forward
        self.rpi.io.dio4_O_13.value = self.indexedLine17.conveyorBeltFeed
        self.rpi.io.dio4_O_14.value = self.indexedLine17.conveyorBeltMilling
        self.rpi.io.dio5_O_11.value = self.indexedLine17.millingMachine
        self.rpi.io.dio5_O_12.value = self.indexedLine17.conveyorBeltDrilling
        self.rpi.io.dio5_O_13.value = self.indexedLine17.drillingMachine
        self.rpi.io.dio5_O_14.value = self.indexedLine17.conveyorBeltSwap

        self.rpi.io.dio5_O_3.value = self.robot18.robotActGripperOpen
        self.rpi.io.dio5_O_4.value = self.robot18.robotActGripperClose
        self.rpi.io.dio5_O_5.value = self.robot18.robotActArmOut
        self.rpi.io.dio5_O_6.value = self.robot18.robotActArmIn
        self.rpi.io.dio5_O_7.value = self.robot18.robotActVerticalDown
        self.rpi.io.dio5_O_8.value = self.robot18.robotActVerticalUp
        self.rpi.io.dio5_O_9.value = self.robot18.robotActRotRight
        self.rpi.io.dio5_O_10.value = self.robot18.robotActRotLeft
