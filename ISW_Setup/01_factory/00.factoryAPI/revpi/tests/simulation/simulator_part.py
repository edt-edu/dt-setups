import logging
from typing import Callable, List

from .simulator_item import SimulatorItem


class SimulatorPart(SimulatorItem):

    def __init__(self, logger: logging.Logger):
        self.logger = logger
        self._items: List[SimulatorItem] = []
        self._functions: List[Callable[[], None]] = []

    def add_item(self, item: SimulatorItem):
        self._items.append(item)

    def add_function(self, function: Callable[[], None]):
        self._functions.append(function)

    def step(self):
        for item in self._items:
            item.step()
        for function in self._functions:
            function()
