import logging
import threading
from typing import Callable, List, Optional

from .simulator_item import SimulatorItem


class Simulator:
    def __init__(self, cycle_time: float = 0.02,
                 logger: Optional[logging.Logger] = None):
        """Simulator

        `cycle_time`: Time in seconds between steps
        """
        self.cycle_time = cycle_time
        self.items: List[SimulatorItem] = []
        self.on_step: List[Callable] = []
        "Functions to run every step"
        self.stop_event = threading.Event()
        self.thread = None
        if logger is not None:
            self._logger = logger
        else:
            self._logger = logging.getLogger(__name__)

    def add_item(self, item: SimulatorItem):
        self.items.append(item)

    def add_step_function(self, function: Callable):
        self.on_step.append(function)

    def start(self):
        if self.thread is not None:
            raise Exception('Only one thread allowed')
        self.thread = threading.Thread(target=self.simulator_thread, name="RevPiSimulator")
        self.stop_event.clear()
        self._logger.info("Starting the simulation")
        self.thread.start()

    def stop(self):
        self.stop_event.set()
        self.thread.join()
        self._logger.info("Simulation stopped")

    def simulator_thread(self):
        while not self.stop_event.is_set():
            for item in self.items:
                item.step()
            for sim_step in self.on_step:
                sim_step()
            self.stop_event.wait(self.cycle_time)
