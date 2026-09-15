import logging
import threading
import time
from threading import Thread
from typing import Any, Dict, Optional
from enum import Enum
from revpimodio2.io import IOBase

from .base_machine import BaseMachine


class State(Enum):
    STOPPED = "stopped"
    "Set if no other action is performed"
    RESETTING = "reset"
    "Initial reset in setup"
    CLOSED = "closed"
    "Valve is closed"
    OPENED = "opened"
    "Valve is open"
    MOVING_INWARD = "moving inward"
    "Feeder moves inward"
    MOVING_OUTWARD = "moving outward"
    "Feeder moves outward"
    ACTIVATED = "activated"
    "Compressor is activated"
    DEACTIVATED = "deactivated"
    "Compressor is deactivated"
    MOVING_FORWARD = "moving forward"
    "Conveyor is moving forward"
    RUNNING = "running"
    "Saw is running"
    ON = "on"
    "Oven light is on"
    OFF = "off"
    "Oven light is off"
    MOVING_TO_VAC = "moving to vac"
    "Turntable moves to vacuum"
    MOVING_TO_CONV = "moving to conv"
    "Turntable moves to conveyor"
    MOVING_TO_SAW = "moving to saw"
    "Turntable moves to saw"
    VAC = "vac"
    "Current position of turntable is at vacuum"
    SAW = "saw"
    "Current position of turntable is at saw"
    CONV = "conv"
    "Current position of turntable is at conveyor"
    MOVING_TO_OVEN = "moving to oven"
    "Vacuum gripper is moving to oven"
    MOVING_TO_TURNTABLE = "moving to turntable"
    "Vacuum gripper is moving to turntable"
    FREEZING = "freezing"
    "Started complete freezing pipeline"


class Reason(Enum):
    """The reason for the actuator state"""
    CMD = "cmd"
    "A request caused this state (or during reset)"
    SENSOR_REACHED = "position-limit"
    "A position limit caused this stop (STOPPED)"
    MAXIMUM_TIME_REACHED = "maximum time reached"
    ""
    FREEZING_PROCESS_FINISHED = "freezing process finished"
    "The input is completely processed in the freezer"


