#!/usr/bin/env python
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




#tests for "this" class can be found in JSONProcessingIntegrationMain(Test)
class RevPiMain:


    def __init__(self):
        # Instantiate RevPiModIO
        self.rpi = revpimodio2.RevPiModIO(autorefresh=True)
        # Handle SIGINT / SIGTERM to exit program cleanly
        self.rpi.handlesignalend(self.cleanup_revpi)
        #objektinit



    """start functions, that start the server and revpi execution threads with the correct helper functions"""
    def start(self):
        # Start event system without blocking here
        self.rpi.mainloop(blocking=False)
        while not self.rpi.exitsignal.wait(0.03):
            self.read()
            self.execute()
            self.write()


    def read(self):
        pass

    def execute(self):
        #freeze

    def write(self):
        pass


    """Allows clean exit by setting all Outputs to false"""
    def cleanup_revpi(self):
        self.rpi.core.a1green.value = False
        print("cleanup")
        self.rpi.io.O_1.value = False
        self.rpi.io.O_2.value = False
        self.rpi.io.O_3.value = False
        self.rpi.io.O_4.value = False
        self.rpi.io.O_5.value = False
        self.rpi.io.O_6.value = False
        self.rpi.io.O_7.value = False
        self.rpi.io.O_8.value = False
        self.rpi.io.O_9.value = False
        self.rpi.io.O_10.value = False
        self.rpi.io.O_11.value = False
        self.rpi.io.O_12.value = False
        self.rpi.io.O_13.value = False
        self.rpi.io.O_14.value = False





if __name__ == "__main__":
    # Start RevPiApp app
    root = RevPiMain()


