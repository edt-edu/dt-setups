from threading import Thread
from threading import Event


class testThreading:

    def __init__(self):
        self.__e = Event()
        self.__f = Event()

    def f1(self):
        while not self.__e.wait(0.03):
            print("f1")
            self.__f.set()
            print(self.__f.is_set())

    def f2(self):
        while not self.__f.wait(0.02):
            self.__f.clear()
            print("f2")

    def f3(self):
        while self.__f.wait():
            self.__f.clear()
            print("f3")

if __name__ == "__main__":
    test = testThreading()
    Thread(target=test.f1).start()
    Thread(target=test.f2).start()
    Thread(target=test.f3).start()