import logging
import threading
from typing import Any, Dict, Optional
from enum import Enum
from .impulse_direction_handler import impulse_direction_handler
from revpimodio2 import RISING


class State(Enum):
    """ The state of the actuator
    """
    MOVING_FORWARD = "moving-forward"
    "The actuator is moving forwards (no targeting)"
    MOVING_BACKWARD = "moving-backward"
    "The actuator is moving backwards (no targeting)"
    TARGETING = "targeting"
    "A targeting operation is in progress"
    STOPPED = "stopped"
    "Set if no other action is performed"
    RESETTING = "resetting"
    "Set during initialisation or reset"


class Reason(Enum):
    """ The reason for the actuator state
    """
    CMD = "cmd"
    "A request caused this state (or during reset)"
    TARGET_REACHED = "target-reached"
    "A targeting operation stopped (STOPPED after TARGETING)"
    POSITION_LIMIT = "position-limit"
    "A position limit caused this stop (STOPPED)"


class ImpulseActuator1D:

    @staticmethod
    def get_from_configuration(forwardActuator: Any,
                               backwardActuator: Any,
                               backwardLimitSwitch: Any,
                               impulseCounter: Any,
                               config: Dict[str, Any],
                               logger: Optional[logging.Logger] = None,
                               lock: Optional[threading.Lock] = None,
                               setupTimeout: int = 30_000):
        forward_limit = config["forward_limit"]  # Required
        if forward_limit <= 0:
            raise AttributeError("Actuator forward limit (%d) must be positive", forward_limit)
        elif forward_limit >= 2147483648:
            raise AttributeError("Actuator forward limit (%d) is out of bounds", forward_limit)
        return ImpulseActuator1D(forwardActuator,
                                 backwardActuator,
                                 backwardLimitSwitch,
                                 impulseCounter,
                                 forward_limit,
                                 logger=logger,
                                 lock=lock,
                                 setupTimeout=setupTimeout)

    def __init__(self,
                 forwardActuator: Any,
                 backwardActuator: Any,
                 backwardLimitSwitch: Any,
                 impulseCounter: Any,
                 impulseForwardLimit: int,
                 setupTimeout: int = 30_000,
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None
                 ):
        if logger is None:
            self._logger = logging.getLogger(__name__)
        else:
            self._logger = logger
        if lock is None:
            self._lock = threading.Lock()
        else:
            self._lock = lock

        self._impulse_direction_handler = impulse_direction_handler("FORWARD", 0)

        self._forwardActuator = forwardActuator
        self._backwardActuator = backwardActuator
        self._backwardLimitSwitch = backwardLimitSwitch
        self._impulseCounter = impulseCounter
        self._impulseForwardLimit = impulseForwardLimit
        self._state = State.RESETTING
        self._reason = Reason.CMD
        self._state_changed = True
        self._target_position: Optional[int] = 0
        self._currentPositon: int = 0
        self._impulses: int = 0
        self._temp: int = 0
        self._direction = True #True == Forward, False == Backward

        self._logger.info("Unregistering sensor events")
        backwardLimitSwitch.unreg_event()
        impulseCounter.unreg_event()

        self._logger.info("Registering backward actuator limit stop event handler")
        backwardLimitSwitch.reg_event(self._limit_switch_event)

        self._reset(timeout=setupTimeout)

        self._logger.info("Registering Impulse counter event")
        impulseCounter.reg_event(self._impulse_counter_event)

        self._state = State.STOPPED
        self._reason = Reason.CMD
        self._state_changed = True

    @property
    def lock(self) -> threading.Lock:
        return self._lock

    @property
    def is_moving(self) -> bool:
        return self._forwardActuator.value or self._backwardActuator.value

    @property
    def is_moving_forward(self) -> bool:
        return self._forwardActuator.value

    @property
    def is_moving_backward(self) -> bool:
        return self._backwardActuator.value

    def _reset(self, timeout: int = 30000):
        self._logger.info("Resetting the impulse actuator position and counter")
        self._forwardActuator.value = False
        if not self._backwardLimitSwitch.value:
            self._backwardActuator.value = True
            self._backwardLimitSwitch.wait(edge=RISING, okvalue=True, timeout=timeout)
        self._backwardActuator.value = False
        if not self._backwardLimitSwitch.value:
            raise Exception("Could not reset the actuator")
        self._impulseCounter.reset()
        self._logger.debug("Reset completed")

    def destroy(self):
        """Stop all movements and unregister event handlers

        The calling method must hold the lock.
        """
        self._stop_moving()
        self._backwardLimitSwitch.unreg_event()
        self._impulseCounter.unreg_event()

    def reset(self):
        """Reset the actuator

        The calling method must hold the lock.
        """
        self._state = State.RESETTING
        self._reason = Reason.CMD
        self._reset()

    def _limit_switch_event(self, name, value):
        """Stop backward motion if hit the limit switch"""
        with self.lock:
            self._state_changed = True
            if self.is_moving_backward:
                self._logger.info("Hit backward limit switch, stopping backward movement")
                self._backwardActuator.value = False
                self._impulseCounter.reset()
                self._currentPositon = 0
                self._target_position = None
                self._stop_moving()
                self._state = State.STOPPED
                self._reason = Reason.POSITION_LIMIT

    def _stop_moving(self):
        self._forwardActuator.value = False
        self._backwardActuator.value = False
        self._target_position = None
        self._impulseCounter.reset()

    def stop_moving(self):
        """Stop all moving actions

        Also trigger a possible target event to prevent infinite waiting.

        The calling method must hold the lock.
        """
        self._state = State.STOPPED
        self._reason = Reason.CMD
        self._state_changed = True
        self._stop_moving()
        if self._impulse_direction_handler.get_direction() == "FORWARD":
            self._currentPositon = self._currentPositon + self._impulseCounter.value
        else:
            self._currentPositon = self._currentPositon - self._impulseCounter.value
        self._logger.info("Current Position: "+str(self._currentPositon))
        self._impulseCounter.reset()

    def _impulse_counter_event(self, name, value):
        with self.lock:
            self._state_changed = True
            if self._target_position is not None:
                if value >= self._target_position:
                    self._stop_moving()
                    self._state = State.STOPPED
                    self._reason = Reason.TARGET_REACHED
                    if self._impulse_direction_handler.get_direction() == "FORWARD":
                        self._currentPositon = self._currentPositon + self._impulseCounter.value
                    else:
                        self._currentPositon = self._currentPositon - self._impulseCounter.value
                    self._impulseCounter.reset()
                    self._logger.info("Reached target position, stopping movement")
            if value >= self._impulseForwardLimit and self._is_moving_forward:
                self._logger.info("Reached forward impulse limit, stopping forward movement")
                self._stop_moving()
                self._state = State.STOPPED
                self._reason = Reason.POSITION_LIMIT

    @property
    def state(self):
        return self._state

    @property
    def _is_moving_forward(self) -> bool:
        return self._forwardActuator.value

    @property
    def _is_moving_backward(self) -> bool:
        return self._backwardActuator.value

    @property
    def is_at_reset(self) -> bool:
        """
        The position encoder limit MIGHT BUT SHOULD NEVER BE active
        while the limit switch is not active.
        NOTE: If this happens, the consequences are likely minor, because
            both, the limit switch and the lower position limit, can stop
            the actuators.
        """
        return self._backwardLimitSwitch.value

    @property
    def is_at_forward_limit(self) -> bool:
        return self._impulseCounter.value >= self._impulseForwardLimit

    def _move_forward(self):
        if self._is_moving_backward:
            self._logger.debug("Switch from backward to forward movement")
            self._backwardActuator.value = False
        self._forwardActuator.value = True

    def _move_backward(self):
        if self._is_moving_forward:
            self._logger.debug("Switch from forward to backward movement")
            self._forwardActuator.value = False
        self._backwardActuator.value = True

    def move_forward(self):
        """Move this actuator forward

        The calling method must hold the lock.
        """
        self._target_position = None
        if self.is_at_forward_limit:
            self._state = State.STOPPED
            self._reason = Reason.POSITION_LIMIT
        else:
            self._move_forward()
            self._state = State.MOVING_FORWARD
            self._reason = Reason.CMD
        self._state_changed = True

    def move_backward_for_reset(self):
        """Move backward until at the reset position

        The calling method must hold the lock.
        """
        if self._is_moving_forward:
            self._logger.debug("Switch from forward to backward movement")
            self._forwardActuator.value = False

        self._state = State.RESETTING
        self._reason = Reason.CMD
        self._state_changed = True
        if not self._backwardLimitSwitch.value:
            self._backwardActuator.value = True
        else:
            self._state = State.STOPPED
        
    def move_backward(self):
        """Move this actuator backward

        The calling method must hold the lock.
        """
        self._target_position = None
        if self.is_at_reset:
            self._state = State.STOPPED
            self._reason = Reason.POSITION_LIMIT
        else:
            self._move_backward()
            self._state = State.MOVING_BACKWARD
            self._reason = Reason.CMD
        self._state_changed = True

    def _move_impulse_forward(self, targetCount: int):
        if targetCount < 0:
            raise AttributeError(f"Target count can not be negative ${targetCount}")
        if targetCount > self._impulseForwardLimit:
            raise AttributeError(f"Target count out of bounds ${targetCount} > ${self._impulseForwardLimit}")
        if self._impulseCounter.value >= targetCount:
            self._stop_moving()
            self._state = State.STOPPED
            self._reason = Reason.TARGET_REACHED
        else:
            self._target_position = targetCount
            self._move_forward()

    def _move_impulse(self, target: int):
        """Move this actuator to the given position"""
        if not 0 <= target <= self._impulseForwardLimit:
            raise AttributeError("Target position out of range.\n"
                                 f"Test '0 <= ${target} <= ${self._impulseForwardLimit}' failed")

        self._impulse_direction_handler.calculate(self._currentPositon, target)
        
        if self._impulseCounter.value < self._impulse_direction_handler.get_impulses():
            if self._impulse_direction_handler.get_direction() == "FORWARD":
                self._state = State.MOVING_FORWARD
                self._reason = Reason.CMD
                self._target_position = self._impulse_direction_handler.get_impulses()
                self._move_forward()
            elif self._impulse_direction_handler.get_direction() == "BACKWARD":
                self._state = State.MOVING_BACKWARD
                self._reason = Reason.CMD
                self._target_position = self._impulse_direction_handler.get_impulses()
                self._move_backward()

    def state_changed(self):
        """Test, if any state changed compared to the last call to `get_status()`

        The calling method should hold the lock.
        """
        return self._state_changed

    def get_status(self):
        """Get the current state

        The calling method must hold the lock.
        """
        status = {
            "state": self._state,
            "reason": self._reason,
            "limit-switch": self._backwardLimitSwitch.value,
            "position": self._impulseCounter.value
        }
        if self._target_position is not None:
            status["target"] = self._target_position

        self._state_changed = False
        return status

    def handle_control(self, cmd: str,
                       forward: str = "forward",
                       backward: str = "backward"):
        """Unify actuator control handling

        The command should be any of `<forward>`, `<backward>`, `stop` or `to=<int>`.
        """
        self._state_changed = True
        if cmd.startswith(forward):
            self.move_forward()
        elif cmd.startswith(backward):
            self.move_backward_for_reset()
        elif cmd.startswith('stop'):
            self.stop_moving()
        elif cmd.startswith('to='):
            self._move_impulse(int(cmd.split('=')[1]))
        else:
            self._logger.warning("Unknown control command '%s'", cmd)
