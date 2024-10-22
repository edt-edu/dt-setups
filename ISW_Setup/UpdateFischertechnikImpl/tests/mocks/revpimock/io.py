""" RevPi IO mock
"""
from typing import Dict

from .ports import IOPort


IOBase = IOPort


class IO:
    def __init__(self, ports: Dict[str, IOPort]):
        """Setup mock ports

        If `port_desc` is not None, it defines the accessible io ports.
        Else wise ports are dynamically created as they are accessed
        """
        self._ports = ports

    def __getitem__(self, item):
        return self._ports[item]

    def __getattr__(self, item):
        return self._ports[item]

    @property
    def ports(self):
        return self._ports.values()
