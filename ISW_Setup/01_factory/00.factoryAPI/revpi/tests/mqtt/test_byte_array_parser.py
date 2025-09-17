from revpi.mqtt.byte_array_parser import encode, decode
from math import isclose


def test_unpack_empty():
    array = b'\x00\x00\x00\x00'
    data = decode(array)

    assert isinstance(data, dict)
    assert not data


def test_pack_empty():
    data = dict()
    array = encode(data)

    assert array == b'\x00\x00\x00\x00'


def test_pack():
    data = {
        "bool": True,
        "not": False,
        "int": 4,
        "float": 3.1417,
        "string": "no string here",
        "none": None
    }
    array = b'\x00\x00\x00\x06\x00\x00\x00\x04bool\x00\x01\x00\x00\x00\x03not\x00\x00\x00\x00\x00\x03int\x02\x00\x00\x00\x04\x00\x00\x00\x05float\x03@I\x11\x9d\x00\x00\x00\x06string\x01\x00\x00\x00\x0eno string here\x00\x00\x00\x04none\x04'
    result = encode(data)

    assert result == array  # NOTE: Might fail if a different ordering is used


def test_unpack():
    array = b'\x00\x00\x00\x06\x00\x00\x00\x04bool\x00\x01\x00\x00\x00\x03not\x00\x00\x00\x00\x00\x03int\x02\x00\x00\x00\x04\x00\x00\x00\x05float\x03@I\x11\x9d\x00\x00\x00\x06string\x01\x00\x00\x00\x0eno string here\x00\x00\x00\x04none\x04'
    result = decode(array)

    assert result['bool'] is True
    assert result['not'] is False
    assert result['int'] == 4
    assert result['string'] == "no string here"
    assert isclose(result['float'], 3.1417, rel_tol=1e-6)
    assert result['none'] is None
