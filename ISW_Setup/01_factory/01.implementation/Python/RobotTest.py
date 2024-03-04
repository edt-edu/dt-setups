import unittest

from Robot import Robot
from ThreeDRobotConfig import ThreeDRobotConfig
from RequestedParameter import RequestedParameter
from PlusMinusStop import PlusMinusStop


class MyTestCase(unittest.TestCase):

    def setUp(self):
        pickupRobot1 = [2600,3550,25]
        placeConveyorRobot1 = [2000,100,100]
        placeRand = [2,3,4,5]
        placeListrobot1 = [pickupRobot1, placeConveyorRobot1, placeRand]
        self.robot1 = Robot("Gripper1Sorting")

    def testSetup(self):
        """Tests, whether setup activity is performed in correct order"""
        self.robot1.robotSensRotEnd = False
        self.robot1.robotSensArmEndIn = False
        self.robot1.robotSensGripperOpen = False
        self.robot1.robotSensVerticalEndUp = False
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        #Arm einfahren
        self.assertEqual(self.robot1.robotActArmIn, True)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        self.assertEqual(self.robot1.robotActArmIn, True)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensArmEndIn = True
        #Greifer öffnen, rotieren, nach oben fahren
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, True)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, True)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, True)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, True)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, True)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, True)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        self.robot1.robotSensRotEnd = True
        self.robot1.robotSensVerticalEndUp = True
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, True)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, True)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.setupFinishedHelper, False)
        self.assertEqual(self.robot1.pc, 0)
        self.robot1.robotSensGripperOpen = True
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.assertEqual(self.robot1.setupFinishedHelper, True)
        self.assertEqual(self.robot1.pc, 0)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.setupFinishedHelper, True)
        self.assertEqual(self.robot1.pc, 1)
        #Übergang zu tatsächlichem Execute geschafft

    def testPickWithExecute(self):
        """Tests, whether the execute function uses the move list correctly with all transitions and all outputs are set accordingly"""
        self.setUpFaker()
        # PICK
        #gripper partially closed
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 1)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[0], ThreeDRobotConfig(0,0,0,6))
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, True)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensGripperOpen = False
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 1)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[0], ThreeDRobotConfig(0,0,0,6))
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, True)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensGripperImpulseCounterRaw = 8
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, True)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensGripperImpulseCounterRaw = 12
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        #next config - Arm einfahren (ist aber bereits eingefahren)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 2)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        #next config - vertical und rot fahren
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 3)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[2], ThreeDRobotConfig(1900,3550,0,6,endArm=True))
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, True)
        self.assertEqual(self.robot1.robotActVerticalDown, True)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, True)
        self.assertEqual(self.robot1.robotActVerticalDown, True)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensRotEncoderCounter = 3550
        self.robot1.robotSensVerticalEncoderCounter = 1900
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        #next config, Arm ausfahren
        self.assertEqual(self.robot1.pc, 4)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[3], ThreeDRobotConfig(1900,3550,25,6))
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, True)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensArmEndIn = False
        self.robot1.robotSensArmImpulseCounterRaw = 2
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, True)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensArmImpulseCounterRaw = 26
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        #next config - vertical runterfahren
        self.assertEqual(self.robot1.pc, 5)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[4], ThreeDRobotConfig(2600,3550,25,6))
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, True)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensVerticalEncoderCounter = 2600
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        #next config - Greifer schließen
        self.assertEqual(self.robot1.pc, 6)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[5], ThreeDRobotConfig(2600,3550,25,14))
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, True)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensGripperImpulseCounterRaw = 13
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, True)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.robotSensGripperImpulseCounterRaw = 26
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)
        self.robot1.execute(0,1)
        #next config - hochfahren
        self.assertEqual(self.robot1.pc, 7)
        self.assertEqual(self.robot1.generateTransferMoveList(0,1)[6], ThreeDRobotConfig(1900,3550,25,14))
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, True)
        self.robot1.robotSensVerticalEncoderCounter = 1900
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.robotActArmIn, False)
        self.assertEqual(self.robot1.robotActArmOut, False)
        self.assertEqual(self.robot1.robotActGripperOpen, False)
        self.assertEqual(self.robot1.robotActGripperClose, False)
        self.assertEqual(self.robot1.robotActRotRight, False)
        self.assertEqual(self.robot1.robotActRotLeft, False)
        self.assertEqual(self.robot1.robotActVerticalDown, False)
        self.assertEqual(self.robot1.robotActVerticalUp, False)

    def testExecuteWithChange(self):
        """Tests, whether a change in the parameters of the execute function triggers a restart"""
        self.setUpFaker()
        # PICK
        #gripper partially closed
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.pc, 1)
        self.robot1.execute(2,1)
        self.assertEqual(self.robot1.pc, 0)

    def setUpFaker(self):
        """Sets all the variables to fake a finished setup process"""
        # SETUP (not tested, just set to finished)
        self.robot1.robotSensRotEnd = True
        self.robot1.robotSensArmEndIn = True
        self.robot1.robotSensGripperOpen = True
        self.robot1.robotSensVerticalEndUp = True
        self.robot1.robotSensGripperImpulseCounterRaw = 6
        self.robot1.robotSensArmImpulseCounterRaw = 1
        self.robot1.execute(0,1)
        self.assertEqual(self.robot1.setupFinishedHelper, True)
        self.assertEqual(self.robot1.pc, 0)

    def testRequest(self):
        print(self.robot1.request([RequestedParameter.REFERNCESWITCHCLAW]))
        self.robot1.robotSensGripperOpen = True
        print(self.robot1.request([RequestedParameter.REFERNCESWITCHCLAW]))
        print(self.robot1.request([RequestedParameter.ALL]))

    def testisExecuting(self):
        self.robot1.robotActArmIn = True
        self.assertTrue(self.robot1.isExecuting)
        self.assertTrue(self.robot1.fakeIsExecuting)
        self.robot1.robotActArmIn = False
        self.assertFalse(self.robot1.isExecuting)
        self.assertTrue(self.robot1.fakeIsExecuting)

if __name__ == '__main__':
    unittest.main()
