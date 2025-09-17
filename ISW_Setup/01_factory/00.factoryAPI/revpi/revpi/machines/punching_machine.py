import logging
import threading
import time
from threading import Thread
from typing import Any, Dict, Optional
from enum import Enum
from revpimodio2.io import IOBase

from .base_machine import BaseMachine


class State(Enum):
    """The state of the actuator"""
    MOVING_TO_PUNCHER = "moving-to-puncher"
    "The actuator is moving to the puncher"
    MOVING_TO_INPUT = "moving-to-input"
    "The actuator is moving to the input"
    PUNCHING_UP = "punching-up"
    "The puncher is moving up"
    PUNCHING_DOWN = "punching-down"
    "The puncher is moving down"
    PUNCHING = "punching"
    "The puncher moves completely down and then up"
    STOPPED = "stopped"
    "Set if no other action is performed"
    RESETTING = "reset"
    "Initial reset in setup"


class Reason(Enum):
    """The reason for the actuator state"""
    CMD = "cmd"
    "A request caused this state (or during reset)"
    SENSOR_REACHED = "position-limit"
    "A position limit caused this stop (STOPPED)"


class PunchingMachine(BaseMachine):
    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):

        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)

        self._logger.debug("starting port initialisation")

        self._thread: Optional[Thread] = None

        self._conveyorState = State.STOPPED
        self._puncherState = State.STOPPED
        self._conveyorReason = Reason.CMD
        self._puncherReason = Reason.CMD
        self._conveyor_state_changed = True
        self._puncher_state_changed = True

        self._sensePunching = ports['sensePunching']
        self._senseInput = ports['senseInput']
        self._sensePunchIsUp = ports['sensePunchIsUp']
        self._sensePunchIsDown = ports['sensePunchIsDown']

        self._actPunching = ports['actPunching']
        self._actInput = ports['actInput']
        self._actPunchUp = ports['actPunchUp']
        self._actPunchDown = ports['actPunchDown']

    @property
    def is_running(self):
        return self._thread is not None

    @property
    def punchingSensor(self):
        return self._sensePunching.value

    @property
    def inputSensor(self):
        return self._senseInput.value

    @property
    def topPunchSensor(self):
        return not self._sensePunchIsUp.value

    @property
    def bottomPunchSensor(self):
        return not self._sensePunchIsDown.value

    @property
    def _is_moving_punching(self) -> bool:
        return self._actPunching.value

    @property
    def _is_moving_input(self) -> bool:
        return self._actInput.value

    @property
    def _is_moving(self) -> bool:
        return self._is_moving_punching or self._is_moving_input

    def _stop_moving(self):
        if self._is_moving_punching:
            self._actPunching.value = False
        if self._is_moving_input:
            self._actInput.value = False

    def stop_conveyor(self):
        self._conveyor_state_changed = True
        self._conveyorState = State.STOPPED
        self._conveyorReason = Reason.CMD
        self._stop_moving()

    def _is_punching_down(self):
        return self._actPunchDown

    def _is_punching_up(self):
        return self._actPunchUp

    def _move_punching(self):
        if self._is_moving_input:
            self._logger.debug("Switch from moving to Input to moving to Punching")
            self._actInput.value = False
        self._actPunching.value = True

    def move_punching(self):
        self._conveyor_state_changed = True
        self._conveyorState = State.MOVING_TO_PUNCHER
        self._conveyorReason = Reason.CMD
        self._move_punching()
        self._sensePunching.wait(timeout=20000)
        self._stop_moving()
        with self.lock:
            self._conveyor_state_changed = True
            self._conveyorState = State.STOPPED
            self._conveyorReason = Reason.SENSOR_REACHED
            self.stop_conveyor()

    def _move_input(self):
        if self._is_moving_punching:
            self._logger.debug("Switch from moving to Punching to moving to Input")
            self._actPunching.value = False
        self._actInput.value = True

    def move_input(self):
        self._conveyor_state_changed = True
        self._conveyorState = State.MOVING_TO_INPUT
        self._conveyorReason = Reason.CMD
        self._move_input()
        self._senseInput.wait(timeout=20000)
        time.sleep(2)
        self._stop_moving()
        with self.lock:
            self._conveyor_state_changed = True
            self._conveyorState = State.STOPPED
            self._conveyorReason = Reason.SENSOR_REACHED
            self._thread = None
        return

    def _puncher_up(self):
        if self._is_punching_down():
            self._actPunchDown.value = False
        self._actPunchUp.value = True

    def _puncher_down(self):
        if self._is_punching_up():
            self._actPunchUp.value = False
        self._actPunchDown.value = True

    def _stop_punching(self):
        if self._is_punching_up():
            self._actPunchUp.value = False
        if self._is_punching_down():
            self._actPunchDown.value = False

    def stop_puncher(self):
        self._puncher_state_changed = True
        self._puncherState = State.STOPPED
        self._puncherReason = Reason.CMD
        self._stop_punching()

    def puncher_up(self):
        if not self._sensePunchIsUp:
            self._puncher_state_changed = True
            self._puncherState = State.PUNCHING_UP
            self._puncherReason = Reason.CMD
            self._puncher_up()
            self._sensePunchIsUp.wait(timeout=10000)
            self._stop_punching()
            with self.lock:
                self._puncher_state_changed = True
                self._puncherState = State.STOPPED
                self._puncherReason = Reason.SENSOR_REACHED
                self._stop_punching()
        else:
            self._logger.info("Puncher is already in the top position")

    def puncher_down(self):
        if not self._sensePunchIsDown:
            self._puncher_state_changed = True
            self._puncherState = State.PUNCHING_DOWN
            self._puncherReason = Reason.CMD
            self._puncher_down()
            self._sensePunchIsDown.wait(timeout=10000)
            time.sleep(0.4)
            self._stop_punching()
            with self.lock:
                self._puncher_state_changed = True
                self._puncherState = State.STOPPED
                self._puncherReason = Reason.SENSOR_REACHED
                self.stop_puncher()
        else:
            self._logger.info("Puncher is already in the bottom position")

    def punch(self):
        self.move_punching()
        self.puncher_down()
        self.puncher_up()
        self.move_input()

    def handle_control(self, control: dict):
        try:
            cmd = control["motor"]
            self._conveyor_state_changed = True
            if cmd.startswith("punch"):
                self._thread = Thread(target=self.move_punching, daemon=True)
                self._thread.start()
            elif cmd.startswith("input"):
                self._thread = Thread(target=self.move_input, daemon=True)
                self._thread.start()
            elif cmd.startswith("stop"):
                self.stop_conveyor()
            else:
                self._logger.warning("Unknown direction")
                return
        except KeyError:
            pass
        try:
            cmd = control["puncher"]
            self._puncher_state_changed = True
            if cmd.startswith("up"):
                self._thread = Thread(target=self.puncher_up, daemon=True)
                self._thread.start()
            elif cmd.startswith("down"):
                self._thread = Thread(target=self.puncher_down, daemon=True)
                self._thread.start()
            elif cmd.startswith("punch"):
                self._thread = Thread(target=self.punch, daemon=True)
                self._thread.start()
            elif cmd.startswith("stop"):
                self.stop_puncher()
            else:
                self._logger.warning("Unknown punching instruction")
                return
        except KeyError:
            pass

    def _stop_all(self):
        self._actInput.value = False
        self._actPunching.value = False
        self._actPunchUp.value = False
        self._actPunchDown.value = False

    def cleanup(self):
        while True:
            with self.lock:
                thread = self._thread
                if thread is None:
                    self._stop_all()
                    return
                thread.join()

    def destroy(self):
        thread = self._thread
        self._stop_all()

    def state_changed(self):
        return (self._conveyor_state_changed
                or self._puncher_state_changed)

    @property
    def state(self):
        return self._state

    def get_status(self):
        status = {
            "conveyorState": self._conveyorState.value,
            "puncherState": self._puncherState.value,
            "conveyorReason": self._conveyorReason.value,
            "punchingReason": self._puncherReason.value,
            "punchingSensor": not self.punchingSensor,
            "inputSensor": not self.inputSensor,
            "topPunchSensor": not self.topPunchSensor,
            "bottomPunchSensor": not self.bottomPunchSensor
        }

        self._conveyor_state_changed = False
        self._puncher_state_changed = False
        return status

