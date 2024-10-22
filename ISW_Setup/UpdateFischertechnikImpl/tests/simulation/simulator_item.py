from abc import ABC, abstractmethod
from typing import Callable


class SimulatorItem(ABC):
    @abstractmethod
    def step(self):
        """Called at every simulation step"""
        pass


class SimulatorFunction(SimulatorItem):
    """Executes a function at every simulation step"""

    def __init__(self, func):
        self._func = func

    def step(self):
        self._func()

    @staticmethod
    def decorator(func: Callable[[], None]) -> SimulatorItem:
        """Return a SimulatorItem which runs the given function on every step"""
        return SimulatorFunction(func)
