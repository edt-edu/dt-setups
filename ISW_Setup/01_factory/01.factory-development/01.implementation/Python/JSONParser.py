import json as json
from JSONOutput import JSONOutput
from MachineStatusRequestAnswer import MachineStatusRequestAnswer
from Robot import Robot
from RequestedParameter import RequestedParameter


class JSONParser:

    @staticmethod
    def parse(obj) -> str:
        s = json.dumps(obj.to_dict(), default=str)
        print(s)
        return s


#only for testing
if __name__ == "__main__":
    r = Robot("robb1")
    a = MachineStatusRequestAnswer("STATUSANSWER", 2, r.request([RequestedParameter.REFERNCESWITCHCLAW]))
    #a = MachineStatusRequestAnswer("STATUSANSWER", 2, r.request([RequestedParameter.ALL]))
    j = JSONOutput("name", 12343.234, a)
    print(j)
    JSONParser.parse(j)
