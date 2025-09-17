import sys
from typing import Any, Dict

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.conveyorbelt import Conveyorbelt


cb_test_config: Dict[str, Any] = {}


def get_test_cb_ports():
    return {
        "senseLeft": MockInput('left', True),
        "senseRight": MockInput('right', True),
        "senseImpulse": MockCounter('impulseCounter', 0),
        "actRight": MockOutput('actRight', False),
        "actLeft": MockOutput('actLeft', False),
    }


def reset_cb_desc(ports):
    ports['senseLeft'].io_set_value(True)
    ports['senseRight'].io_set_value(True)
    ports['senseImpulse'].io_set_value(0)
    ports['actRight'].value = False
    ports['actLeft'].value = False


def test_init():
    ports = get_test_cb_ports()
    cb = Conveyorbelt(cb_test_config, ports)

    assert cb.state_changed()
    status = cb.get_status()
    assert status["state"] == "reset"


class TestConveyorbelt:
    ports = get_test_cb_ports()
    cb = Conveyorbelt(cb_test_config, ports)

    def setup_method(self):
        reset_cb_desc(self.ports)
        self.cb._state_changed = False
        self.cb._targeting = False

    def test_move_right(self):
        self.cb.handle_control({"motor": "right"})

        assert self.ports['actRight'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "moving-right"
        assert status["reason"] == "cmd"

        self.ports['senseRight'].io_set_value(False)

        assert self.ports['actRight'].value is False
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "stopped"
        assert status["reason"] == "position-limit"

    def test_move_left(self):
        self.cb.handle_control({"motor": "left"})

        assert self.ports['actLeft'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "moving-left"
        assert status["reason"] == "cmd"

        self.ports['senseLeft'].io_set_value(False)

        assert self.ports['actLeft'].value is False
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "stopped"
        assert status["reason"] == "position-limit"

    def test_no_stop_on_opposite_barrier_right(self):
        self.cb.handle_control({"motor": "right"})

        self.ports['senseLeft'].io_set_value(False)

        assert self.ports['actRight'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "moving-right"
        assert status["reason"] == "cmd"

    def test_no_stop_on_opposite_barrier_left(self):
        self.cb.handle_control({"motor": "left"})

        self.ports['senseRight'].io_set_value(False)

        assert self.ports['actLeft'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "moving-left"
        assert status["reason"] == "cmd"

    def test_stop(self):
        self.cb.handle_control({"motor": "right"})
        assert self.ports['actRight'].value is True

        self.cb.handle_control({"motor": "stop"})
        assert self.ports['actRight'].value is False
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "stopped"
        assert status["reason"] == "cmd"

    def test_move_right_to(self):
        self.cb.handle_control({"motor": "right-to=1000"})

        assert self.ports['actRight'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "targeting"
        assert status["reason"] == "cmd"

        self.ports['senseImpulse'].io_set_value(1000)

        assert self.ports['actRight'].value is False
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "stopped"
        assert status["reason"] == "target-reached"

    def test_move_left_to(self):
        self.cb.handle_control({"motor": "left-to=1000"})

        assert self.ports['actLeft'].value is True
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "targeting"
        assert status["reason"] == "cmd"

        self.ports['senseImpulse'].io_set_value(1000)

        assert self.ports['actLeft'].value is False
        assert self.cb.state_changed()
        status = self.cb.get_status()
        assert status["state"] == "stopped"
        assert status["reason"] == "target-reached"