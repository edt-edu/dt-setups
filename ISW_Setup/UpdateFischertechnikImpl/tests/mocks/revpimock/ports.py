import logging
import threading
from typing import Tuple

from .edge import BOTH, FALLING, RISING


class IOPort:
    """IO port"""

    def __init__(self, name, value, blocking=False):
        """Create a new mock IO port

        `name`: port name (No dots or slashes)
        `value`: initial value
        `blocking`: Set True to wait for changes.
            Otherwise, the io changes are simulated.
        """
        self._name = name
        self._value = value
        self._blocking = blocking
        self._event_handlers = []
        self._waiting = []
        self._logger = logging.getLogger(f"{__name__}.{name}")

    @property
    def name(self):
        return self._name

    @property
    def value(self):
        return self._value

    def reg_event(self, func, delay=0, edge=BOTH, as_thread=False, prefire=False):
        self._logger.debug("Registering event handler")
        if as_thread:
            return
        self._event_handlers.append((func, edge))
        if prefire:
            func(self._name, self.value)

    def unreg_event(self, func=None, edge=None):
        self._logger.debug("Unregistering all event handlers")
        # Unregister all
        self._event_handlers = []

    def _fake_wait(self, edge=BOTH, exitevent=None, okvalue=None, timeout=0):
        if edge == BOTH:
            self.io_set_value(not self.value)
        elif edge == RISING:
            self.io_set_value(True)
        elif edge == FALLING:
            self.io_set_value(False)
        return 0

    def _blocking_wait(self, edge=BOTH, exitevent=None, okvalue=None, timeout=0):
        change_event = threading.Event()
        self._waiting.append((change_event, edge))
        self._logger.debug("Wait for change")
        if timeout == 0:
            timeout = None
            self._logger.debug("Waiting without a timeout")
        else:
            timeout = timeout / 1000
        signal = change_event.wait(timeout)
        if signal:
            return 0
        return 2

    def wait(self, edge=BOTH, exitevent=None, okvalue=None, timeout=0) -> int:
        if okvalue is not None:
            if okvalue == self.value:
                return -1
            elif okvalue:
                edge = RISING
            else:
                edge = FALLING
        if self._blocking:
            return self._blocking_wait(edge, exitevent, okvalue, timeout)
        else:
            return self._fake_wait(edge, exitevent, okvalue, timeout)

    def _edge_triggered(self, edge, old):
        return edge == BOTH or (self.value == (edge == RISING))

    def _wait_set_or_keep(self, wait: Tuple[threading.Event, int], old):
        "Set the event or return True based on the edge"
        event, edge = wait
        if self._edge_triggered(edge, old):
            event.set()
            return False
        return True

    def _value_is_valid(self, value):
        return True

    def _set_value(self, value):
        self._value = bool(value)

    def io_set_value(self, value):
        if not self._value_is_valid(value):
            raise AttributeError("Invalid value")
        old = self.value
        self._set_value(value)
        new = self.value
        for func, edge in self._event_handlers:
            if self._edge_triggered(edge, old):
                func(self._name, new)
        self._waiting[:] = [wait for wait in self._waiting if self._wait_set_or_keep(wait, old)]


class MockInput(IOPort):
    """Value boolean read only"""
    pass


class MockCounter(IOPort):
    """Value is a resettable integer count

    The value is always stored as unsigned, but might be interpreted as signed.
    """

    _U32_MAX = 2**32
    _S32_MAX = 2**31 - 1
    _S32_MIN = -(2**31)

    def __init__(self, name, value, blocking=False):
        super().__init__(name, value, blocking)
        self.signed = False

    @property
    def value(self):
        value = self._value
        if self.signed:
            if value > self._S32_MAX:
                return value - self._U32_MAX
            else:
                return value
        else:
            return value

    def _edge_triggered(self, edge, old):
        return True

    def _value_is_valid(self, value):
        return isinstance(value, int) and self._S32_MIN <= value < self._U32_MAX

    def _set_value(self, value):
        if value < 0:
            self._value = value + self._U32_MAX
        else:
            self._value = value

    def io_set_modulo(self, value):
        self.io_set_value(value % self._U32_MAX)

    def io_add(self, offset):
        self.io_set_value((self._value + offset) % self._U32_MAX)

    def io_subtract(self, offset):
        self.io_set_value((self._value - offset) % self._U32_MAX)

    def reset(self):
        self._logger.debug("Resetting the counter")
        self._set_value(0)


class MockOutput(IOPort):
    """Value is boolean settable"""

    @property
    def value(self):
        return self._value

    @value.setter
    def value(self, value):
        self._logger.debug("Set port to %s", value)
        self.io_set_value(value)

    def reg_event(self, func, delay=0, edge=BOTH, as_thread=False, prefire=False):
        raise Exception("Can not register events on output port")

    def unreg_event(self, func=None, edge=None):
        raise Exception("Can not unregister events on output port")

    def wait(self, edge=BOTH, exitevent=None, okvalue=None, timeout=0):
        raise Exception("Can not wait for value changes on output port")
