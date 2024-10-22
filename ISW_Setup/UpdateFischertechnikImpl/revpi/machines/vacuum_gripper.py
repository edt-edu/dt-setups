import logging
import threading
from typing import Any, Dict, Optional
from revpimodio2.io import IOBase

from .actuator_1d import Actuator1D, State
from .base_machine import BaseMachine


class VacuumGripper(BaseMachine):
    """Vacuum Gripper

    If not specified otherwise, all method calls expect the lock to be locked.

    In order to prevent collisions, the reset sequence should be:
    1. Disable the compressor and valve (don't grip)
    2. Move to upper limit
    3. Move arm in
    4. Rotate right
    """

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):
        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)

        self._logger.debug("Starting port initialisation")

        self._compressor = ports['actCompressorOn']
        self._valve = ports['actValve']

        self._compressor.value = False
        self._valve.value = False

        self._state_changed = True

        self._vertical = Actuator1D.get_from_configuration(
            ports['actVerticalDown'],
            ports['actVerticalUp'],
            ports['senseVerticalEndUp'],
            ports['encoderVertical'],
            config['vertical'],
            logger=self._logger.getChild('verticalActuator'),
            lock=self._lock
        )
        self._arm = Actuator1D.get_from_configuration(
            ports['actArmOut'],
            ports['actArmIn'],
            ports['senseArmEndIn'],
            ports['encoderArm'],
            config['arm'],
            logger=self._logger.getChild('armActuator'),
            lock=self._lock
        )
        self._rotor = Actuator1D.get_from_configuration(
            ports['actRotLeft'],
            ports['actRotRight'],
            ports['senseRotEndRight'],
            ports['encoderRot'],
            config['rotor'],
            logger=self._logger.getChild('rotorActuator'),
            lock=self._lock
        )

        self._logger.debug("Initialisation completed")

    def stop_moving(self):
        self._logger.info("Stop all movements")
        self._vertical.stop_moving()
        self._arm.stop_moving()
        self._rotor.stop_moving()
        self.stop_compressor()

    @property
    def vertical_position(self):
        return self._vertical.position

    def move_up(self):
        self._vertical.move_backward()

    def move_down(self):
        self._vertical.move_forward()

    def move_vertical_to(self, target, **kwargs):
        self._vertical.move_to_position(target, **kwargs)

    @property
    def arm_position(self):
        return self._arm.position

    def move_in(self):
        self._arm.move_backward()

    def move_out(self):
        self._arm.move_forward()

    def move_arm_to(self, target, **kwargs):
        self._arm.move_to_position(target, **kwargs)

    @property
    def rotor_position(self):
        return self._rotor.position

    def rotate_right(self):
        self._rotor.move_backward()

    def rotate_left(self):
        self._rotor.move_forward()

    def rotate_to(self, target, **kwargs):
        self._rotor.move_to_position(target, **kwargs)

    @property
    def compressor_is_on(self):
        return self._compressor.value

    def start_compressor(self):
        self._logger.debug("Activating compressor")
        self._compressor.value = True

    def stop_compressor(self):
        self._logger.debug("Deactivating compressor")
        self._compressor.value = False

    @property
    def valve_is_gripping(self):
        return self._valve.value

    def valve_grip(self):
        self._logger.debug("Activating valve")
        self._valve.value = True

    def valve_release(self):
        self._logger.debug("Deactivating valve")
        self._valve.value = False

    def grip(self):
        """Grip an Any

        The compressor might need some time before gripping is possible.
        The Any might not be fully gripped until some time has passed.
        NOTE: The gripper must be in contact with the object at the time the valve is closed.
        NOTE: The compressor must be on for approximately one second after the valve is closed.
            After that, the gripper can hold items for approximately 30 seconds. For longer
            actions the compressor should stay on.

        The calling method must hold the lock.
        """
        self.start_compressor()
        self.valve_grip()

    def release(self):
        """ Release any gripped item and stop the compressor

        The calling method must hold the lock.
        """
        self.stop_compressor()
        self.valve_release()

    def handle_control(self, control: dict):
        try:
            cmd = control["rotate"]
            self._rotor.handle_control(cmd, forward="left", backward="right")
        except KeyError:
            pass
        try:
            cmd = control["arm"]
            self._arm.handle_control(cmd, forward="out", backward="in")
        except KeyError:
            pass
        try:
            cmd = control["vertical"]
            self._vertical.handle_control(cmd, forward="down", backward="up")
        except KeyError:
            pass
        try:
            compressor = control["compressor"]
            self._state_changed = True
            self._compressor.value = compressor
        except KeyError:
            pass
        try:
            valve = control["valve"]
            self._state_changed = True
            self._valve.value = valve
        except KeyError:
            pass

    def cleanup(self):
        with self.lock:
            self.destroy()

    def destroy(self):
        self._rotor.destroy()
        self._arm.destroy()
        self._vertical.destroy()
        self._compressor.value = False
        self._valve.value = False

    def state_changed(self):
        return (self._state_changed
                or self._arm.state_changed()
                or self._vertical.state_changed()
                or self._rotor.state_changed())

    def get_status(self):
        self._state_changed = False
        rotor_status = self._rotor.get_status()
        arm_status = self._arm.get_status()
        vertical_status = self._vertical.get_status()
        status = {
            "rotor-status": self._rotor_state(rotor_status["state"]),
            "rotor-reason": rotor_status["reason"].value,
            "limit-switch-right": rotor_status["limit-switch"],
            "rotor-position": rotor_status["position"],

            "arm-status": self._arm_state(arm_status["state"]),
            "arm-reason": arm_status["reason"].value,
            "limit-switch-in": arm_status["limit-switch"],
            "arm-position": arm_status["position"],

            "vertical-status": self._vertical_state(vertical_status["state"]),
            "vertical-reason": vertical_status["reason"].value,
            "limit-switch-up": vertical_status["limit-switch"],
            "vertical-position": vertical_status["position"],

            "compressor": self._compressor.value,
            "valve": self._valve.value,
        }
        try:
            status["rotor-target"] = rotor_status["target"]
        except KeyError:
            pass
        try:
            status["arm-target"] = arm_status["target"]
        except KeyError:
            pass
        try:
            status["vertical-target"] = vertical_status["target"]
        except KeyError:
            pass
        return status

    @staticmethod
    def _vertical_state(state: State):
        if state is State.MOVING_FORWARD:
            return "moving-down"
        elif state is State.MOVING_BACKWARD:
            return "moving-up"
        else:
            return state.value

    @staticmethod
    def _arm_state(state: State):
        if state is State.MOVING_FORWARD:
            return "moving-out"
        elif state is State.MOVING_BACKWARD:
            return "moving-in"
        else:
            return state.value

    @staticmethod
    def _rotor_state(state: State):
        if state is State.MOVING_FORWARD:
            return "rotating-left"
        elif state is State.MOVING_BACKWARD:
            return "rotating-right"
        else:
            return state.value
