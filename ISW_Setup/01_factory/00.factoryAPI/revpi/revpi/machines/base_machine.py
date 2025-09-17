import logging
from abc import ABC, abstractmethod
import threading
from typing import Any, Optional


class OperationInProgress(Exception):
    """ Indicate that an operation is in progress and the requested operation is not possible
    """
    pass


class BaseMachine(ABC):

    def __init__(self, logger: logging.Logger, lock: Optional[threading.Lock] = None):
        """
        `logger`: This machines logger
        `lock`: The lock to guard all state changes.
            If set, `.lock` is the same lock.
        """
        self._logger = logger
        if lock is None:
            self._lock = threading.Lock()
        else:
            self._lock = lock

    @property
    def lock(self):
        """Lock guarding all state changes

        If not specified otherwise, each method must be called with this lock acquired.
        """
        return self._lock

    @abstractmethod
    def handle_control(self, value: Any):
        """Handle a machine control request

        This method should not block!
        `value`: Whatever the machine expects as a control value. Usually a dict.

        The calling method must hold the lock.
        """
        pass

    @abstractmethod
    def cleanup(self):
        """ Clean up this machine

        All actions should be stopped and event handlers unregistered.

        This method might be used to reset to a known good state.

        The calling method must not hold the lock.
        This method might acquire and release the lock multiple times.
        """
        pass

    @abstractmethod
    def destroy(self):
        """ Stop all movements and unregister event handlers

        This method should not block!
        This is the fast version of `.cleanup()` without any resetting.

        Raise `OperationInProgress` to indicate a situation in which the machine cannot be destroyed.
        Such a situation should be temporary only and signaled to the client, e.g. ensure that
        `state_changed()` returns `True` on the next call.

        The calling method must hold the lock.
        """
        pass

    @abstractmethod
    def state_changed(self):
        """Test, if any state changed compared to the last call to `get_status()`

        This method should not block!
        The calling method should hold the lock.
        """
        pass

    @abstractmethod
    def get_status(self):
        """Get the current status of this machine

        This will reset any internal state change indicators.
        The status should usually be a (flat) dict with string keys.

        This method should not block!
        The calling method must hold the lock.
        """
        pass
