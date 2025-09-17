from typing import Any, Dict

from .main import RevPiModIO as RevPiModIO_Mocked

_instance = None
_ports: Dict


def set_fake_ports(ports):
    global _ports
    if _instance is not None:
        raise Exception('There is already an instance configured')
    _ports = ports


class RevPiModIO(RevPiModIO_Mocked):
    def __init__(self, autorefresh: bool = True,
                 configsrc: Any = None):
        global _instance
        global _ports
        if _instance is not None:
            raise Exception('There is already an instance of RevPiModIO created')
        super().__init__(_ports,
                         autorefresh=autorefresh,
                         configsrc=configsrc)

    def exit(self):
        super().exit()
        global _instance
        _instance = None
