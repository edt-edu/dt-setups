import unittest
from unittest.mock import MagicMock, Mock, patch
from queue import Queue
from Position import Position
from Direction import Direction
from BoxNumber import BoxNumber
from Colour import Colour
from RequestedParameter import RequestedParameter
from JSONReader import JSONReader
from JSONProcessingIntegrationMain import JSONProcessingIntegrationMain


class MyTestCase(unittest.TestCase):


#WITH MOCKING TO MAKE SURE CORRECT FUNCTION IS CALLED
#######################################################################################################################
    # ROBOT #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.Robot.setup")
    def testRobotSetup(self, mock1):
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1673975631.125000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 1,
            "name" : "SETUP",
            "parameters" : [ ]
          }
        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        #in mock1.call_args.args[0] steht die Instanz auf der die Methode aufgerufen wird
        #in mock1.call_args.args[>=1] stehen die Parameter, mit der die Methode aufgerufen wird (in Reihenfolge der Überladung)
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.robot1)

    @patch("JSONProcessingIntegrationMain.Robot.pick")
    def testRobotPick(self, mock1):
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 1,
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
        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.robot1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='START', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.Robot.place")
    def testRobotPlace(self, mock1):
        jsonMessage = JSONReader.read("""{
              "topicName" : "Gripper1Sorting",
              "timestamp" : 1677144787.891000000,
              "message" : {
                "jsonType" : "COMMAND",
                "type" : "GRIPPER",
                "outputId" : 1,
                "name" : "PLACE",
                "parameters" : [ {
                  "passableType" : "POSITIONPARAMETERTHREED",
                  "passable" : {
                    "meaning" : "END",
                    "vertical" : 1,
                    "rot" : 2,
                    "horizontal" : 4
                  }
                } ]
              }
            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.robot1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='END', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.Robot.move")
    def testRobotMove(self, mock1):
        jsonMessage = JSONReader.read("""{
                  "topicName" : "Gripper1Sorting",
                  "timestamp" : 1677144787.891000000,
                  "message" : {
                    "jsonType" : "COMMAND",
                    "type" : "GRIPPER",
                    "outputId" : 1,
                    "name" : "MOVE",
                    "parameters" : [ {
                      "passableType" : "POSITIONPARAMETERTHREED",
                      "passable" : {
                        "meaning" : "END",
                        "vertical" : 1,
                        "rot" : 2,
                        "horizontal" : 4
                      }
                    }, {
                      "passableType" : "POSITIONPARAMETERTHREED",
                      "passable" : {
                        "meaning" : "START",
                        "vertical" : 1,
                        "rot" : 2,
                        "horizontal" : 4
                      }
                    } ]
                  }
                }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 3)
        self.assertEqual(mock1.call_args.args[0], j.robot1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='START', vertical=1, rot=2, horizontal=4))
        self.assertEqual(mock1.call_args.args[2], Position(meaning='END', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.Robot.stop")
    def testRobotStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                      "topicName" : "Gripper1Sorting",
                      "timestamp" : 1677144787.891000000,
                      "message" : {
                        "jsonType" : "COMMAND",
                        "type" : "GRIPPER",
                        "outputId" : 1,
                        "name" : "STOP",
                        "parameters" : []
                      }
                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.robot1)

    @patch("JSONProcessingIntegrationMain.Robot.stop")
    def testRobotStopWithOtherRobotObject(self, mock1):
        jsonMessage = JSONReader.read("""{
                          "topicName" : "Gripper7Sorting",
                          "timestamp" : 1677144787.891000000,
                          "jsonType" : "COMMAND",
                          "message" : {
                            "jsonType" : "COMMAND",
                            "type" : "GRIPPER",
                            "outputId" : 1,
                            "name" : "STOP",
                            "parameters" : []
                          }
                        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.robot2)

#######################################################################################################################
    # VACUUM #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.VacuumGripper.setup")
    def testVacuumSetup(self, mock1):
        jsonMessage = JSONReader.read("""{
              "topicName" : "Vacuum4Sorting",
              "timestamp" : 1673975631.125000000,
              "message" : {
                "jsonType" : "COMMAND",
                "type" : "VACUUM",
                "outputId" : 1,
                "name" : "SETUP",
                "parameters" : [ ]
              }
            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.vacuum1)

    @patch("JSONProcessingIntegrationMain.VacuumGripper.pick")
    def testVacuumPick(self, mock1):
        jsonMessage = JSONReader.read("""{
                  "topicName" : "Vacuum4Sorting",
                  "timestamp" : 1677144787.891000000,
                  "message" : {
                    "jsonType" : "COMMAND",
                    "type" : "VACUUM",
                    "outputId" : 1,
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
                }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.vacuum1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='START', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.VacuumGripper.place")
    def testVacuumPlace(self, mock1):
        jsonMessage = JSONReader.read("""{
                          "topicName" : "Vacuum4Sorting",
                          "timestamp" : 1677144787.891000000,
                          "message" : {
                            "jsonType" : "COMMAND",
                            "type" : "VACUUM",
                            "outputId" : 1,
                            "name" : "PLACE",
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
                        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.vacuum1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='START', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.VacuumGripper.move")
    def testVacuumMove(self, mock1):
        jsonMessage = JSONReader.read("""{
                          "topicName" : "Vacuum4Sorting",
                          "timestamp" : 1677144787.891000000,
                          "message" : {
                            "jsonType" : "COMMAND",
                            "type" : "VACUUM",
                            "outputId" : 1,
                            "name" : "MOVE",
                            "parameters" : [ {
                              "passableType" : "POSITIONPARAMETERTHREED",
                              "passable" : {
                                "meaning" : "END",
                                "vertical" : 1,
                                "rot" : 2,
                                "horizontal" : 4
                              }
                            }, {
                              "passableType" : "POSITIONPARAMETERTHREED",
                              "passable" : {
                                "meaning" : "START",
                                "vertical" : 1,
                                "rot" : 2,
                                "horizontal" : 5
                              }
                            } ]
                          }
                        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 3)
        self.assertEqual(mock1.call_args.args[0], j.vacuum1)
        self.assertEqual(mock1.call_args.args[1], Position(meaning='START', vertical=1, rot=2, horizontal=5))
        self.assertEqual(mock1.call_args.args[2], Position(meaning='END', vertical=1, rot=2, horizontal=4))

    @patch("JSONProcessingIntegrationMain.VacuumGripper.stop")
    def testVacuumStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                      "topicName" : "Vacuum4Sorting",
                      "timestamp" : 1677144787.891000000,
                      "message" : {
                        "jsonType" : "COMMAND",
                        "type" : "VACUUM",
                        "outputId" : 1,
                        "name" : "STOP",
                        "parameters" : []
                      }
                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.vacuum1)

#######################################################################################################################
    # WAREHOUSE #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.Warehouse.setup")
    def testWarehouseSetup(self, mock1):
        jsonMessage = JSONReader.read("""{
                    "topicName" : "Warehouse5Sorting",
                    "timestamp" : 1677663434.246000000,
                    "message" : {
                        "jsonType" : "COMMAND",
                        "type" : "WAREHOUSE",
                        "outputId" : 2,
                        "name" : "SETUP",
                        "parameters" : []
                    }
                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.warehouse1)

    @patch("JSONProcessingIntegrationMain.Warehouse.store")
    def testWarehouseStore(self, mock1):
        jsonMessage = JSONReader.read("""{
            "topicName" : "Warehouse5Sorting",
            "timestamp" : 1677663434.246000000,
            "message" : {
                "jsonType" : "COMMAND",
                "type" : "WAREHOUSE",
                "outputId" : 2,
                "name" : "STORE",
                "parameters" : [ {
                    "passableType" : "BOXNUMBER",
                    "passable" : "BOX1"
                } ]
            }
            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.warehouse1)
        self.assertEqual(mock1.call_args.args[1], BoxNumber.BOX1)

    @patch("JSONProcessingIntegrationMain.Warehouse.get")
    def testWarehouseGet(self, mock1):
        jsonMessage = JSONReader.read("""{
                    "topicName" : "Warehouse5Sorting",
                    "timestamp" : 1677663434.246000000,
                    "message" : {
                        "jsonType" : "COMMAND",
                        "type" : "WAREHOUSE",
                        "outputId" : 2,
                        "name" : "GET",
                        "parameters" : [ {
                            "passableType" : "BOXNUMBER",
                            "passable" : "BOX2"
                        } ]
                    }
                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.warehouse1)
        self.assertEqual(mock1.call_args.args[1], BoxNumber.BOX2)

    @patch("JSONProcessingIntegrationMain.Warehouse.inout")
    def testWarehouseInOut(self, mock1):
        jsonMessage = JSONReader.read("""{
                            "topicName" : "Warehouse5Sorting",
                            "timestamp" : 1677663434.246000000,
                            "message" : {
                                "jsonType" : "COMMAND",
                                "type" : "WAREHOUSE",
                                "outputId" : 2,
                                "name" : "INOUT",
                                "parameters" : [ {
                                    "passableType" : "BOXNUMBER",
                                    "passable" : "BOX2"
                                }, {
                                    "passableType" : "BOXNUMBER",
                                    "passable" : "BOX1"
                                } ]
                            }
                            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 3)
        self.assertEqual(mock1.call_args.args[0], j.warehouse1)
        self.assertEqual(mock1.call_args.args[1], BoxNumber.BOX2)
        self.assertEqual(mock1.call_args.args[2], BoxNumber.BOX1)

    @patch("JSONProcessingIntegrationMain.Warehouse.stop")
    def testWarehouseStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                              "topicName" : "Warehouse5Sorting",
                              "timestamp" : 1677144787.891000000,
                              "message" : {
                                "jsonType" : "COMMAND",
                                "type" : "WAREHOUSE",
                                "outputId" : 1,
                                "name" : "STOP",
                                "parameters" : []
                              }
                            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.warehouse1)

#######################################################################################################################
    # SORTING LINE #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.SortingLine.stop")
    def testSortingLineStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                              "topicName" : "Sorting3Sorting",
                              "timestamp" : 1677144787.891000000,
                              "message" : {
                                "jsonType" : "COMMAND",
                                "type" : "SORTING",
                                "outputId" : 1,
                                "name" : "STOP",
                                "parameters" : []
                              }
                            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.sortingLine1)

    @patch("JSONProcessingIntegrationMain.SortingLine.eject")
    def testSortingLineEject(self, mock1):
        jsonMessage = JSONReader.read("""{
                              "topicName" : "Sorting3Sorting",
                              "timestamp" : 1677144787.891000000,
                              "message" : {
                                "jsonType" : "COMMAND",
                                "type" : "SORTING",
                                "outputId" : 1,
                                "name" : "EJECT",
                                "parameters" : [ {
                                    "passableType" : "COLOUR",
                                    "passable" : "RED"
                                } ]
                              }
                            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.sortingLine1)
        self.assertEqual(mock1.call_args.args[1], Colour.RED)

#######################################################################################################################
    # INDEXED LINE #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.IndexedLine.stop")
    def testIndexedLineStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "IndexedLine6Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "INDEXEDLINE",
                                        "outputId" : 1,
                                        "name" : "STOP",
                                        "parameters" : [ ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.indexedLine)

    @patch("JSONProcessingIntegrationMain.IndexedLine.stirr")
    def testIndexedLineStirr(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "IndexedLine6Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "INDEXEDLINE",
                                        "outputId" : 1,
                                        "name" : "STIRR",
                                        "parameters" : [{
                                          "passableType" : "NUMBERNATURAL",
                                          "passable" : {
                                            "number" : 3
                                          }
                                        }]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.indexedLine)
        self.assertEqual(mock1.call_args.args[1], 3)

#######################################################################################################################
    # MULTIPROCESSING #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.MultiProcessing.stop")
    def testMultiprocessingStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Multiprocessing9Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "MULTIPROCESSING",
                                        "outputId" : 1,
                                        "name" : "STOP",
                                        "parameters" : [ ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.multiprocessing)

    @patch("JSONProcessingIntegrationMain.MultiProcessing.freeze")
    def testMultiprocessingFreeze(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Multiprocessing9Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "MULTIPROCESSING",
                                        "outputId" : 1,
                                        "name" : "FREEZE",
                                        "parameters" : [{
                                          "passableType" : "NUMBERNATURAL",
                                          "passable" : {
                                            "number" : 3
                                          }
                                        }]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.multiprocessing)
        self.assertEqual(mock1.call_args.args[1], 3)

#######################################################################################################################
    # CONVEYOR #
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.Conveyor.stop")
    def testConveyorStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Conveyor2Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "CONVEYOR",
                                        "outputId" : 1,
                                        "name" : "STOP",
                                        "parameters" : [ ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.conveyor1)


    @patch("JSONProcessingIntegrationMain.Conveyor.movelb")
    def testConveyorMoveLB(self, mock1):
        jsonMessage = JSONReader.read("""{
                                              "topicName" : "Conveyor2Sorting",
                                              "timestamp" : 1677144787.891000000,
                                              "message" : {
                                                "jsonType" : "COMMAND",
                                                "type" : "CONVEYOR",
                                                "outputId" : 1,
                                                "name" : "MOVELB",
                                                "parameters" : [{
                                          "passableType" : "DIRECTION",
                                          "passable" : "BACKWARD"
                                        }  ]
                                              }
                                            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.conveyor1)
        self.assertEqual(mock1.call_args.args[1], Direction.BACKWARD)

    @patch("JSONProcessingIntegrationMain.Conveyor.move")
    def testConveyorMove(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Conveyor2Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "CONVEYOR",
                                        "outputId" : 1,
                                        "name" : "MOVE",
                                        "parameters" : [{
                                          "passableType" : "DIRECTION",
                                          "passable" : "BACKWARD"
                                        } ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 2)
        self.assertEqual(mock1.call_args.args[0], j.conveyor1)
        self.assertEqual(mock1.call_args.args[1], Direction.BACKWARD)

    @patch("JSONProcessingIntegrationMain.Conveyor.gotoconfig")
    def testConveyorGoToConfig(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Conveyor2Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "CONVEYOR",
                                        "outputId" : 1,
                                        "name" : "GOTOCONFIG",
                                        "parameters" : [{
                                          "passableType" : "NUMBERNATURAL",
                                          "passable" : {
                                            "number" : 3
                                          }
                                        }, {
                                          "passableType" : "DIRECTION",
                                          "passable" : "BACKWARD"
                                        } ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 3)
        self.assertEqual(mock1.call_args.args[0], j.conveyor1)
        self.assertEqual(mock1.call_args.args[1], Direction.BACKWARD)
        self.assertEqual(mock1.call_args.args[2], 3)

    @patch("JSONProcessingIntegrationMain.Conveyor.gotoconfig")
    def testConveyorGoToConfigOtherParamOrder(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Conveyor2Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "CONVEYOR",
                                        "outputId" : 1,
                                        "name" : "GOTOCONFIG",
                                        "parameters" : [{
                                          "passableType" : "DIRECTION",
                                          "passable" : "BACKWARD"
                                        }, {
                                          "passableType" : "NUMBERNATURAL",
                                          "passable" : {
                                            "number" : 3
                                          }
                                        } ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 3)
        self.assertEqual(mock1.call_args.args[0], j.conveyor1)
        self.assertEqual(mock1.call_args.args[1], Direction.BACKWARD)
        self.assertEqual(mock1.call_args.args[2], 3)

#######################################################################################################################
    # PUNCHING MACHINE#
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.PunchingMachine.stop")
    def testPunchingMachineStop(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Punching0Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "PUNCHING",
                                        "outputId" : 1,
                                        "name" : "STOP",
                                        "parameters" : [ ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.punching)

    @patch("JSONProcessingIntegrationMain.PunchingMachine.press")
    def testPunchingMachinePress(self, mock1):
        jsonMessage = JSONReader.read("""{
                                      "topicName" : "Punching0Sorting",
                                      "timestamp" : 1677144787.891000000,
                                      "message" : {
                                        "jsonType" : "COMMAND",
                                        "type" : "PUNCHING",
                                        "outputId" : 1,
                                        "name" : "PRESS",
                                        "parameters" : [ ]
                                      }
                                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(mock1.call_args.args[0], j.punching)

#######################################################################################################################
#######################################################################################################################
#######################################################################################################################
 # REQUESTS #
#######################################################################################################################
#######################################################################################################################
#######################################################################################################################

    @patch("JSONProcessingIntegrationMain.Robot.request")
    def testGetData(self, mock1):
        jsonMessage = JSONReader.read("""{
                "topicName" : "Gripper1Sorting",
                "timestamp" : 1678444092.417000000,
                "message" : {
                  "jsonType" : "STATUSREQUEST",
                  "type" : "GRIPPER",
                  "requestId" : 1,
                  "params" : [ "REFERENCESWITCHROTATE", "ROTATESTEP" ]
                }
            }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        assert mock1.called
        self.assertEqual(len(mock1.call_args.args), 1)
        self.assertEqual(len(mock1.call_args.args[0]), 2)
        self.assertEqual(mock1.call_args.args[0][0], RequestedParameter.REFERENCESWITCHROTATE)
        self.assertEqual(mock1.call_args.args[0][1], RequestedParameter.ROTATESTEP)






#WITHOUT MOCKING TO TEST FUNCTION MECHANISMS
#######################################################################################################################
    # ROBOT #
#######################################################################################################################

    def testRobotPickExecution(self):
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 1,
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
        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        self.assertNotEqual(j.currentlyExecuting[j.robot1], None)
        print(j.currentlyExecuting[j.robot1])
        j.exLoop()
        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())
        j.exLoop()
        j.exLoop()
        j.robot1.robotActGripperOpen = j.robot1.robotActGripperClose = j.robot1.robotActArmOut = j.robot1.robotActArmIn = j.robot1.robotActVerticalDown = j.robot1.robotActVerticalUp = j.robot1.robotActRotRight = j.robot1.robotActRotLeft = False
        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())

    #TODO executing the same command(exactly identical parameters) will fail
    #TODO why does this execution not work a second time?
    def testExecuteTwiceMovingMachine(self):
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 1,
            "name" : "MOVE",
            "parameters" : [ {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "START",
                "vertical" : 100,
                "rot" : 200,
                "horizontal" : 400
              }
            }, {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "END",
                "vertical" : 1000,
                "rot" : 2000,
                "horizontal" : 4000
              }
            } ]
          }
        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        # position als ob setup fertig
        j.robot1.robotSensRotEnd = j.robot1.robotSensArmEndIn = j.robot1.robotSensVerticalEndUp = j.robot1.robotSensGripperOpen = True
        self.assertEqual(j.robot1.pc, 0)
        #Greifer öffnen/schließen bis counter = 6
        j.exLoop()
        j.processJson(jsonMessageList)
        self.assertNotEqual(j.currentlyExecuting[j.robot1][0], None)
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, True)
        j.robot1.robotSensGripperOpen = False
        self.assertEqual(j.robot1.pc, 1)
        #Arm einfahren
        j.exLoop()
        j.robot1.robotSensGripperImpulseCounterRaw = 6
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, False)
        j.robot1.robotSensArmEndIn = True
        j.exLoop()
        self.assertEqual(j.robot1.pc, 2)
        #Rot und vertikal bewegen (hier nur rot weil vertikal klein)
        self.assertEqual(j.robot1.robotActArmIn, False)
        j.robot1.robotSensRotEncoderCounter = 200
        j.exLoop()
        self.assertEqual(j.robot1.robotActRotLeft, False)
        self.assertEqual(j.robot1.pc, 3)
        #Arm ausfahren
        j.exLoop()
        j.robot1.robotSensArmEndIn = False
        j.exLoop()
        j.robot1.robotSensArmImpulseCounterRaw = 400
        j.exLoop()
        self.assertEqual(j.robot1.robotActArmOut, False)
        self.assertEqual(j.robot1.pc, 4)
        #absenken
        j.robot1.robotSensVerticalEncoderCounter = 100
        j.exLoop()
        self.assertEqual(j.robot1.robotActVerticalDown, False)
        self.assertEqual(j.robot1.pc, 5)
        #Greifer schließen
        j.exLoop()
        j.robot1.robotSensGripperImpulseCounterRaw = 14
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, False)
        self.assertEqual(j.robot1.robotActGripperOpen, False)
        self.assertEqual(j.robot1.pc, 6)
        #anheben

        j.robot1.robotSensVerticalEndUp = False
        j.robot1.robotSensVerticalEncoderCounter = 0
        j.exLoop()
        self.assertEqual(j.robot1.robotActVerticalUp, False)
        self.assertEqual(j.robot1.pc, 7)
        #Arm einfahren

        j.exLoop()
        self.assertEqual(j.robot1.robotActArmIn, True)
        j.robot1.robotSensArmImpulseCounterRaw = 800
        j.robot1.robotSensArmEndIn = True
        j.exLoop()
        self.assertEqual(j.robot1.robotActArmIn, False)
        self.assertEqual(j.robot1.pc, 8)
        #rot und vert bewegen
        j.exLoop()
        j.robot1.robotSensVerticalEncoderCounter = 300
        j.robot1.robotSensRotEncoderCounter = 2000
        j.exLoop()
        self.assertEqual(j.robot1.robotActRotRight, False)
        self.assertEqual(j.robot1.robotActRotLeft, False)
        self.assertEqual(j.robot1.robotActVerticalUp, False)
        self.assertEqual(j.robot1.robotActVerticalDown, False)
        self.assertEqual(j.robot1.pc, 9)
        #arm ausfahren
        j.exLoop()
        j.exLoop()
        j.robot1.robotSensArmImpulseCounterRaw = 4800
        j.robot1.robotSensArmEndIn = False
        j.exLoop()
        self.assertEqual(j.robot1.robotActArmOut, False)
        self.assertEqual(j.robot1.robotActArmIn, False)
        self.assertEqual(j.robot1.pc, 10)
        #absenken
        j.exLoop()
        j.robot1.robotSensVerticalEncoderCounter = 1000
        j.exLoop()
        self.assertEqual(j.robot1.robotActVerticalDown, False)
        self.assertEqual(j.robot1.robotActVerticalUp, False)
        self.assertEqual(j.robot1.pc, 11)
        #Greifer öffnen
        j.exLoop()
        j.robot1.robotSensGripperImpulseCounterRaw = 28
        j.robot1.robotSensGripperOpen = True
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperOpen, False)
        self.assertEqual(j.robot1.pc, 12)
        #anheben
        j.exLoop()
        j.robot1.robotSensGripperOpen = False
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, True)
        self.assertEqual(j.robot1.robotActVerticalUp, True)
        j.robot1.robotSensGripperImpulseCounterRaw = 34
        j.robot1.robotSensArmEndIn = True
        j.robot1.robotSensVerticalEncoderCounter = 300
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, False)
        self.assertEqual(j.robot1.robotActVerticalUp, False)
        self.assertEqual(j.robot1.pc, 13)
        self.assertEqual(j.robot1.isExecuting, False)
        ##warten bis fake feedback auch false
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.currentlyExecuting[j.robot1][0], None)

        ###zweite ausführungrunde, andere outputID
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 2,
            "name" : "MOVE",
            "parameters" : [ {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "START",
                "vertical" : 100,
                "rot" : 200,
                "horizontal" : 400
              }
            }, {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "END",
                "vertical" : 1000,
                "rot" : 2000,
                "horizontal" : 4000
              }
            } ]
          }
        }""")
        jsonMessageList.put(jsonMessage)
        j.processJson(jsonMessageList)
        self.assertNotEqual(j.currentlyExecuting[j.robot1][0], None)
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.robot1.pc, 1) #pc nicht zurückgesetzt

    def testRobotPickExecution2(self):
        jsonMessage = JSONReader.read("""{
          "topicName" : "Gripper1Sorting",
          "timestamp" : 1677144787.891000000,
          "message" : {
            "jsonType" : "COMMAND",
            "type" : "GRIPPER",
            "outputId" : 1,
            "name" : "PICK",
            "parameters" : [ {
              "passableType" : "POSITIONPARAMETERTHREED",
              "passable" : {
                "meaning" : "START",
                "vertical" : 1000,
                "rot" : 2000,
                "horizontal" : 4000
              }
            } ]
          }
        }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)

        j = JSONProcessingIntegrationMain()
        # position als ob setup fertig
        j.robot1.robotSensRotEnd = j.robot1.robotSensArmEndIn = j.robot1.robotSensVerticalEndUp = j.robot1.robotSensGripperOpen = True
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.processJson(jsonMessageList)
        self.assertNotEqual(j.currentlyExecuting[j.robot1][0], None)
        print(j.currentlyExecuting[j.robot1])
        print(j.robot1.robotActVerticalDown)
        self.assertEqual(j.robot1.robotActVerticalDown, False)
        j.exLoop()

        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, True)
        self.assertEqual(j.robot1.isExecuting, True)
        self.assertEqual(j.robot1.fakeIsExecuting, True)
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.robot1.isExecuting, True)
        self.assertEqual(j.robot1.fakeIsExecuting, True)
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, True)
        self.assertEqual(j.robot1.isExecuting, True)
        self.assertNotEqual(j.currentlyExecuting[j.robot1][0], None)
        j.robot1.robotSensGripperImpulseCounterRaw = 6
        j.robot1.robotSensGripperOpen = False
        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        j.exLoop()
        self.assertNotEqual(j.currentlyExecuting[j.robot1][0], None)
        self.assertEqual(j.robot1.isExecuting, True)
        self.assertEqual(j.robot1.robotActGripperClose, False)
        print(j.robot1.robotActVerticalDown)
        j.exLoop()

        j.exLoop()
        self.assertEqual(j.robot1.robotActGripperClose, False)

        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())
        j.exLoop()
        j.exLoop()
        j.robot1.robotActGripperOpen = j.robot1.robotActGripperClose = j.robot1.robotActArmOut = j.robot1.robotActArmIn = j.robot1.robotActVerticalDown = j.robot1.robotActVerticalUp = j.robot1.robotActRotRight = j.robot1.robotActRotLeft = False
        j.createFeedbackOnChange()
        while not j.outputBuffer.empty():
            print(j.outputBuffer.get())


    def testGetDataGetting(self):
        jsonMessage = JSONReader.read("""{
                        "topicName" : "Gripper1Sorting",
                        "timestamp" : 1678444092.417000000,
                        "message" : {
                          "jsonType" : "STATUSREQUEST",
                          "type" : "GRIPPER",
                          "requestId" : 1,
                          "params" : [ "REFERENCESWITCHROTATE", "ROTATESTEP" ]
                        }
                    }""")
        jsonMessageList = Queue()
        jsonMessageList.put(jsonMessage)
        j = JSONProcessingIntegrationMain()
        j.processJson(jsonMessageList)
        print(j.outputBuffer.get())