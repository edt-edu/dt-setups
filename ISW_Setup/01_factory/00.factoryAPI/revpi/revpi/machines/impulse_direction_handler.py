import logging
from enum import Enum

class impulse_direction_handler:
    
    def __init__(self,
                 direction: str,
                 impulses: int):
        self._direction = direction
        self._impulses = impulses

    def set_direction(self, direction: str):
        self._direction = direction

    def set_impulses(self, impulses: int):
        self._impulses = impulses

    def calculate(self, current_position: int, target_position: int):
        self._impulses = target_position - current_position

        if self._impulses > 0:
            self._direction = "FORWARD"
        else:
            self._impulses = -self._impulses
            self._direction = "BACKWARD"

    def get_direction(self):
        return self._direction
    
    def get_impulses(self):
        return self._impulses