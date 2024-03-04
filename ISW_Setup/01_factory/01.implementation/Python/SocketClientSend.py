import socket
from JSONReader import JSONReader
import time
from fakeFeedbackGen import fakeFeedbackGen

HOST = "127.0.0.1"  #localhost
#HOST = "192.168.1.102" current revpi S1s
PORT = 6001  # The port used by the server

dataToSend = ["bla", "blubb"]

# def setup():
#     with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
#         s.connect((HOST, PORT))
#         print("connected")
def clientSend():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST, PORT))
        print("connected")
        while True:
            if len(dataToSend) > 0:
                s.sendall(bytes(dataToSend.pop(), "utf-8"))

if __name__ == "__main__":
    clientSend()
    #setup()
    # with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    #     s.connect((HOST, PORT))
    #     print("connected")
    #     f = fakeFeedbackGen()
    #     # clientSend()
    #     while True:
    #         if len(li) > 0:
    #             s.sendall(li.pop())
    #         v = f.create()
    #         if not v is None:
    #             print(v)
    #             li.append(bytes(v, "utf-8"))