class Freezer(BaseMachine):
    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):

        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)

        self._logger.debug("starting port initialisation")

        self._thread: Optional[Thread] = None

        self._turntableState = State.STOPPED
        self._conveyorState = State.STOPPED
        self._vacuumGripperState = State.STOPPED
        self._compressorState = State.STOPPED
        self._valveState = State.CLOSED
        self._lowerValveState = State.CLOSED
        self._valveOvenDoorState = State.CLOSED
        self._valveFeederState = State.CLOSED
        self._sawState = State.STOPPED
        self._ovenState = State.OFF
        self._ovenFeederState = State.STOPPED
        self._freezerState = State.STOPPED

        self._turntableReason = Reason.CMD
        self._conveyorReason = Reason.CMD
        self._vacuumGripperReason = Reason.CMD
        self._compressorReason = Reason.CMD
        self._valveReason = Reason.CMD
        self._lowerValveReason = Reason.CMD
        self._valveOvenDoorReason = Reason.CMD
        self._valveFeederReason = Reason.CMD
        self._sawReason = Reason.CMD
        self._ovenReason = Reason.CMD
        self._ovenFeederReason = Reason.CMD
        self._freezerReason = Reason.CMD

        self._turntable_state_changed = True
        self._conveyor_state_changed = True
        self._vacuum_gripper_state_changed = True
        self._compressor_state_changed = True
        self._valve_state_changed = True
        self._lower_valve_state_changed = True
        self._valve_oven_door_state_changed = True
        self._valve_feeder_state_changed = True
        self._saw_state_changed = True
        self._oven_state_changed = True
        self._oven_feeder_state_changed = True
        self._freezer_state_changed = True

        self._senseTurntablePosVacuum = ports['senseTurntablePosVacuum']
        self._senseTurntablePosConveyor = ports['senseTurntablePosConveyor']
        self._senseDelivery = ports['senseDelivery']
        self._senseTurntablePosSaw = ports['senseTurntablePosSaw']
        self._senseVacuumGripperAtTurntable = ports['senseVacuumGripperAtTurntable']
        self._senseOvenFeederIn = ports['senseOvenFeederIn']
        self._senseOvenFeederOut = ports['senseOvenFeederOut']
        self._senseVacuumGripperAtOven = ports['senseVacuumGripperAtOven']
        self._senseOven = ports['senseOven']

        self._actRotClockwise = ports['actRotClockwise']
        self._actRotCounterclockwise = ports['actRotCounterclockwise']
        self._actConveyorForward = ports['actConveyorForward']
        self._actSaw = ports['actSaw']
        self._actOvenInward = ports['actOvenInward']
        self._actOvenOutward = ports['actOvenOutward']
        self._actGripperToOven = ports['actGripperToOven']
        self._actGripperToTurntable = ports['actGripperToTurntable']
        self._actOvenLight = ports['actOvenLight']
        self._actCompressor = ports['actCompressor']
        self._actValve = ports['actValve']
        self._actLowerValve = ports['actLowerValve']
        self._actValveOvenDoor = ports['actValveOvenDoor']
        self._actValveFeeder = ports['actValveFeeder']

        self.reset()
        self._last_position = State.VAC

        ports['senseTurntablePosVacuum'].reg_event(self._position_vac)
        ports['senseTurntablePosConveyor'].reg_event(self._position_conv)
        ports['senseTurntablePosSaw'].reg_event(self._position_saw)

    def reset(self):
        self.move_to_vac()
        self.move_vac_turntable()
        self.activate_compressor()
        self.open_oven_door_valve()
        self.move_feeder_out()
        self.close_oven_door_valve()
        self.close_lower_valve()
        self.close_feeder_valve()
        self.close_vacuum_gripper_valve()
        self.deactivate_compressor()

    def _position_vac(self, name, value):
        self._last_position = State.VAC

    def _position_saw(self, name, value):
        self._last_position = State.SAW

    def _position_conv(self, name, value):
        self._last_position = State.CONV

    def is_running(self):
        return self._thread is not None

    def _is_rot_clockwise(self) -> bool:
        return self._actRotClockwise.value

    def _is_rot_counterclockwise(self) -> bool:
        return self._actRotCounterclockwise.value

    def _is_conveyor_forward(self) -> bool:
        return self._actConveyorForward.value

    def _is_sawing(self) -> bool:
        return self._actSaw.value

    def _is_oven_moving_inward(self) -> bool:
        return self._actOvenInward.value

    def _is_oven_moving_outward(self) -> bool:
        return self._actOvenOutward.value

    def _is_gripper_to_oven(self) -> bool:
        return self._actGripperToOven.value

    def _is_gripper_to_turntable(self) -> bool:
        return self._actGripperToTurntable.value

    def _is_oven_light(self) -> bool:
        return self._actOvenLight.value

    def _is_compressor(self) -> bool:
        return self._actCompressor.value

    def _is_valve(self) -> bool:
        return self._actValve.value

    def _is_lower_valve(self) -> bool:
        return self._actLowerValve.value

    def _is_valve_oven_door(self) -> bool:
        return self._actValveOvenDoor.value

    def _is_valve_feeder(self) -> bool:
        return self._actValveFeeder.value

    def _stop_moving_feeder(self):
        if self._is_oven_moving_inward:
            self._actOvenInward.value = False
        if self._is_oven_moving_outward:
            self._actOvenOutward.value = False

    def stop_moving_feeder(self):
        self._oven_feeder_state_changed = True
        self._ovenFeederState = State.STOPPED
        self._ovenFeederReason = Reason.CMD
        self._stop_moving_feeder()

    def _move_feeder_in(self):
        if self._is_oven_moving_outward:
            self._logger.debug("Switch from moving feeder outward to inward")
            self._actOvenOutward.value = False
        self._actOvenInward.value = True

    def move_feeder_in(self):
        if self._senseOvenFeederIn.value:
            self._logger.info("Feeder already in")
        else:
            self._oven_feeder_state_changed = True
            self._ovenFeederState = State.MOVING_INWARD
            self._ovenFeederReason = Reason.CMD
            self._move_feeder_in()
            self._senseOvenFeederIn.wait()
            self._stop_moving_feeder()
            with self.lock:
                self._oven_feeder_state_changed = True
                self._ovenFeederState = State.STOPPED
                self._ovenFeederReason = Reason.SENSOR_REACHED
                self._thread = None

    def _move_feeder_out(self):
        if self._is_oven_moving_inward:
            self._logger.debug("Switch from moving feeder inward to outward")
            self._actOvenInward.value = False
        self._actOvenOutward.value = True

    def move_feeder_out(self):
        if self._senseOvenFeederOut.value:
            self._logger.info("Feeder already out")
        else:
            self._oven_feeder_state_changed = True
            self._ovenFeederState = State.MOVING_OUTWARD
            self._ovenFeederReason = Reason.CMD
            self._move_feeder_out()
            self._senseOvenFeederOut.wait()
            self._stop_moving_feeder()
            with self.lock:
                self._oven_feeder_state_changed = True
                self._ovenFeederState = State.STOPPED
                self._ovenFeederReason = Reason.SENSOR_REACHED
                self._thread = None

    def _deactivate_compressor(self):
        self._actCompressor.value = False

    def deactivate_compressor(self):
        self._compressor_state_changed = True
        self._compressorState = State.DEACTIVATED
        self._compressorReason = Reason.CMD
        self._deactivate_compressor()

    def _activate_compressor(self):
        if not self._actCompressor.value:
            self._actCompressor.value = True

    def activate_compressor(self):
        self._compressor_state_changed = True
        self._compressorState = State.ACTIVATED
        self._compressorReason = Reason.CMD
        self._activate_compressor()

    def _open_oven_door_valve(self):
        if not self._actValveOvenDoor:
            self._actValveOvenDoor.value = True

    def open_oven_door_valve(self):
        self._valve_oven_door_state_changed = True
        self._valveOvenDoorState = State.OPENED
        self._valveOvenDoorReason = Reason.CMD
        self._open_oven_door_valve()

    def _close_oven_door_valve(self):
        if self._actValveOvenDoor:
            self._actValveOvenDoor.value = False

    def close_oven_door_valve(self):
        self._valve_oven_door_state_changed = True
        self._valveOvenDoorState = State.CLOSED
        self._valveOvenDoorReason = Reason.CMD
        self._close_oven_door_valve()

    def _open_vacuum_gripper_valve(self):
        if not self._actValve:
            self._actValve.value = True

    def open_vacuum_gripper_valve(self):
        self._valve_state_changed = True
        self._valveState = State.OPENED
        self._valveReason = Reason.CMD
        self._open_vacuum_gripper_valve()

    def _close_vacuum_gripper_valve(self):
        if self._actValve:
            self._actValve.value = False

    def close_vacuum_gripper_valve(self):
        self._valve_state_changed = True
        self._valveState = State.CLOSED
        self._valveReason = Reason.CMD
        self._close_vacuum_gripper_valve()

    def _open_lower_valve(self):
        if not self._actLowerValve:
            self._actLowerValve.value = True

    def open_lower_valve(self):
        self._lower_valve_state_changed = True
        self._lowerValveState = State.OPENED
        self._lowerValveReason = Reason.CMD
        self._open_lower_valve()

    def _close_lower_valve(self):
        if self._actLowerValve:
            self._actLowerValve.value = False

    def close_lower_valve(self):
        self._lower_valve_state_changed = True
        self._lowerValveState = State.CLOSED
        self._lowerValveReason = Reason.CMD
        self._close_lower_valve()

    def _open_feeder_valve(self):
        if not self._actValveFeeder:
            self._actValveFeeder.value = True

    def open_feeder_valve(self):
        self._valve_feeder_state_changed = True
        self._valveFeederState = State.OPENED
        self._valveFeederReason = Reason.CMD
        self._open_feeder_valve()

    def _close_feeder_valve(self):
        if self._actValveFeeder:
            self._actValveFeeder.value = False

    def close_feeder_valve(self):
        self._valve_feeder_state_changed = True
        self._valveFeederState = State.CLOSED
        self._valveFeederReason = Reason.CMD
        self._close_feeder_valve()

    def _stop_conveyor(self):
        if self._actConveyorForward:
            self._actConveyorForward.value = False

    def stop_conveyor(self):
        self._conveyor_state_changed = True
        self._conveyorState = State.STOPPED
        self._conveyorReason = Reason.CMD
        self._stop_conveyor()

    def _move_conveyor(self):
        if not self._actConveyorForward:
            self._actConveyorForward.value = True

    def move_conveyor(self):
        self._conveyor_state_changed = True
        self._conveyorState = State.MOVING_FORWARD
        self._conveyorReason = Reason.CMD
        self._move_conveyor()
        self._senseDelivery.wait(timeout=20000)
        time.sleep(2)
        self._stop_conveyor()
        with self.lock:
            self._conveyor_state_changed = True
            self._conveyorState = State.STOPPED
            self._conveyorReason = Reason.SENSOR_REACHED
            self._thread = None

    def _stop_saw(self):
        if self._actSaw:
            self._actSaw.value = False

    def stop_saw(self):
        self._saw_state_changed = True
        self._sawState = State.STOPPED
        self._sawReason = Reason.CMD
        self._stop_saw()

    def _start_saw(self):
        if not self._actSaw:
            self._actSaw.value = True

    def start_saw(self):
        self._saw_state_changed = True
        self._sawState = State.RUNNING
        self._sawReason = Reason.CMD
        self._start_saw()

    def _stop_oven_light(self):
        if self._actOvenLight:
            self._actOvenLight.value = False

    def _start_oven_light(self):
        if not self._actOvenLight:
            self._actOvenLight.value = True

    def start_oven_light(self):
        self._oven_state_changed = True
        self._ovenState = State.ON
        self._ovenReason = Reason.CMD
        for x in range(20):
            self._start_oven_light()
            time.sleep(0.1)
            self._stop_oven_light()
            time.sleep(0.1)
        with self.lock:
            self._oven_state_changed = True
            self._ovenState = State.OFF
            self._ovenReason = Reason.MAXIMUM_TIME_REACHED
            self._stop_oven_light()

    def _stop_turntable(self):
        if self._actRotClockwise:
            self._actRotClockwise.value = False
        if self._actRotCounterclockwise:
            self._actRotCounterclockwise.value = False

    def stop_turntable(self):
        self._turntable_state_changed = True
        self._turntableState = State.STOPPED
        self._turntableReason = Reason.CMD
        self._stop_turntable()

    def _rot_table_clockwise(self):
        if self._actRotCounterclockwise:
            self._logger.info("Switching from counterclockwise to clockwise")
            self._actRotCounterclockwise.value = False
        self._actRotClockwise.value = True

    def _rot_table_counter_clockwise(self):
        if self._actRotClockwise:
            self._logger.info("Switching from clockwise to counterclockwise")
            self._actRotClockwise.value = False
        self._actRotCounterclockwise.value = True

    def move_to_saw(self):
        if self._senseTurntablePosSaw:
            self._logger.info("Turntable already on saw position")
            return
        else:
            self._turntable_state_changed = True
            self._turntableState = State.MOVING_TO_SAW
            self._turntableReason = Reason.CMD
            if self._last_position == State.VAC:
                self._rot_table_clockwise()
                self._senseTurntablePosSaw.wait()
                self._stop_turntable()
                with self.lock:
                    self._turntable_state_changed = True
                    self._turntableState = State.STOPPED
                    self._turntableReason = Reason.SENSOR_REACHED
                    self._thread = None
            elif self._last_position == State.CONV:
                self._rot_table_counter_clockwise()
                self._senseTurntablePosSaw.wait()
                self._stop_turntable()
                with self.lock:
                    self._turntable_state_changed = True
                    self._turntableState = State.STOPPED
                    self._turntableReason = Reason.SENSOR_REACHED
                    self._thread = None

    def move_to_vac(self):
        if self._senseTurntablePosVacuum:
            self._logger.info("Turntable already on vacuum position")
            return
        else:
            self._turntable_state_changed = True
            self._turntableState = State.MOVING_TO_SAW
            self._turntableReason = Reason.CMD
            self._rot_table_counter_clockwise()
            self._senseTurntablePosVacuum.wait()
            self._stop_turntable()
            with self.lock:
                self._turntable_state_changed = True
                self._turntableState = State.STOPPED
                self._turntableReason = Reason.SENSOR_REACHED
                self._thread = None

    def move_to_conv(self):
        if self._senseTurntablePosConveyor:
            self._logger.info("Turntable already on conveyor position")
            return
        else:
            self._turntable_state_changed = True
            self._turntableState = State.MOVING_TO_CONV
            self._turntableReason = Reason.CMD
            self._rot_table_clockwise()
            self._senseTurntablePosConveyor.wait()
            self._stop_turntable()
            with self.lock:
                self._turntable_state_changed = True
                self._turntableState = State.STOPPED
                self._turntableReason = Reason.SENSOR_REACHED
                self._thread = None

    def _stop_vac_moving(self):
        if self._actGripperToTurntable:
            self._actGripperToTurntable.value = False
        if self._actGripperToOven:
            self._actGripperToOven.value = False

    def stop_vac_moving(self):
        self._vacuum_gripper_state_changed = True
        self._vacuumGripperState = State.STOPPED
        self._vacuumGripperReason = Reason.CMD
        self._stop_vac_moving()

    def _move_vac_oven(self):
        if self._actGripperToTurntable:
            self._logger.info("Switching from moving to turntable to moving to oven")
            self._actGripperToTurntable.value = False
        self._actGripperToOven.value = True

    def move_vac_oven(self):
        if not self._senseVacuumGripperAtOven:
            self._vacuum_gripper_state_changed = True
            self._vacuumGripperState = State.MOVING_TO_OVEN
            self._vacuumGripperReason = Reason.CMD
            self._move_vac_oven()
            self._senseVacuumGripperAtOven.wait()
            self._stop_vac_moving()
            with self.lock:
                self._vacuum_gripper_state_changed = True
                self._vacuumGripperState = State.STOPPED
                self._vacuumGripperReason = Reason.SENSOR_REACHED
                self._thread = None
        else:
            self._logger.info("Vacuum gripper already at oven")
            return

    def _move_vac_turntable(self):
        if self._actGripperToOven:
            self._logger.info("Switching from moving to oven to moving to turntable")
            self._actGripperToOven.value = False
        self._actGripperToTurntable.value = True

    def move_vac_turntable(self):
        if not self._senseVacuumGripperAtTurntable:
            self._vacuum_gripper_state_changed = True
            self._vacuumGripperState = State.MOVING_TO_TURNTABLE
            self._vacuumGripperReason = Reason.CMD
            self._move_vac_turntable()
            self._senseVacuumGripperAtTurntable.wait()
            self._stop_vac_moving()
            with self.lock:
                self._vacuum_gripper_state_changed = True
                self._vacuumGripperState = State.STOPPED
                self._vacuumGripperReason = Reason.SENSOR_REACHED
                self._thread = None
        else:
            self._logger.info("Vacuum gripper already at turntable")
            return

    def stop_freezing(self):
        with self.lock:
            self._freezer_state_changed = True
            self._freezerState = State.STOPPED
            self._freezerReason = Reason.CMD
        self.stop_saw()
        self.stop_vac_moving()
        self.stop_turntable()
        self.stop_conveyor()
        self.stop_moving_feeder()
        self.deactivate_compressor()
        with self.lock:
            self._thread = None

    def handle_control(self, control: dict):
        try:
            cmd = control["feeder"]
            self._oven_feeder_state_changed = True
            if cmd.startswith("in"):
                self._thread = Thread(target=self.move_feeder_in, daemon=True)
                self._thread.start()
            elif cmd.startswith("out"):
                self._thread = Thread(target=self.move_feeder_out, daemon=True)
                self._thread.start()
            elif cmd.startswith("stop"):
                self.stop_moving_feeder()
            else:
                self._logger.warning("Unknown command")
                return
        except KeyError:
            pass
        try:
            cmd = control["compressor"]
            self._compressor_state_changed = True
            if cmd.startswith("on"):
                self.activate_compressor()
            elif cmd.startswith("off"):
                self.deactivate_compressor()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["ovenDoorValve"]
            self._valve_oven_door_state_changed = True
            if cmd.startswith("open"):
                self.open_oven_door_valve()
            elif cmd.startswith("close"):
                self.close_oven_door_valve()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["vacuumGripperValve"]
            self._valve_state_changed = True
            if cmd.startswith("open"):
                self.open_vacuum_gripper_valve()
            elif cmd.startswith("close"):
                self.close_vacuum_gripper_valve()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["feederValve"]
            self._valve_state_changed = True
            if cmd.startswith("open"):
                self.open_feeder_valve()
            elif cmd.startswith("close"):
                self.close_feeder_valve()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["conveyor"]
            self._conveyor_state_changed = True
            if cmd.startswith("move"):
                self._thread = Thread(target=self.move_conveyor, daemon=True)
                self._thread.start()
            elif cmd.startswith("stop"):
                self.stop_conveyor()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["saw"]
            self._saw_state_changed = True
            if cmd.startswith("on"):
                self.start_saw()
            elif cmd.startswith("off"):
                self.stop_saw()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["ovenLight"]
            self._oven_state_changed = True
            if cmd.startswith("start"):
                self._thread = Thread(target=self.start_oven_light, daemon=True)
                self._thread.start()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["turntable"]
            self._turntable_state_changed = True
            if cmd.startswith("vac"):
                self._thread = Thread(target=self.move_to_vac, daemon=True)
                self._thread.start()
            elif cmd.startswith("saw"):
                self._thread = Thread(target=self.move_to_saw, daemon=True)
                self._thread.start()
            elif cmd.startswith("conv"):
                self._thread = Thread(target=self.move_to_conv, daemon=True)
                self._thread.start()
            elif cmd.startswith("stop"):
                self.stop_turntable()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["vacuumGripper"]
            self._vacuum_gripper_state_changed = True
            if cmd.startswith("oven"):
                self._thread = Thread(target=self.move_vac_oven, daemon=True)
                self._thread.start()
            elif cmd.startswith("turntable"):
                self._thread = Thread(target=self.move_vac_turntable, daemon=True)
                self._thread.start()
            if cmd.startswith("stop"):
                self._thread = Thread(target=self.stop_vac_moving, daemon=True)
                self._thread.start()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["lowerValve"]
            self._lower_valve_state_changed = True
            if cmd.startswith("open"):
                self.open_lower_valve()
            elif cmd.startswith("close"):
                self.close_lower_valve()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass
        try:
            cmd = control["freezer"]
            if cmd.startswith("stop"):
                self._thread = Thread(target=self.stop_freezing, daemon=True)
                self._thread.start()
            elif cmd.startswith("reset"):
                self._thread = Thread(target=self.reset, daemon=True)
                self._thread.start()
            else:
                self._logger.warning("Unknown command")
        except KeyError:
            pass

    def get_status(self):
        status = {
            "turntableState": self._turntableState.value,
            "conveyorState": self._conveyorState.value,
            "vacuumGripperState": self._vacuumGripperState.value,
            "compressorState": self._compressorState.value,
            "valveState": self._valveState.value,
            "lowerValveState": self._lowerValveState.value,
            "valveOvenDoorState": self._valveOvenDoorState.value,
            "valveFeederState": self._valveFeederState.value,
            "sawState": self._sawState.value,
            "ovenState": self._ovenState.value,
            "ovenFeederState": self._ovenFeederState.value,
            "freezerState": self._freezerState.value,
            "turntableReason": self._turntableReason.value,
            "conveyorReason": self._conveyorReason.value,
            "vacuumGripperReason": self._vacuumGripperReason.value,
            "compressorReason": self._compressorReason.value,
            "valveReason": self._valveReason.value,
            "lowerValveReason": self._lowerValveReason.value,
            "valveOvenDoorReason": self._valveOvenDoorReason.value,
            "valveFeederReason": self._valveFeederReason.value,
            "sawReason": self._sawReason.value,
            "ovenReason": self._ovenReason.value,
            "ovenFeederReason": self._ovenFeederReason.value,
            "freezerReason": self._freezerReason.value,
            "turntablePosVacuumSensor": self._senseTurntablePosVacuum.value,
            "turntablePosConveyorSensor": self._senseTurntablePosConveyor.value,
            "turntablePosSawSensor": self._senseTurntablePosSaw.value,
            "deliverySensor": not self._senseDelivery.value,
            "vacuumGripperAtTurntableSensor": not self._senseVacuumGripperAtTurntable.value,
            "vacuumGripperAtOvenSensor": not self._senseVacuumGripperAtOven.value,
            "ovenFeederInSensor": self._senseOvenFeederIn.value,
            "ovenFeederOutSensor": self._senseOvenFeederOut.value,
            "ovenSensor": not self._senseOven.value
        }

        self._turntable_state_changed = False
        self._conveyor_state_changed = False
        self._vacuum_gripper_state_changed = False
        self._compressor_state_changed = False
        self._valve_state_changed = False
        self._lower_valve_state_changed = False
        self._valve_oven_door_state_changed = False
        self._valve_feeder_state_changed = False
        self._saw_state_changed = False
        self._oven_state_changed = False
        self._oven_feeder_state_changed = False
        self._freezer_state_changed = False

        return status

    def destroy(self):
        if self.is_running():
            return

    def state_changed(self):
        return (self._turntable_state_changed
                or self._conveyor_state_changed
                or self._vacuum_gripper_state_changed
                or self._compressor_state_changed
                or self._valve_state_changed
                or self._lower_valve_state_changed
                or self._valve_oven_door_state_changed
                or self._valve_feeder_state_changed
                or self._saw_state_changed
                or self._oven_state_changed
                or self._oven_feeder_state_changed)

    def _stop_all(self):
        self._actRotClockwise = False
        self._actRotCounterclockwise = False
        self._actConveyorForward = False
        self._actSaw = False
        self._actOvenInward = False
        self._actOvenOutward = False
        self._actGripperToOven = False
        self._actGripperToTurntable = False
        self._actOvenLight = False
        self._actCompressor = False
        self._actValve = False
        self._actLowerValve = False
        self._actValveOvenDoor = False
        self._actValveFeeder = False

    def cleanup(self):
        while True:
            with self.lock:
                thread = self._thread
                if thread is None:
                    self._stop_all()
                    return
                thread.join()
