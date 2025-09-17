""" Mock RevPiModIO
"""
from typing import Any, Callable

from .helper.cycletools import Cycletools
from .io import IO


class RevPiModIO:

    def __init__(self, ports: dict,
                 autorefresh: bool = True,
                 configsrc: Any = None,
                 fake_changed_value=None):
        self._autorefresh = autorefresh
        self._configsrc = configsrc
        self.io = IO(ports)
        self._fake_changed_value = fake_changed_value
        self._running = False

    def mainloop(self, freeze=False, blocking=True):
        """Start the loop"""
        if self._running:
            raise Exception('Already running')
        self._running = True

    def exit(self, full=True):
        """Terminate a running loop

        If `full`=True, remove all devices from autorefresh and sync outputs.
        """
        self._running = False
        self._cycle_func = None

    def cycleloop(self, func: Callable[[Cycletools], None], cycletime=50, blocking=True):
        """Run the function every cycle"""
        raise Exception("Not implemented")

    def mock_cycle(self):
        pass
