private import Locations::IndexedLineLocationDef;
private import CmdTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::API;
private import MachineModulesAPIs::*;
private import Factory::HardwarePackage::Hardware;
private import Factory::HardwarePackage::*;
private import ModuleHardware::*;
private import Factory::*;
private import ScalarValues::*;
package IndexedLine {
    part def IndexedLineAPI :> API {
        item indexedLineState :>> stateAPI : IndexedLineState;
        attribute packageAtMill : Boolean {
            doc
            /*
             * Sensor at mill
             */
        }
        attribute packageAtDrill : Boolean {
            doc
            /*
             * Sensor at drill
             */
        }
        attribute packageAtStart : Boolean {
            doc
            /*
             * Sensor at the start of the feeding conveyor
             */
        }
        attribute packageAtEnd : Boolean {
            doc
            /*
             * Sensor after slider two (slider that transfers from drill to end)
             */
        }
        attribute packageAtSlider : Boolean {
            doc
            /*
             * Sensor right before slider one (slider that transfers from feed to mill)
             */
        }
        attribute transferFromTo : IndexedLineLocationDef {
            doc
            /*
             * isOptional
             */
        }
        doc
        /*
         * The indexed line consists of 4 conveyors, a drilling machine, a milling machine and 2 conveyor sliders.
         */
        attribute target : Integer;
        action transfer;
        in port indexedLineCmdPort :>> cmdAPIPort {
            in item indexedLineAPICmdItem : IndexedLineCmd;
        }
        out port indexedLineStatePort :>> stateAPIPort {
            out item indexedLineAPIStateItem : IndexedLineState;
        }
        part millAPI : MillAPI;
        part drillAPI : DrillAPI;
        part conveyorBeltEndAPI : ConveyorBeltSimpleAPI;
        part conveyorBeltDrillAPI : ConveyorBeltSimpleAPI;
        part conveyorBeltFeedAPI : ConveyorBeltSimpleAPI;
        part conveyorBeltMillAPI : ConveyorBeltSimpleAPI;
        part conveyorSliderFeedToMillAPI : ConveyorSliderAPI {
            attribute location = IndexedLineLocationDef::FEED_TO_MILL;
        }
        part conveyorSliderDrillToEndAPI : ConveyorSliderAPI {
            attribute location = IndexedLineLocationDef::DRILL_TO_END;
        }
        part engine {
            in port indexedLineEngineCmdPort {
                in item indexedLineEngineCmdItem : IndexedLineCmd;
            }
            out port indexedLineEngineStatePort {
                out item indexedLineStateItem : IndexedLineState;
            }
            in port conveyorSliderFeedToMillState {
                in item conveyorSliderFeedToMillStateItem : ConveyorSliderState;
            }
            in port conveyorSliderDrillToEndState {
                in item conveyorSliderFeedToMillStateItem : ConveyorSliderState;
            }
            out port conveyorSliderFeedToMillCmd {
                out item conveyorSliderFeedToMillCmdItem : ConveyorSliderCmd;
            }
            out port conveyorSliderDrillToEndCmd {
                out item conveyorSliderDrillToEndCmdItem : ConveyorSliderCmd;
            }
            in port millStatePort {
                in item millStateItem : MillState;
            }
            out port millCmdPort {
                out item millCmdItem : MillCmd;
            }
            in port drillStatePort {
                in item drillStateItem : DrillState;
            }
            out port drillCmdPort {
                out item drillCmdItem : DrillCmd;
            }
            in port phototransistorInputPort {
                phototransistorInput : Boolean;
            }
            in port phototransistorFeedToMillPort {
                phototransistorFeedToMill : Boolean;
            }
            in port phototransistorDrill {
                phototransistorDrill : Boolean;
            }
            in port phototransistorEndPort {
                phototransistorEnd : Boolean;
            }
            in port phototransistorMillPort {
                phototransistorMill : Boolean;
            }
            out port conveyorBeltEndCmdPort {
                conveyorBeltEndCmdItem : ConveyorBeltCmd;
            }
            out port conveyorBeltDrillCmdPort {
                conveyorBeltDrillCmdItem : ConveyorBeltCmd;
            }
            out port conveyorBeltMillCmdPort {
                conveyorBeltMillCmdItem : ConveyorBeltCmd;
            }
            out port conveyorBeltFeedCmdPort {
                conveyorBeltFeedCmdItem : ConveyorBeltCmd;
            }
            out port conveyorBeltEndStatePort {
                conveyorBeltEndStateItem : ConveyorBeltState;
            }
            out port conveyorBeltDrillStatePort {
                conveyorBeltDrillStateItem : ConveyorBeltState;
            }
            out port conveyorBeltMillStatePort {
                conveyorBeltMillStateItem : ConveyorBeltState;
            }
            out port conveyorBeltFeedStatePort {
                conveyorBeltFeedStateItem : ConveyorBeltState;
            }
        }
    }
    part def IndexedLineHardware :> Hardware {
        part mill : Mill;
        part drill : Drill;

        part conveyorBeltDrill : ConveyorBeltSimple;
        part conveyorBeltEnd : ConveyorBeltSimple;
        part conveyorBeltFeed : ConveyorBeltSimple;
        part conveyorBeltMill : ConveyorBeltSimple;

        part phototransistorEnd : Phototransistor;
        part phototransistorInput : Phototransistor;
        part phototransistorDrill : Phototransistor;
        part phototransistorMill : Phototransistor;
        part phototransistorFeedToMill : Phototransistor;

        part conveyorSliderDrillToEnd : ConveyorSlider {
            attribute location = IndexedLineLocationDef::DRILL_TO_END;
        }
        part conveyorSliderFeedToMill : ConveyorSlider {
            attribute location = IndexedLineLocationDef::FEED_TO_MILL;
        }
    }

    part def IndexedLineSystem :> System {
        in port indexedLineCmdPort {
            in item indexedLineCmdItem : IndexedLineCmd;
        }
        out port indexedLineStatePort {
            out item indexedLineStateItem : IndexedLineState;
        }
        part indexedLineAPI : IndexedLineAPI;
        part indexedLineHardware : IndexedLineHardware;

        connect indexedLineCmdPort to indexedLineAPI.indexedLineCmdPort;
        connect indexedLineAPI.indexedLineCmdPort to indexedLineAPI.engine.indexedLineEngineCmdPort;
        connect indexedLineAPI.engine.indexedLineEngineStatePort to indexedLineAPI.indexedLineStatePort;
        connect indexedLineAPI.indexedLineStatePort to indexedLineStatePort;

        connect indexedLineAPI.engine.conveyorSliderFeedToMillCmd
            to indexedLineAPI.conveyorSliderFeedToMillAPI.conveyorSliderAPICmdPort;
        connect indexedLineAPI.conveyorSliderFeedToMillAPI.actConveyorSliderForwardPort
            to indexedLineHardware.conveyorSliderFeedToMill.actMotorSliderForwardPort;
        connect indexedLineAPI.conveyorSliderFeedToMillAPI.actMotorSliderBackwardConveyorSliderAPIPort
            to indexedLineHardware.conveyorSliderFeedToMill.actMotorSliderBackwardPort;
        connect indexedLineHardware.conveyorSliderFeedToMill.senseButtonPushBackwardPort
            to indexedLineAPI.conveyorSliderFeedToMillAPI.senseButtonPushBackConveyorSliderAPI;
        connect indexedLineHardware.conveyorSliderFeedToMill.senseButtonPushForwardPort
            to indexedLineAPI.conveyorSliderFeedToMillAPI.senseButtonPushFrontConveyorSliderAPIPort;
        connect indexedLineAPI.conveyorSliderFeedToMillAPI.conveyorSliderStatePort
            to indexedLineAPI.engine.conveyorSliderFeedToMillState;

        connect indexedLineAPI.engine.conveyorSliderDrillToEndCmd
            to indexedLineAPI.conveyorSliderDrillToEndAPI.conveyorSliderAPICmdPort;
        connect indexedLineAPI.conveyorSliderDrillToEndAPI.actConveyorSliderForwardPort
            to indexedLineHardware.conveyorSliderDrillToEnd.actMotorSliderForwardPort;
        connect indexedLineAPI.conveyorSliderDrillToEndAPI.actMotorSliderBackwardConveyorSliderAPIPort
            to indexedLineHardware.conveyorSliderDrillToEnd.actMotorSliderBackwardPort;
        connect indexedLineHardware.conveyorSliderDrillToEnd.senseButtonPushBackwardPort
            to indexedLineAPI.conveyorSliderDrillToEndAPI.senseButtonPushBackConveyorSliderAPI;
        connect indexedLineHardware.conveyorSliderDrillToEnd.senseButtonPushForwardPort
            to indexedLineAPI.conveyorSliderDrillToEndAPI.senseButtonPushFrontConveyorSliderAPIPort;
        connect indexedLineAPI.conveyorSliderDrillToEndAPI.conveyorSliderStatePort
            to indexedLineAPI.engine.conveyorSliderDrillToEndState;

        connect indexedLineAPI.engine.millCmdPort to indexedLineAPI.millAPI.millAPICmdPort;
        connect indexedLineAPI.millAPI.actMillingAPIPort to indexedLineHardware.mill.actMillPort;
        connect indexedLineAPI.millAPI.millAPIStatePort to indexedLineAPI.engine.millStatePort;

        connect indexedLineAPI.engine.drillCmdPort to indexedLineAPI.drillAPI.drillAPICmdPort;
        connect indexedLineAPI.drillAPI.actDrillingAPIPort to indexedLineHardware.drill.actDrillPort;
        connect indexedLineAPI.drillAPI.drillAPIStatePort to indexedLineAPI.engine.drillStatePort;

        connect indexedLineAPI.engine.conveyorBeltFeedCmdPort
            to indexedLineAPI.conveyorBeltFeedAPI.conveyorBeltSimpleAPICmdPort;
        connect indexedLineAPI.conveyorBeltFeedAPI.actConveyorBeltSimpleAPIPort
            to indexedLineHardware.conveyorBeltFeed.actConveyorBeltForwardPort;
        connect indexedLineAPI.conveyorBeltFeedAPI.conveyorBeltSimpleAPIStatePort
            to indexedLineAPI.engine.conveyorBeltFeedStatePort;

        connect indexedLineAPI.engine.conveyorBeltMillCmdPort
            to indexedLineAPI.conveyorBeltMillAPI.conveyorBeltSimpleAPICmdPort;
        connect indexedLineAPI.conveyorBeltMillAPI.actConveyorBeltSimpleAPIPort
            to indexedLineHardware.conveyorBeltMill.actConveyorBeltForwardPort;
        connect indexedLineAPI.conveyorBeltMillAPI.conveyorBeltSimpleAPIStatePort
            to indexedLineAPI.engine.conveyorBeltMillStatePort;

        connect indexedLineAPI.engine.conveyorBeltDrillCmdPort
            to indexedLineAPI.conveyorBeltDrillAPI.conveyorBeltSimpleAPICmdPort;
        connect indexedLineAPI.conveyorBeltDrillAPI.actConveyorBeltSimpleAPIPort
            to indexedLineHardware.conveyorBeltDrill.actConveyorBeltForwardPort;
        connect indexedLineAPI.conveyorBeltDrillAPI.conveyorBeltSimpleAPIStatePort
            to indexedLineAPI.engine.conveyorBeltDrillCmdPort;

        connect indexedLineAPI.engine.conveyorBeltEndCmdPort
            to indexedLineAPI.conveyorBeltEndAPI.conveyorBeltSimpleAPICmdPort;
        connect indexedLineAPI.conveyorBeltEndAPI.actConveyorBeltSimpleAPIPort
            to indexedLineHardware.conveyorBeltEnd.actConveyorBeltForwardPort;
        connect indexedLineAPI.conveyorBeltEndAPI.conveyorBeltSimpleAPIStatePort
            to indexedLineAPI.engine.conveyorBeltEndCmdPort;

        connect indexedLineHardware.phototransistorDrill to indexedLineAPI.engine.phototransistorDrill;
        connect indexedLineHardware.phototransistorEnd to indexedLineAPI.engine.phototransistorEndPort;
        connect indexedLineHardware.phototransistorFeedToMill to indexedLineAPI.engine.phototransistorFeedToMillPort;
        connect indexedLineHardware.phototransistorInput to indexedLineAPI.engine.phototransistorInputPort;
        connect indexedLineHardware.phototransistorMill to indexedLineAPI.engine.phototransistorMillPort;
    }
}
