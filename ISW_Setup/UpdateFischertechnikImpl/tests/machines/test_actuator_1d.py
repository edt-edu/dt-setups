import sys

from tests.mocks import revpimock
from tests.mocks.revpimock.ports import MockCounter, MockInput, MockOutput

sys.modules['revpimodio2'] = revpimock
from revpi.machines.actuator_1d import Actuator1D, Reason, State


def test_init():
    actFwd = MockOutput('actFwd', False)
    actBwd = MockOutput('actBwd', False)
    senseLimit = MockInput('senseLimit', True)
    counter = MockCounter('counter', 12345)

    actuator = Actuator1D(actFwd, actBwd, senseLimit, counter, 1000)

    assert counter.value == 0
    assert actFwd.value is False
    assert actBwd.value is False
    assert not actuator.is_at_forward_limit
    assert actuator.is_at_reset
    assert actuator.state_changed()
    status = actuator.get_status()
    assert status["state"] is State.STOPPED
    assert status["reason"] is Reason.CMD
    assert status["position"] == 0
    assert status["limit-switch"] is True
    assert not actuator.state_changed()


class TestActuator:
    actFwd = MockOutput('actFwd', False)
    actBwd = MockOutput('actBwd', False)
    senseLimit = MockInput('senseLimit', True)
    counter = MockCounter('counter', 12345)
    POSITION_LOWER_LIMIT = 0
    POSITION_UPPER_LIMIT = 1_000
    actuator: Actuator1D

    def setup_method(self):
        self.actuator = Actuator1D(self.actFwd,
                                   self.actBwd,
                                   self.senseLimit,
                                   self.counter,
                                   self.POSITION_UPPER_LIMIT,
                                   stopOffset=0)
        # self.actuator._target = None
        # self.actFwd.value = False
        # self.actBwd.value = False
        # self.set_position(0)
        # self.actuator._stopOffset = 0

    def set_position(self, position):
        if position <= self.POSITION_LOWER_LIMIT:
            self.senseLimit.io_set_value(True)
        else:
            self.senseLimit.io_set_value(False)
        self.counter.io_set_value(position)

    def asert_state(self, state: State, reason: Reason, position: int, limit_switch: bool, target=None):
        status = self.actuator.get_status()
        assert status["state"] is state
        assert status["reason"] is reason
        assert status["position"] == position
        assert status["limit-switch"] == limit_switch
        if target is not None:
            assert status["target"] == target
        else:
            assert "target" not in status

    def test_move_forward_and_stop(self):
        self.actuator.move_forward()

        assert self.actFwd.value is True
        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_FORWARD, Reason.CMD, 0, True)

        self.set_position(500)
        self.actuator.stop_moving()

        assert self.actFwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.CMD, 500, False)

    def test_move_backward_and_stop(self):
        self.set_position(1001)
        self.actuator.move_backward()

        assert self.actBwd.value is True
        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_BACKWARD, Reason.CMD, 1001, False)

        self.set_position(500)
        self.actuator.stop_moving()

        assert self.actBwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.CMD, 500, False)

    def test_move_forward_than_backward(self):
        self.actuator.move_forward()

        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_FORWARD, Reason.CMD, 0, True)

        self.set_position(500)
        self.actuator.move_backward()

        assert self.actFwd.value is False
        assert self.actBwd.value is True
        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_BACKWARD, Reason.CMD, 500, False)

    def test_move_backward_than_forward(self):
        self.set_position(500)
        self.actuator.move_backward()

        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_BACKWARD, Reason.CMD, 500, False)

        self.set_position(400)
        self.actuator.move_forward()

        assert self.actFwd.value is True
        assert self.actBwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_FORWARD, Reason.CMD, 400, False)

    def test_move_forward_position_limit(self):
        self.actuator.move_forward()

        self.set_position(1001)

        assert self.actFwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.POSITION_LIMIT, 1001, False)

    def test_move_forward_at_backward_position_limit(self):
        self.set_position(-100)

        self.actuator.move_forward()

        self.set_position(-50)

        assert self.actuator.state_changed()
        self.asert_state(State.MOVING_FORWARD, Reason.CMD, -50, True)

    def test_move_backward_position_limit(self):
        self.set_position(500)
        self.actuator.move_backward()

        # Don't set the limit switch
        self.counter.io_set_value(-100)

        assert self.actBwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.POSITION_LIMIT, -100, False)

    def test_move_backward_limit_switch(self):
        self.set_position(500)
        self.actuator.move_backward()

        # Don't set the limit switch
        self.senseLimit.io_set_value(True)

        assert self.actBwd.value is False
        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.POSITION_LIMIT, 500, True)

    def test_can_not_start_moving_backward_at_limit_switch(self):
        self.actuator.move_backward()

        self.asert_state(State.STOPPED, Reason.POSITION_LIMIT, 0, True)

    def test_can_not_start_moving_forward_at_position_limit(self):
        self.set_position(1000)
        self.actuator.move_forward()

        self.asert_state(State.STOPPED, Reason.POSITION_LIMIT, position=1000, limit_switch=False)

    def test_target_position_forward(self):
        self.set_position(500)
        self.actuator.move_to_position(800)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 500, False, target=800)
        assert not self.actuator.position_within_target(800)

        self.set_position(795)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 795, False, target=800)

        self.set_position(796)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 796, False, target=800)
        assert self.actuator.position_within_target(800)

    def test_target_position_forward_stop_after_overshoot(self):
        self.set_position(500)
        self.actuator.move_to_position(800)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 500, False, target=800)
        assert not self.actuator.position_within_target(800)

        self.set_position(900)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 900, False, target=800)
        assert not self.actuator.position_within_target(800)

    def test_target_position_forward_from_underflow(self):
        self.set_position(-100)
        self.actuator.move_to_position(800)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, -100, True, target=800)
        assert not self.actuator.position_within_target(800)

    def test_target_position_backward(self):
        self.set_position(500)
        self.actuator.move_to_position(200)

        assert self.actFwd.value is False
        assert self.actBwd.value is True
        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 500, False, target=200)
        assert not self.actuator.position_within_target(200)

        self.set_position(205)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 205, False, target=200)

        self.set_position(204)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 204, False, target=200)
        assert self.actuator.position_within_target(200)

    def test_target_position_backward_stop_after_overshoot(self):
        self.set_position(500)
        self.actuator.move_to_position(200)

        assert self.actuator.state_changed()
        self.asert_state(State.TARGETING, Reason.CMD, 500, False, target=200)

        self.set_position(100)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 100, False, target=200)
        assert not self.actuator.position_within_target(200)

    def test_abort_targeting(self):
        self.actuator.move_to_position(500)

        self.actuator.stop_moving()

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.CMD, 0, True)

    def test_targeting_stop_at_offset_backward(self):
        self.set_position(500)
        self.actuator._stopOffset = 20
        self.actuator.move_to_position(400)

        self.set_position(420)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 420, False, target=400)
        assert not self.actuator.position_within_target(400)

    def test_targeting_stop_at_offset_forward(self):
        self.set_position(500)
        self.actuator._stopOffset = 20
        self.actuator.move_to_position(600)

        self.set_position(580)

        assert self.actuator.state_changed()
        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 580, False, target=600)
        assert not self.actuator.position_within_target(600)

    def test_start_targeting_in_range_does_not_activate_motors(self):
        self.set_position(500)
        self.actuator.move_to_position(500)

        self.asert_state(State.STOPPED, Reason.TARGET_REACHED, 500, False, target=500)
        assert self.actFwd.value is False
        assert self.actBwd.value is False
