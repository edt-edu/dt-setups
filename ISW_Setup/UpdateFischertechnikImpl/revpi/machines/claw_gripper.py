import logging
import threading
from typing import Any, Dict, Optional
from revpimodio2.io import IOBase

from .actuator_1d import Actuator1D, State
from .base_machine import BaseMachine
from .impulse_actuator_1d import ImpulseActuator1D, State as ImpulseState


class ClawGripper(BaseMachine):
    """Claw Gripper

    If not specified otherwise, all method calls expect the lock to be locked.
    """

    def __init__(self, config: Dict[str, Any],
                 ports: Dict[str, IOBase],
                 logger: Optional[logging.Logger] = None,
                 lock: Optional[threading.Lock] = None):
        if logger is None:
            logger = logging.getLogger(__name__)
        super().__init__(logger, lock)

        self._logger.debug("Starting port initialisation")

        self._state_changed = True

        self._vertical = Actuator1D.get_from_configuration(
            ports['actVerticalDown'],
            ports['actVerticalUp'],
            ports['senseVerticalEndUp'],
            ports['encoderVertical'],
            config['vertical'],
            logger=self._logger.getChild('verticalActuator'),
            lock=lock
        )
        self._arm = ImpulseActuator1D.get_from_configuration(
            ports['actArmOut'],
            ports['actArmIn'],
            ports['senseArmEndIn'],
            ports['impulseCounterArm'],
            config['arm'],
            logger=self._logger.getChild('armActuator'),
            lock=lock
        )
        self._rotor = Actuator1D.get_from_configuration(
            ports['actRotLeft'],
            ports['actRotRight'],
            ports['senseRotEndRight'],
            ports['encoderRot'],
            config['rotor'],
            logger=self._logger.getChild('rotorActuator'),
            lock=lock)
        self._claw = ImpulseActuator1D.get_from_configuration(
            ports['actClawClose'],
            ports['actClawOpen'],
            ports['senseClawOpen'],
            ports['impulseCounterClaw'],
            config['claw'],
            logger=self._logger.getChild('clawActuator'),
            lock=lock
        )

    def stop_moving(self):
        self._logger.info("Stop all movements")
        self._vertical.stop_moving()
        self._arm.stop_moving()
        self._rotor.stop_moving()
        self._claw.stop_moving()

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
        return self._arm._impulseCounter.value

    def arm_reset(self):
        self._arm._reset()

    def move_out(self):
        self._arm.move_forward()

    def move_arm_to(self, target):
        self._arm._move_impulse_forward(target)

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
    def impulse_counter_claw(self):
        return self._claw._impulseCounter.value

    def claw_close_to(self, target):
        self._claw._move_impulse_forward(target)

    def claw_reset(self):
        self._claw._reset()

    def act_claw_close(self):
        self._claw.move_forward()

    def handle_control(self, control: dict):
        try:
            cmd = control["rotate"]
            self._rotor.handle_control(cmd, forward="left", backward="right")
        except KeyError:
            pass
        try:
            cmd = control["arm"]
            self._arm.handle_control(cmd, forward="out")
        except KeyError:
            pass
        try:
            cmd = control["vertical"]
            self._vertical.handle_control(cmd, forward="down", backward="right")
        except KeyError:
            pass
        try:
            cmd = control["claw"]
            self._claw.handle_control(cmd, forward="close")
        except KeyError:
            pass

    def state_changed(self):
        return (self._state_changed
                or self._arm.state_changed()
                or self._vertical.state_changed()
                or self._rotor.state_changed()
                or self._claw.state_changed())

    def get_status(self):
        self._state_changed = False
        rotor_status = self._rotor.get_status()
        arm_status = self._arm.get_status()
        vertical_status = self._vertical.get_status()
        claw_status = self._claw.get_status()
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

            "claw-status": self._claw_state(claw_status["state"]),
            "claw-reason": claw_status["reason"].value,
            "limit-switch-open": claw_status["limit-switch"],
            "claw-position": claw_status["position"],

        }
        try:
            status["rotor-target"] = rotor_status["target"]
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
    def _arm_state(state: ImpulseState):
        if state == ImpulseState.MOVING_FORWARD:
            return "moving-out"
        elif state == ImpulseState.MOVING_BACKWARD:
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

    @staticmethod
    def _claw_state(state: ImpulseState):
        if state == ImpulseState.MOVING_FORWARD:
            return "closing"
        elif state == ImpulseState.MOVING_BACKWARD:
            return "opening"
        else:
            return state.value

    def cleanup(self):
        with self.lock:
            self.destroy()

    def destroy(self):
        self._rotor.destroy()
        self._arm.destroy()
        self._vertical.destroy()
        self._claw.destroy()
