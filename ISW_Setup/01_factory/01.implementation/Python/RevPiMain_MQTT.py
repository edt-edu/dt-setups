#!/usr/bin/env python
import multiprocessing
import threading
from multiprocessing import Process
from multiprocessing import Event
from multiprocessing import Queue
import socket
import revpimodio2
import logging
#import queue
from queue import Empty
import time

from RevPiNumber import RevPiNumber
from ExecutionStatus import ExecutionStatus
from Direction import Direction

from MqttPublisher import MqttPublisher
from MqttSubscriber import MqttSubscriber

from JSONOutput import JSONOutput
from JSONReader import JSONReader
from JSONParser import JSONParser
from MachineCommandFeedback import MachineCommandFeedback
from MachineStatusRequestAnswer import MachineStatusRequestAnswer

from Conveyor import Conveyor
from Robot import Robot
from SortingLine import SortingLine
from Warehouse import Warehouse
from VacuumGripper import VacuumGripper
from IndexedLine import IndexedLine
from MultiProcessing import MultiProcessing
from PunchingMachine import PunchingMachine

# change this RevPiNumber to make this file suitable for the RevPi it is running on
coreNumber: RevPiNumber = RevPiNumber.CORE2
BROKERIP: str = "192.168.1.104" #erwartete IP des Steuerungspcs
#TODO für jeden Socket einen eigenen Port nutzen und hier sinnvoll verwalten
PORT = 6000
PORT_BASE = 6000


