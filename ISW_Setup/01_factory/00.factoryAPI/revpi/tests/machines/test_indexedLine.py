import sys
from unittest.mock import patch

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.indexedLine import IndexedLine


def get_test_il_ports():
    return {
        "sensePushButton1Front": MockInput('button1Front', True),
        "sensePushButton1Back": MockInput('button1Back', True),
        "sensePushButton2Front": MockInput('button2Front', True),
        "sensePushButton2Back": MockInput('button2Back', True),
        "senseSlider1": MockInput('slider1', True),
        "senseMilling": MockInput('senseMilling', True),
        "senseLoading": MockInput('senseLoading', True),
        "senseDrilling": MockInput('senseDrilling', True),
        "senseConveyorSwap": MockInput('senseConveyorSwap', True),
        "actMotorSlider1Backward": MockOutput('motorSlider1Backward', False),
        "actMotorSlider1Forward": MockOutput('motorSlider1Forward', False),
        "actMotorSlider2Backward": MockOutput('motorSlider2Backward', False),
        "actMotorSlider2Forward": MockOutput('motorSlider2Forward', False),
        "actConveyorBeltFeed": MockOutput('conveyorBeltFeed', False),
        "actConveyorBeltMilling": MockOutput('conveyorBeltMilling', False),
        "actMillingMachine": MockOutput('millingMachine', False),
        "actConveyorBeltDrilling": MockOutput('conveyorBeltDrilling', False),
        "actDrillingMachine": MockOutput('drillingMachine', False),
        "actConveyorBeltSwap": MockOutput('conveyorBeltSwap', False),
        }


def reset_il_ports(ports):
    ports['sensePushButton1Front'].io_set_value(True)
    ports['sensePushButton1Back'].io_set_value(True)
    ports['sensePushButton2Front'].io_set_value(True)
    ports['sensePushButton2Back'].io_set_value(True)
    ports['senseSlider1'].io_set_value(True)
    ports['senseMilling'].io_set_value(True)
    ports['senseLoading'].io_set_value(True)
    ports['senseDrilling'].io_set_value(True)
    ports['senseConveyorSwap'].io_set_value(True)
    ports['actMotorSlider1Backward'].value = False
    ports['actMotorSlider1Forward'].value = False
    ports['actMotorSlider2Backward'].value = False
    ports['actMotorSlider2Forward'].value = False
    ports['actConveyorBeltFeed'].value = False
    ports['actConveyorBeltMilling'].value = False
    ports['actMillingMachine'].value = False
    ports['actConveyorBeltDrilling'].value = False
    ports['actDrillingMachine'].value = False
    ports['actConveyorBeltSwap'].value = False


il_test_config = {
    'drill_time': 2.0,
    'mill_time': 2.0,
    'conveyor_movement_time': 1.0,
}

def test_init():
    ports = get_test_il_ports()
    il = IndexedLine(il_test_config, ports)

    assert il.state_changed()
    status = il.get_status()
    assert status["state"] == "ready"
    assert status["reason"] == "reset"
    assert status["package_at_drill"] is False
    assert status["package_at_mill"] is False
    assert status["package_at_slider"] is False
    assert status["package_at_end"] is False
    assert status["package_at_start"] is False


class TestIndexedLine:
    ports = get_test_il_ports()
    il = IndexedLine(il_test_config, ports)

    def setup_method(self):
        reset_il_ports(self.ports)
        self.il._state_changed = False

    @patch('time.sleep', return_value=None)
    def test_drill(self, mock_sleep):
        self.ports['senseDrilling'].io_set_value(False)
        status = self.il.get_status()
        assert status["package_at_drill"]

        with self.il.lock:
            self.il.handle_control({"action": "drill"})
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "drilling"
            assert status["reason"] == "cmd"
            worker = self.il._thread

        # Simulate the condition after the command
        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.il.lock:
            assert not self.ports['actDrillingMachine'].value
            assert not self.ports['senseDrilling'].value
            assert self.il._thread is None
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "ready"
            assert status["reason"] == "drilling complete"
            assert status["package_at_drill"]

    @patch('time.sleep', return_value=None)
    def test_mill(self, mock_sleep):
        self.ports['senseMilling'].io_set_value(False)
        status = self.il.get_status()
        assert status["package_at_mill"]

        with self.il.lock:
            self.il.handle_control({"action": "mill"})
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "milling"
            assert status["reason"] == "cmd"
            worker = self.il._thread

        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.il.lock:
            assert self.il._thread is None
            assert not self.ports['actMillingMachine'].value
            assert not self.ports['senseMilling'].value
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "ready"
            assert status["reason"] == "milling complete"
            assert status["package_at_mill"]

    @patch('time.sleep', return_value=None)
    def test_transfer(self, mock_sleep):
        status = self.il.get_status()
        assert not status["package_at_mill"]

        with self.il.lock:
            self.il.handle_control({"action": "transfer", "transfer_from_to": "feed_to_mill"})
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "transferring"
            assert status["reason"] == "cmd"
            worker = self.il._thread

        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.il.lock:
            assert self.il._thread is None
            assert not self.ports['actConveyorBeltFeed'].value
            assert not self.ports['actConveyorBeltMilling'].value
            assert not self.ports['senseMilling'].value
            assert self.il.state_changed()
            status = self.il.get_status()
            assert status["state"] == "ready"
            assert status["reason"] == "transferred to mill"
            assert status["package_at_mill"]
