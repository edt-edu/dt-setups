import time
class fakeFeedbackGen:
    def __init__(self):
        self.i = -1
        self.last = time.time()

    def create(self):
        if self.last + 2 < time.time():
            l = ["bla\n", "blu\n", "blo", "ble"]
            self.last = time.time()
            self.i = self.i + 1
            print("used index " + str(self.i))
            return l[self.i]
