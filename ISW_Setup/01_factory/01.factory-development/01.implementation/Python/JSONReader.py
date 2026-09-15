import json as json
from decoderFunctions import customDecoder
from JSONOutput import JSONOutput


class JSONReader:
    """
    Reader that takes the json String and returns the corresponding object
    Currently only method defined: read, which only works with JSONOutput strings and objects
    """

    @staticmethod
    def read(jsonString: str) -> JSONOutput:
        return json.loads(jsonString, object_hook=customDecoder)
