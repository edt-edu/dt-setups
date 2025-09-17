import threading
import logging
from typing import Any, Dict

from .base_machine import BaseMachine
from .vacuum_gripper import VacuumGripper
from .claw_gripper import ClawGripper
from .sortingLine import SortingLine
from .indexedLine import IndexedLine
from .warehouse import Warehouse
from .conveyorbelt import Conveyorbelt
from .punching_machine import PunchingMachine


__all__ = [
    "BaseMachine",
    "VacuumGripper",
    "ClawGripper",
    "Warehouse",
    "UnknownMachineType",
    "get_machine_from_description"
]


_logger = logging.getLogger(__name__)


class UnknownMachineType(Exception):
    pass


def get_machine_from_description(name: str,
                                 description: Dict[str, Any],
                                 logger: logging.Logger,
                                 lock: threading.Lock,
                                 io) -> BaseMachine:
    type = description['type']
    machine_logger = logger.getChild(name)
    try:
        config = description['configuration']
    except KeyError:
        _logger.info("No configuration in description for %s (type: %s)", name, type)
        config = {}
    ports = get_ports_from_names(io, description['ports'])
    if type == 'vacuum_gripper':
        return VacuumGripper(config, ports, machine_logger, lock)
    elif type == 'claw_gripper':
        return ClawGripper(config, ports, machine_logger, lock)
    elif type == 'warehouse':
        return Warehouse(config, ports, machine_logger, lock)
    elif type == 'conveyor':
        return Conveyorbelt(config, ports, machine_logger, lock)
    elif type == 'sorting_line':
        return SortingLine(config, ports, machine_logger, lock)
    elif type == 'indexed_line':
        return IndexedLine(config, ports, machine_logger, lock)
    elif type == 'punching_machine':
        return PunchingMachine(config, ports, machine_logger, lock)
    else:
        raise UnknownMachineType(f"Unknown type '{type}' for machine '{name}', can not create machine")


def get_ports_from_names(io, names: Dict[str, str]) -> Dict[str, Any]:
    """Convert symbolic port names into RevPi IO ports"""
    ports = dict()
    for name, port_name in names.items():
        ports[name] = io[port_name]
    return ports
