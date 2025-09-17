import sys

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.vacuum_gripper import VacuumGripper


vg_test_config = {
    'arm': {
        'forward_limit': 1950,
        'accuracy': 5,
    },
    'vertical': {
        'forward_limit': 1692,
        'accuracy': 5,
    },
    'rotor': {
        'forward_limit': 2800,
        'accuracy': 5,
    },
}


def get_test_vg_ports():
    return {
        "senseVerticalEndUp": MockInput('verticalLimit', True),
        "senseArmEndIn": MockInput('armLimit', True),
        "senseRotEndRight": MockInput('rotLimit', True),
        "encoderVertical": MockCounter('verticalCounter', 12345),
        "encoderArm": MockCounter('armCounter', 12345),
        "encoderRot": MockCounter('rotCounter', 12345),
        "actVerticalUp": MockOutput('actUp', False),
        "actVerticalDown": MockOutput('actDown', False),
        "actArmIn": MockOutput('actIn', False),
        "actArmOut": MockOutput('actOut', False),
        "actRotRight": MockOutput('actRight', False),
        "actRotLeft": MockOutput('actLeft', False),
        "actCompressorOn": MockOutput('actCompressorOn', False),
        "actValve": MockOutput('actValve', False),
    }


def reset_vg_ports(ports):
    ports['senseVerticalEndUp'].io_set_value(True)
    ports['senseArmEndIn'].io_set_value(True)
    ports['senseRotEndRight'].io_set_value(True)
    ports['encoderVertical'].io_set_value(0)
    ports['encoderArm'].io_set_value(0)
    ports['encoderRot'].io_set_value(0)
    ports['actVerticalUp'].value = False
    ports['actVerticalDown'].value = False
    ports['actArmIn'].value = False
    ports['actArmOut'].value = False
    ports['actRotRight'].value = False
    ports['actRotLeft'].value = False
    ports['actCompressorOn'].value = False
    ports['actValve'].value = False


def test_init():
    ports = get_test_vg_ports()
    vg = VacuumGripper(vg_test_config, ports)

    assert vg.state_changed()
    status = vg.get_status()
    assert status["rotor-status"] == "stopped"
    assert status["arm-status"] == "stopped"
    assert status["vertical-status"] == "stopped"
    assert status["compressor"] is False
    assert status["valve"] is False
    assert ports['actCompressorOn'].value is False
    assert ports['actValve'].value is False


class TestVacuumGripper:
    ports = get_test_vg_ports()
    vg = VacuumGripper(vg_test_config, ports)

    def setup_method(self):
        self.vg._vertical._target = None
        self.vg._arm._target = None
        self.vg._rotor._target = None
        reset_vg_ports(self.ports)
        self.vg._state_changed = False

    @staticmethod
    def _set_position(position, switch, counter):
        if position <= 0:
            switch.io_set_value(True)
        else:
            switch.io_set_value(False)
        counter.io_set_value(position)

    def set_position(self, vertical=None, arm=None, rotor=None):
        if vertical is not None:
            self._set_position(vertical,
                               self.ports['senseVerticalEndUp'],
                               self.ports['encoderVertical'])
        if arm is not None:
            self._set_position(arm,
                               self.ports['senseArmEndIn'],
                               self.ports['encoderArm'])
        if rotor is not None:
            self._set_position(rotor,
                               self.ports['senseRotEndRight'],
                               self.ports['encoderRot'])

    def test_move_vertical_down_and_stop(self):
        self.vg.handle_control({"vertical": "down"})

        assert self.ports['actVerticalDown'].value is True
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["vertical-status"] == "moving-down"
        assert status["vertical-reason"] == "cmd"

        self.set_position(vertical=500)

        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["vertical-status"] == "moving-down"
        assert status["vertical-reason"] == "cmd"
        assert status["limit-switch-up"] is False
        assert status["vertical-position"] == 500

        self.vg.handle_control({"vertical": "stop"})

        assert self.ports['actVerticalDown'].value is False
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["vertical-status"] == "stopped"
        assert status["vertical-reason"] == "cmd"

    def test_move_arm_out_limit(self):
        self.vg.handle_control({"arm": "out"})

        assert self.ports['actArmOut'].value is True
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["arm-status"] == "moving-out"
        assert status["arm-reason"] == "cmd"

        self.set_position(arm=2000)

        assert self.ports['actArmOut'].value is False
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["arm-status"] == "stopped"
        assert status["arm-reason"] == "position-limit"
        assert status["arm-position"] == 2000

    def test_rotate_to_position(self):
        self.vg.handle_control({"rotate": "to=643"})

        assert self.ports['actRotLeft'].value is True
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["rotor-status"] == "targeting"
        assert status["rotor-reason"] == "cmd"
        assert status["rotor-target"] == 643

        self.set_position(rotor=644)

        assert self.ports['actRotLeft'].value is False
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["rotor-status"] == "stopped"
        assert status["rotor-reason"] == "target-reached"
        assert status["rotor-target"] == 643
        assert status["rotor-position"] == 644

    def test_activate_compressor_valve(self):
        self.vg.handle_control({"compressor": True, "valve": True})

        assert self.ports['actCompressorOn'].value is True
        assert self.ports['actValve'].value is True
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["compressor"] is True
        assert status["valve"] is True
        assert self.vg.compressor_is_on

    def test_deactivate_compressor_valve(self):
        self.vg.handle_control({"compressor": True, "valve": True})

        self.vg.handle_control({"compressor": False, "valve": False})

        assert self.ports['actCompressorOn'].value is False
        assert self.ports['actValve'].value is False
        assert self.vg.state_changed()
        status = self.vg.get_status()
        assert status["compressor"] is False
        assert status["valve"] is False
