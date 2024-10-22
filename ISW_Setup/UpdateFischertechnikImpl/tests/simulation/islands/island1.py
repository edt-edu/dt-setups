import logging
from typing import List

from ..actuator_1d_impulse import ImpulseActuator
from ..actuator_1d import Actuator1D
from ..restricted_actuator_1d import RestrictedActuator1D
from ..actuator_1d_unlimited import UnlimitedActuator
from ..actuator_forward_unlimited import UnlimitedForwardActuator
from ..simulator_item import SimulatorItem
from ..simulator_part import SimulatorPart


class Island1(SimulatorPart):
    def __init__(self, ports, logger: logging.Logger):
        super().__init__(logger)
        self._actuators: List[SimulatorItem] = []
        self._handlers: List[SimulatorItem] = []

        self.vc16 = VacuumGripper16Simulator(ports, logger)
        self.vc16.step()
        self.add_item(self.vc16)

        self.cb11 = Conveyor11Simulation(ports, logger)
        self.cb11.step()
        self.add_item(self.cb11)

        self.cb13 = Conveyor13Simulation(ports, logger)
        self.cb13.step()
        self.add_item(self.cb13)

        self.w15 = Warehouse15Simulation(ports, logger)
        self.w15.step()
        self.add_item(self.w15)

        self.cg12 = ClawGripper12Simulation(ports, logger)
        self.cg12.step()
        self.add_item(self.cg12)

        self.cg18 = ClawGripper18Simulation(ports, logger)
        self.cg18.step()
        self.add_item(self.cg18)

        self.sl14 = SortingLine14Simulator(ports, logger)
        self.sl14.step()
        self.add_item(self.sl14)

        self.il17 = IndexedLine17Simulator(ports, logger)
        self.il17.step()
        self.add_item(self.il17)

        self.add_function(self._clear_light_barriers)

        self.vacuum = ports['dio4_O_8']
        self.old_vacuum = False

    def _clear_light_barriers(self):
        # Clear sorting line detector whenever the vc is gripping
        if self.vacuum.value:
            if not self.old_vacuum:
                self.sl14.clear_white()
                self.sl14.clear_red()
                self.sl14.clear_blue()
                self.il17.clear_end()
        else:
            if self.old_vacuum:
                self.il17.set_input_light_barrier(True)
        self.old_vacuum = self.vacuum.value


