from enum import Enum
import time
import logging
from typing import Optional, Any, Dict
from threading import Thread, Lock
from revpimodio2.io import IOBase

from .base_machine import BaseMachine, OperationInProgress


class State(Enum):
    READY = 'ready'
    MILLING = 'milling'
    DRILLING = 'drilling'
    TRANSFERRING = 'transferring'


class Reason(Enum):
    RESET = "reset"
    CMD = "cmd"
    TRANSFERRED_TO_MILL = "transferred to mill"
    TRANSFERRED_TO_DRILL = "transferred to drill"
    TRANSFERRED_TO_END = "transferred to end"
    MILLING_COMPLETE = "milling complete"
    DRILLING_COMPLETE = "drilling complete"


class Transfer(Enum):
    FEED_TO_MILL = 'feed to mill'
    MILL_TO_DRILL = 'mill to drill'
    DRILL_TO_END = 'drill to end'


class IndexedLine(BaseMachine):

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[Lock] = None,
                 conveyor_timeout=15000):
        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)
        self._conveyor_timeout = conveyor_timeout
        "Timeout for conveyor movements"

        self._state_changed = True
        self._state = State.READY
        self._reason = Reason.RESET
        self._transfer_from_to: Optional[Transfer] = None
        self._thread: Optional[Thread] = None

        self._conveyor_movement_time = config["conveyor_movement_time"]
        self._drill_time = config["drill_time"]
        self._mill_time = config["mill_time"]

        self._sensePushButton1Front = ports["sensePushButton1Front"]
        self._sensePushButton1Back = ports["sensePushButton1Back"]
        self._sensePushButton2Front = ports["sensePushButton2Front"]
        self._sensePushButton2Back = ports["sensePushButton2Back"]
        self._senseSlider1 = ports["senseSlider1"]
        self._senseMilling = ports["senseMilling"]
        self._senseLoading = ports["senseLoading"]
        self._senseDrilling = ports["senseDrilling"]
        self._senseConveyorSwap = ports["senseConveyorSwap"]

        self._actMotorSlider1Backward = ports["actMotorSlider1Backward"]
        self._actMotorSlider1Forward = ports["actMotorSlider1Forward"]
        self._actMotorSlider2Backward = ports["actMotorSlider2Backward"]
        self._actMotorSlider2Forward = ports["actMotorSlider2Forward"]
        self._actConveyorBeltFeed = ports["actConveyorBeltFeed"]
        self._actConveyorBeltMilling = ports["actConveyorBeltMilling"]
        self._actMillingMachine = ports["actMillingMachine"]
        self._actConveyorBeltDrilling = ports["actConveyorBeltDrilling"]
        self._actDrillingMachine = ports["actDrillingMachine"]
        self._actConveyorBeltSwap = ports["actConveyorBeltSwap"]

    def _light_barrier_event(self, name, value):
        with self.lock:
            self._state_changed = True

    @property
    def is_running(self):
        return self._thread is not None

    @property
    def current_state(self):
        state = self._state
        if state == State.MILLING:
            return "milling package"
        elif state == State.DRILLING:
            return "drilling package"
        elif state == State.TRANSFERRING:
            return "transferring package"

    def _stop_all(self):
        self._actMotorSlider1Backward.value = False
        self._actMotorSlider1Forward.value = False
        self._actMotorSlider2Backward.value = False
        self._actMotorSlider2Forward.value = False
        self._actConveyorBeltFeed.value = False
        self._actConveyorBeltMilling.value = False
        self._actMillingMachine.value = False
        self._actConveyorBeltDrilling.value = False
        self._actDrillingMachine.value = False
        self._actConveyorBeltSwap.value = False

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

    def get_status(self):
        self._state_changed = False
        state = self._state

        status = {
            "state": state.value,
            "reason": self._reason.value,
            "package_at_drill": not self._senseDrilling.value,
            "package_at_mill": not self._senseMilling.value,
            "package_at_slider": not self._senseSlider1.value,
            "package_at_end": not self._senseConveyorSwap.value,
            "package_at_start": not self._senseLoading.value
        }
        if state == State.TRANSFERRING:
            status["transfer_from_to"] = self._transfer_from_to.value
        return status

    def handle_control(self, value: Any):
        self._state_changed = True
        action = value['action']
        if self.is_running:
            self._logger.warning("Already running with '%s' when received request %s", self.current_state, value)
            return
        if action == 'mill':
            if self._senseMilling.value:
                self._logger.warning("No package available to mill!")
                return
            self._state = State.MILLING
            self._reason = Reason.CMD
            self._thread = Thread(target=self._mill_thread, daemon=True)
            self._thread.start()
        elif action == 'drill':
            if self._senseDrilling.value:
                self._logger.warning("No package available to drill!")
                return
            self._state = State.DRILLING
            self._reason = Reason.CMD
            self._thread = Thread(target=self._drill_thread, daemon=True)
            self._thread.start()
        elif action == 'transfer':
            transfer_from_to = value['transfer_from_to']
            if transfer_from_to == "feed_to_mill":
                if not self._senseMilling.value:
                    self._logger.warning("Package already at milling machine. Action canceled!")
                    return
                self._transfer_from_to = Transfer.FEED_TO_MILL
            elif transfer_from_to == "mill_to_drill":
                if self._senseMilling.value:
                    self._logger.warning("No package at milling machine. Action canceled!")
                    return
                if not self._senseDrilling.value:
                    self._logger.warning("Package already at drilling machine. Action canceled!")
                    return
                self._transfer_from_to = Transfer.MILL_TO_DRILL
            elif transfer_from_to == "drill_to_end":
                if self._senseDrilling.value:
                    self._logger.warning("No package at drilling machine. Action canceled!")
                    return
                self._transfer_from_to = Transfer.DRILL_TO_END
            self._state = State.TRANSFERRING
            self._reason = Reason.CMD
            self._thread = Thread(target=self._transfer_thread, daemon=True)
            self._thread.start()
        else:
            self._logger.error("Unknown action '%s' for command %s", action, value)

    def _mill_thread(self):
        self._logger.debug("Starting milling thread")
        self.mill()
        # Clean up task context
        self._logger.debug("Stopping milling thread")
        with self.lock:
            self._state = State.READY
            self._reason = Reason.MILLING_COMPLETE
            self._state_changed = True
            self._thread = None
        return

    def _drill_thread(self):
        self._logger.debug("Starting drilling thread")
        self.drill()
        # Clean up task context
        self._logger.debug("Stopping drilling thread")
        with self.lock:
            self._state = State.READY
            self._reason = Reason.DRILLING_COMPLETE
            self._state_changed = True
            self._thread = None
        return

    def _transfer_thread(self):
        self._logger.debug("Starting transfer thread")
        reason = Reason.RESET  # Default in case of an error
        if self._transfer_from_to == Transfer.FEED_TO_MILL:
            self.transfer_from_feed_to_mill()
            reason = Reason.TRANSFERRED_TO_MILL
        elif self._transfer_from_to == Transfer.MILL_TO_DRILL:
            self.transfer_from_mill_to_drill()
            reason = Reason.TRANSFERRED_TO_DRILL
        elif self._transfer_from_to == Transfer.DRILL_TO_END:
            self.transfer_from_drill_to_end()
            reason = Reason.TRANSFERRED_TO_END
        # Clean up task context
        self._logger.debug("Stopping transfer thread")
        with self.lock:
            self._transfer_from_to = None
            self._state = State.READY
            self._reason = reason
            self._state_changed = True
            self._thread = None
        return

    def extendSlider(self, transferSlider: Transfer):
        """Extends the conveyor slider with the given id to a maximum"""
        if transferSlider == Transfer.FEED_TO_MILL:
            self._actMotorSlider1Forward.value = True
            self._sensePushButton1Front.wait(okvalue=True)
            self._actMotorSlider1Forward.value = False
        elif transferSlider == Transfer.DRILL_TO_END:
            self._actMotorSlider2Forward.value = True
            self._sensePushButton2Front.wait(okvalue=True)
            self._actMotorSlider2Forward.value = False
        else:
            self._logger.warning("Invalid slider")

    def retractSlider(self, slider: Transfer):
        """Retracts the conveyor slider with the given id to a minimum"""
        if slider == Transfer.FEED_TO_MILL:
            self._actMotorSlider1Backward.value = True
            self._sensePushButton1Back.wait(okvalue=True)
            self._actMotorSlider1Backward.value = False
        elif slider == Transfer.DRILL_TO_END:
            self._actMotorSlider2Backward.value = True
            self._sensePushButton2Back.wait(okvalue=True)
            self._actMotorSlider2Backward.value = False
        else:
            self._logger.warning("Invalid slider ID")

    def transfer_from_feed_to_mill(self):
        """Transfers a package from the starting conveyor to the milling machine"""
        self.retractSlider(Transfer.FEED_TO_MILL)
        self._actConveyorBeltFeed.value = True
        self._senseSlider1.wait(okvalue=False, timeout=self._conveyor_timeout)
        if self._senseSlider1.value:
            self._logger.warning("No package arrived at input gate. Continuing...")
        time.sleep(self._conveyor_movement_time)
        self._actConveyorBeltFeed.value = False
        self._actConveyorBeltMilling.value = True
        self.extendSlider(Transfer.FEED_TO_MILL)
        self._senseMilling.wait(okvalue=False, timeout=self._conveyor_timeout)
        if self._senseMilling.value:
            self._logger.warning("No package arrived at milling machine. Stopping!")
        self._actConveyorBeltMilling.value = False

    def transfer_from_drill_to_end(self):
        """Transfers a package from the drilling machine to the end conveyor"""
        if not self._senseConveyorSwap.value:
            # Move package from light barrier to end.
            self._logger.info("Detected package at end light barrier. Moving package to the pickup zone.")
            self._actConveyorBeltSwap.value = True
            time.sleep(self._conveyor_movement_time)
            self._actConveyorBeltSwap.value = False
        else:
            # Move package from drilling to end.
            self.retractSlider(Transfer.DRILL_TO_END)
            self._actConveyorBeltDrilling.value = True
            time.sleep(self._conveyor_movement_time)
            self._actConveyorBeltDrilling.value = False
            self._actConveyorBeltSwap.value = True
            self.extendSlider(Transfer.DRILL_TO_END)
            self._senseConveyorSwap.wait(okvalue=False, timeout=self._conveyor_timeout)
            if self._senseConveyorSwap.value:
                self._logger.warning("No package arrived at end gate. Stopping!")
            time.sleep(self._conveyor_movement_time)
            self._actConveyorBeltSwap.value = False

    def transfer_from_mill_to_drill(self):
        """Transfers a package from the drilling machine to the milling machine"""
        self._actConveyorBeltMilling.value = True
        self._actConveyorBeltDrilling.value = True
        self._senseDrilling.wait(okvalue=False, timeout=self._conveyor_timeout)
        if self._senseDrilling.value:
            self._logger.warning("No package arrived at drilling machine. Stopping!")
        self._actConveyorBeltMilling.value = False
        self._actConveyorBeltDrilling.value = False

    def mill(self):
        """Mills a package

        Simulated by activating the machine for two seconds.
        """
        self._actMillingMachine.value = True
        time.sleep(self._mill_time)
        self._actMillingMachine.value = False

    def drill(self):
        """Drills a package

        Simulated by activating the machine for two seconds.
        """
        self._actDrillingMachine.value = True
        time.sleep(self._drill_time)
        self._actDrillingMachine.value = False
