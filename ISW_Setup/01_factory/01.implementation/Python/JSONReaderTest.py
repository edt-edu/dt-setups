import unittest
from JSONReader import JSONReader
from RequestedParameter import RequestedParameter


class MyTestCase(unittest.TestCase):

    def testReading(self):
        t = """{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "commandId" : 1,
            "name" : "PICK",
            "parameters" : [ {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "START",
                "vertical" : 1,
                "rot" : 2,
                "horizontal" : 4
              }
            } ]
          }
        }"""

        readValue = JSONReader.read(t)
        print(readValue)
        self.assertEqual(readValue.topicName, "Gripper1Sorting")
        self.assertEqual(readValue.message.jsonType, "COMMAND")
        self.assertEqual(readValue.message.parameters[0].meaning, "START")

    def testReadingRequest(self):
        #TODO fails

        self.assertTrue(True)
        t = """{
                "topicName" : "Gripper2Sorting",
                "timestamp" : 1678444092.417000000,
                "message" : {
                  "jsonType" : "STATUSREQUEST",
                  "type" : "GRIPPER",
                  "requestId" : 1,
                  "params" : [ "REFERENCESWITCHROTATE", "ROTATESTEP" ]
                }
            }"""

        readValue = JSONReader.read(t)
        print(readValue)
        self.assertEqual(readValue.topicName, "Gripper2Sorting")
        self.assertEqual(readValue.message.jsonType, "STATUSREQUEST")
        self.assertEqual(readValue.message.parameters[0], RequestedParameter.REFERENCESWITCHROTATE)
        self.assertEqual(readValue.message.parameters[1], RequestedParameter.ROTATESTEP)