class VacuumGripper16Simulator(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the vacuum gripper 16 simulation"""
        self.logger = logger.getChild('vacuum')

        vertical = Actuator1D(ports['dio4_O_2'],
                              ports['dio4_O_1'],
                              ports['dio4_I_1'],
                              ports['dio4_Counter_5'],
                              initial_position=72,
                              speed=25,
                              start_speed=10,
                              logger=self.logger.getChild('vertical'))
        vertical.step()
        self.vertical = vertical
        arm = Actuator1D(ports['dio4_O_4'],
                         ports['dio4_O_3'],
                         ports['dio4_I_2'],
                         ports['dio4_Counter_7'],
                         initial_position=300,
                         speed=25,
                         start_speed=10,
                         logger=self.logger.getChild('arm'))
        arm.step()
        self.arm = arm
        rotor = Actuator1D(ports['dio4_O_6'],
                           ports['dio4_O_5'],
                           ports['dio4_I_3'],
                           ports['dio4_Counter_9'],
                           initial_position=0,
                           speed=25,
                           logger=self.logger.getChild('rotor'))
        rotor.step()
        self.rotor = rotor

        self.valve = ports['dio4_O_8']
        self.compressor = ports['dio4_O_7']

    @property
    def is_holding_item(self):
        return self.valve.value and self.compressor.value

    def step(self):
        self.rotor.step()
        self.arm.step()
        self.vertical.step()


class Conveyor11Simulation(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the conveyorbelt 11 simulation"""
        self.logger = logger.getChild('conv1')

        self.senseLeft = ports['dio1_I_1']
        self.senseRight = ports['dio1_I_2']
        senseImpulse = ports['dio1_Counter_3']

        self.actRight = ports['dio1_O_1']
        self.actLeft = ports['dio1_O_2']

        self.senseLeft.io_set_value(True)
        self.senseRight.io_set_value(True)

        conveyor = UnlimitedActuator(self.actRight,
                                     self.actLeft,
                                     senseImpulse,
                                     logger=self.logger.getChild('conveyor'))
        conveyor.step()
        self.conveyor = conveyor

    def _fake_conveyor(self):
        """Fake the conveyor light barriers

        When moving to the right, the right light barrier detects the container and
        when moving to the left, the left light barrier detects the container.

        """
        if self.actRight.value:
            self.senseRight.io_set_value(False)
            self.senseLeft.io_set_value(True)
        elif self.actLeft.value:
            self.senseRight.io_set_value(True)
            self.senseLeft.io_set_value(False)

    def step(self):
        self.conveyor.step()
        self._fake_conveyor()


class Conveyor13Simulation(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the conveyorbelt 11 simulation"""
        self.logger = logger.getChild('conv2')

        self.senseLeft = ports['dio2_I_1']
        self.senseRight = ports['dio2_I_2']
        senseImpulse = ports['dio2_Counter_3']

        self.actRight = ports['dio2_O_1']
        self.actLeft = ports['dio2_O_2']

        self.senseLeft.io_set_value(True)
        self.senseRight.io_set_value(True)

        conveyor = UnlimitedActuator(self.actRight,
                                     self.actLeft,
                                     senseImpulse,
                                     logger=self.logger.getChild('conveyor'))
        conveyor.step()
        self.conveyor = conveyor

    def _fake_conveyor(self):
        """ Fake the conveyor light barriers

        When moving to the right, the right light barrier detects the container and
        when moving to the left, the left light barrier detects the container.

        """
        if self.actRight.value:
            self.senseRight.io_set_value(False)
            self.senseLeft.io_set_value(True)
        elif self.actLeft.value:
            self.senseRight.io_set_value(True)
            self.senseLeft.io_set_value(False)

    def step(self):
        self.conveyor.step()
        self._fake_conveyor()


class Warehouse15Simulation(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the warehouse 15 simulation"""
        self.logger = logger.getChild('warehouse')

        vertical = Actuator1D(ports['dio3_O_5'],
                              ports['dio3_O_6'],
                              ports['dio3_I_4'],
                              ports['dio3_Counter_7'],
                              initial_position=72,
                              speed=25,
                              start_speed=10,
                              logger=self.logger.getChild('vertical'))
        vertical.step()
        self.vertical = vertical
        horizontal = Actuator1D(ports['dio3_O_3'],
                                ports['dio3_O_4'],
                                ports['dio3_I_1'],
                                ports['dio3_Counter_5'],
                                initial_position=72,
                                speed=25,
                                start_speed=10,
                                logger=self.logger.getChild('horizontal'))
        horizontal.step()
        self.horizontal = horizontal
        arm = RestrictedActuator1D(ports['dio3_O_7'],
                                   ports['dio3_O_8'],
                                   ports['dio3_I_9'],
                                   ports['dio3_I_10'],
                                   initial_position=30,
                                   movement_span=100,
                                   speed=10,
                                   logger=self.logger.getChild('arm'))
        arm.step()
        self.arm = arm

        # Fake conveyor light barriers
        self.lb_in = ports['dio3_I_2']
        self.lb_in.io_set_value(True)
        self.lb_out = ports['dio3_I_3']
        self.lb_out.io_set_value(True)
        self.conv_in = ports['dio3_O_2']
        self.conv_out = ports['dio3_O_1']
        self.logger = logger.getChild("warehouse.conveyor")

    @property
    def container_at_pickup(self):
        return not self.lb_out.value

    def _fake_conveyor(self):
        """Fake the warehouse conveyor light barriers

        When moving out, the outwards light barrier detects the container and
        when moving in, the inwards light barrier detects the container.

        The inward light barrier is not reset after the container is stored.
        """
        if self.conv_in.value:
            self.logger.info("Moving conveyor in")
            self.lb_in.io_set_value(False)
            self.lb_out.io_set_value(True)
        elif self.conv_out.value:
            self.logger.info("Moving conveyor out")
            self.lb_in.io_set_value(True)
            self.lb_out.io_set_value(False)

    def step(self):
        self.vertical.step()
        self.horizontal.step()
        self.arm.step()
        self._fake_conveyor()


class ClawGripper12Simulation(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the claw gripper 12 simulation"""
        self.logger = logger.getChild('claw1')

        claw1_vertical = Actuator1D(ports['dio1_O_7'],
                                    ports['dio1_O_8'],
                                    ports['dio1_I_9'],
                                    ports['dio1_Counter_11'],
                                    initial_position=72,
                                    speed=25,
                                    start_speed=10,
                                    logger=self.logger.getChild("vertical"))
        claw1_vertical.step()
        self.vertical = claw1_vertical

        claw1_rotor = Actuator1D(ports['dio1_O_10'],
                                 ports['dio1_O_9'],
                                 ports['dio1_I_10'],
                                 ports['dio1_Counter_13'],
                                 initial_position=0,
                                 speed=25,
                                 logger=self.logger.getChild("rotor"))
        claw1_rotor.step()
        self.rotor = claw1_rotor

        claw1_arm = ImpulseActuator(ports['dio1_O_5'],
                                    ports['dio1_O_6'],
                                    ports['dio1_I_7'],
                                    ports['dio1_Counter_8'],
                                    initial_position=0,
                                    speed=10,
                                    logger=self.logger.getChild("arm"))
        claw1_arm.step()
        self.arm = claw1_arm

        claw1_claw = ImpulseActuator(ports['dio1_O_4'],
                                     ports['dio1_O_3'],
                                     ports['dio1_I_5'],
                                     ports['dio1_Counter_6'],
                                     initial_position=0,
                                     speed=1,
                                     logger=self.logger.getChild("claw"))
        claw1_claw.step()
        self.claw = claw1_claw

    @property
    def is_gripping(self):
        return self.claw.position >= 12

    def step(self):
        self.rotor.step()
        self.arm.step()
        self.vertical.step()
        self.claw.step()


class ClawGripper18Simulation(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger):
        """Set up the claw gripper 18 simulation"""
        self.logger = logger.getChild('claw2')

        claw2_vertical = Actuator1D(ports['dio5_O_7'],
                                    ports['dio5_O_8'],
                                    ports['dio5_I_9'],
                                    ports['dio5_Counter_11'],
                                    initial_position=72,
                                    speed=25,
                                    start_speed=10,
                                    logger=self.logger.getChild("vertical"))
        claw2_vertical.step()
        self.vertical = claw2_vertical

        claw2_rotor = Actuator1D(ports['dio5_O_10'],
                                 ports['dio5_O_9'],
                                 ports['dio5_I_10'],
                                 ports['dio5_Counter_13'],
                                 initial_position=0,
                                 speed=25,
                                 logger=self.logger.getChild("rotor"))
        claw2_rotor.step()
        self.rotor = claw2_rotor

        claw2_arm = ImpulseActuator(ports['dio5_O_5'],
                                    ports['dio5_O_6'],
                                    ports['dio5_I_7'],
                                    ports['dio5_Counter_8'],
                                    initial_position=0,
                                    speed=10,
                                    logger=self.logger.getChild("arm"))
        claw2_arm.step()
        self.arm = claw2_arm

        claw2_claw = ImpulseActuator(ports['dio5_O_4'],
                                     ports['dio5_O_3'],
                                     ports['dio5_I_5'],
                                     ports['dio5_Counter_6'],
                                     initial_position=0,
                                     speed=1,
                                     logger=self.logger.getChild("claw"))
        claw2_claw.step()
        self.claw = claw2_claw

    @property
    def is_gripping(self):
        return self.claw.position >= 12

    def step(self):
        self.rotor.step()
        self.arm.step()
        self.vertical.step()
        self.claw.step()


class SortingLine14Simulator(SimulatorItem):
    def __init__(self, ports, logger: logging.Logger):
        self.logger = logger.getChild("sortingLine4")

        self.impulse_counter = ports['dio2_Counter_4']
        self.sense_input = ports['dio2_I_5']
        self.sense_middle = ports['dio2_I_6']
        self.sense_white = ports['dio2_I_7']
        self.sense_red = ports['dio2_I_8']
        self.sense_blue = ports['dio2_I_9']

        self.act_conveyor = ports['dio2_O_3']
        self.compressor = ports['dio2_O_4']
        self.white_ejector = ports['dio2_O_5']
        self.red_ejector = ports['dio2_O_6']
        self.blue_ejector = ports['dio2_O_7']

        self.sense_input.io_set_value(False)
        self.sense_middle.io_set_value(True)
        self.sense_white.io_set_value(True)
        self.sense_red.io_set_value(True)
        self.sense_blue.io_set_value(True)

        self.act_conveyor.io_set_value(False)
        self.compressor.io_set_value(False)
        self.white_ejector.io_set_value(False)
        self.red_ejector.io_set_value(False)
        self.blue_ejector.io_set_value(False)

        self.conveyor = UnlimitedForwardActuator(self.act_conveyor, self.impulse_counter,
                                                 logger=self.logger.getChild("conveyor"))
        self.conveyor.step()

        self.item_by_ejectors = False
        "Set true iff an item was at the middle light barrier and the conveyor is moving"
        self.is_moving = False

    def set_input_light_barrier(self, item_present: bool):
        """Set item at the input

        `item_present` is True iff an item is detected (opposite to light barriers)
        """
        self.sense_input.io_set_value(not item_present)

    def clear_white(self):
        self.sense_white.io_set_value(True)

    def clear_red(self):
        self.sense_red.io_set_value(True)

    def clear_blue(self):
        self.sense_blue.io_set_value(True)

    def _step_ejector(self):
        """No complicated actions

        Control code uses timing, which isn't simulated.
        So just assume that everything works.
        """
        if self.compressor.value and self.item_by_ejectors:
            if self.white_ejector.value:
                self.sense_white.io_set_value(False)
                self.item_by_ejectors = False
            elif self.red_ejector.value:
                self.sense_red.io_set_value(False)
                self.item_by_ejectors = False
            elif self.blue_ejector.value:
                self.sense_blue.io_set_value(False)
                self.item_by_ejectors = False
        if not self.conveyor.is_moving_forward:
            self.item_by_ejectors = False
            self.is_moving = False
        else:
            if not self.is_moving:
                self.item_by_ejectors = not self.sense_middle.value
                self.is_moving = True
            self.sense_middle.io_set_value(self.sense_input.value)  # Propagate item from start to middle

    def step(self):
        self.conveyor.step()
        self._step_ejector()


class IndexedLine17Simulator(SimulatorItem):

    def __init__(self, ports, logger: logging.Logger, slowdown=3):
        self.logger = logger.getChild("indexedLine7")
        assert slowdown >= 0
        self._slowdown = slowdown
        self._slowdown_counter = 0

        self.feed_conveyor = ports['dio4_O_13']
        self.mill_conveyor = ports['dio4_O_14']
        self.drill_conveyor = ports['dio5_O_12']
        self.end_conveyor = ports['dio5_O_14']

        self.mill = ports['dio5_O_11']
        self.drill = ports['dio5_O_13']

        self.slider1 = RestrictedActuator1D(ports['dio4_O_9'],
                                            ports['dio4_O_10'],
                                            ports['dio4_I_4'],
                                            ports['dio4_I_11'],
                                            movement_span=10,
                                            speed=1,
                                            logger=self.logger.getChild('slider1'))
        self.slider1.step()
        self.slider2 = RestrictedActuator1D(ports['dio4_O_11'],
                                            ports['dio4_O_12'],
                                            ports['dio4_I_12'],
                                            ports['dio4_I_13'],
                                            movement_span=10,
                                            speed=1,
                                            logger=self.logger.getChild('slider2'))
        self.slider2.step()

        self.sense_feed = ports['dio5_I_2']
        self.sense_feed.io_set_value(False)
        self.sense_slider1 = ports['dio4_I_14']
        self.sense_slider1.io_set_value(True)
        self.sense_mill = ports['dio5_I_1']
        self.sense_mill.io_set_value(True)
        self.sense_drill = ports['dio5_I_3']
        self.sense_drill.io_set_value(True)
        self.sense_end = ports['dio5_I_4']
        self.sense_end.io_set_value(True)

        # Intermediate item positions
        self._item_at_slider1 = False
        self._item_at_mill_to_drill = False
        self._item_at_slider2 = False
        self._item_at_end = False

    def set_input_light_barrier(self, item_present: bool):
        """Set item at the input

        `item_present` is True iff an item is detected (opposite to light barriers)
        """
        self.sense_feed.io_set_value(not item_present)

    def clear_end(self):
        self._item_at_end = False

    @property
    def item_at_end(self):
        return self._item_at_end

    def _simulate_item_move(self):
        """Simulate the movement of an item in the indexed line.

        Only accurate if the usual production sequence is followed.
        """
        if self.end_conveyor.value:
            self.logger.info("Moving items on end conveyor")
            self._item_at_end = (self._item_at_end
                                 or not self.sense_end.value)
            if self.slider2.at_forward_limit:
                self.sense_end.io_set_value(not self._item_at_slider2)
                self._item_at_slider2 = False
            else:
                self.logger.info("Slider2 not at forward limit")
        if self.drill_conveyor.value:
            self.logger.info("Moving items on drill conveyor")
            if self.slider2.at_forward_limit:
                self.logger.info("Slider2 not at backward limit")
            self._item_at_slider2 = (self._item_at_slider2
                                     or (self.slider2.at_backward_limit
                                         and not self.sense_drill.value))
            self.sense_drill.io_set_value(not self._item_at_mill_to_drill)
            self._item_at_mill_to_drill = False
        if self.mill_conveyor.value:
            self.logger.info("Moving items on mill conveyor")
            self._item_at_mill_to_drill = (self._item_at_mill_to_drill
                                           or not self.sense_mill.value)
            if self.slider1.at_forward_limit:
                self.sense_mill.io_set_value(not self._item_at_slider1)
                self._item_at_slider1 = False
            else:
                self.logger.info("Slider1 not at forward limit")
        if self.feed_conveyor.value:
            self.logger.info("Moving items on feed conveyor")
            if self.slider1.at_forward_limit:
                self.logger.info("Slider1 not at backward limit")
            self._item_at_slider1 = (self._item_at_slider1
                                     or (self.slider1.at_backward_limit
                                         and not self.sense_slider1.value))
            self.sense_slider1.io_set_value(self.sense_feed.value)
            self.sense_feed.io_set_value(True)

    def step(self):
        self.slider1.step()
        self.slider2.step()
        if self._slowdown_counter >= self._slowdown:
            self._simulate_item_move()
            self._slowdown_counter = 0
        else:
            self._slowdown_counter += 1
