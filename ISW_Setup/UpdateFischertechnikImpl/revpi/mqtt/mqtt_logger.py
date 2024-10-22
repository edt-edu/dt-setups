import logging
import json
import traceback
from typing import Optional, Tuple


class MQTTHandler(logging.Handler):
    def __init__(self, client, topic, qos=0, retain=False):
        super().__init__()
        self._client = client
        self._topic = topic
        self._qos = qos
        self._retain = retain

    def emit(self, record):
        message = self._get_log_message(record)
        msg = json.dumps(message, indent=2)
        self._client.publish(self._topic, msg, self._qos, self._retain)

    def _get_log_message(self, record):
        message = {
            "logger": record.name,
            "level": record.levelname,
            "path": record.pathname,
            "lineno": record.lineno,
            "time": record.created,
            "message": record.message,
            "thread": record.thread,
        }
        exception = self._get_exception_info(record.exc_info)
        if exception is not None:
            message["exception"] = exception
        return message

    def _get_exception_info(self, exc_info: Optional[Tuple]):
        if exc_info is None:
            return None
        extype, exc, tb = exc_info
        exception = {
            "type": extype.__name__,
            "message": str(exc),
            "traceback": traceback.format_tb(tb),
        }
        return exception
