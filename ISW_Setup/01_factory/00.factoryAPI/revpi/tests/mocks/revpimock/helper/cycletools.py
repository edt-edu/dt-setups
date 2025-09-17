from ..edge import BOTH, FALLING, RISING
from ..ports import IOPort


class Var:
    """Store cycle variables"""

    def __init__(self):
        self._vars = dict()

    def __getattr__(self, item):
        return self._vars[item]

    def __setattr__(self, key, value):
        self._vars[key] = value


class Cycletools:
    """Control revpi cycle execution"""

    def __init__(self, cycletime, rpi, fake_changed_value=False):
        self._cycletime = cycletime
        self._rpi = rpi
        self._cycle = -1
        self._fake_changed_value = fake_changed_value
        self._last_values = dict()

    @property
    def first(self):
        return self._cycle == 0

    @property
    def flag1c(self):
        return self._cycle % 2 == 0

    @property
    def flag5c(self):
        return self._cycle % 10 < 5

    @property
    def flag10c(self):
        return self._cycle % 20 < 10

    @property
    def flag15c(self):
        return self._cycle % 30 < 15

    @property
    def flag20c(self):
        return self._cycle % 40 < 20

    @property
    def flank5c(self):
        return self._cycle % 5 == 0

    @property
    def flank10c(self):
        return self._cycle % 10 == 0

    @property
    def flank15c(self):
        return self._cycle % 15 == 0

    @property
    def flank20c(self):
        return self._cycle % 20 == 0

    @property
    def runtime(self):
        return 0

    def _update_port_values(self):
        for port in self._rpi.io.ports:
            self._last_values[port.name] = port.value

    def next_cycle(self):
        self._cycle += 1

    def cycle_end(self):
        self._update_port_values()

    def changed(self, io_object: IOPort, edge=BOTH):
        """Test, if the value of an io port changed compared to the last cycle"""
        if self._fake_changed_value is not None:
            return self._fake_changed_value
        changed = self._last_values[io_object.name] != io_object.value
        if not changed:
            return False
        if edge == RISING:
            return io_object.value
        elif edge == FALLING:
            return not io_object.value
        return True
