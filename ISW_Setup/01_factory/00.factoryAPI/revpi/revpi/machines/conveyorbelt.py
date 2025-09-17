import logging
import threading
from typing import Any, Dict, Optional
from enum import Enum
from revpimodio2.io import IOBase

from .base_machine import BaseMachine


class State(Enum):
    """The state of the actuator"""
    MOVING_RIGHT = "moving-right"
    "The actuator is moving right (no targeting)"
    MOVING_LEFT = "moving-left"
    "The actuator is moving left (no targeting)"
    TARGETING = "targeting"
    "A targeting operation is in progress"
    STOPPED = "stopped"
    "Set if no other action is performed"
    RESETTING = "reset"
    "Initial reset in setup"


class Reason(Enum):
    """The reason for the actuator state"""
    CMD = "cmd"
    "A request caused this state (or during reset)"
    TARGET_REACHED = "target-reached"
    "A targeting operation stopped (STOPPED after TARGETING)"
    SENSOR_REACHED = "position-limit"
    "A position limit caused this stop (STOPPED)"


class Conveyorbelt(BaseMachine):
    """Conveyorbelt

    If not specified otherwise, all method calls expect the lock to be locked.
    """

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):

        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)

        self._logger.debug("starting port initialisation")

        self._state = State.RESETTING
        self._reason = Reason.CMD
        self._state_changed = True

        self._senseLeft = ports['senseLeft']
        self._senseRight = ports['senseRight']
        self._senseImpulse = ports['senseImpulse']

        self._actRight = ports['actRight']
        self._actLeft = ports['actLeft']

        self._actLeft.value = False
        self._actRight.value = False

        ports['senseLeft'].unreg_event()
        ports['senseRight'].unreg_event()
        ports['senseImpulse'].unreg_event()

        ports['senseImpulse'].reset()

        "ports['senseLeft'].reg_event(self._sense_left_event)"
        "ports['senseRight'].reg_event(self._sense_right_event)"
        ports['senseImpulse'].reg_event(self._impulse_event)

        self._targetCount = 10000
        self._targeting = False

    @property
    def leftSensor(self):
        return self._senseLeft.value

    @property
    def rigthSensor(self):
        return self._senseRight.value

    @property
    def impulseCounter(self):
        return self._senseImpulse.value

    @property
    def _is_moving_right(self) -> bool:
        return self._actRight.value

    @property
    def _is_moving_left(self) -> bool:
        return self._actLeft.value

    @property
    def _is_moving(self) -> bool:
        return self._is_moving_left or self._is_moving_right

    def _stop_moving(self):
        if self._is_moving_left:
            self._actLeft.value = False
        if self._is_moving_right:
            self._actRight.value = False

    def stop_moving(self):
        self._state_changed = True
        self._state = State.STOPPED
        self._reason = Reason.CMD
        self._stop_moving()

    def _move_right(self):
        if self._is_moving_left:
            self._logger.debug("Switch from left to right movement")
            self._actLeft.value = False
        self._actRight.value = True

    def move_right(self):
        self._state_changed = True
        self._state = State.MOVING_RIGHT
        self._reason = Reason.CMD
        self._move_right()

    def move_left(self):
        self._state_changed = True
        self._state = State.MOVING_LEFT
        self._reason = Reason.CMD
        self._move_left()

    def _move_left(self):
        if self._is_moving_right:
            self._logger.debug("Switch from right to left movement")
            self._actRight.value = False
        self._actLeft.value = True

    def move_right_to(self, targetCount: int):
        self._stop_moving()
        self._targeting = True
        self._targetCount = targetCount
        self._senseImpulse.reset()
        self._state_changed = True
        self._state = State.TARGETING
        self._reason = Reason.CMD
        self._move_right()

    def move_left_to(self, targetCount: int):
        self._stop_moving()
        self._targeting = True
        self._targetCount = targetCount
        self._senseImpulse.reset()
        self._state_changed = True
        self._state = State.TARGETING
        self._reason = Reason.CMD
        self._move_left()

    def _sense_right_event(self, name, value):
        """Stop right movement if reached right sensor"""
        with self.lock:
            self._state_changed = True
            if self._is_moving_right and (not value):
                self._targeting = False
                self._actRight.value = False
                self._state = State.STOPPED
                self._reason = Reason.SENSOR_REACHED
                self._senseImpulse.reset()

    def _sense_left_event(self, name, value):
        """Stop left movement if reached left sensor"""
        with self.lock:
            self._state_changed = True
            if self._is_moving_left and (not value):
                self._targeting = False
                self._actLeft.value = False
                self._state = State.STOPPED
                self._reason = Reason.SENSOR_REACHED
                self._senseImpulse.reset()

    def _impulse_event(self, name, value):
        """Stop movement when target reached"""
        with self.lock:
            self._state_changed = True
            if self._is_moving and self._targeting and self._senseImpulse.value >= self._targetCount:
                self._targeting = False
                self._state = State.STOPPED
                self._reason = Reason.TARGET_REACHED
                self._stop_moving()
                self._senseImpulse.reset()
                self._logger.info("Reached target position, stopping movement")

    def handle_control(self, control: dict):
        try:
            cmd = control["motor"]
            self._state_changed = True
            if cmd.startswith('right'):
                if cmd.startswith('right-to='):
                    self.move_right_to(int(cmd.split('=')[1]))
                else:
                    self.move_right()
            elif cmd.startswith('left'):
                if cmd.startswith('left-to='):
                    self.move_left_to(int(cmd.split('=')[1]))
                else:
                    self.move_left()
            elif cmd.startswith('stop'):
                self.stop_moving()
            else:
                self._logger.warning("Unknown motor command %s", cmd)
        except KeyError:
            pass

    def cleanup(self):
        with self.lock:
            self.destroy()

    def state_changed(self):
        return self._state_changed

    @property
    def state(self):
        return self._state

    def get_status(self):
        status = {
            "state": self._state.value,
            "reason": self._reason.value,
            "leftSensor": not self.leftSensor,
            "rightSensor": not self.rigthSensor,
            "position": self.impulseCounter
        }
        if self._targeting:
            status["target"] = self._targetCount

        self._state_changed = False
        return status

    def destroy(self):
        self.stop_moving()
        self._senseImpulse.unreg_event()
        self._senseLeft.unreg_event()
        self._senseRight.unreg_event()
