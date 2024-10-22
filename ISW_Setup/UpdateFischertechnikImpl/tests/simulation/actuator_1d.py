"""
Simulate a 1D actuator
"""
import logging
from typing import Optional

from ..mocks.revpimock.ports import MockCounter, MockInput, MockOutput
from .simulator_item import SimulatorItem


class Actuator1D(SimulatorItem):
    def __init__(self, forward_actuator: MockOutput,
                 backward_actuator: MockOutput,
                 backward_limit_switch: MockInput,
                 position_encoder: MockCounter,
                 initial_position: int = 0,
                 speed: int = 5,
                 start_speed: int = 2,
                 overshoot: int = 20,
                 limit_switch_max_set: int = 15,
                 logger: Optional[logging.Logger] = None):
        """ Simulated 1d actuator

        `speed`: Move speed units per step
        `limit_switch_max_set`: Maximum position for the limit switch to be active
        """
        self.forward_actuator = forward_actuator
        self.backward_actuator = backward_actuator
        self.backward_limit_switch = backward_limit_switch
        self.position_encoder = position_encoder
        self.position = initial_position
        "Current position"
        self.speed = speed
        self.start_speed = start_speed
        self.overshoot = overshoot
        self.is_moving_forward = False
        self.is_moving_backward = False
        self.limit_switch_max_set = limit_switch_max_set
        if logger is not None:
            self._logger = logger
        else:
            self._logger = logging.getLogger(__name__)

    @property
    def is_moving(self):
        return (self._moving_forward()
                or self._moving_backward()
                or self.is_moving_forward
                or self.is_moving_backward)

    def _moving_forward(self):
        return self.forward_actuator.value and not self.backward_actuator.value

    def _moving_backward(self):
        return self.backward_actuator.value and not self.forward_actuator.value

    def _moving_error(self):
        return self.backward_actuator.value and self.forward_actuator.value

    def step(self):
        if self._moving_error():
            self._logger.error("Movement error: Forward and backward actuator set")
        elif self._moving_forward():
            if self.is_moving_forward:
                speed = self.speed
            else:
                speed = self.start_speed
                self.is_moving_forward = True
                self.is_moving_backward = False
            self.position += speed
            self.position_encoder.io_add(speed)
            self._logger.info("Move forward to %d", self.position)
        elif self._moving_backward():
            if self.is_moving_backward:
                speed = self.speed
            else:
                speed = self.start_speed
                self.is_moving_backward = True
                self.is_moving_forward = False
            self.position -= speed
            self.position_encoder.io_subtract(speed)
            self._logger.info("Move backward to %d", self.position)
        elif self.is_moving_forward:
            self.position += self.overshoot
            self.position_encoder.io_add(self.overshoot)
            self._logger.info("Overshoot forward to %d", self.position)
            self.is_moving_forward = False
        elif self.is_moving_backward:
            self.position -= self.overshoot
            self.position_encoder.io_subtract(self.overshoot)
            self._logger.info("Overshoot backward to %d", self.position)
            self.is_moving_backward = False
        if self.position <= self.limit_switch_max_set:
            if not self.backward_limit_switch.value:
                self._logger.info("Trigger limit switch")
                self.backward_limit_switch.io_set_value(True)
        else:
            if self.backward_limit_switch.value:
                self._logger.info("Release limit switch")
                self.backward_limit_switch.io_set_value(False)
