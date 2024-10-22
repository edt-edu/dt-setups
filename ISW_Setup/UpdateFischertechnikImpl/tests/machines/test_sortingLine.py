import sys
from unittest.mock import patch

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.sortingLine import SortingLine

sl_test_conf = {
    'white_eject_time': 0.3,
    'red_eject_time': 1.3,
    'blue_eject_time': 2.2,
}


def get_test_sl_ports():
    return {
        "senseInputLightBarrier": MockInput('inputLight', True),
        "senseMiddleLightBarrier": MockInput('middleLight', True),
        "senseWhiteLightBarrier": MockInput('whiteLight', True),
        "senseRedLightBarrier": MockInput('redLight', True),
        "senseBlueLightBarrier": MockInput('blueLight', True),
        "impulseCounter": MockCounter('impulseCounter', 0),
        "actMotorConveyor": MockOutput('actConveyor', False),
        "actCompressorOn": MockOutput('actCompressor', False),
        "actWhiteEjector": MockOutput('actWhite', False),
        "actRedEjector": MockOutput('actRed', False),
        "actBlueEjector": MockOutput('actBlue', False),
    }


def reset_sl_ports(ports):
    ports['senseInputLightBarrier'].io_set_value(True)
    ports['senseMiddleLightBarrier'].io_set_value(True)
    ports['senseWhiteLightBarrier'].io_set_value(True)
    ports['senseRedLightBarrier'].io_set_value(True)
    ports['senseBlueLightBarrier'].io_set_value(True)
    ports['impulseCounter'].io_set_value(0)
    ports['actMotorConveyor'].value = False
    ports['actCompressorOn'].value = False
    ports['actWhiteEjector'].value = False
    ports['actRedEjector'].value = False
    ports['actBlueEjector'].value = False


def test_init():
    ports = get_test_sl_ports()
    sl = SortingLine(sl_test_conf, ports)

    assert sl.state_changed()
    status = sl.get_status()
    assert status["state"] == "ready"


class TestSortingLine:
    ports = get_test_sl_ports()
    sl = SortingLine(sl_test_conf, ports)

    def setup_method(self):
        reset_sl_ports(self.ports)
        self.sl._state_changed = False

    @patch('time.sleep', return_value=None)
    def test_move_to_ejector(self, mock_sleep):

        with self.sl.lock:
            self.sl.handle_control({'action': 'move to ejectors'})
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "moving to ejectors"
            worker = self.sl._thread

        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.sl.lock:
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "package waiting at ejectors"
            assert not self.ports['actMotorConveyor'].value
            assert not self.ports['senseMiddleLightBarrier'].value

    @patch('time.sleep', return_value=None)
    def test_sort_white(self, mock_sleep):

        self.ports['senseMiddleLightBarrier'].io_set_value(False)
        status = self.sl.get_status()
        assert not status["isWhiteStored"]

        with self.sl.lock:
            self.sl.handle_control({'action': 'sort', 'color': 'white'})
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "sorting"
            assert status["color"] == "white"
            worker = self.sl._thread

        self.ports['senseMiddleLightBarrier'].io_set_value(True)
        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.sl.lock:
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "ready"
            assert not self.ports['actMotorConveyor'].value
            assert not self.ports['actCompressorOn'].value
            assert not self.ports['actWhiteEjector'].value
            assert not self.ports['senseWhiteLightBarrier'].value
            assert status["isWhiteStored"]

    @patch('time.sleep', return_value=None)
    def test_sort_red(self, mock_sleep):
        
        self.ports['senseMiddleLightBarrier'].io_set_value(False)
        status = self.sl.get_status()
        assert not status["isRedStored"]

        with self.sl.lock:
            self.sl.handle_control({'action': 'sort', 'color': 'red'})
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "sorting"
            assert status["color"] == "red"
            worker = self.sl._thread

        self.ports['senseMiddleLightBarrier'].io_set_value(True)
        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.sl.lock:
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "ready"
            assert not self.ports['actMotorConveyor'].value
            assert not self.ports['actCompressorOn'].value
            assert not self.ports['actRedEjector'].value
            assert not self.ports['senseRedLightBarrier'].value
            assert status["isRedStored"]

    @patch('time.sleep', return_value=None)
    def test_sort_blue(self, mock_sleep):
        
        self.ports['senseMiddleLightBarrier'].io_set_value(False)
        status = self.sl.get_status()
        assert not status["isBlueStored"]

        with self.sl.lock:
            self.sl.handle_control({'action': 'sort', 'color': 'blue'})
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "sorting"
            assert status["color"] == "blue"
            worker = self.sl._thread

        self.ports['senseMiddleLightBarrier'].io_set_value(True)
        worker.join(timeout=5)
        assert not worker.is_alive()

        with self.sl.lock:
            assert self.sl.state_changed()
            status = self.sl.get_status()
            assert status["state"] == "ready"
            assert not self.ports['actMotorConveyor'].value
            assert not self.ports['actCompressorOn'].value
            assert not self.ports['actBlueEjector'].value
            assert not self.ports['senseBlueLightBarrier'].value
            assert status["isBlueStored"]