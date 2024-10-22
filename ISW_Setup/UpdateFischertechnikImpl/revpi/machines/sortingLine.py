import threading
from enum import Enum
import logging
from typing import Optional, Any, Dict
from threading import Thread
import time
from revpimodio2.io import IOBase

from .base_machine import BaseMachine, OperationInProgress


class State(Enum):
    READY = 'ready'
    SORTING = 'sorting'
    MOVE_TO_EJECTORS = 'move to ejectors'
    AT_EJECTORS = 'package at ejectors'
    SORT_CRITICALLY_FAILED = 'critical failure while sorting'


class Color(Enum):
    WHITE = 'white'
    RED = 'red'
    BLUE = 'blue'


class CriticalSortFailure(Exception):
    pass


class SortingLine(BaseMachine):

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None,
                 ejector_timeout=5000,
                 conveyor_timeout=20000):
        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)
        self._ejector_timeout = ejector_timeout
        "Timeout for ejector movements"
        self._conveyor_timeout = conveyor_timeout
        "Timeout for conveyor movements"
        self._white_eject_time = config["white_eject_time"]
        self._red_eject_time = config["red_eject_time"]
        self._blue_eject_time = config["blue_eject_time"]

        self._state_changed = True
        self._state: State = State.READY
        self._color_to_sort: Optional[Color] = None
        self._thread: Optional[Thread] = None

        self._impulseCounter = ports["impulseCounter"]
        self._senseInputLightBarrier = ports["senseInputLightBarrier"]
        self._senseMiddleLightBarrier = ports["senseMiddleLightBarrier"]
        self._senseWhiteLightBarrier = ports["senseWhiteLightBarrier"]
        self._senseRedLightBarrier = ports["senseRedLightBarrier"]
        self._senseBlueLightBarrier = ports["senseBlueLightBarrier"]

        self._actMotorConveyor = ports["actMotorConveyor"]
        self._actCompressorOn = ports["actCompressorOn"]
        self._actWhiteEjector = ports["actWhiteEjector"]
        self._actRedEjector = ports["actRedEjector"]
        self._actBlueEjector = ports["actBlueEjector"]

    def _light_barrier_event(self, name, value):
        with self.lock:
            self._state_changed = True

    @property
    def is_running(self):
        return self._thread is not None

    @property
    def current_state(self):
        state = self._state
        if state == State.SORTING:
            return f"Storing {self._color_to_sort.value} package"
        elif state == State.MOVE_TO_EJECTORS:
            return "Moving to ejectors"
        elif state == State.AT_EJECTORS:
            return "Package waiting at ejectors"
        elif state == State.SORT_CRITICALLY_FAILED:
            return "Critical failure while sorting."

    def _stop_all(self):
        self._actMotorConveyor.value = False
        self._actCompressorOn.value = False
        self._actWhiteEjector.value = False
        self._actRedEjector.value = False
        self._actBlueEjector.value = False

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
        if thread is not None:
            self._state_changed = True
            raise OperationInProgress(self.current_state)
        self._stop_all()

    def state_changed(self):
        return self._state_changed

    def get_status(self) -> dict:
        self._state_changed = False
        state = self._state
        color: Optional[str] = None
        if self._color_to_sort is not None:
            color = self._color_to_sort.value
        isWhiteStored = not self._senseWhiteLightBarrier.value
        isRedStored = not self._senseRedLightBarrier.value
        isBlueStored = not self._senseBlueLightBarrier.value
        isAtStart = not self._senseInputLightBarrier.value
        if state == State.READY and not self._senseMiddleLightBarrier.value:
            state = State.AT_EJECTORS
        elif state == State.AT_EJECTORS and self._senseMiddleLightBarrier.value:
            state = State.READY
        if state == State.READY:
            return {
                "state": "ready",
                "isWhiteStored": isWhiteStored,
                "isRedStored": isRedStored,
                "isBlueStored": isBlueStored,
                "isAtStart": isAtStart
            }
        elif state == State.SORTING:
            return {
                "state": "sorting",
                "color": color,
                "isWhiteStored": isWhiteStored,
                "isRedStored": isRedStored,
                "isBlueStored": isBlueStored,
                "isAtStart": isAtStart
            }
        elif state == State.MOVE_TO_EJECTORS:
            return {
                "state": "moving to ejectors",
                "isWhiteStored": isWhiteStored,
                "isRedStored": isRedStored,
                "isBlueStored": isBlueStored,
                "isAtStart": isAtStart
            }
        elif state == State.AT_EJECTORS:
            return {
                "state": "package waiting at ejectors",
                "color": color,
                "isWhiteStored": isWhiteStored,
                "isRedStored": isRedStored,
                "isBlueStored": isBlueStored,
                "isAtStart": isAtStart
            }
        elif state == State.SORT_CRITICALLY_FAILED:
            return {
                "state": "critical failure while sorting",
                "color": color,
                "isWhiteStored": isWhiteStored,
                "isRedStored": isRedStored,
                "isBlueStored": isBlueStored,
                "isAtStart": isAtStart
            }

    def handle_control(self, value: Any):
        action = value['action']
        if self.is_running:
            self._state_changed = True
            self._logger.warning("Already running with '%s' when received request %s", self.current_state, value)
            return
        self._state_changed = True
        if action == 'move to ejectors':
            if not self._senseMiddleLightBarrier.value:
                self._logger.warning("Package already at ejectors. Action stopped!")
            else:
                self._state = State.MOVE_TO_EJECTORS
                self._thread = Thread(target=self._move_to_ejectors_thread, daemon=True)
                self._thread.start()
        elif action == 'sort':
            try:
                color = Color(value['color'])
            except ValueError:
                color = None
                self._logger.warning("Unknown color stated! Cancelling action!")
                return
            if self._senseMiddleLightBarrier.value:
                self._logger.warning("No package available")
                return
            self._color_to_sort = color
            self._state = State.SORTING
            self._thread = Thread(target=self._sort_thread, daemon=True)
            self._thread.start()
        elif action == 'resolve failure':
            # NOTE: This action should only be called after ensuring the failure has been resolved.
            self._state_changed = True
            self._state = State.READY
            self._logger.info("Resolved failure manually. Machine ready!")
            self.get_status()
        else:
            self._logger.error("Unknown action '%s' for command %s", action, value)

    def _sort_thread(self):
        self._logger.debug("Starting sort thread")
        failed = False
        try:
            self.sort()
        except CriticalSortFailure:
            failed = True
        # Clean up task context
        self._logger.debug("Stopping sort thread")
        with self.lock:
            if failed:
                self._state = State.SORT_CRITICALLY_FAILED
            else:
                if not self._senseMiddleLightBarrier.value:
                    self._state = State.AT_EJECTORS
                else:
                    self._state = State.READY
                    self._color_to_sort = None
            self._state_changed = True
            self._thread = None
        return

    def _move_to_ejectors_thread(self):
        self._logger.debug("Starting moving to ejectors thread")
        self.moveToEjectors()
        # Clean up task context
        self._logger.debug("Stopping moving to ejectors thread")
        with self.lock:
            self._state = State.AT_EJECTORS
            self._state_changed = True
            self._thread = None
        return

    def moveToEjectors(self):
        """Moves a package to the ejector gate"""
        self._actMotorConveyor.value = True
        self._senseMiddleLightBarrier.wait(okvalue=False, timeout=self._conveyor_timeout)
        self._actMotorConveyor.value = False

    def sort(self):
        """Sorts the color into the given cell

        The package needs to be at the ejector gate.

        Returns `True` iff there was no error.
        """
        color = self._color_to_sort
        if color == Color.WHITE:
            if not self._senseWhiteLightBarrier.value:
                self._logger.warning("Storage for white already filled. Stopped action")
                return
            self._actMotorConveyor.value = True
            time.sleep(self._white_eject_time)
            self._actMotorConveyor.value = False

            self._actCompressorOn.value = True
            self._actWhiteEjector.value = True

            self._senseWhiteLightBarrier.wait(okvalue=False, timeout=self._ejector_timeout)
            self._actCompressorOn.value = False
            self._actWhiteEjector.value = False

            if self._senseWhiteLightBarrier.value:
                raise CriticalSortFailure("White item not detected")
        elif color == Color.RED:
            if not self._senseRedLightBarrier.value:
                self._logger.warning("Storage for red already filled. Stopped action")
                return
            self._actMotorConveyor.value = True
            time.sleep(self._red_eject_time)
            self._actMotorConveyor.value = False

            self._actCompressorOn.value = True
            self._actRedEjector.value = True

            self._senseRedLightBarrier.wait(okvalue=False, timeout=self._ejector_timeout)
            self._actCompressorOn.value = False
            self._actRedEjector.value = False

            if self._senseRedLightBarrier.value:
                raise CriticalSortFailure("Red item not detected")
        elif color == Color.BLUE:
            if not self._senseBlueLightBarrier.value:
                self._logger.warning("Storage for blue already filled. Stopped action")
                return
            self._actMotorConveyor.value = True
            time.sleep(self._blue_eject_time)
            self._actMotorConveyor.value = False

            self._actCompressorOn.value = True
            self._actBlueEjector.value = True

            self._senseBlueLightBarrier.wait(okvalue=False, timeout=self._ejector_timeout)
            self._actCompressorOn.value = False
            self._actBlueEjector.value = False

            if self._senseBlueLightBarrier.value:
                raise CriticalSortFailure("Blue item not detected")
        else:
            self._logger.warning("Invalid ejector color")
