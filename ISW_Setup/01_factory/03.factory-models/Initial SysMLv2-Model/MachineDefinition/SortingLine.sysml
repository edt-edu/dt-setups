private import CmdTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::*;
private import MachineModulesAPIs::*;
private import ModuleHardware::*;
private import ScalarValues::*;
private import Factory::*;
package SortingLine {
    part def SortingLineAPI :> API {
        item sortingLineState :>> stateAPI : SortingLineState;
        attribute color : String;
        attribute isWhiteStored : Boolean;
        attribute isRedStored : Boolean;
        attribute isBlueStored : Boolean;
        attribute isAtStart : Boolean;
        attribute target : Integer;
        action sort;
        action moveToEjectors;
        in port sortingLineAPICmdPort :>> cmdAPIPort {
            in item sortingLineAPICmdItem : SortingLineCmd;
        }
        out port sortingLineAPIStatePort :>> stateAPIPort {
            out item sortingLineAPIStateItem : SortingLineState;
        }
        part conveyorBeltSortingLineAPI : ConveyorBeltSimpleAPI {}
        part whiteEjector : ValveAPI {}
        part blueEjector : ValveAPI {}
        part redEjector : ValveAPI {}
        part compressorAPI : CompressorAPI {}

        part sortingLineEngine {
            in port sortingLineCmdPort {
                in sortingLineCmd : SortingLineCmd;
            }
            out port sortingLineStatePort {
                out sortingLineState : SortingLineState;
            }
            in port whiteLightBarrierPort {
                in whiteLightBarrier : Boolean;
            }
            in port redLightBarrierPort {
                in redLightBarrier : Boolean;
            }
            in port blueLightBarrierPort {
                in blueLightBarrier : Boolean;
            }
            in port colorSensorPort {
                in colorSensor : Boolean;
            }
            in port inputLightBarrierPort {
                in inputeLightBarrier : Boolean;
            }
            in port lightBarrierAfterColorSensorPort {
                in lightBarrierAfterColorSensor : Boolean;
            }
            in port conveyorBeltSimpleStatePort {
                in conveyorBeltSimpleState : ConveyorBeltSimpleState;
            }
            out port conveyorBeltCmdPort {
                out conveyorBeltSimpleCmd : ConveyorBeltSimpleCmd;
            }
            out port redValveCmdPort {
                out redValceCmd : ValveCmd;
            }
            out port blueValveCmdPort {
                out blueValveCmd : ValveCmd;
            }
            out port whiteValveCmdPort {
                out whiteValveCmd : ValveCmd;
            }
            in port redValveStatePort {
                redValveState : ValveState;
            }
            in port blueValveStatePort {
                blutValveState : ValveState;
            }
            in port whiteValveStatePort {
                whiteValveState : ValveState;
            }
            out port compressorCmdPort {
                out compressorCmd : CompressorCmd;
            }
            in port compressorStatePort {
                in compressorState : CompressorState;
            }
        }
    }
    part def SortingLineHardware {
        part inputLightBarrier : LightBarrier {}
        part lightBarrierAfterColorSensor : LightBarrier {}
        part whiteLightBarrier : LightBarrier {}
        part redLightBarrier : LightBarrier {}
        part blueLightBarrier : LightBarrier {}
        part conveyorBelt : ConveyorBeltSimple {}
        part colorSensor : ColorSensor {}
        part redEjector : Valve {}
        part blueEjector : Valve {}
        part whiteEjector : Valve {}
        part compressor : Compressor {}
    }

    part def SortingLineSystem :> System {
        in port sortingLineCmdPort {
            in item sortingLineCmdItem : SortingLineCmd;
        }
        out port sortingLineStatePort {
            out item sortingLineStateItem : SortingLineState;
        }
        part sortingLineAPI : SortingLineAPI;
        part sortingLineHardware : SortingLineHardware;

        connect sortingLineCmdPort to sortingLineAPI.sortingLineAPICmdPort;
        connect sortingLineAPI.sortingLineAPICmdPort to sortingLineAPI.sortingLineEngine.sortingLineCmdPort;
        connect sortingLineAPI.sortingLineEngine.sortingLineStatePort to sortingLineAPI.sortingLineAPIStatePort;
        connect sortingLineAPI.sortingLineAPIStatePort to sortingLineStatePort;

        connect sortingLineAPI.sortingLineEngine.conveyorBeltCmdPort
            to sortingLineAPI.conveyorBeltSortingLineAPI.conveyorBeltSimpleAPICmdPort;
        connect sortingLineAPI.conveyorBeltSortingLineAPI.actConveyorBeltSimpleAPIPort
            to sortingLineHardware.conveyorBelt.actConveyorBeltForwardPort;
        connect sortingLineAPI.conveyorBeltSortingLineAPI.conveyorBeltSimpleAPIStatePort
            to sortingLineAPI.sortingLineEngine.conveyorBeltSimpleStatePort;

        connect sortingLineAPI.sortingLineEngine.compressorCmdPort to sortingLineAPI.compressorAPI.compressorAPICmdPort;
        connect sortingLineAPI.compressorAPI.actCompressorAPIPort to sortingLineHardware.compressor.actCompressorOnPort;
        connect sortingLineAPI.compressorAPI.compressorAPIStatePort
            to sortingLineAPI.sortingLineEngine.compressorStatePort;

        connect sortingLineAPI.sortingLineEngine.whiteValveCmdPort to sortingLineAPI.whiteEjector.valveAPICmdPort;
        connect sortingLineAPI.whiteEjector.actValvePort to sortingLineHardware.whiteEjector.openValvePort;
        connect sortingLineAPI.whiteEjector.valveAPIStatePort to sortingLineAPI.sortingLineEngine.whiteValveStatePort;

        connect sortingLineAPI.sortingLineEngine.blueValveCmdPort to sortingLineAPI.blueEjector.valveAPICmdPort;
        connect sortingLineAPI.blueEjector.actValvePort to sortingLineHardware.blueEjector.openValvePort;
        connect sortingLineAPI.blueEjector.valveAPIStatePort to sortingLineAPI.sortingLineEngine.blueValveStatePort;

        connect sortingLineAPI.sortingLineEngine.redValveCmdPort to sortingLineAPI.redEjector.valveAPICmdPort;
        connect sortingLineAPI.redEjector.actValvePort to sortingLineHardware.redEjector.openValvePort;
        connect sortingLineAPI.redEjector.valveAPIStatePort to sortingLineAPI.sortingLineEngine.redValveStatePort;

        connect sortingLineHardware.inputLightBarrier.lightBarrierPort
            to sortingLineAPI.sortingLineEngine.inputLightBarrierPort;
        connect sortingLineHardware.blueLightBarrier.lightBarrierPort
            to sortingLineAPI.sortingLineEngine.blueLightBarrierPort;
        connect sortingLineHardware.whiteLightBarrier.lightBarrierPort
            to sortingLineAPI.sortingLineEngine.whiteLightBarrierPort;
        connect sortingLineHardware.redLightBarrier.lightBarrierPort
            to sortingLineAPI.sortingLineEngine.redLightBarrierPort;
        connect sortingLineHardware.lightBarrierAfterColorSensor.lightBarrierPort
            to sortingLineAPI.sortingLineEngine.lightBarrierAfterColorSensorPort;
        connect sortingLineHardware.colorSensor.colorSensorPort to sortingLineAPI.sortingLineEngine.colorSensorPort;
    }
}
