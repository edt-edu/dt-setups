import threading
import logging
import time
from enum import Enum
from threading import Thread
from typing import Any, Dict, Optional, Tuple
from revpimodio2.io import IOBase

from .base_machine import BaseMachine, OperationInProgress


class State(Enum):
    READY = 'ready'
    "No item at the pickup zone"
    AT_PICKUP = 'at pickup'
    "The requested container is at the pickup zone"
    RETRIEVING = 'moving to conveyor'
    STORING = 'moving to rack'
    RETRIEVE_FAILED = 'retrieve failed'
    "No container detected after retrieve"
    TASK_FAILED = 'task failed'


class Warehouse(BaseMachine):

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None,
                 conveyor_timeout=10_000,
                 conveyor_retrieve_timeout=700,
                 busy_wait_time=0.02):
        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)
        self._conveyor_timeout = conveyor_timeout
        "Timeout for conveyor movements"
        self._conveyor_retrieve_timeout = conveyor_retrieve_timeout
        self._busy_wait_time = busy_wait_time

        self._state_changed = True
        self._state: State = State.READY
        self._task = "<none>"
        "Description of the current task (for error messages)"
        self._current_container: Optional[Tuple[int, int]] = None
        "The rack position (row, column) of the current container or None"
        self._thread: Optional[Thread] = None
        """The current running thread

        No new operations shall be started until after the thread terminates.
        The last action of each thread should be to reset this value to `None`, indicating a successful operation.
        Threads should be daemons.
        """

        self._horizontal_positions = config['rack']['horizontal_positions']
        """
        represents the horizontal positions of the rack cells. It's a matrix
        since the rack cells aren't straight
        top-conveyor-side
        """
        self._vertical_positions = config['rack']['vertical_positions']
        """
        represents the vertical positions of the rack cells. It's a matrix
        since the rack cells aren't straight
        top-conveyor-side
        """
        self._rack_lower_time = config['rack']['lower_time']
        self._rack_lift_time = config['rack']['lift_time']
        self._vertical_offset = config['rack']['vertical_offset']
        self._horizontal_offset = config['rack']['horizontal_offset']

        self._conveyor_horizontal = config['conveyor']['horizontal']
        "The height at which the crane must be to retrieve a package from the conveyor."
        self._conveyor_vertical_get = config['conveyor']['vertical_get']
        "The horizontal position at which the crane must be to retrieve a package from the conveyor."
        self._conveyor_vertical_put = config['conveyor']['vertical_put']
        "The horizontal position at which the crane must be to put a package onto the conveyor"
        self._conveyor_lower_time = config['conveyor']['lower_time']
        self._conveyor_lift_time = config['conveyor']['lift_time']

        self._senseHorizontalEnd = ports["senseHorizontalEnd"]
        self._senseLightBarrierIn = ports["senseLightBarrierIn"]
        self._senseLightBarrierOut = ports["senseLightBarrierOut"]
        self._senseVerticalEndUp = ports["senseVerticalEndUp"]
        self._encoderHorizontal = ports["encoderHorizontal"]
        self._encoderVertical = ports["encoderVertical"]
        self._senseArmOut = ports["senseArmOut"]
        self._senseArmIn = ports["senseArmIn"]

        self._actConveyorOut = ports["actConveyorOut"]
        self._actConveyorIn = ports["actConveyorIn"]
        self._actHorizontalToRack = ports["actHorizontalToRack"]
        self._actHorizontalToConveyor = ports["actHorizontalToConveyor"]
        self._actVerticalDown = ports["actVerticalDown"]
        self._actVerticalUp = ports["actVerticalUp"]
        self._actArmOut = ports["actArmOut"]
        self._actArmIn = ports["actArmIn"]

        self._senseLightBarrierOut.unreg_event()
        self._senseLightBarrierIn.unreg_event()

        # Reset
        self.home()

        self._senseLightBarrierOut.reg_event(self._light_barrier_event)
        self._senseLightBarrierIn.reg_event(self._light_barrier_event)

    def _light_barrier_event(self, name, value):
        with self.lock:
            self._state_changed = True

    @property
    def is_running(self):
        """Indicate that a threaded operation is currently running

        No further operations should happen until that operation finishes.
        """
        return self._thread is not None

    def cleanup(self):
        while True:
            with self.lock:
                thread = self._thread
                if thread is None:
                    return
            thread.join()

    def destroy(self):
        if self.is_running:
            self._state_changed = True
            raise OperationInProgress(self._task)

    def state_changed(self):
        if self.is_running and not self._thread.is_alive():
            # The last operation of each worker thread is to set `self._thread` to None.
            # Do not change `self._thread` until sending of the status message to ensure that this message is not lost.
            self._state = State.TASK_FAILED
            return True
        return self._state_changed

    def get_status(self):
        self._state_changed = False
        state = self._state
        container = self._current_container
        if state == State.AT_PICKUP and self._senseLightBarrierOut.value:
            state = State.READY
        if state == State.READY:
            if not self._senseLightBarrierOut.value:
                status = {
                    "state": "unknown_pickup"
                }
            else:
                status = {
                    "state": "ready"
                }
        elif state == State.AT_PICKUP:
            status = {
                "state": "at_pickup",
                "row": container[0],
                "column": container[1],
            }
        elif state == State.RETRIEVE_FAILED:
            status = {
                "state": "retrieve_failed",
                "row": container[0],
                "column": container[1],
            }
        elif state == State.RETRIEVING:
            status = {
                "state": "retrieving",
                "row": container[0],
                "column": container[1],
            }
        elif state == State.STORING:
            status = {
                "state": "storing",
                "row": container[0],
                "column": container[1],
            }
        else:  # state == State.TASK_FAILED:
            status = {
                "state": "failed",
                "task": self._task,
            }
            self._thread = None
        status["pickup"] = not self._senseLightBarrierOut.value
        status["conveyor"] = not self._senseLightBarrierIn.value
        return status

    def handle_control(self, value: Any):
        self._state_changed = True
        try:
            action = value['action']
            column = value['column']
            row = value['row']
        except KeyError as error:
            self._logger.warning("Missing control key %s", error)
            return
        if not (0 <= row < 3) or not (0 <= column < 3):
            self._logger.warning("Invalid rack position (%d, %d)", row, column)
            return
        container = row, column
        if self.is_running:
            self._logger.info("Already running with '%s' when received request %s", self._task, value)
            return
        if action == 'retrieve':
            if self._current_container == container:
                if self._state is State.AT_PICKUP:
                    return
            if not self._senseLightBarrierOut.value:
                self._logger.info("A container is already at the pickup position")
                # Abort if a container is detected at the pickup position.
                return
            self._current_container = container
            self._state = State.RETRIEVING
            self._task = f"retrieve {container}"
            self._thread = Thread(target=self._retrieve_thread, daemon=True)
            self._thread.start()
        elif action == 'store':
            if self._current_container is None:
                self._logger.warning("No container currently in use that can be stored, but continuing...")
            elif self._current_container != container:
                self._logger.debug("Mismatch between current %s and target container %s position",
                                   self._current_container,
                                   container)
            self._current_container = container
            self._state = State.STORING
            self._task = f"store {container}"
            self._thread = Thread(target=self._store_thread, daemon=True)
            self._thread.start()
        else:
            self._logger.error("Unknown action '%s' for command %s", action, value)

    def _retrieve_thread(self):
        self._logger.debug("Starting retrieve thread")
        with self._lock:
            container = self._current_container
        self._retrieve(container)
        # Clean up task context
        with self._lock:
            if self._senseLightBarrierOut.value:
                self._state = State.RETRIEVE_FAILED
            else:
                self._state = State.AT_PICKUP
            self._state_changed = True
            self._thread = None
        self._logger.debug("Stopping retrieve thread")
        return

    def _store_thread(self):
        self._logger.debug("Starting store thread")
        with self._lock:
            container = self._current_container
        self._store(container)
        # Clean up task context
        with self._lock:
            self._state = State.READY
            self._current_container = None
            self._state_changed = True
            self._thread = None
        self._logger.debug("Stopping store thread")
        return

    def _retrieve(self, container):
        """Retrieve a container from the rack and put it onto the pickup zone on the conveyor

        1. Move to rack position
        2. Extend arm
        3. Pickup container
        4. Retract arm
        5. Move to conveyor
        6. Move conveyor out until light barrier
        """
        # 1-3
        row, column = container
        self.retrieve(row, column)
        # 4-6
        self.put_onto_conveyor()

    def _store(self, container):
        """Get a container from the conveyor and store it in the rack

        1. Move to conveyor drop
        2. Move conveyor in until ...
        3. Move to rack target
        4. Store container
        """
        # 1, 2
        self.load_from_conveyor()
        # 3, 4
        row, column = container
        self.store(row, column)

    def home(self):
        """Homes all axis

        The home position is at the top of the conveyor side with the arm retraced.
        """
        self._logger.info("Resetting all actuators")
        self.retract_arm()
        self.vertical_home()
        self.horizontal_home()
        self.reset_encoder()

    def horizontal_home(self):
        """Moves storage crane towards the storage conveyor until the conveyor sensor triggers

        Retracts crane arm as a precaution.
        """
        self.retract_arm()
        self._actHorizontalToConveyor.value = True
        self._senseHorizontalEnd.wait(okvalue=True)
        self._actHorizontalToConveyor.value = False

    def vertical_home(self):
        """Lifts storage crane until the upper sensor triggers

        Retracts crane arm as a precaution.
        """
        self.retract_arm()
        self._actVerticalUp.value = True
        self._senseVerticalEndUp.wait(okvalue=True)
        self._actVerticalUp.value = False

    def reset_encoder(self):
        """Reset the encoders"""
        self._logger.info("Resetting position encoders")
        self._encoderHorizontal.signed = True
        self._encoderVertical.signed = True
        self._encoderHorizontal.reset()
        self._encoderVertical.reset()

    def lower_crane(self, duration):
        """Lowers the storage crane for the given time

        NOTE: That the arm could be extended on call!
        """
        self._actVerticalDown.value = True
        time.sleep(duration)
        self._actVerticalDown.value = False

    def lift_crane(self, duration):
        """Lifts the storage crane for the given time

        NOTE: That the arm could be extended on call!
        """
        self._actVerticalUp.value = True
        time.sleep(duration)
        self._actVerticalUp.value = False

    def move_to_conveyor_height(self):
        """Lowers storage crane to the height of the conveyor

        Retracts crane arm as a precaution.
        """
        self.retract_arm()
        self.move_to_pos(self._encoderHorizontal.value, self._conveyor_vertical_get)

    def move_to_conveyor_horizontal(self):
        """Moves storage crane to the horizontal position of the conveyor

        Retracts crane arm as a precaution.
        """
        self.retract_arm()
        self.move_to_pos(self._conveyor_horizontal, self._encoderVertical.value)

    def move_to_cell(self, row: int, col: int):
        """Moves storage crane to the given cell

        Such that the crane can be moved into the cell.
        NOTE: cells are numbered from top-conveyor-side to bottom-rack-side
        """
        self._logger.info("Move to cell (%d, %d)", row, col)
        self.retract_arm()
        target_horizontal = self._horizontal_positions[row][col]
        target_vertical = self._vertical_positions[row][col]
        self.move_to_pos(target_horizontal, target_vertical)

    def extend_arm(self):
        """Completely extends the crane arm"""
        self._logger.info("Extend the arm")
        if not self._senseArmOut.value:
            self._actArmOut.value = True
            self._senseArmOut.wait(okvalue=True)
            self._actArmOut.value = False

    def retract_arm(self):
        """Completely retracts the crane arm"""
        self._logger.info("Retract the arm")
        if not self._senseArmIn.value:
            self._logger.debug("Retract arm")
            self._actArmIn.value = True
            self._senseArmIn.wait(okvalue=True)
            self._actArmIn.value = False

    def store(self, row: int, col: int):
        """Get the item from the given cell"""
        self._logger.info("Store item at the rack position (%d, %d)", row, col)
        self.move_to_cell(row, col)
        self.extend_arm()
        self.lower_crane(self._rack_lower_time)
        self.retract_arm()

    def retrieve(self, row: int, col: int):
        """Picks up the item from the given cell"""
        self._logger.info("Get item from the rack at position (%d, %d)", row, col)
        self.move_to_cell(row, col)
        self.lower_crane(self._rack_lower_time)
        self.extend_arm()
        self.lift_crane(self._rack_lift_time)
        self.retract_arm()

    def move_to_pos(self, horizontal, vertical):
        """Moves to the given encoder position

        NOTE: Encoder positions have small fluctuations.
        Since the motor doesn't immediately stop, there are buffers of approximately 30.
        """
        if self._encoderHorizontal.value < horizontal:
            while self._encoderHorizontal.value < horizontal - self._horizontal_offset:
                self._actHorizontalToRack.value = True
                time.sleep(self._busy_wait_time)
        elif self._encoderHorizontal.value > horizontal:
            while horizontal + self._horizontal_offset < self._encoderHorizontal.value:
                self._actHorizontalToConveyor.value = True
                time.sleep(self._busy_wait_time)
        self._actHorizontalToRack.value = False
        self._actHorizontalToConveyor.value = False

        if self._encoderVertical.value < vertical:
            while self._encoderVertical.value < vertical - self._vertical_offset:
                self._actVerticalDown.value = True
                time.sleep(self._busy_wait_time)
        elif self._encoderVertical.value > vertical:
            while vertical + self._vertical_offset < self._encoderVertical.value:
                self._actVerticalUp.value = True
                time.sleep(self._busy_wait_time)
        self._actVerticalDown.value = False
        self._actVerticalUp.value = False

    def load_from_conveyor(self):
        """Moves to the conveyor position and loads the crane by activating the conveyor"""
        self._logger.info("Get item from the conveyor")
        self.move_to_pos(self._conveyor_horizontal, self._conveyor_vertical_get)
        self.extend_arm()
        if self._senseLightBarrierOut.value:
            self._logger.warning("Outward light barrier did not detect container")
        self._actConveyorIn.value = True
        self._senseLightBarrierIn.wait(okvalue=False, timeout=self._conveyor_retrieve_timeout)
        self._actConveyorIn.value = False
        if self._senseLightBarrierIn.value:
            self._logger.warning("Inward light barrier did not detect container")
        self.lift_crane(self._conveyor_lift_time)
        self.retract_arm()

    def put_onto_conveyor(self):
        """Put the container onto the conveyor pickup position"""
        self._logger.info("Put item onto the conveyor")
        self.move_to_pos(self._conveyor_horizontal, self._conveyor_vertical_put)
        self.extend_arm()
        self.lower_crane(self._conveyor_lower_time)
        if self._senseLightBarrierIn.value:
            self._logger.warning("Inward light barrier did not detect container")
        self._actConveyorOut.value = True
        self._senseLightBarrierOut.wait(okvalue=False, timeout=self._conveyor_timeout)
        self._actConveyorOut.value = False
        self.retract_arm()
