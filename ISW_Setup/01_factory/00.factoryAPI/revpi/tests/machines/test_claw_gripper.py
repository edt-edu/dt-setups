import sys

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.claw_gripper import ClawGripper


cg_test_config = {
    'arm': {
        'forward_limit': 81,
    },
    'claw': {
        'forward_limit': 20,
    },
    'vertical': {
        'forward_limit': 2400,
        'accuracy': 5,
    },
    'rotor': {
        'forward_limit': 4000,
        'accuracy': 5,
    },
}


def get_test_cg_ports():
    return {
        "senseVerticalEndUp": MockInput('verticalLimit', True),
        "senseArmEndIn": MockInput('armLimit', True),
        "senseRotEndRight": MockInput('rotLimit', True),
        "senseClawOpen": MockInput('clawOpen', True),
        "encoderVertical": MockCounter('verticalCounter', 12345),
        "encoderRot": MockCounter('rotCounter', 12345),
        "impulseCounterArm": MockCounter('impulseCounterArm', 0),
        "impulseCounterClaw": MockCounter('impulseCounterClaw', 0),
        "actVerticalUp": MockOutput('actUp', False),
        "actVerticalDown": MockOutput('actDown', False),
        "actArmIn": MockOutput('actIn', False),
        "actArmOut": MockOutput('actOut', False),
        "actRotRight": MockOutput('actRight', False),
        "actRotLeft": MockOutput('actLeft', False),
        "actClawClose": MockOutput('actClose', False),
        "actClawOpen": MockOutput('actOpen', False),
    }


def reset_cg_ports(ports):
    ports['senseVerticalEndUp'].io_set_value(True)
    ports['senseArmEndIn'].io_set_value(True)
    ports['senseRotEndRight'].io_set_value(True)
    ports['senseClawOpen'].io_set_value(True)
    ports['encoderVertical'].io_set_value(0)
    ports['encoderRot'].io_set_value(0)
    ports['impulseCounterArm'].io_set_value(0)
    ports['impulseCounterClaw'].io_set_value(0)
    ports['actVerticalUp'].value = False
    ports['actVerticalDown'].value = False
    ports['actArmIn'].value = False
    ports['actArmOut'].value = False
    ports['actRotRight'].value = False
    ports['actRotLeft'].value = False
    ports['actClawClose'].value = False
    ports['actClawOpen'].value = False


def test_init():
    ports = get_test_cg_ports()
    cg = ClawGripper(cg_test_config, ports)

    assert cg.state_changed()
    status = cg.get_status()
    assert status["rotor-status"] == "stopped"
    assert status["arm-status"] == "stopped"
    assert status["vertical-status"] == "stopped"
    assert status["claw-status"] == "stopped"


class TestClawGripper:
    ports = get_test_cg_ports()
    cg = ClawGripper(cg_test_config, ports)

    def setup_method(self):
        self.cg._vertical._target = None
        self.cg._arm._target = None
        self.cg._rotor._target = None
        self.cg._claw._target = None
        reset_cg_ports(self.ports)
        self.cg._state_changed = False

    @staticmethod
    def _set_position(position, switch, counter):
        if position <= 0:
            switch.io_set_value(True)
        else:
            switch.io_set_value(False)
        counter.io_set_value(position)

    def set_position(self, vertical=None, arm=None, rotor=None, claw=None):
        if vertical is not None:
            self._set_position(vertical,
                               self.ports['senseVerticalEndUp'],
                               self.ports['encoderVertical'])
        if arm is not None:
            self._set_position(arm,
                               self.ports['senseArmEndIn'],
                               self.ports['impulseCounterArm'])
        if rotor is not None:
            self._set_position(rotor,
                               self.ports['senseRotEndRight'],
                               self.ports['encoderRot'])
        if claw is not None:
            self._set_position(claw,
                               self.ports['senseClawOpen'],
                               self.ports['impulseCounterClaw'])

    def test_move_vertical_down_and_stop(self):
        self.cg.handle_control({"vertical": "down"})

        assert self.ports['actVerticalDown'].value is True
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["vertical-status"] == "moving-down"
        assert status["vertical-reason"] == "cmd"

        self.set_position(vertical=500)

        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["vertical-status"] == "moving-down"
        assert status["vertical-reason"] == "cmd"
        assert status["limit-switch-up"] is False
        assert status["vertical-position"] == 500

        self.cg.handle_control({"vertical": "stop"})

        assert self.ports['actVerticalDown'].value is False
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["vertical-status"] == "stopped"
        assert status["vertical-reason"] == "cmd"

    def test_move_arm_out_limit(self):
        self.cg.handle_control({"arm": "out"})

        assert self.ports['actArmOut'].value is True
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["arm-status"] == "moving-out"
        assert status["arm-reason"] == "cmd"

        self.set_position(arm=2000)

        assert self.ports['actArmOut'].value is False
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["arm-status"] == "stopped"
        assert status["arm-reason"] == "position-limit"
        assert status["arm-position"] == 2000

    def test_rotate_to_position(self):
        self.cg.handle_control({"rotate": "to=643"})

        assert self.ports['actRotLeft'].value is True
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["rotor-status"] == "targeting"
        assert status["rotor-reason"] == "cmd"
        assert status["rotor-target"] == 643

        self.set_position(rotor=644)

        assert self.ports['actRotLeft'].value is False
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["rotor-status"] == "stopped"
        assert status["rotor-reason"] == "target-reached"
        assert status["rotor-target"] == 643
        assert status["rotor-position"] == 644
    
    def test_close_claw(self):
        self.cg.handle_control({"claw": "close"})

        assert self.ports['actClawClose'].value is True
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["claw-status"] == "closing"
        assert status["claw-reason"] == "cmd"

        self.set_position(claw=20)

        assert self.ports['actClawClose'].value is False
        assert self.cg.state_changed()
        status = self.cg.get_status()
        assert status["claw-status"] == "stopped"
        assert status["claw-reason"] == "position-limit"
        assert status["claw-position"] == 20
