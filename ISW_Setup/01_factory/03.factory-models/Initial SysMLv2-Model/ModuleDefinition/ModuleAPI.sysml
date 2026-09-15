private import Factory::*;
private import CmdTypes::*;
private import StateTypes::*;
private import ScalarValues::*;
private import Factory::SoftwarePackage::API;
package MachineModulesAPIs {
    part def Actuator1DAPI :> API {
        action moveForward;
        action stopMoving;
        action moveBackward;
        action moveToPosition;
        action reset;
        attribute backwardLimitSwitch;
        out port forward1DActuatorPort {
            out forward1DActuatorAPI : Boolean;
        }
        out port backward1DActuatorPort {
            out backward1DActuatorAPI : Boolean;
        }
        in port backwardLimitSwitchPort {
            in backwardLimitSwitch1DActuatorAPI : Boolean;
        }
        out port actuator1DAPIStatePort :>> stateAPIPort {
            out actuator1DAPIStateItem : Actuator1DState;
        }
        in port actuator1DAPICmdPort :>> cmdAPIPort {
            in actuator1DAPICmdItem : Actuator1DCmd;
        }
        attribute forwardLimit : Integer {
            doc
            /*
             * forwardPositionLimit >= 0 && forwardPositionLimit <= 2147483648
             */
        }

        item actuator1DState :>> stateAPI : Actuator1DState;
        attribute accuracy : Integer;
        attribute stopOffset : Integer;
        attribute backwardLimit : Integer;
        attribute sensorBackwardLimitSwitch : Boolean;
        attribute actuatorForward : Boolean;
        attribute actuatorBackward : Boolean;
        attribute target : Integer;
        item currentCmd : Actuator1DCmd;
    }

    part def CantileverMotorAPI :> Actuator1DAPI{
        in port forwardLimitSwitchPort{
            in forwardLimitSwitch1DActuatorAPI : Boolean;
        }
    } 

    part def Rotor1DActuatorAPI :> MachineModulesAPIs::Actuator1DAPI {
        attribute leftLimit : Integer;
        attribute sensorRightLimitSwitch :>> sensorBackwardLimitSwitch : Boolean;
        attribute rightLimit :>> backwardLimit : Integer;
        attribute actuatorRight :>> actuatorBackward : Boolean;
        attribute actuatorLeft :>> actuatorForward : Boolean;
        attribute limitLeft :>> forwardLimit : Integer;

        item currentState :>> actuator1DState : RotorState;
        item currentCmd :>> currentCmd : RotorCmd;

        out port actRotLeftPort :>> forward1DActuatorPort {
            out actRotLeft : Boolean;
        }
        out port actRotRightPort :>> backward1DActuatorPort {
            out actRotRight : Boolean;
        }
        in port senseRotEndRightPort :>> backwardLimitSwitchPort {
            in senseRotEndRIght : Boolean;
        }
        in port rotorAPICmd :>> actuator1DAPICmdPort {
            in rotorAPICmdItem : RotorCmd;
        }
        out port rotorAPIState :>> actuator1DAPIStatePort {
            out rotorAPIStateItem : RotorState;
        }

        action rotateLeft :>> moveForward;
        action rotateRight :>> moveBackward;
    }

    part def Vertical1DActuatorAPI :> MachineModulesAPIs::Actuator1DAPI {
        attribute sensorUpLimitSwitch :>> sensorBackwardLimitSwitch : Boolean;
        attribute downLimit : Integer;
        attribute upLimit :>> backwardLimit : Integer;
        attribute actuatorUp :>> actuatorBackward : Boolean;
        attribute actuatorDown :>> actuatorForward : Boolean;
        attribute limitUp :>> forwardLimit : Integer;

        item currentState :>> actuator1DState : VerticalState;
        item currentCmd :>> currentCmd : VerticalCmd;

        out port actVertUpPort :>> forward1DActuatorPort {
            out actVertUp : Boolean;
        }
        out port actVertDownPort :>> backward1DActuatorPort {
            out actVertDown : Boolean;
        }
        in port senseVertEndDownPort :>> backwardLimitSwitchPort {
            in senseVertEndDown : Boolean;
        }
        in port verticalCmdPort :>> actuator1DAPICmdPort {
            in verticalCmdItem : VerticalCmd;
        }
        out port verticalStatePort :>> actuator1DAPIStatePort {
            out verticalStateItem : VerticalState;
        }

        action verticalUp :>> moveForward;
        action verticalDown :>> moveBackward;
    }

part def Horizontal1DActuatorAPI :> MachineModulesAPIs::Actuator1DAPI {
        attribute sensorUpLimitSwitch :>> sensorBackwardLimitSwitch : Boolean;
        attribute rightLimit :>> backwardLimit : Integer;
        attribute actuatorRight :>> actuatorBackward : Boolean;
        attribute actuatorLeft :>> actuatorForward : Boolean;
        attribute leftLimit :>> forwardLimit : Integer;

        item currentState :>> actuator1DState : HorizontalState;
        item currentCmd :>> currentCmd : HorizontalCmd;

        out port actHorRightPort :>> forward1DActuatorPort {
            out actHorRight : Boolean;
        }
        out port actHorLeftPort :>> backward1DActuatorPort {
            out actHorLeft : Boolean;
        }
        in port senseHorEndLeftPort :>> backwardLimitSwitchPort {
            in senseHorEndLeft : Boolean;
        }
        in port horAPICmdPort :>> actuator1DAPICmdPort {
            in horAPICmdItem : HorizontalCmd;
        }
        out port horAPIStatePort :>> actuator1DAPIStatePort {
            out horAPIStateItem : HorizontalState;
        }

        action horRight :>> moveForward;
        action horLeft :>> moveBackward;
    }


    part def Arm1DActuatorAPI :> Actuator1DAPI {
        attribute downLimit : Integer;
        attribute sensorUpLimitSwitch :>> sensorBackwardLimitSwitch : Boolean;
        attribute armInLimit :>> backwardLimit : Integer;
        attribute armIn :>> actuatorBackward : Boolean;
        attribute armOut :>> actuatorForward : Boolean;
        attribute armOutLimit :>> forwardLimit : Integer;

        item currentState :>> actuator1DState : ArmState;
        item currentCmd :>> currentCmd : ArmCmd;

        out port actArmOutVacuumGripperPort :>> forward1DActuatorPort {
            out actArmOutVacuumGripper : Boolean;
        }
        out port actArmInVacuumGripperPort :>> backward1DActuatorPort {
            out actArmInVacuumGripper : Boolean;
        }
        in port senseArmEndInVacuumGripperPort :>> backwardLimitSwitchPort {
            in senseVertEndRightVacuumGripepr : Boolean;
        }
        in port armVacuumGripperAPICmd :>> actuator1DAPICmdPort {
            in verticalVacuumGripperAPICmdItem : ArmCmd;
        }
        out port armVacuumGripperAPIState :>> actuator1DAPIStatePort {
            out verticalVacuumGripperAPIStateItem : ArmState;
        }

        action moveDown :>> moveForward;
        action moveUp :>> moveBackward;
    }

    part def ImpulseActuatorAPI :> API {
        action moveForward;
        action moveBackwardForReset;
        out port forwardImpulseActuatorPort {
            out forwardImpulseActuator : Boolean;
        }
        out port backwardImpulseActuatorPort {
            out backwardImpulseActuator : Boolean;
        }
        in port backwardLimitSwitchImpulseActuatorPort {
            in backwardLimitImpulseActuator : Boolean;
        }
        attribute impulseCounter : Integer;
        attribute impulseForwardLimit : Integer;
        out port impulseActuatorStatePort :>> stateAPIPort {
            out item impulseActuatorStateItem : ImpulseActuatorState;
        }
        item impulseActuatorState :>> stateAPI : ImpulseActuatorState;
        in port impulseActuatorCmdPort :>> cmdAPIPort {
            in item impulseActuatorCmdItem : ImpulseActuatorCmd;
        }
        attribute sensorBackwardLimitSwitch : Boolean;
        attribute actuatorForward : Boolean;
        attribute actuatorBackward : Boolean;
        attribute target : Integer;
        item currentCmdImpulseActuator : ImpulseActuatorCmd;
    }
    part def ArmImpulseActuatorAPI :> ImpulseActuatorAPI {
        attribute impulseCounterArmAPI :>> impulseCounter;
        attribute limitOut :> impulseForwardLimit : Boolean;
        attribute armOut :>> actuatorForward : Boolean;
        attribute armIn :>> actuatorBackward : Boolean;
        attribute senseorArmEnd :>> sensorBackwardLimitSwitch : Boolean;

        item currentStateArm : ArmState;
        item currentCmdArm :>> currentCmdImpulseActuator : ArmCmd;

        action armOutAction :>> moveForward;
        action armInAction :>> moveBackwardForReset;

        out port actArmOutAPIPort :>> forwardImpulseActuatorPort {
            out actArmOutAPI : Boolean;
        }
        out port actArmInAPIPort :>> backwardImpulseActuatorPort {
            out actArmInAPI : Boolean;
        }
        in port senseArmEndInAPIPort :>> backwardLimitSwitchImpulseActuatorPort {
            in senseArmEndInAPI : Boolean;
        }

        out port armStateAPIPort :>> impulseActuatorStatePort {
            out item armStateClawGripperAPIItem : ArmState;
        }
        in port armCmdAPIPort :>> impulseActuatorCmdPort {
            in item armCmdAPIItem : ArmCmd;
        }
    }

    part def ClawImpulseActuatorAPI :> ImpulseActuatorAPI {
        attribute impulseCounterClawAPI :>> impulseCounter;
        attribute limitClaw :> impulseForwardLimit : Boolean;
        attribute closeClaw :>> actuatorForward : Boolean;
        attribute openClaw :>> actuatorBackward : Boolean;
        attribute sensorClawOpen :>> sensorBackwardLimitSwitch : Boolean;

        item currentStateClaw : ClawImpulseState;
        item currentCmdClaw :>> currentCmdImpulseActuator : ClawImpulseCmd;

        action closeClawAction :>> moveForward;
        action openClawAction :>> moveBackwardForReset;

        out port actClawCloseAPIPort :>> forwardImpulseActuatorPort {
            out actClawCloseAPI : Boolean;
        }
        out port actClawOpenAPIPort :>> backwardImpulseActuatorPort {
            out actClawOpenAPI : Boolean;
        }
        in port senseClawOpenEndAPIPort :>> backwardLimitSwitchImpulseActuatorPort {
            in senseClawOpenEndAPI : Boolean;
        }
        out port clawStateAPIPort :>> impulseActuatorStatePort {
            out item clawStateAPIItem : ClawImpulseState;
        }
        in port clawCmdAPIPort :>> impulseActuatorCmdPort {
            in item clawCmdAPIItem : ArmCmd;
        }
    }
    part def ConveyorSliderAPI :> API {
        out port actConveyorSliderForwardPort {
            out actConveyorSliderForwardAPI : Boolean;
        }
        in port senseButtonPushFrontConveyorSliderAPIPort {
            in senseButtonPushFrontConveyorSliderAPI : Boolean;
        }
        out port actMotorSliderBackwardConveyorSliderAPIPort {
            out actMotorSliderBackwardConveyorSliderAPI : Boolean;
        }
        in port senseButtonPushBackConveyorSliderAPI {
            in senseButtonPushBackConveyorSliderAPI : Boolean;
        }
        attribute location : Locations::IndexedLineLocationDef;
        in port conveyorSliderAPICmdPort :>> cmdAPIPort {
            in item conveyorSliderAPICmdItem : ConveyorSliderCmd;
        }
        out port conveyorSliderStatePort :>> stateAPIPort {
            out item conveyorSliderAPiStateItem : ConveyorSliderState;
        }
        item conveyorSliderState :>> stateAPI : ConveyorSliderState;
        item currentCmd : ConveyorSliderCmd;
        action motorSliderForward;
        action motorSliderBackward;
    }
    part def DrillAPI :> API {
        out port actDrillingAPIPort {
            out actDrillingAPI : Boolean;
        }
        action drill;
        action stop;
        in port drillAPICmdPort :>> cmdAPIPort {
            in item drillAPICmdItem : DrillCmd;
        }
        out port drillAPIStatePort :>> stateAPIPort {
            out item drillAPIStateItem : DrillState;
        }
        item drillState :>> stateAPI : DrillState;
        item currentCmd : DrillCmd;
        attribute isDrilling : Boolean;
        attribute drillTime : Integer;
    }
    part def MillAPI :> API {
        out port actMillingAPIPort {
            out actMillingAPI : Boolean;
        }
        action mill;
        action stop;
        item millState :>> stateAPI : MillState;
        in port millAPICmdPort :>> cmdAPIPort {
            in item millAPICmdItem : MillCmd;
        }
        out port millAPIStatePort :>> stateAPIPort {
            out item millAPIStateItem : MillState;
        }
        attribute isMilling : Boolean;
        item currentCmd : MillCmd;
        attribute millTime : Integer;
    }
    part def VacuumModuleAPI :> API{
        part valveAPI : ValveAPI;
        part compressorAPI : CompressorAPI;
    }
    part def ValveAPI :> API {
        item valveState :>> stateAPI : ValveState;
        item currentCmd : ValveCmd;
        out port valveAPIStatePort :>> stateAPIPort {
            out item valveAPIStateItem : ValveState;
        }
        in port valveAPICmdPort :>> cmdAPIPort {
            in item valveAPICmdItem : ValveCmd;
        }
        out port actValvePort {
            out actValve : Boolean;
        }
    }
    part def CompressorAPI :> API {
        in port compressorAPICmdPort :>> cmdAPIPort {
            in item compressorAPICmdItem : CompressorCmd;
        }
        out port compressorAPIStatePort :>> stateAPIPort {
            out item compressorAPIStateItem : CompressorState;
        }
        item compressorAPIState :>> stateAPI : CompressorState;
        item currentCmd : CompressorCmd;
        out port actCompressorAPIPort {
            out actCompressor : Boolean;
        }
    }
    part def ConveyorBeltSimpleAPI :> API {
        item conveyorBeltSimpleAPIState :>> stateAPI : ConveyorBeltSimpleState;
        attribute isMoving : Boolean;
        in port conveyorBeltSimpleAPICmdPort :>> cmdAPIPort {
            in item conveyorBeltSimpleAPICmdItem : ConveyorBeltSimpleCmd;
        }
        out port actConveyorBeltSimpleAPIPort {
            out actConveyorBeltSimple : Boolean;
        }
        out port conveyorBeltSimpleAPIStatePort :>> stateAPIPort {
            out item conveyorBeltSimpleAPIStateItem : ConveyorBeltSimpleState;
        }
        action stopMoving;
        action move;
    }
    part def ConveyorBeltAPI :> ConveyorBeltSimpleAPI {
        action moveBackward;
        action moveForwardTo;
        action moveBackwardTo;
        in port conveyorBeltAPICmdPort :>> conveyorBeltSimpleAPICmdPort {
            in item conveyorBeltAPICmdPort : ConveyorBeltCmd;
        }
        item currentCmd : ConveyorBeltCmd;
        item conveyorBeltAPIState :>> conveyorBeltSimpleAPIState : ConveyorBeltState;
        attribute targetCount : Integer;
        attribute targeting : Boolean;
        out port actConveyorBeltAPIForwardPort {
            out actConveyorBeltAPIFOrward : Boolean;
        }
        out port actConveyorBeltAPIBackwardPort {
            out actConveyorBeltAPIBackward : Boolean;
        }
        action moveForward :> move;
        out port conveyorBeltAPIStatePort :>> conveyorBeltSimpleAPIStatePort {
            out item conveyorBeltStateItem : ConveyorBeltState;
        }
    }
}