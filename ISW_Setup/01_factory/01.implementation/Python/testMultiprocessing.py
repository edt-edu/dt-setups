import multiprocessing
import queue
from random import random
from time import sleep
from threading import Thread
from multiprocessing import Process
from multiprocessing import Queue
from multiprocessing import Event
from SocketClient import clientListen


class testM:
    def __init__(self):
        self.inputBuffer = multiprocessing.Queue()
        self.outputBuffer = multiprocessing.Queue()
        Process(target=self.testTask1).start()
        Process(target=self.testTask2).start()

    def startup(self):
        i = 0
        Thread(target=self.task1).start()
        Process(target=self.task2).start()
        while not self.f.wait(0.03):
            self.e.set()
            print("put", flush=True)
            self.q.put(i)
            print("i = " + str(i), flush=True)
            i = i + 1

    def task1(self):
        j = 0
        while True:
            j = j + 1
            if j > 10000000:
                j = 0
                print("t1", flush=True)

    def task2(self):
        clientListen()

    def testTask1(self):
        eFlag = Event()
        while not eFlag.wait(0.5):
            r = random()
            self.outputBuffer.put(r, block=False)
            print("put " + str(r))

    def testTask2(self):
        while True:#not eFlag.wait(0.01):
            s = self.outputBuffer.get()
            print("got " + str(s))

# entry point
if __name__ == '__main__':
    t = testM()