#tests for "this" class can be found in JSONProcessingIntegrationMain(Test)
class RevPiMain:
    logging.basicConfig(format='%(levelname)s: %(module)s,%(lineno)s: %(message)s', level=logging.DEBUG)
    logging.debug('debug level messages displayed')
    logging.info('info level messages displayed')
    logging.warning('warning level messages displayed')
    logging.error('error level messages displayed')

    def __init__(self):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """
        logging.debug('init started')
        # Instantiate RevPiModIO
        self.rpi = revpimodio2.RevPiModIO(autorefresh=True)
        self.rpi.handlesignalend(self.cleanup_revpi)
        #the list of all machines that are connected to this core
        self.machines = []
        #dict, which keys are the machines, than there is a tuple holding the function currently executed ([0]) and the id it was sent with ([1])
        self.currentlyExecuting = {}
        #dict, which keys are the machines, feedback as the values
        self.feedback = {}
        # init a buffer to store all incoming/outgoing messages, received in a different thread than the one executing the revpi functions
        # stored as python objects, so parse/deserialize before putting into buffer
        self.inputBuffer = multiprocessing.Queue()
        self.outputBuffer = multiprocessing.Queue()

        """Erstellung der spezifischen Maschinenobjekte für den jeweiligen CORE"""
        if coreNumber == RevPiNumber.CORE1:
            logging.debug('CORE1')
            self.conveyor11 = Conveyor("1.1-Conv")
            self.robot12 = Robot("1.2-Grip")
            self.conveyor13 = Conveyor("1.3-Conv")
            self.sortingLine14 = SortingLine("1.4-Sort")
            self.warehouse15 = Warehouse("1.5-Store")
            self.vacuumGripper16 = VacuumGripper("1.6-Vac")
            self.indexedLine17 = IndexedLine("1.7-Indexed")
            self.robot18 = Robot("1.8-Grip")
            self.machines = [self.conveyor11, self.robot12, self.conveyor13, self.sortingLine14, self.warehouse15,
                             self.vacuumGripper16, self.indexedLine17, self.robot18]
            self.currentlyExecuting = {
                self.conveyor11: [None, None],
                self.robot12: [None, None],
                self.conveyor13: [None, None],
                self.sortingLine14: [None, None],
                self.warehouse15: [None, None],
                self.vacuumGripper16: [None, None],
                self.indexedLine17: [None, None],
                self.robot18: [None, None]
            }
            self.feedback = {
                self.conveyor11: None,
                self.robot12: None,
                self.conveyor13: None,
                self.sortingLine14: None,
                self.warehouse15: None,
                self.vacuumGripper16: None,
                self.indexedLine17: None,
                self.robot18: None
            }
            self.publisher = MqttPublisher(BROKERIP, PORT, 1, "publish/isle1", "Isle1Publisher")
            self.subscriber = MqttSubscriber(BROKERIP, PORT, "subscribe/isle1", "Isle1Subscriber")

            self.startCore1()
        elif coreNumber == RevPiNumber.CORE2:
            logging.debug('CORE2')
            self.robot21 = Robot("2.1-Grip")
            self.robot22 = Robot("2.2-Grip")
            self.conveyor23 = Conveyor("2.3-Conv")
            self.conveyor24 = Conveyor("2.4-Conv")
            self.vacuumGripper25 = VacuumGripper("2.5-Vac")
            self.multiprocessing26 = MultiProcessing("2.6-Freeze")
            self.machines = [self.robot21, self.robot22, self.conveyor23, self.conveyor24, self.vacuumGripper25,
                             self.multiprocessing26]
            self.currentlyExecuting = {
                self.robot21: [None, None],
                self.robot22: [None, None],
                self.conveyor23: [None, None],
                self.conveyor24: [None, None],
                self.vacuumGripper25: [None, None],
                self.multiprocessing26: [None, None]
            }
            self.feedback = {
                self.robot21: None,
                self.robot22: None,
                self.conveyor23: None,
                self.conveyor24: None,
                self.vacuumGripper25: None,
                self.multiprocessing26: None
            }
            self.publisher = MqttPublisher(BROKERIP, PORT, 1, "publish/isle2", "Isle2Publisher")
            self.subscriber = MqttSubscriber(BROKERIP, PORT, "subscribe/isle2", "Isle2Subscriber")

            self.startCore2()
        elif coreNumber == RevPiNumber.CORE3:
            logging.debug('CORE3')
            self.vacuumGripper31 = VacuumGripper("3.1-Vac")
            self.robot32 = Robot("3.2-Grip")
            self.punchingMachine33 = PunchingMachine("3.3-Press")
            self.conveyor34 = Conveyor("3.4-Conv")
            self.vacuumGripper35 = VacuumGripper("3.5-Vac")
            self.robot36 = Robot("3.6-Grip")
            self.punchingMachine37 = PunchingMachine("3.7-Press")
            self.conveyor38 = Conveyor("3.8-Conv")
            self.warehouse39 = Warehouse("3.9-Store")
            self.vacuumGripper310 = VacuumGripper("3.10-Vac")
            self.warehouse311 = Warehouse("3.11-Store")
            self.machines = [self.vacuumGripper31, self.robot32, self.punchingMachine33, self.conveyor34,
                             self.vacuumGripper35, self.robot36, self.punchingMachine37, self.conveyor38,
                             self.warehouse39, self.vacuumGripper310, self.warehouse311]
            self.currentlyExecuting = {
                self.vacuumGripper31: [None, None],
                self.robot32: [None, None],
                self.punchingMachine33: [None, None],
                self.conveyor34: [None, None],
                self.vacuumGripper35: [None, None],
                self.robot36: [None, None],
                self.punchingMachine37: [None, None],
                self.conveyor38: [None, None],
                self.warehouse39: [None, None],
                self.vacuumGripper310: [None, None],
                self.warehouse311: [None, None]
            }
            self.feedback = {
                self.vacuumGripper31: None,
                self.robot32: None,
                self.punchingMachine33: None,
                self.conveyor34: None,
                self.vacuumGripper35: None,
                self.robot36: None,
                self.punchingMachine37: None,
                self.conveyor38: None,
                self.warehouse39: None,
                self.vacuumGripper310: None,
                self.warehouse311: None
            }
            self.publisher = MqttPublisher(BROKERIP, PORT, 1, "publish/isle3", "Isle3Publisher")
            self.subscriber = MqttSubscriber(BROKERIP, PORT, "subscribe/isle3", "Isle3Subscriber")

            self.startCore3()
        elif coreNumber == RevPiNumber.CORE4:
            logging.debug('CORE4')
            self.robot41 = Robot("4.1-Grip")
            self.conveyor42 = Conveyor("4.2-Conv")
            self.conveyor43 = Conveyor("4.3-Conv")
            self.vacuumGripper44 = VacuumGripper("4.4-Vac")
            self.warehouse45 = Warehouse("4.5-Store")
            self.machines = [self.robot41, self.conveyor42, self.conveyor43, self.vacuumGripper44, self.warehouse45]
            self.currentlyExecuting = {
                self.robot41: [None, None],
                self.conveyor42: [None, None],
                self.conveyor43: [None, None],
                self.vacuumGripper44: [None, None],
                self.warehouse45: [None, None]
            }
            self.feedback = {
                self.robot41: ExecutionStatus.FINISHED,
                self.conveyor42: ExecutionStatus.FINISHED,
                self.conveyor43: ExecutionStatus.FINISHED,
                self.vacuumGripper44: ExecutionStatus.FINISHED,
                self.warehouse45: ExecutionStatus.FINISHED
            }
            self.publisher = MqttPublisher(BROKERIP, PORT, 1, "publish/isle4", "Isle4Publisher")
            self.subscriber = MqttSubscriber(BROKERIP, PORT, "subscribe/isle4", "Isle4Subscriber")

            self.startCore4()


    """start functions, that start the server and revpi execution threads with the correct helper functions"""
    def startCore1(self):
        # Start event system without blocking here
        logging.debug('Starting Subscriber ...')
        self.rpi.mainloop(blocking=False)
        self.subscriber.run()
        logging.debug('Subscriber started!')
        while not self.rpi.exitsignal.wait(0.03):
            if not self.subscriber.get_is_empty():
                self.inputBuffer.put(JSONReader.read(self.subscriber.get_oldest_message()))
            print(self.inputBuffer.empty())
            self.processJson(self.inputBuffer)
            self.read1()
            self.exLoop()
            self.write1()
            self.reset1()
            self.createFeedbackOnChange()
            if not self.outputBuffer.empty():
                self.publisher.run(JSONParser.parse(self.outputBuffer.get()) + "\n")


    def startCore2(self):
        # Start event system without blocking here
        logging.debug('Starting Subscriber ...')
        self.rpi.mainloop(blocking=False)
        self.subscriber.run()
        logging.debug('Subscriber started!')
        while not self.rpi.exitsignal.wait(0.03):
            if not self.subscriber.get_is_empty():
                self.inputBuffer.put(JSONReader.read(self.subscriber.get_oldest_message()))
            self.processJson(self.inputBuffer)
            self.read2()
            self.exLoop()
            self.write2()
            self.reset2()
            self.createFeedbackOnChange()
            if not self.outputBuffer.empty():
                self.publisher.run(JSONParser.parse(self.outputBuffer.get()) + "\n")


    def startCore3(self):
        # Start event system without blocking here
        logging.debug('Starting Subscriber ...')
        self.rpi.mainloop(blocking=False)
        self.subscriber.run()
        logging.debug('Subscriber started!')
        while not self.rpi.exitsignal.wait(0.03):
            if not self.subscriber.get_is_empty():
                self.inputBuffer.put(JSONReader.read(self.subscriber.get_oldest_message()))
            print(self.inputBuffer.empty())
            self.processJson(self.inputBuffer)
            self.read3()
            self.exLoop()
            self.write3()
            self.reset3()
            self.createFeedbackOnChange()
            if not self.outputBuffer.empty():
                self.publisher.run(JSONParser.parse(self.outputBuffer.get()) + "\n")


    def startCore4(self):
        # Start event system without blocking here
        logging.debug('Starting Subscriber ...')
        self.rpi.mainloop(blocking=False)
        self.subscriber.run()
        logging.debug('Subscriber started!')
        while not self.rpi.exitsignal.wait(0.03):
            if not self.subscriber.get_is_empty():
                self.inputBuffer.put(JSONReader.read(self.subscriber.get_oldest_message()))
            self.processJson(self.inputBuffer)
            self.read4()
            self.exLoop()
            self.write4()
            self.reset4()
            self.createFeedbackOnChange()
            if not self.outputBuffer.empty():
                self.publisher.run(JSONParser.parse(self.outputBuffer.get()) + "\n")


#todo auslagern in config file
#beware of copy paste errors!11!! und links rechts problemen

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

    def read2(self):
        self.robot21.robotSensGripperOpen = self.rpi.io.dio1_I_5.value
        self.robot21.robotSensGripperImpulseCounterRaw = self.rpi.io.dio1_Counter_6.value
        self.robot21.robotSensArmEndIn = self.rpi.io.dio1_I_7.value
        self.robot21.robotSensArmImpulseCounterRaw = self.rpi.io.dio1_Counter_8.value
        self.robot21.robotSensVerticalEndUp = self.rpi.io.dio1_I_9.value
        self.robot21.robotSensRotEnd = self.rpi.io.dio1_I_10.value
        self.robot21.robotSensVerticalEncoderCounter = self.rpi.io.dio1_Counter_11.value
        self.robot21.robotSensRotEncoderCounter = self.rpi.io.dio1_Counter_13.value

        self.robot22.robotSensGripperOpen = self.rpi.io.dio2_I_5.value
        self.robot22.robotSensGripperImpulseCounterRaw = self.rpi.io.dio2_Counter_6.value
        self.robot22.robotSensArmEndIn = self.rpi.io.dio2_I_7.value
        self.robot22.robotSensArmImpulseCounterRaw = self.rpi.io.dio2_Counter_8.value
        self.robot22.robotSensVerticalEndUp = self.rpi.io.dio2_I_9.value
        self.robot22.robotSensRotEnd = self.rpi.io.dio2_I_10.value
        self.robot22.robotSensVerticalEncoderCounter = self.rpi.io.dio2_Counter_11.value
        self.robot22.robotSensRotEncoderCounter = self.rpi.io.dio2_Counter_13.value

        self.conveyor23.conveyorSensLeft = self.rpi.io.dio1_I_1.value
        self.conveyor23.conveyorSensRight = self.rpi.io.dio1_I_2.value
        self.conveyor23.conveyorSensImpulse = self.rpi.io.dio1_Counter_3.value

        self.conveyor24.conveyorSensLeft = self.rpi.io.dio2_I_1.value
        self.conveyor24.conveyorSensRight = self.rpi.io.dio2_I_2.value
        self.conveyor24.conveyorSensImpulse = self.rpi.io.dio2_Counter_3.value

        self.vacuumGripper25.vacuumSensVerticalEndUp = self.rpi.io.dio3_I_1.value
        self.vacuumGripper25.vacuumSensArmEndIn = self.rpi.io.dio3_I_2.value
        self.vacuumGripper25.vacuumSensRotEnd = self.rpi.io.dio3_I_3.value
        self.vacuumGripper25.vacuumSensVerticalEncoderCounter = self.rpi.io.dio3_Counter_5.value
        self.vacuumGripper25.vacuumSensArmEncoderCounter = self.rpi.io.dio3_Counter_7.value
        self.vacuumGripper25.vacuumSensRotEncoderCounter = self.rpi.io.dio3_Counter_9.value

        self.multiprocessing26.multiProcessingSensTurntablePosVacuum = self.rpi.io.dio4_I_1.value
        self.multiprocessing26.multiProcessingSensTurntablePosConveyor = self.rpi.io.dio4_I_2.value
        self.multiprocessing26.multiProcessingSensDelivery = self.rpi.io.dio4_I_3.value
        self.multiprocessing26.multiProcessingSensTurntablePosSaw = self.rpi.io.dio4_I_4.value
        self.multiprocessing26.multiProcessingSensVacuumGripperAtTurntable = self.rpi.io.dio4_I_5.value
        self.multiprocessing26.multiProcessingSensOvenFeederIn = self.rpi.io.dio4_I_6.value
        self.multiprocessing26.multiProcessingSensOvenFeederOut = self.rpi.io.dio4_I_7.value
        self.multiprocessing26.multiProcessingSensVacuumGripperAtOven = self.rpi.io.dio4_I_8.value
        self.multiprocessing26.multiProcessingSensOven = self.rpi.io.dio4_I_9.value

    def read3(self):
        self.vacuumGripper31.vacuumSensVerticalEndUp = self.rpi.io.dio1_I_1.value
        self.vacuumGripper31.vacuumSensArmEndIn = self.rpi.io.dio1_I_2.value
        self.vacuumGripper31.vacuumSensRotEnd = self.rpi.io.dio1_I_3.value
        self.vacuumGripper31.vacuumSensVerticalEncoderCounter = self.rpi.io.dio1_Counter_5.value
        self.vacuumGripper31.vacuumSensArmEncoderCounter = self.rpi.io.dio1_Counter_7.value
        self.vacuumGripper31.vacuumSensRotEncoderCounter = self.rpi.io.dio1_Counter_9.value
        logging.debug(self.vacuumGripper31.vacuumSensVerticalEncoderCounter)

        self.robot32.robotSensGripperOpen = self.rpi.io.dio2_I_5.value
        self.robot32.robotSensGripperImpulseCounterRaw = self.rpi.io.dio2_Counter_6.value
        self.robot32.robotSensArmEndIn = self.rpi.io.dio2_I_7.value
        self.robot32.robotSensArmImpulseCounterRaw = self.rpi.io.dio2_Counter_8.value
        self.robot32.robotSensVerticalEndUp = self.rpi.io.dio2_I_9.value
        self.robot32.robotSensRotEnd = self.rpi.io.dio2_I_10.value
        self.robot32.robotSensVerticalEncoderCounter = self.rpi.io.dio2_Counter_11.value
        self.robot32.robotSensRotEncoderCounter = self.rpi.io.dio2_Counter_13.value

        self.punchingMachine33.punchingSensLeft = self.rpi.io.dio1_I_11.value
        self.punchingMachine33.punchingSensRight = self.rpi.io.dio1_I_12.value
        self.punchingMachine33.punchingMachineSensIsUp = self.rpi.io.dio1_I_13.value
        self.punchingMachine33.punchingMachineSensIsDown = self.rpi.io.dio1_I_14.value

        self.conveyor34.conveyorSensLeft = self.rpi.io.dio2_I_1.value
        self.conveyor34.conveyorSensRight = self.rpi.io.dio2_I_2.value
        self.conveyor34.conveyorSensImpulse = self.rpi.io.dio2_Counter_3.value

        self.vacuumGripper35.vacuumSensVerticalEndUp = self.rpi.io.dio3_I_1.value
        self.vacuumGripper35.vacuumSensArmEndIn = self.rpi.io.dio3_I_2.value
        self.vacuumGripper35.vacuumSensRotEnd = self.rpi.io.dio3_I_3.value
        self.vacuumGripper35.vacuumSensVerticalEncoderCounter = self.rpi.io.dio3_Counter_5.value
        self.vacuumGripper35.vacuumSensArmEncoderCounter = self.rpi.io.dio3_Counter_7.value
        self.vacuumGripper35.vacuumSensRotEncoderCounter = self.rpi.io.dio3_Counter_9.value

        self.robot36.robotSensGripperOpen = self.rpi.io.dio4_I_5.value
        self.robot36.robotSensGripperImpulseCounterRaw = self.rpi.io.dio4_Counter_6.value
        self.robot36.robotSensArmEndIn = self.rpi.io.dio4_I_7.value
        self.robot36.robotSensArmImpulseCounterRaw = self.rpi.io.dio4_Counter_8.value
        self.robot36.robotSensVerticalEndUp = self.rpi.io.dio4_I_9.value
        self.robot36.robotSensRotEnd = self.rpi.io.dio4_I_10.value
        self.robot36.robotSensVerticalEncoderCounter = self.rpi.io.dio4_Counter_11.value
        self.robot36.robotSensRotEncoderCounter = self.rpi.io.dio4_Counter_13.value
        logging.debug(self.robot36.robotSensVerticalEncoderCounter)

        self.punchingMachine37.punchingSensLeft = self.rpi.io.dio3_I_11.value
        self.punchingMachine37.punchingSensRight = self.rpi.io.dio3_I_12.value
        self.punchingMachine37.punchingMachineSensIsUp = self.rpi.io.dio3_I_13.value
        self.punchingMachine37.punchingMachineSensIsDown = self.rpi.io.dio3_I_14.value

        self.conveyor38.conveyorSensLeft = self.rpi.io.dio4_I_1.value
        self.conveyor38.conveyorSensRight = self.rpi.io.dio4_I_2.value
        self.conveyor38.conveyorSensImpulse = self.rpi.io.dio4_Counter_3.value

        self.warehouse39.warehouseSensHorizontalEnd = self.rpi.io.dio5_I_1.value
        self.warehouse39.warehouseSensLightBarrierIn = self.rpi.io.dio5_I_2.value
        self.warehouse39.warehouseSensLightBarrierOut = self.rpi.io.dio5_I_3.value
        self.warehouse39.warehouseSensVerticalEnd = self.rpi.io.dio5_I_4.value
        self.warehouse39.warehouseSensEncoderHorizontal = self.rpi.io.dio5_Counter_5.value
        self.warehouse39.warehouseSensEncoderVertical = self.rpi.io.dio5_Counter_7.value
        self.warehouse39.warehouseSensArmOut = self.rpi.io.dio5_I_9.value
        self.warehouse39.warehouseSensArmIn = self.rpi.io.dio5_I_10.value

        self.vacuumGripper310.vacuumSensVerticalEndUp = self.rpi.io.dio6_I_1.value
        self.vacuumGripper310.vacuumSensArmEndIn = self.rpi.io.dio6_I_2.value
        self.vacuumGripper310.vacuumSensRotEnd = self.rpi.io.dio6_I_3.value
        self.vacuumGripper310.vacuumSensVerticalEncoderCounter = self.rpi.io.dio6_Counter_5.value
        self.vacuumGripper310.vacuumSensArmEncoderCounter = self.rpi.io.dio6_Counter_7.value
        self.vacuumGripper310.vacuumSensRotEncoderCounter = self.rpi.io.dio6_Counter_9.value

        self.warehouse311.warehouseSensHorizontalEnd = self.rpi.io.dio7_I_1.value
        self.warehouse311.warehouseSensLightBarrierIn = self.rpi.io.dio7_I_2.value
        self.warehouse311.warehouseSensLightBarrierOut = self.rpi.io.dio7_I_3.value
        self.warehouse311.warehouseSensVerticalEnd = self.rpi.io.dio7_I_4.value
        self.warehouse311.warehouseSensEncoderHorizontal = self.rpi.io.dio7_Counter_5.value
        logging.debug(self.warehouse311.warehouseSensEncoderHorizontal)
        self.warehouse311.warehouseSensEncoderVertical = self.rpi.io.dio7_Counter_7.value
        self.warehouse311.warehouseSensArmOut = self.rpi.io.dio7_I_9.value
        self.warehouse311.warehouseSensArmIn = self.rpi.io.dio7_I_10.value

    def read4(self):
        self.robot41.robotSensGripperOpen = self.rpi.io.dio1_I_5.value
        self.robot41.robotSensGripperImpulseCounterRaw = self.rpi.io.dio1_Counter_6.value
        self.robot41.robotSensArmEndIn = self.rpi.io.dio1_I_7.value
        self.robot41.robotSensArmImpulseCounterRaw = self.rpi.io.dio1_Counter_8.value
        self.robot41.robotSensVerticalEndUp = self.rpi.io.dio1_I_9.value
        self.robot41.robotSensRotEnd = self.rpi.io.dio1_I_10.value
        self.robot41.robotSensVerticalEncoderCounter = self.rpi.io.dio1_Counter_11.value
        self.robot41.robotSensRotEncoderCounter = self.rpi.io.dio1_Counter_13.value
        #logging.debug(self.robot41.robotSensRotEncoderCounter)

        self.conveyor42.conveyorSensLeft = self.rpi.io.dio1_I_1.value
        self.conveyor42.conveyorSensRight = self.rpi.io.dio1_I_2.value
        self.conveyor42.conveyorSensImpulse = self.rpi.io.dio1_Counter_3.value

        self.conveyor43.conveyorSensLeft = self.rpi.io.dio2_I_11.value
        self.conveyor43.conveyorSensRight = self.rpi.io.dio2_I_12.value
        self.conveyor43.conveyorSensImpulse = self.rpi.io.dio2_Counter_13.value

        self.vacuumGripper44.vacuumSensVerticalEndUp = self.rpi.io.dio2_I_1.value
        self.vacuumGripper44.vacuumSensArmEndIn = self.rpi.io.dio2_I_2.value
        self.vacuumGripper44.vacuumSensRotEnd = self.rpi.io.dio2_I_3.value
        self.vacuumGripper44.vacuumSensVerticalEncoderCounter = self.rpi.io.dio2_Counter_5.value
        self.vacuumGripper44.vacuumSensArmEncoderCounter = self.rpi.io.dio2_Counter_7.value
        self.vacuumGripper44.vacuumSensRotEncoderCounter = self.rpi.io.dio2_Counter_9.value

        self.warehouse45.warehouseSensHorizontalEnd = self.rpi.io.dio3_I_1.value
        self.warehouse45.warehouseSensLightBarrierIn = self.rpi.io.dio3_I_2.value
        self.warehouse45.warehouseSensLightBarrierOut = self.rpi.io.dio3_I_3.value
        self.warehouse45.warehouseSensVerticalEnd = self.rpi.io.dio3_I_4.value
        self.warehouse45.warehouseSensEncoderHorizontal = self.rpi.io.dio3_Counter_5.value
        self.warehouse45.warehouseSensEncoderVertical = self.rpi.io.dio3_Counter_7.value
        self.warehouse45.warehouseSensArmOut = self.rpi.io.dio3_I_9.value
        self.warehouse45.warehouseSensArmIn = self.rpi.io.dio3_I_10.value


    """write functions that set the RevPi Outputs according to the internal machine parameters"""
    def write1(self):
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

    def write2(self):
        self.rpi.io.dio1_O_3.value = self.robot21.robotActGripperOpen
        self.rpi.io.dio1_O_4.value = self.robot21.robotActGripperClose
        self.rpi.io.dio1_O_5.value = self.robot21.robotActArmOut
        self.rpi.io.dio1_O_6.value = self.robot21.robotActArmIn
        self.rpi.io.dio1_O_7.value = self.robot21.robotActVerticalDown
        self.rpi.io.dio1_O_8.value = self.robot21.robotActVerticalUp
        self.rpi.io.dio1_O_9.value = self.robot21.robotActRotRight
        self.rpi.io.dio1_O_10.value = self.robot21.robotActRotLeft

        self.rpi.io.dio2_O_3.value = self.robot22.robotActGripperOpen
        self.rpi.io.dio2_O_4.value = self.robot22.robotActGripperClose
        self.rpi.io.dio2_O_5.value = self.robot22.robotActArmOut
        self.rpi.io.dio2_O_6.value = self.robot22.robotActArmIn
        self.rpi.io.dio2_O_7.value = self.robot22.robotActVerticalDown
        self.rpi.io.dio2_O_8.value = self.robot22.robotActVerticalUp
        self.rpi.io.dio2_O_9.value = self.robot22.robotActRotRight
        self.rpi.io.dio2_O_10.value = self.robot22.robotActRotLeft

        self.rpi.io.dio1_O_1.value = self.conveyor23.conveyorActForward
        self.rpi.io.dio1_O_2.value = self.conveyor23.conveyorActBackward

        self.rpi.io.dio2_O_1.value = self.conveyor24.conveyorActForward
        self.rpi.io.dio2_O_2.value = self.conveyor24.conveyorActBackward

        self.rpi.io.dio3_O_1.value = self.vacuumGripper25.vacuumActVerticalUp
        self.rpi.io.dio3_O_2.value = self.vacuumGripper25.vacuumActVerticalDown
        self.rpi.io.dio3_O_3.value = self.vacuumGripper25.vacuumActArmIn
        self.rpi.io.dio3_O_4.value = self.vacuumGripper25.vacuumActArmOut
        self.rpi.io.dio3_O_5.value = self.vacuumGripper25.vacuumActRotRight
        self.rpi.io.dio3_O_6.value = self.vacuumGripper25.vacuumActRotLeft
        self.rpi.io.dio3_O_7.value = self.vacuumGripper25.vacuumActCompressorOn
        self.rpi.io.dio3_O_8.value = self.vacuumGripper25.vacuumActValve

        self.rpi.io.dio4_O_1.value = self.multiprocessing26.multiProcessingActRotClockwise
        self.rpi.io.dio4_O_2.value = self.multiprocessing26.multiProcessingActRotCounterclockwise
        self.rpi.io.dio4_O_3.value = self.multiprocessing26.multiProcessingActConveyorForward
        self.rpi.io.dio4_O_4.value = self.multiprocessing26.multiProcessingActSaw
        self.rpi.io.dio4_O_5.value = self.multiprocessing26.multiProcessingActOvenInward
        self.rpi.io.dio4_O_6.value = self.multiprocessing26.multiProcessingActOvenOutward
        self.rpi.io.dio4_O_7.value = self.multiprocessing26.multiProcessingActGripperToOven
        self.rpi.io.dio4_O_8.value = self.multiprocessing26.multiProcessingActGripperToTurntable
        self.rpi.io.dio4_O_9.value = self.multiprocessing26.multiProcessingOvenLight
        self.rpi.io.dio4_O_10.value = self.multiprocessing26.multiProcessingCompressor
        self.rpi.io.dio4_O_11.value = self.multiprocessing26.multiProcessingValve
        self.rpi.io.dio4_O_12.value = self.multiprocessing26.multiProcessingActLowerValve
        self.rpi.io.dio4_O_13.value = self.multiprocessing26.multiProcessingValveOvenDoor
        self.rpi.io.dio4_O_14.value = self.multiprocessing26.multiProcessingValveFeeder

    def write3(self):
        self.rpi.io.dio1_O_1.value = self.vacuumGripper31.vacuumActVerticalUp
        self.rpi.io.dio1_O_2.value = self.vacuumGripper31.vacuumActVerticalDown
        self.rpi.io.dio1_O_3.value = self.vacuumGripper31.vacuumActArmIn
        self.rpi.io.dio1_O_4.value = self.vacuumGripper31.vacuumActArmOut
        self.rpi.io.dio1_O_5.value = self.vacuumGripper31.vacuumActRotRight
        self.rpi.io.dio1_O_6.value = self.vacuumGripper31.vacuumActRotLeft
        self.rpi.io.dio1_O_7.value = self.vacuumGripper31.vacuumActCompressorOn
        self.rpi.io.dio1_O_8.value = self.vacuumGripper31.vacuumActValve

        self.rpi.io.dio2_O_3.value = self.robot32.robotActGripperOpen
        self.rpi.io.dio2_O_4.value = self.robot32.robotActGripperClose
        self.rpi.io.dio2_O_5.value = self.robot32.robotActArmOut
        self.rpi.io.dio2_O_6.value = self.robot32.robotActArmIn
        self.rpi.io.dio2_O_7.value = self.robot32.robotActVerticalDown
        self.rpi.io.dio2_O_8.value = self.robot32.robotActVerticalUp
        self.rpi.io.dio2_O_9.value = self.robot32.robotActRotRight
        self.rpi.io.dio2_O_10.value = self.robot32.robotActRotLeft

        self.rpi.io.dio1_O_9.value = self.punchingMachine33.punchingActForward
        self.rpi.io.dio1_O_10.value = self.punchingMachine33.punchingActBackward
        self.rpi.io.dio1_O_11.value = self.punchingMachine33.punchingMachineMoveUp
        self.rpi.io.dio1_O_12.value = self.punchingMachine33.punchingMachineMoveDown

        self.rpi.io.dio2_O_1.value = self.conveyor34.conveyorActForward
        self.rpi.io.dio2_O_2.value = self.conveyor34.conveyorActBackward

        self.rpi.io.dio3_O_1.value = self.vacuumGripper35.vacuumActVerticalUp
        self.rpi.io.dio3_O_2.value = self.vacuumGripper35.vacuumActVerticalDown
        self.rpi.io.dio3_O_3.value = self.vacuumGripper35.vacuumActArmIn
        self.rpi.io.dio3_O_4.value = self.vacuumGripper35.vacuumActArmOut
        self.rpi.io.dio3_O_5.value = self.vacuumGripper35.vacuumActRotRight
        self.rpi.io.dio3_O_6.value = self.vacuumGripper35.vacuumActRotLeft
        self.rpi.io.dio3_O_7.value = self.vacuumGripper35.vacuumActCompressorOn
        self.rpi.io.dio3_O_8.value = self.vacuumGripper35.vacuumActValve

        self.rpi.io.dio4_O_3.value = self.robot36.robotActGripperOpen
        self.rpi.io.dio4_O_4.value = self.robot36.robotActGripperClose
        self.rpi.io.dio4_O_5.value = self.robot36.robotActArmOut
        self.rpi.io.dio4_O_6.value = self.robot36.robotActArmIn
        self.rpi.io.dio4_O_7.value = self.robot36.robotActVerticalDown
        self.rpi.io.dio4_O_8.value = self.robot36.robotActVerticalUp
        self.rpi.io.dio4_O_9.value = self.robot36.robotActRotRight
        self.rpi.io.dio4_O_10.value = self.robot36.robotActRotLeft

        self.rpi.io.dio3_O_9.value = self.punchingMachine37.punchingActForward
        self.rpi.io.dio3_O_10.value = self.punchingMachine37.punchingActBackward
        self.rpi.io.dio3_O_11.value = self.punchingMachine37.punchingMachineMoveUp
        self.rpi.io.dio3_O_12.value = self.punchingMachine37.punchingMachineMoveDown

        self.rpi.io.dio4_O_1.value = self.conveyor38.conveyorActForward
        self.rpi.io.dio4_O_2.value = self.conveyor38.conveyorActBackward

        self.rpi.io.dio5_O_1.value = self.warehouse39.warehouseActConveyorOut
        self.rpi.io.dio5_O_2.value = self.warehouse39.warehouseActConveyorIn
        self.rpi.io.dio5_O_3.value = self.warehouse39.warehouseActHorizontalToRack
        self.rpi.io.dio5_O_4.value = self.warehouse39.warehouseActHorizontalToConveyor
        self.rpi.io.dio5_O_5.value = self.warehouse39.warehouseActVerticalDown
        self.rpi.io.dio5_O_6.value = self.warehouse39.warehouseActVerticalUp
        self.rpi.io.dio5_O_7.value = self.warehouse39.warehouseActArmOut
        self.rpi.io.dio5_O_8.value = self.warehouse39.warehouseActArmIn

        self.rpi.io.dio6_O_1.value = self.vacuumGripper310.vacuumActVerticalUp
        self.rpi.io.dio6_O_2.value = self.vacuumGripper310.vacuumActVerticalDown
        self.rpi.io.dio6_O_3.value = self.vacuumGripper310.vacuumActArmIn
        self.rpi.io.dio6_O_4.value = self.vacuumGripper310.vacuumActArmOut
        self.rpi.io.dio6_O_5.value = self.vacuumGripper310.vacuumActRotRight
        self.rpi.io.dio6_O_6.value = self.vacuumGripper310.vacuumActRotLeft
        self.rpi.io.dio6_O_7.value = self.vacuumGripper310.vacuumActCompressorOn
        self.rpi.io.dio6_O_8.value = self.vacuumGripper310.vacuumActValve

        self.rpi.io.dio7_O_1.value = self.warehouse311.warehouseActConveyorOut
        self.rpi.io.dio7_O_2.value = self.warehouse311.warehouseActConveyorIn
        self.rpi.io.dio7_O_3.value = self.warehouse311.warehouseActHorizontalToRack
        self.rpi.io.dio7_O_4.value = self.warehouse311.warehouseActHorizontalToConveyor
        self.rpi.io.dio7_O_5.value = self.warehouse311.warehouseActVerticalDown
        self.rpi.io.dio7_O_6.value = self.warehouse311.warehouseActVerticalUp
        self.rpi.io.dio7_O_7.value = self.warehouse311.warehouseActArmOut
        self.rpi.io.dio7_O_8.value = self.warehouse311.warehouseActArmIn

    def write4(self):
        self.rpi.io.dio1_O_3.value = self.robot41.robotActGripperOpen
        self.rpi.io.dio1_O_4.value = self.robot41.robotActGripperClose
        self.rpi.io.dio1_O_5.value = self.robot41.robotActArmOut
        self.rpi.io.dio1_O_6.value = self.robot41.robotActArmIn
        self.rpi.io.dio1_O_7.value = self.robot41.robotActVerticalDown
        self.rpi.io.dio1_O_8.value = self.robot41.robotActVerticalUp
        self.rpi.io.dio1_O_9.value = self.robot41.robotActRotRight
        self.rpi.io.dio1_O_10.value = self.robot41.robotActRotLeft

        self.rpi.io.dio1_O_1.value = self.conveyor42.conveyorActForward
        self.rpi.io.dio1_O_2.value = self.conveyor42.conveyorActBackward

        self.rpi.io.dio2_O_9.value = self.conveyor43.conveyorActForward
        self.rpi.io.dio2_O_10.value = self.conveyor43.conveyorActBackward

        self.rpi.io.dio2_O_1.value = self.vacuumGripper44.vacuumActVerticalUp
        self.rpi.io.dio2_O_2.value = self.vacuumGripper44.vacuumActVerticalDown
        self.rpi.io.dio2_O_3.value = self.vacuumGripper44.vacuumActArmIn
        self.rpi.io.dio2_O_4.value = self.vacuumGripper44.vacuumActArmOut
        self.rpi.io.dio2_O_5.value = self.vacuumGripper44.vacuumActRotRight
        self.rpi.io.dio2_O_6.value = self.vacuumGripper44.vacuumActRotLeft
        self.rpi.io.dio2_O_7.value = self.vacuumGripper44.vacuumActCompressorOn
        self.rpi.io.dio2_O_8.value = self.vacuumGripper44.vacuumActValve

        self.rpi.io.dio3_O_1.value = self.warehouse45.warehouseActConveyorOut
        self.rpi.io.dio3_O_2.value = self.warehouse45.warehouseActConveyorIn
        self.rpi.io.dio3_O_3.value = self.warehouse45.warehouseActHorizontalToRack
        self.rpi.io.dio3_O_4.value = self.warehouse45.warehouseActHorizontalToConveyor
        self.rpi.io.dio3_O_5.value = self.warehouse45.warehouseActVerticalDown
        self.rpi.io.dio3_O_6.value = self.warehouse45.warehouseActVerticalUp
        self.rpi.io.dio3_O_7.value = self.warehouse45.warehouseActArmOut
        self.rpi.io.dio3_O_8.value = self.warehouse45.warehouseActArmIn



    """Reset functions that are used to set encoder values to 0 after the setup of the individual machine connected to
    these ports is finished"""
    def reset1(self):
        r12 = self.robot12.executeHelper()
        w15 = self.warehouse15.executeHelper()
        v16 = self.vacuumGripper16.executeHelper()
        r18 = self.robot18.executeHelper()
        if r12[0]:
            self.rpi.io.dio1_Counter_11.reset()
            self.rpi.io.dio1_Counter_13.reset()
        if w15[0]:
            self.rpi.io.dio3_Counter_5.reset()
            self.rpi.io.dio3_Counter_7.reset()
        if v16[0]:
            self.rpi.io.dio4_Counter_5.reset()
            self.rpi.io.dio4_Counter_7.reset()
            self.rpi.io.dio4_Counter_9.reset()
        if r18[0]:
            self.rpi.io.dio5_Counter_11.reset()
            self.rpi.io.dio5_Counter_13.reset()

    def reset2(self):
        r21 = self.robot21.executeHelper()
        r22 = self.robot22.executeHelper()
        v25 = self.vacuumGripper25.executeHelper()
        if r21[0]:
            logging.debug("reset grip1 counters")
            self.rpi.io.dio1_Counter_11.reset()
            self.rpi.io.dio1_Counter_13.reset()
        if r22[0]:
            logging.debug("reset grip2 counters")
            self.rpi.io.dio2_Counter_11.reset()
            self.rpi.io.dio2_Counter_13.reset()
        if v25[0]:
            logging.debug("reset vac counters")
            self.rpi.io.dio3_Counter_5.reset()
            self.rpi.io.dio3_Counter_7.reset()
            self.rpi.io.dio3_Counter_9.reset()

    def reset3(self):
        v31 = self.vacuumGripper31.executeHelper()
        r32 = self.robot32.executeHelper()
        v35 = self.vacuumGripper35.executeHelper()
        r36 = self.robot36.executeHelper()
        w39 = self.warehouse39.executeHelper()
        v310 = self.vacuumGripper310.executeHelper()
        w311 = self.warehouse311.executeHelper()
        if v31[0]:
            self.rpi.io.dio1_Counter_5.reset()
            self.rpi.io.dio1_Counter_7.reset()
            self.rpi.io.dio1_Counter_9.reset()
        if r32[0]:
            self.rpi.io.dio2_Counter_11.reset()
            self.rpi.io.dio2_Counter_13.reset()
        if v35[0]:
            self.rpi.io.dio3_Counter_5.reset()
            self.rpi.io.dio3_Counter_7.reset()
            self.rpi.io.dio3_Counter_9.reset()
        if r36[0]:
            self.rpi.io.dio4_Counter_11.reset()
            self.rpi.io.dio4_Counter_13.reset()
            logging.debug("reset robot36")
        if w39[0]:
            self.rpi.io.dio5_Counter_5.reset()
            self.rpi.io.dio5_Counter_7.reset()
        if v310[0]:
            self.rpi.io.dio6_Counter_5.reset()
            self.rpi.io.dio6_Counter_7.reset()
            self.rpi.io.dio6_Counter_9.reset()
        if w311[0]:
            self.rpi.io.dio7_Counter_5.reset()
            self.rpi.io.dio7_Counter_7.reset()

    def reset4(self):
        r41 = self.robot41.executeHelper()
        v44 = self.vacuumGripper44.executeHelper()
        w45 = self.warehouse45.executeHelper()
        if r41[0]:
            self.rpi.io.dio1_Counter_11.reset()
            self.rpi.io.dio1_Counter_13.reset()
        if v44[0]:
            self.rpi.io.dio2_Counter_5.reset()
            self.rpi.io.dio2_Counter_7.reset()
            self.rpi.io.dio2_Counter_9.reset()
        if w45[0]:
            self.rpi.io.dio3_Counter_5.reset()
            self.rpi.io.dio3_Counter_7.reset()

    ###################################################################################################################
    # ab hier allgemeine Funktionen, die nicht an den spezifischen RevPi oder das Stationssetup angepasst werden müssen
    ###################################################################################################################

    def exLoop(self):
        """
        The execute loop, which activates all the necessary functions on each machine
        """
        for key in self.currentlyExecuting.keys():
            # call method
            if not self.currentlyExecuting[key][0] is None:
                #print(key)
                #print(self.currentlyExecuting[key][0])
                #if key == self.robot41:
                    #print(self.currentlyExecuting[key][0])
                # noinspection PyCallingNonCallable
                self.currentlyExecuting[key][0]()
            # remove method once it is finished
            if key.feedback() == ExecutionStatus.FINISHED:
                self.currentlyExecuting[key][0] = None

    def createFeedbackOnChange(self):
        """Whenever the state of the machine changes, feedback is created
        a machine can be in several states as definded in the ExecutionStatus enum
        (currently, only INACTION and FINISHED are used)"""
        #todo position für roboter mitsenden
        for m in self.machines:
            #debug-if
            #if m == self.warehouse45:
            #    logging.debug(self.feedback[m])
            # bei Feedback-Änderung senden
            if self.feedback[m] != m.feedback():
                self.feedback[m] = m.feedback()
                # bei allererstem Feedback ohne command als id 0 senden (evtl. problematisch weil ungewünschter Programm-
                # fluss in späterer realer Ausführung in Fehlerfällen, bislang keine Probleme bekannt)
                if self.currentlyExecuting[m][1] is None:
                    jsonid = 0
                else:
                    jsonid = self.currentlyExecuting[m][1]
                # append feedback to outputBuffer
                f = MachineCommandFeedback("FEEDBACK", jsonid, m.feedback().name,  "")
                j = JSONOutput(m.id, time.time(), f)
                #logging.debug("created Feedback")
                self.outputBuffer.put(j, block=False)

    def processJson(self, inputBuffer: Queue):
        #maybe output buffer als parameter übergeben wie inputbuffer???
        """
        gets the JSONOutput-objects out of the input buffer and decides which function to execute
        :param inputBuffer:
        :return:
        """
        foundMatchingMachine = False
        func = None
        ret = None
        inputBufferItem = None
        try:
            inputBufferItem = inputBuffer.get(block=False)
        except Empty:
            pass
        if inputBufferItem is not None:
            for m in self.machines:
                # überprüfe ob maschinen-id bekannt
                if m.id == inputBufferItem.message.topicName:
                    foundMatchingMachine = True
                    print("found matching machine")
                    # find diff btw command and request
                    if inputBufferItem.message.jsonType == "STATUSREQUEST":
                        print("Status")
                        # Status abfragen und Ergebnis an outputBuffer anfügen
                        try:
                            statusanswer = m.request(inputBufferItem.message.parameters)
                            a = MachineStatusRequestAnswer("STATUSANSWER", inputBufferItem.message.requestId, statusanswer)
                            j = JSONOutput(m.id, time.time(), a)
                            self.outputBuffer.put(j)
                        except AttributeError:
                            #TODO change:
                            #raise JSONCommandNotSupportedOnThisMachineException()
                            print("status not supported")
                            break
                    elif inputBufferItem.message.jsonType == "COMMAND":
                        print("command")
                        try:
                            print(inputBufferItem.message.name)
                            # map between functions and the name of functions sent with the JSON
                            if inputBufferItem.message.type == "ThreeDGripper" and isinstance(m, Robot):
                                func = getattr(Robot, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "VacuumGripper" and isinstance(m, VacuumGripper):
                                func = getattr(VacuumGripper, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Warehouse" and isinstance(m, Warehouse):
                                func = getattr(Warehouse, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Sorting" and isinstance(m, SortingLine):
                                func = getattr(SortingLine, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Indexedline" and isinstance(m, IndexedLine):
                                func = getattr(IndexedLine, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Multiprocessing" and isinstance(m, MultiProcessing):
                                func = getattr(MultiProcessing, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Conveyor" and isinstance(m, Conveyor):
                                func = getattr(Conveyor, str.lower(inputBufferItem.message.name))
                            elif inputBufferItem.message.type == "Punching" and isinstance(m, PunchingMachine):
                                func = getattr(PunchingMachine, str.lower(inputBufferItem.message.name))
                            else:
                                #TODO raise an exception here
                                print("seems like a wrong json command, no id known to this machine type")
                            # Funktionsparameter in korrekte Reihenfolge bringen und mit Funktion zusammenbringen
                            if inputBufferItem.message.type == "ThreeDGripper" or inputBufferItem.message.type == "VacuumGripper":
                                pos = inputBufferItem.message.parameters
                                i = len(pos)
                                if i == 0:
                                    logging.debug("function called with no args")
                                    #m.setupFirst = True unschön
                                    ret = func(m)
                                if i == 1:
                                    # just assume that start/end is correct
                                    # TODO get rid of assumption
                                    logging.debug("function called with one arg")
                                    ret = func(m, pos[0])
                                if i == 2:
                                    if pos[0].meaning == "START" and pos[1].meaning == "END":
                                        logging.debug("function called with two args")
                                        ret = func(m, pos[0], pos[1])
                                    elif pos[0].meaning == "END" and pos[1].meaning == "START":
                                        logging.debug("function called with two args")
                                        ret = func(m, pos[1], pos[0])
                                    else:
                                        logging.error("command not supported - params")
                                        # TODO activate:
                                        # raise JSONCommandNotSupportedOnThisMachineException()
                            elif inputBufferItem.message.type == "Warehouse":
                                box = inputBufferItem.message.parameters
                                i = len(box)
                                if i == 0:
                                    ret = func(m)
                                if i == 1:
                                    ret = func(m, box[0])
                                #TODO clarify in API wich box the object is put to and from which the object is retrieved
                                #currently: first arg: in, second argument: out
                                if i == 2:
                                    ret = func(m, box[0], box[1])
                            elif inputBufferItem.message.type == "Sorting" or inputBufferItem.message.type == "Indexedline" or inputBufferItem.message.type == "Multiprocessing":
                                colour = inputBufferItem.message.parameters
                                i = len(colour)
                                if i == 0:
                                    ret = func(m)
                                if i == 1:
                                    ret = func(m, colour[0])
                            elif inputBufferItem.message.type == "Punching":
                                ret = func(m)
                            elif inputBufferItem.message.type == "Conveyor":
                                mix = inputBufferItem.message.parameters
                                i = len(mix)
                                if i == 0:
                                    ret = func(m)
                                if i == 1:
                                    ret = func(m, mix[0])
                                if i == 2:
                                    if mix[0] == Direction.BACKWARD or mix[0] == Direction.FORWARD:
                                        ret = func(m, mix[0], mix[1])
                                    else:
                                        ret = func(m, mix[1], mix[0])

                            # holds the function that is currently executed on each machine
                            self.currentlyExecuting[m] = [ret, inputBufferItem.commandId]
                            break
                        except AttributeError:
                            #TODO activate:
                            #raise JSONCommandNotSupportedOnThisMachineException()
                            print("command not supported")
                            break
            if not foundMatchingMachine:
                print("unknown id")


    """Allows clean exit by setting all Outputs to false"""
    def cleanup_revpi(self):
        self.rpi.core.a1green.value = False
        print("cleanup")
        self.rpi.io.dio1_O_1.value = False
        self.rpi.io.dio1_O_2.value = False
        self.rpi.io.dio1_O_3.value = False
        self.rpi.io.dio1_O_4.value = False
        self.rpi.io.dio1_O_5.value = False
        self.rpi.io.dio1_O_6.value = False
        self.rpi.io.dio1_O_7.value = False
        self.rpi.io.dio1_O_8.value = False
        self.rpi.io.dio1_O_9.value = False
        self.rpi.io.dio1_O_10.value = False
        self.rpi.io.dio1_O_11.value = False
        self.rpi.io.dio1_O_12.value = False
        self.rpi.io.dio1_O_13.value = False
        self.rpi.io.dio1_O_14.value = False

        self.rpi.io.dio2_O_1.value = False
        self.rpi.io.dio2_O_2.value = False
        self.rpi.io.dio2_O_3.value = False
        self.rpi.io.dio2_O_4.value = False
        self.rpi.io.dio2_O_5.value = False
        self.rpi.io.dio2_O_6.value = False
        self.rpi.io.dio2_O_7.value = False
        self.rpi.io.dio2_O_8.value = False
        self.rpi.io.dio2_O_9.value = False
        self.rpi.io.dio2_O_10.value = False
        self.rpi.io.dio2_O_11.value = False
        self.rpi.io.dio2_O_12.value = False
        self.rpi.io.dio2_O_13.value = False
        self.rpi.io.dio2_O_14.value = False

        self.rpi.io.dio3_O_1.value = False
        self.rpi.io.dio3_O_2.value = False
        self.rpi.io.dio3_O_3.value = False
        self.rpi.io.dio3_O_4.value = False
        self.rpi.io.dio3_O_5.value = False
        self.rpi.io.dio3_O_6.value = False
        self.rpi.io.dio3_O_7.value = False
        self.rpi.io.dio3_O_8.value = False
        self.rpi.io.dio3_O_9.value = False
        self.rpi.io.dio3_O_10.value = False
        self.rpi.io.dio3_O_11.value = False
        self.rpi.io.dio3_O_12.value = False
        self.rpi.io.dio3_O_13.value = False
        self.rpi.io.dio3_O_14.value = False

        if coreNumber == RevPiNumber.CORE1 or coreNumber == RevPiNumber.CORE2 or coreNumber == RevPiNumber.CORE3:
            self.rpi.io.dio4_O_1.value = False
            self.rpi.io.dio4_O_2.value = False
            self.rpi.io.dio4_O_3.value = False
            self.rpi.io.dio4_O_4.value = False
            self.rpi.io.dio4_O_5.value = False
            self.rpi.io.dio4_O_6.value = False
            self.rpi.io.dio4_O_7.value = False
            self.rpi.io.dio4_O_8.value = False
            self.rpi.io.dio4_O_9.value = False
            self.rpi.io.dio4_O_10.value = False
            self.rpi.io.dio4_O_11.value = False
            self.rpi.io.dio4_O_12.value = False
            self.rpi.io.dio4_O_13.value = False
            self.rpi.io.dio4_O_14.value = False

        if coreNumber == RevPiNumber.CORE1 or  coreNumber == RevPiNumber.CORE3:
            self.rpi.io.dio5_O_1.value = False
            self.rpi.io.dio5_O_2.value = False
            self.rpi.io.dio5_O_3.value = False
            self.rpi.io.dio5_O_4.value = False
            self.rpi.io.dio5_O_5.value = False
            self.rpi.io.dio5_O_6.value = False
            self.rpi.io.dio5_O_7.value = False
            self.rpi.io.dio5_O_8.value = False
            self.rpi.io.dio5_O_9.value = False
            self.rpi.io.dio5_O_10.value = False
            self.rpi.io.dio5_O_11.value = False
            self.rpi.io.dio5_O_12.value = False
            self.rpi.io.dio5_O_13.value = False
            self.rpi.io.dio5_O_14.value = False

        if coreNumber == RevPiNumber.CORE3:
            self.rpi.io.dio6_O_1.value = False
            self.rpi.io.dio6_O_2.value = False
            self.rpi.io.dio6_O_3.value = False
            self.rpi.io.dio6_O_4.value = False
            self.rpi.io.dio6_O_5.value = False
            self.rpi.io.dio6_O_6.value = False
            self.rpi.io.dio6_O_7.value = False
            self.rpi.io.dio6_O_8.value = False
            self.rpi.io.dio6_O_9.value = False
            self.rpi.io.dio6_O_10.value = False
            self.rpi.io.dio6_O_11.value = False
            self.rpi.io.dio6_O_12.value = False
            self.rpi.io.dio6_O_13.value = False
            self.rpi.io.dio6_O_14.value = False

            self.rpi.io.dio7_O_1.value = False
            self.rpi.io.dio7_O_2.value = False
            self.rpi.io.dio7_O_3.value = False
            self.rpi.io.dio7_O_4.value = False
            self.rpi.io.dio7_O_5.value = False
            self.rpi.io.dio7_O_6.value = False
            self.rpi.io.dio7_O_7.value = False
            self.rpi.io.dio7_O_8.value = False
            self.rpi.io.dio7_O_9.value = False
            self.rpi.io.dio7_O_10.value = False
            self.rpi.io.dio7_O_11.value = False
            self.rpi.io.dio7_O_12.value = False
            self.rpi.io.dio7_O_13.value = False
            self.rpi.io.dio7_O_14.value = False

        #for o in revpimodio2.io.IOList(): # why doesn't this work?
        #    print("cleanup - set False")
        #    if o.type == 301:
        #        o.value = False


if __name__ == "__main__":
    # Start RevPiApp app
    root = RevPiMain()



