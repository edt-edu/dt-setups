"""
Simulate a 1D actuator with an impulse counter
"""
import logging
from typing import Optional

from .simulator_item import SimulatorItem
from ..mocks.revpimock.ports import MockCounter, MockInput, MockOutput


class ImpulseActuator(SimulatorItem):

    def __init__(self, act_fwd: MockOutput,
                 act_bwd: MockOutput,
                 sense_bwd_limit: MockInput,
                 impulse_counter: MockCounter,
                 initial_position: float = 0,
                 speed: float = 2,
                 logger: Optional[logging.Logger] = None):
        self._act_fwd = act_fwd
        self._act_bwd = act_bwd
        self._sense_bwd_limit = sense_bwd_limit
        self._impulse_counter = impulse_counter
        self._position = initial_position
        self._actuator_speed = speed
        if logger is not None:
            self._logger = logger
        else:
            self._logger = logging.getLogger(__name__)

    @property
    def position(self):
        return self._position

    @property
    def is_moving(self):
        return self._moving_forward() or self._moving_backward()

    def _moving_forward(self):
        return self._act_fwd.value and not self._act_bwd.value

    def _moving_backward(self):
        return self._act_bwd.value and not self._act_fwd.value

    def _moving_error(self):
        return self._act_bwd.value and self._act_fwd.value

    def step(self):
        if self._moving_error():
            self._logger.error("Movement error: Forward and backward actuator set")
        elif self._moving_forward():
            position = self._position
            new_position = position + self._actuator_speed
            diff = int(new_position) - int(position)
            self._impulse_counter.io_add(diff)
            self._position = new_position
            self._logger.info("Move forward to %d", new_position)
        elif self._moving_backward():
            position = self._position
            new_position = position - self._actuator_speed
            diff = int(position) - int(new_position)
            self._impulse_counter.io_add(diff)
            self._position = new_position
            self._logger.info("Move backward to %d", new_position)
        if self._position <= 0:
            if not self._sense_bwd_limit.value:
                self._logger.info("Trigger backward limit switch")
                self._sense_bwd_limit.io_set_value(True)
        else:
            if self._sense_bwd_limit.value:
                self._logger.info("Release backward limit switch")
                self._sense_bwd_limit.io_set_value(False)
