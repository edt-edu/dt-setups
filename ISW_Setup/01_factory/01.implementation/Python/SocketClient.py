import socket
from JSONReader import JSONReader

HOST = "127.0.0.1"  #localhost
#HOST = "192.168.1.102" current revpi S1s
PORT = 6000  # The port used by the server

receivedData = []

def clientListen():
    while True:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            print("trying to connect")
            s.connect((HOST, PORT))
            print("connected, waiting for data")
            while True:
                data = s.recv(1024)
                if not data.isspace():
                    print(f"Received {data!r}")
                    objdata = JSONReader.read(data)
                    print(objdata)
                    receivedData.append(objdata)


if __name__ == "__main__":
    clientListen()
