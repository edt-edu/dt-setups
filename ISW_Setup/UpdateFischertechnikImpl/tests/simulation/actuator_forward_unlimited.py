"""
Simulate an actuator only capable of moving forward with an impulse counter and no position limit
"""
import logging
from typing import Optional

from .simulator_item import SimulatorItem
from ..mocks.revpimock.ports import MockCounter, MockOutput


class UnlimitedForwardActuator(SimulatorItem):

    def __init__(self, act_fwd: MockOutput,
                 impulse_counter: MockCounter,
                 speed: float = 2,
                 logger: Optional[logging.Logger] = None):
        self._act_fwd = act_fwd
        self._impulse_counter = impulse_counter
        self._position = 0
        self._actuator_speed = speed
        if logger is not None:
            self._logger = logger
        else:
            self._logger = logging.getLogger(__name__)

    @property
    def position(self):
        return self._position

    @property
    def is_moving_forward(self):
        return self._act_fwd.value

    def step(self):
        if self.is_moving_forward:
            position = self._position
            new_position = position + self._actuator_speed
            diff = int(new_position) - int(position)
            self._impulse_counter.io_add(diff)
            self._position = new_position
            self._logger.info("Moving forward")
