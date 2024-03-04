import unittest

from Warehouse import Warehouse
from WarehouseConfig import WarehouseConfig


class MyTestCase(unittest.TestCase):

    def setUp(self):
        #counterVertical,counterHorizontal,armEndIn,armEndOut,conveyorIn,conveyorOut,horizontalEnd,verticalEnd
        warehouseLoadConf1 = WarehouseConfig(1500,30,True,False,False,False,False,False) #mit eingefahrenem Arm in Ladepos
        warehouseLoadConf2 = WarehouseConfig(1500,30,False,True,False,False,False,False) #Arm ausfahren
        warehouseLoadConf3 = WarehouseConfig(1500,30,False,True,True,False,False,False) #Förderband aktivieren
        warehouseLoadConf4 = WarehouseConfig(1000,30,False,True,False,False,False,False) #anheben
        self.warehouse1 = Warehouse(1)

    def testSetup(self):
        """Tests, whether setup activity is performed in correct order"""
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.setupFinishedHelper, False)
        self.assertEqual(self.warehouse1.pc, 0)
        #Arm einfahren
        self.assertEqual(self.warehouse1.warehouseActArmIn, True)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.warehouse1.warehouseSensArmIn = True
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.setupFinishedHelper, False)
        self.assertEqual(self.warehouse1.pc, 0)
        #andere Achsen fahren
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, True)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, True)
        self.warehouse1.warehouseSensVerticalEnd = True
        self.warehouse1.warehouseSensHorizontalEnd = True
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.setupFinishedHelper, True)
        self.assertEqual(self.warehouse1.pc, 0)
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.setupFinishedHelper, True)
        self.assertEqual(self.warehouse1.pc, 1)

    def testGoToLoadConfigAfterSetup(self):
        self.setUpFaker()
        #execute, nach der Config ist der Arm bereits eingefahren
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 1)
        self.assertEqual(self.warehouse1.generateTransferMoveList(0,1)[0], WarehouseConfig(0,0,armEndIn=True))
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        #execute - in ladeposition fahren
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 2)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, True)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, True)
        self.warehouse1.warehouseSensEncoderVertical = 100
        #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 2)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, True)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, True)
        self.warehouse1.warehouseSensEncoderVertical = 1430 #1380+50 offset
        self.warehouse1.warehouseSensEncoderHorizontal = 30
        #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 2)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        #execute - Arm ausfahren
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 3)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, True)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 3)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, True)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        self.warehouse1.warehouseSensArmOut = True
        #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 3)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        #config2 reached
        #execute - Förderband aktivieren
        self.warehouse1.warehouseSensLightBarrierIn = True #Lichtschranke invers zu Taster
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 4)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, True)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 4)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, True)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
        self.warehouse1.warehouseSensLightBarrierIn = False
        self.warehouse1.warehouseSensLightBarrierOut = True
        #execute
        #TODO why does this take one extra cycle? unexpected behavior
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 4)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, True)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)
         #execute
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.pc, 5)
        self.assertEqual(self.warehouse1.warehouseActArmIn, False)
        self.assertEqual(self.warehouse1.warehouseActArmOut, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorIn, False)
        self.assertEqual(self.warehouse1.warehouseActConveyorOut, False)
        self.assertEqual(self.warehouse1.warehouseActVerticalUp, True)
        self.assertEqual(self.warehouse1.warehouseActVerticalDown, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToConveyor, False)
        self.assertEqual(self.warehouse1.warehouseActHorizontalToRack, False)

    def setUpFaker(self):
        """Sets all the variables to fake a finished setup process"""
        # SETUP (not tested, just set to finished)
        self.warehouse1.warehouseSensArmIn = True
        self.warehouse1.warehouseSensHorizontalEnd= True
        self.warehouse1.warehouseSensVerticalEnd = True
        self.warehouse1.execute(0,1)
        self.assertEqual(self.warehouse1.setupFinishedHelper, True)
        self.assertEqual(self.warehouse1.pc, 0)


if __name__ == '__main__':
    unittest.main()
