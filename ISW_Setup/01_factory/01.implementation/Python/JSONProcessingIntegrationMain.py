import queue
import time

import revpimodio2
from Robot import Robot
from Warehouse import Warehouse
from Conveyor import Conveyor
from VacuumGripper import VacuumGripper
from SortingLine import SortingLine
from IndexedLine import IndexedLine
from MultiProcessing import MultiProcessing
from PunchingMachine import PunchingMachine
from Direction import Direction
from JSONOutput import JSONOutput
from MachineStatusRequestAnswer import MachineStatusRequestAnswer
from ExecutionStatus import ExecutionStatus
from MachineCommandFeedback import MachineCommandFeedback
from RequestedParameter import RequestedParameter
from JSONReader import JSONReader
from JSONCommandNotSupportedOnThisMachineException import JSONCommandNotSupportedOnThisMachineException
from queue import Queue
import logging

#class for testing purposes
class JSONProcessingIntegrationMain:

    logging.basicConfig(format='%(levelname)s: %(module)s,%(lineno)s: %(message)s', level=logging.DEBUG)
    logging.debug('debug level messages displayed')
    logging.info('info level messages displayed')
    logging.warning('warning level messages displayed')
    logging.error('error level messages displayed')

    # currently without init of revpimodio
    def __init__(self):
        #TODO activate before transfer to revpi
        # Instantiate RevPiModIO
        #self.rpi = revpimodio2.RevPiModIO(autorefresh=True)
        # Handle SIGINT / SIGTERM to exit program cleanly
        #self.rpi.handlesignalend(self.cleanup_revpi)

        # create station objects
        self.robot1 = Robot("Gripper1Sorting")
        self.conveyor1 = Conveyor("Conveyor2Sorting")
        self.sortingLine1 = SortingLine("Sorting3Sorting")
        self.vacuum1 = VacuumGripper("Vacuum4Sorting")
        self.warehouse1 = Warehouse("Warehouse5Sorting")
        self.indexedLine = IndexedLine("IndexedLine6Sorting")
        self.robot2 = Robot("Gripper7Sorting")
        self.conveyor2 = Conveyor("Conveyor8Sorting")
        self.multiprocessing = MultiProcessing("Multiprocessing9Sorting")
        self.punching = PunchingMachine("Punching0Sorting")
        self.machines = [self.robot1, self.conveyor1, self.sortingLine1, self.vacuum1, self.warehouse1,
                         self.indexedLine, self.robot2, self.conveyor2, self.multiprocessing, self.punching]
        #init a buffer to store all incoming/outgoing messages, received in a different thread than the one executing the revpi functions
        #stored as python objects, so parse/deserialize before putting into buffer
        self.feedback = []
        self.inputBuffer = Queue()
        self.outputBuffer = Queue()
        self.currentlyExecuting = {
            self.robot1: [None, None],
            self.conveyor1: [None, None],
            self.sortingLine1: [None, None],
            self.vacuum1: [None, None],
            self.warehouse1: [None, None],
            self.indexedLine: [None, None],
            self.robot2: [None, None],
            self.conveyor2: [None, None],
            self.multiprocessing: [None, None],
            self.punching: [None, None]
        }
        self.feedback = {
            self.robot1: ExecutionStatus.FINISHED,
            self.conveyor1: ExecutionStatus.FINISHED,
            self.sortingLine1: ExecutionStatus.FINISHED,
            self.vacuum1: ExecutionStatus.FINISHED,
            self.warehouse1: ExecutionStatus.FINISHED,
            self.indexedLine: ExecutionStatus.FINISHED,
            self.robot2: ExecutionStatus.FINISHED,
            self.conveyor2: ExecutionStatus.FINISHED,
            self.multiprocessing: ExecutionStatus.FINISHED,
            self.punching: ExecutionStatus.FINISHED
        }

    #TODO add cleanup, add start of revpi loop before transfer to revpi
    #TODO add read write for IOs

    #TODO add sockets to send/receive JSON strings
    def sendReceiveJSON(self):
        #receive commands/statusrequests
        #deserialize received strings and put them in the inputBuffer
        #serialize objects in the outputBuffer to JSONStrings
        #send jsonStrings of Statusanswer/Feedback
        pass

    def exLoop(self):
        #TODO in while loop non blocking parallel to revpi loop - wirklich? bezweifel ich grad, eher seriell
        for key in self.currentlyExecuting.keys():
            if not self.currentlyExecuting[key][0] is None:
                print(key)
                print(self.currentlyExecuting[key][0])
                # noinspection PyCallingNonCallable
                self.currentlyExecuting[key][0]()
            # remove method once it is finished
            if key.fakeFeedback() == ExecutionStatus.FINISHED:
                self.currentlyExecuting[key][0] = None

    def createFeedbackOnChange(self):
        #todo position für roboter mitsenden
        #for m in self.machines:
        m = self.robot1
        logging.debug(m.feedback().name)
        if self.feedback[m] != m.fakeFeedback():
            self.feedback[m] = m.fakeFeedback()
            if self.currentlyExecuting[m][1] is None:
                jsonid = 0
            else:
                jsonid = self.currentlyExecuting[m][1]
            logging.debug(m.feedback().name)
            f = MachineCommandFeedback("FEEDBACK", jsonid, m.feedback().name,  "")
            j = JSONOutput(m.id, time.time(), f)
            #logging.debug("created Feedback")
            self.outputBuffer.put(j, block=False)

    def processJson(self, inputBuffer: queue):
        # TODO in while loop non blocking parallel to revpi loop
        b = False
        func = None
        ret = None
        c = inputBuffer.get()
        print(inputBuffer.empty())
        #TODO handle type all
        for m in self.machines:
            if m.id == c.topicName:
                b = True
                print("found matching machine")
                #find diff btw command and request
                if c.message.jsonType == "STATUSREQUEST":
                    print("Status")
                    try:
                        statusanswer = m.request(c.message.parameters)
                        a = MachineStatusRequestAnswer("STATUSANSWER", c.message.requestId, statusanswer)
                        j = JSONOutput(m.id, time.time(), a)
                        self.outputBuffer.put(j)
                    except AttributeError:
                        #TODO change:
                        #raise JSONCommandNotSupportedOnThisMachineException()
                        #TODO append feedback to outputBuffer
                        #probably fixed
                        print("status not supported")
                        break
                elif c.message.jsonType == "COMMAND":
                    print("command")
                    try:
                        print(c.message.name)
                        #map between functions and the name of functions
                        if c.message.type == "GRIPPER" and isinstance(m, Robot):
                            func = getattr(Robot, str.lower(c.message.name))
                        elif c.message.type == "VACUUM" and isinstance(m, VacuumGripper):
                            func = getattr(VacuumGripper, str.lower(c.message.name))
                        elif c.message.type == "WAREHOUSE" and isinstance(m, Warehouse):
                            func = getattr(Warehouse, str.lower(c.message.name))
                        elif c.message.type == "SORTING" and isinstance(m, SortingLine):
                            func = getattr(SortingLine, str.lower(c.message.name))
                        elif c.message.type == "INDEXEDLINE" and isinstance(m, IndexedLine):
                            func = getattr(IndexedLine, str.lower(c.message.name))
                        elif c.message.type == "MULTIPROCESSING" and isinstance(m, MultiProcessing):
                            func = getattr(MultiProcessing, str.lower(c.message.name))
                        elif c.message.type == "CONVEYOR" and isinstance(m, Conveyor):
                            func = getattr(Conveyor, str.lower(c.message.name))
                        elif c.message.type == "PUNCHING" and isinstance(m, PunchingMachine):
                            func = getattr(PunchingMachine, str.lower(c.message.name))
                        else:
                            #TODO raise an exception here
                            print("seems like a wrong json command, no id known to this machine type")
                        if c.message.type == "GRIPPER" or c.message.type == "VACUUM":
                            pos = c.message.parameters
                            i = len(pos)
                            if i == 0:
                                logging.debug("function called with no args")
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
        #TODO ret für alle anderen funktionen
                        elif c.message.type == "WAREHOUSE":
                            box = c.message.parameters
                            i = len(box)
                            if i == 0:
                                func(m)
                            if i == 1:
                                func(m, box[0])
                            #TODO clarify in API wich box the object is put to and from which the object is retrieved
                            #currently: first arg: in, second argument: out
                            if i == 2:
                                func(m, box[0], box[1])
                        elif c.message.type == "SORTING" or c.message.type == "INDEXEDLINE" or c.message.type == "MULTIPROCESSING":
                            colour = c.message.parameters
                            i = len(colour)
                            if i == 0:
                                func(m)
                            if i == 1:
                                func(m, colour[0])
                        elif c.message.type == "PUNCHING":
                            func(m)
                        elif c.message.type == "CONVEYOR":
                            mix = c.message.parameters
                            i = len(mix)
                            if i == 0:
                                func(m)
                            if i == 1:
                                ret = func(m, mix[0])
                            if i == 2:
                                if mix[0] == Direction.BACKWARD or mix[0] == Direction.FORWARD:
                                    func(m, mix[0], mix[1])
                                else:
                                    func(m, mix[1], mix[0])

                        # holds the function that is currently executed on each machine
                        self.currentlyExecuting[m] = [ret, c.message.commandId]
                        break
                    except AttributeError:
                        #TODO activate:
                        #raise JSONCommandNotSupportedOnThisMachineException()
                        # TODO append feedback to outputBuffer
                        # probably fixed
                        print("command not supported")
                        break
        if not b:
            print("unknown id")

