import logging
import threading
from typing import Any, Dict, Optional, Tuple
from enum import Enum

from revpimodio2 import RISING


class State(Enum):
    """The state of the actuator"""
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
    """The reason for the actuator state"""
    CMD = "cmd"
    "A request caused this state (or during reset)"
    TARGET_REACHED = "target-reached"
    "A targeting operation stopped (STOPPED after TARGETING)"
    POSITION_LIMIT = "position-limit"
    "A position limit caused this stop (STOPPED)"


class Actuator1D:
    """1-Dimensional actuator

    Can move forward and backward and has a backward limit switch
    and a position encoder increasing forward.

    Will ensure that the actuator stays within given position limits.
    """

    @staticmethod
    def get_from_configuration(forwardActuator: Any,
                               backwardActuator: Any,
                               backwardLimitSwitch: Any,
                               positionEncoder: Any,
                               config: Dict[str, Any],
                               logger: Optional[logging.Logger] = None,
                               lock: Optional[threading.Lock] = None,
                               setupTimeout: int = 30_000):
        """Helper for creating a new 1d actuator with given configuration.

        Provides defaults for some configuration values.
        Guards against invalid values and warns iff improbable values are given.
        """
        if logger is None:
            logger = logging.getLogger(__name__)
        forward_limit = config["forward_limit"]  # Required
        if forward_limit <= 0:
            raise AttributeError("Actuator forward limit (%d) must be positive", forward_limit)
        elif forward_limit >= 2147483648:
            raise AttributeError("Actuator forward limit (%d) is out of bounds", forward_limit)
        accuracy = config.get("accuracy", 5)
        if accuracy <= 0:
            raise AttributeError("Actuator accuracy (%d) must be positive", accuracy)
        stop_offset = config.get("stop_offset", 20)
        if stop_offset <= 0:
            raise AttributeError("Actuator stop offset (%d) must be positive", stop_offset)
        backward_limit = config.get("backward_limit", -10)
        if backward_limit > 0:
            logger.warning("Actuator backward limit is positive %d", backward_limit)
        return Actuator1D(forwardActuator,
                          backwardActuator,
                          backwardLimitSwitch,
                          positionEncoder,
                          forward_limit,
                          positionAccuracy=accuracy,
                          stopOffset=stop_offset,
                          backwardPositionLimit=backward_limit,
                          logger=logger,
                          lock=lock,
                          setupTimeout=setupTimeout)

    def __init__(self, forwardActuator: Any,
                 backwardActuator: Any,
                 backwardLimitSwitch: Any,
                 positionEncoder: Any,
                 forwardPositionLimit: int,
                 positionAccuracy: int = 5,
                 stopOffset: int = 20,
                 backwardPositionLimit: int = -10,
                 setupTimeout: int = 30_000,
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):
        """
        Will wait (block) for up to `setupTimeout` milliseconds for the actuator to reset.

        `signalCallback`: Callback to send status updates
        `positionAccuracy`: The distance the machine is expected to move within one cycle
        `stopOffset`: Stop the actuator early to prevent overshoot (when targeting a position)
        `backwardPositionLimit`: Unsigned 32-bit encoder underflows.
            Thus, every value above this limit is treated as the backward limit.
            Should be higher than the `forwardPositionLimit`.
        `setupTimeout`: Number of milliseconds to wait for the reset operation. 0 disables waiting
        `lock`: The lock to guard all state changes.
            This class will only acquire/release the lock from port event handlers.
            If set, `.lock` is the same lock.

        All events on the `backwardLimitSwitch` and `positionEncoder` are unregistered.
        """
        if logger is None:
            self._logger = logging.getLogger(__name__)
        else:
            self._logger = logger
        if lock is None:
            self._lock = threading.Lock()
        else:
            self._lock = lock
        self._forwardActuator = forwardActuator
        self._backwardActuator = backwardActuator
        self._backwardLimitSwitch = backwardLimitSwitch
        self._positionEncoder = positionEncoder
        positionEncoder.signed = True  # The position encoder has a signed output
        if forwardPositionLimit <= 0:
            raise AttributeError("forwardPositionLimit must be positive")
        self._forwardPositionLimit = forwardPositionLimit
        if positionAccuracy <= 0:
            raise AttributeError("positionAccuracy must be positive")
        self._positionAccuracy = positionAccuracy
        self._stopOffset = stopOffset
        self._backwardPositionLimit = backwardPositionLimit
        self._is_targeting = False
        self._target_position: Optional[int] = None

        self._state: Tuple[State, Reason]
        self._state = State.RESETTING, Reason.CMD
        self._state_changed = True

        self._logger.info("Unregistering sensor events")
        backwardLimitSwitch.unreg_event()
        positionEncoder.unreg_event()

        self._logger.info("Registering backward actuator limit stop event handler")
        backwardLimitSwitch.reg_event(self._limit_switch_event)

        self._reset(timeout=setupTimeout)

        self._logger.info("Registering backward actuator limit stop event handler")
        positionEncoder.reg_event(self._encoder_event)

        self._state = State.STOPPED, Reason.CMD

    def _reset(self, timeout: int):
        """Reset the actuator position and encoder to the backward limit

        This needs to run before any encoder limit events are registered!
        """
        self._logger.info("Resetting the actuator position and counter")
        self._forwardActuator.value = False
        if not self._backwardLimitSwitch.value:
            self._backwardActuator.value = True
            self._backwardLimitSwitch.wait(edge=RISING, okvalue=True, timeout=timeout)
        self._backwardActuator.value = False
        if not self._backwardLimitSwitch.value:
            raise Exception("Could not reset the actuator")
        self._positionEncoder.reset()
        self._logger.debug("Reset completed")

    def destroy(self):
        """Stop all movements and unregister event handlers

        The calling method must hold the lock.
        """
        self._stop_moving()
        self._positionEncoder.unreg_event()
        self._backwardLimitSwitch.unreg_event()

    def _limit_switch_event(self, name, value):
        """Stop backward motion if hit the limit switch"""
        with self.lock:
            if value and self._is_moving_backward:
                self._logger.info("Hit backward limit switch, stopping backward movement")
                if self._is_targeting and self._moving_within_target(self._target_position):
                    self._state = State.STOPPED, Reason.TARGET_REACHED
                else:
                    self._state = State.STOPPED, Reason.POSITION_LIMIT
                self._stop_moving()
            self._state_changed = True

    def _encoder_event(self, name, value):
        """Stop actuator based on position limits"""
        with self.lock:
            if self._is_targeting:
                if self._moving_within_target(self._target_position):
                    self._logger.info("Stop targeting operation at %d (target %d)", value, self._target_position)
                    self._state = State.STOPPED, Reason.TARGET_REACHED
                    self._stop_moving()
            if value <= self._backwardPositionLimit and self._is_moving_backward:
                self._logger.info("Reached backward position limit, stopping backward movement")
                self._stop_moving()
                self._state = State.STOPPED, Reason.POSITION_LIMIT
            elif value >= self._forwardPositionLimit and self._is_moving_forward:
                self._logger.info("Reached forward position limit, stopping forward movement")
                self._stop_moving()
                self._state = State.STOPPED, Reason.POSITION_LIMIT
            self._state_changed = True

    @property
    def lock(self) -> threading.Lock:
        return self._lock

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
        return self.position >= self._forwardPositionLimit

    @property
    def position(self) -> int:
        return self._positionEncoder.value

    def _stop_moving(self):
        self._forwardActuator.value = False
        self._backwardActuator.value = False
        self._is_targeting = False

    def stop_moving(self):
        """Stop all moving actions

        Also trigger a possible target event to prevent infinite waiting.

        The calling method must hold the lock.
        """
        self._state = State.STOPPED, Reason.CMD
        self._target_position = None
        self._state_changed = True
        self._stop_moving()

    def _move_forward(self):
        if self._is_moving_backward:
            self._logger.debug("Switch from backward to forward movement")
            self._backwardActuator.value = False
        self._forwardActuator.value = True

    def move_forward(self):
        """ Move in the forward direction

        The calling method must hold the lock.
        """
        self._is_targeting = False
        self._target_position = None
        if self.is_at_forward_limit:
            self._state = State.STOPPED, Reason.POSITION_LIMIT
        else:
            self._move_forward()
            self._state = State.MOVING_FORWARD, Reason.CMD
        self._state_changed = True

    def _move_backward(self):
        if self._is_moving_forward:
            self._logger.debug("Switch from forward to backward movement")
            self._forwardActuator.value = False
        self._backwardActuator.value = True

    def move_backward(self):
        """ Move in the backward direction

        The calling method must hold the lock.
        """
        self._is_targeting = False
        self._target_position = None
        if self.is_at_reset:
            self._state = State.STOPPED, Reason.POSITION_LIMIT
        else:
            self._move_backward()
            self._state = State.MOVING_BACKWARD, Reason.CMD
        self._state_changed = True

    def position_within_target(self, target: int) -> bool:
        """Test if this actuator is within the limits of target.

        Returns true iff position in the interval around target with the given accuracy.
        The interval limits are exclusive.
        """
        accuracy = self._positionAccuracy
        delta = target - self.position
        return -accuracy < delta < accuracy

    def _moving_within_target(self, target: int) -> bool:
        """Test if this actuator is moving within the limits or beyond a target

        The actuator MUST BE MOVING for valid results.
        Will return true, whenever the position is within the position limit
        or when moving away from the target.
        """
        accuracy = self._positionAccuracy
        delta = target - self.position
        if self._is_moving_forward:
            return (delta - self._stopOffset) < accuracy
        else:
            return -accuracy < (delta + self._stopOffset)

    def move_to_position(self, target: int):
        """Move this actuator to the given position

        This method is not more accurate than `self._positionAccuracy`.
        This method might overshoot and does not compensate for that.

        The calling method must hold the lock.
        """
        if not 0 <= target <= self._forwardPositionLimit:
            raise AttributeError("Target position out of range.\n"
                                 f"Test '0 <= ${target} <= ${self._forwardPositionLimit}' failed")
        self._target_position = target
        if self.position_within_target(target):
            self._state = State.STOPPED, Reason.TARGET_REACHED
            self._stop_moving()
        else:
            self._state = State.TARGETING, Reason.CMD
            self._is_targeting = True
            self._logger.info("Stating targeting operation to %d", target)
            if target > self.position:
                self._move_forward()
            else:
                self._move_backward()
        self._state_changed = True

    def state_changed(self):
        """Test, if any state changed compared to the last call to `get_status()`

        The calling method should hold the lock.
        """
        return self._state_changed

    def get_status(self):
        """Get the current state

        The calling method must hold the lock.
        """
        self._state_changed = False
        state, reason = self._state
        status = {
            "state": state,
            "reason": reason,
            "limit-switch": self._backwardLimitSwitch.value,
            "position": self._positionEncoder.value
        }
        if self._target_position is not None:
            status["target"] = self._target_position
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
            self.move_backward()
        elif cmd.startswith('stop'):
            self.stop_moving()
        elif cmd.startswith('to='):
            self.move_to_position(int(cmd.split('=')[1]))
        else:
            self._logger.warning("Unknown control command '%s'", cmd)
