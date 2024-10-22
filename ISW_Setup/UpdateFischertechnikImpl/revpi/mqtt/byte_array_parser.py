import struct
import sys
from enum import Enum
from typing import Any, Dict, Union
import logging


NoneType = type(None)

_logger = logging.getLogger(__name__)


class MessageType(Enum):
    BOOL = 0
    STRING = 1
    INT = 2
    FLOAT = 3
    NONE = 4


type_dic = {
    bool: MessageType.BOOL,
    str: MessageType.STRING,
    int: MessageType.INT,
    float: MessageType.FLOAT,
    NoneType: MessageType.NONE
}


class DataStream(bytearray):
    """ Helper Class for working with the byteArray
    """
    position = 0

    def put(self, bytesliteral: bytes):
        self.extend(bytesliteral)

    def putBool(self, boolean: bool):
        self.extend(boolean.to_bytes(1, 'big'))

    def putInt(self, integer: int, length: int = 4):
        self.extend(integer.to_bytes(length, "big", signed=True))

    def putString(self, string: str):
        encodedString = string.encode('utf-8')
        self.putInt(len(encodedString))
        self.extend(encodedString)

    def putFloat(self, floatnumber: float):
        if sys.byteorder == "little":
            self.extend(struct.pack("f", floatnumber)[::-1])
        else:
            self.extend(struct.pack("f", floatnumber))

    def get(self, length: int) -> bytes:
        upper = self.position + length
        if upper > len(self):
            raise ValueError("Invalid ByteArray message. Message is to short, can't decode", self)
        array = self[self.position:self.position + length]
        self.position += length
        return array

    def getInt(self) -> int:
        return int.from_bytes(self.get(4), "big", signed=True)

    def getString(self) -> str:
        str_length = self.getInt()
        return self.get(str_length).decode('utf-8')

    def getBool(self) -> bool:
        return bool(self.get(1)[0])

    def getFloat(self) -> float:
        if sys.byteorder == "little":
            return struct.unpack('f', self.get(4)[::-1])[0]
        else:
            return struct.unpack('f', self.get(4))[0]


def encode(data: Dict[str, Any]) -> bytearray:
    """ Encodes a dictionary as a byteArray that can be transferred via MQTT
    """
    buffer = DataStream()
    buffer.putInt(len(data))
    for key, value in data.items():
        buffer.putString(key)  # Key encoding
        try:
            valueType = type_dic[type(value)]
        except KeyError:
            raise TypeError(f"Invalid value type for ${key}: ${type(value)}", data)
        buffer.putInt(valueType.value, 1)
        if valueType == MessageType.BOOL:
            buffer.putBool(bool(value))
        elif valueType == MessageType.STRING:
            buffer.putString(value)
        elif valueType == MessageType.INT:
            buffer.putInt(value)
        elif valueType == MessageType.FLOAT:
            buffer.putFloat(value)
        elif valueType == MessageType.NONE:
            pass  # Null/None is singleton
    return buffer


def decode(array: bytearray) -> Dict[str, Any]:
    """ Decodes byteArray received through MQTT to a dictionary
    """
    stream = DataStream(array)
    elementCount = stream.getInt()
    keyValuePairs: Dict[str, Any] = dict()
    for i in range(elementCount):
        key: str = stream.getString()  # Decode key
        try:
            valueType: MessageType = MessageType(stream.get(1)[0])
        except KeyError:
            raise ValueError("Invalid ByteArray Message, unknown type. Can't decode", array)
        value: Union[bool, str, int, float, None]
        if valueType == MessageType.BOOL:
            value = stream.getBool()
        elif valueType == MessageType.STRING:
            value = stream.getString()
        elif valueType == MessageType.INT:
            value = stream.getInt()
        elif valueType == MessageType.FLOAT:
            value = stream.getFloat()
        elif valueType == MessageType.NONE:
            value = None
        else:
            _logger.warning("Ignoring unknown type")
        keyValuePairs[key] = value
    return keyValuePairs
