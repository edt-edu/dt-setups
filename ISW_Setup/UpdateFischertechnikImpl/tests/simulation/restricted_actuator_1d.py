"""
Simulate a 1D actuator limited at both ends with an im
"""
import logging
from typing import Optional

from ..mocks.revpimock.ports import MockInput, MockOutput
from .simulator_item import SimulatorItem


class RestrictedActuator1D(SimulatorItem):

    def __init__(self, act_fwd: MockOutput,
                 act_bwd: MockOutput,
                 sense_fwd_limit: MockInput,
                 sense_bwd_limit: MockInput,
                 initial_position: float = 0,
                 movement_span: int = 1000,
                 speed: float = 2,
                 logger: Optional[logging.Logger] = None):
        self._act_fwd = act_fwd
        self._act_bwd = act_bwd
        self._sense_fwd_limit = sense_fwd_limit
        self._sense_bwd_limit = sense_bwd_limit
        self._position = initial_position
        self._movement_span = movement_span
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

    @property
    def at_backward_limit(self):
        return self._sense_bwd_limit.value

    @property
    def at_forward_limit(self):
        return self._sense_fwd_limit.value

    def step(self):
        if self._moving_error():
            self._logger.error("Movement error: Forward and backward actuator set")
        elif self._moving_forward():
            self._position += self._actuator_speed
            self._logger.info("Move forward to %d", self._position)
        elif self._moving_backward():
            self._position -= self._actuator_speed
            self._logger.info("Move backward to %d", self._position)
        if self._position <= 0:
            if not self._sense_bwd_limit.value:
                self._logger.info("Trigger backward limit switch")
                self._sense_bwd_limit.io_set_value(True)
        else:
            if self._sense_bwd_limit.value:
                self._logger.info("Release backward limit switch")
                self._sense_bwd_limit.io_set_value(False)
        if self._position >= self._movement_span:
            if not self._sense_fwd_limit.value:
                self._logger.info("Trigger forward limit switch")
                self._sense_fwd_limit.io_set_value(True)
        else:
            if self._sense_fwd_limit.value:
                self._logger.info("Release forward limit switch")
                self._sense_fwd_limit.io_set_value(False)
