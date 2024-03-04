#!/usr/bin/env python

import threading
from SocketClient import clientListen


if __name__ == '__main__':
    jsoninputthread = threading.Thread(target=clientListen)
    jsoninputthread.start()