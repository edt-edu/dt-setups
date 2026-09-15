private import CmdTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::API;
private import MachineModulesAPIs::*;
private import Factory::HardwarePackage::Hardware;
private import ModuleHardware::*;
private import Factory::*;
private import BooleanFunctions;
private import ScalarValues::*;
package ClawGripper {
    part def ClawGripperAPI :> API {
        doc
        /*
         * The ClawGripper utilzes 2 1DActuator and 2 impulseActuator
         */
        item clawGripperState :>> stateAPI : ClawGripperState;
        in port clawGripperCmdPort :>> cmdAPIPort {
            in item clawGripperAPICmdItem : ClawGripperCmd;
        }
        out port clawGripperAPIStatePort :>> stateAPIPort {
            out item clawGripperAPIStateItem : ClawGripperState;
        }
        part rotor1DActuatorClawGripperAPI : Rotor1DActuatorAPI {}
        part vertical1DActuatorClawGripperAPI : Vertical1DActuatorAPI {}
        part impulseActuatorArmClawGripperAPI : ArmImpulseActuatorAPI {}
        part impulseActuatorClawClawGripperAPI : ClawImpulseActuatorAPI {}

        part clawGripperEngine {
            out port clawCmdClawGripperAPIPort {
                out item clawCmdClawGripperAPIItem : ClawImpulseCmd;
            }
            out port armCmdClawGripperAPIPort {
                out item armCmdClawGripperAPIItem : ArmCmd;
            }
            out port verticalClawGripperAPICmd {
                out verticalClawGripperAPICmdItem : VerticalCmd;
            }
            out port rotorClawGripperAPICmd {
                out rotorClawGripperAPICmdItem : RotorCmd;
            }
            out port clawGripperStateAPI {
                out clawGripperStateAPIItem : ClawGripperState;
            }
            in port clawGripperCmdAPI {
                in clawGripperCmdItem : ClawGripperCmd;
            }
            in port clawStateClawGripperAPIPort {
                in item clawStateClawGripperAPIItem : ClawImpulseState;
            }
            in port armStateClawGripperAPIPort {
                in item armStateClawGripperAPIItem : ArmState;
            }
            in port rotorClawGripperAPIState {
                in item rotorClawGripperAPIStateItem : RotorState;
            }
            in port verticalClawGripperAPIState {
                in item verticalClawGripperAPIStateItem : VerticalState;
            }
            in port horizontalEncoderPort {
                in impulseLeft : Boolean;
                in impulseRight : Boolean;
            }
            in port rotaryEncoderPort {
                in impulseClockwise : Boolean;
                in impulseCounterClockwise : Boolean;
            }

            in port verticalEncoderPort {
                in impulseUp : Boolean;
                in impulseDown : Boolean;
            }
        }
    }
    part def ClawGripperHardware :> Hardware {
        part armImpulseActuatorClawGripper : ArmImpulseActuator {}
        part clawImpulseActuatorClawGripper : ClawImpulseActuator {}
        part vertical1DActuator : VerticalActuator1D {}
        part rotor1DActuator : RotorActuator1D {}
        part encoderRotate : RotaryEncoder {}
        part encoderVertical : VerticalEncoder {}
        part encoderHorizontal : HorizontalEncoder {}
    }

    part def ClawGripperSystem :> System {
        in port clawGripperCmdPort {
            in item clawGripperCmdItem : ClawGripperCmd;
        }
        out port clawGripperStatePort {
            out item clawGripperStateItem : ClawGripperState;
        }
        part clawGripperAPI : ClawGripperAPI {}
        part clawGripperHardware : ClawGripperHardware {}

        connect clawGripperCmdPort to clawGripperAPI.clawGripperCmdPort;
        connect clawGripperAPI.clawGripperCmdPort to clawGripperAPI.clawGripperEngine.clawGripperCmdAPI;
        connect clawGripperAPI.clawGripperEngine.clawGripperStateAPI to clawGripperAPI.clawGripperAPIStatePort;
        connect clawGripperAPI.clawGripperAPIStatePort to clawGripperStatePort;

        connect clawGripperAPI.clawGripperEngine.clawCmdClawGripperAPIPort
            to clawGripperAPI.impulseActuatorClawClawGripperAPI.clawCmdAPIPort;
        connect clawGripperAPI.impulseActuatorClawClawGripperAPI.actClawOpenAPIPort
            to clawGripperHardware.clawImpulseActuatorClawGripper.actClawOpenPort;
        connect clawGripperAPI.impulseActuatorClawClawGripperAPI.actClawCloseAPIPort
            to clawGripperHardware.clawImpulseActuatorClawGripper.actClawClosePort;
        connect clawGripperHardware.clawImpulseActuatorClawGripper.senseClawOpenPort
            to clawGripperAPI.impulseActuatorClawClawGripperAPI.senseClawOpenEndAPIPort;
        connect clawGripperAPI.impulseActuatorClawClawGripperAPI.clawStateAPIPort
            to clawGripperAPI.clawGripperEngine.clawStateClawGripperAPIPort;

        connect clawGripperAPI.clawGripperEngine.armCmdClawGripperAPIPort
            to clawGripperAPI.impulseActuatorArmClawGripperAPI.armCmdAPIPort;
        connect clawGripperAPI.impulseActuatorArmClawGripperAPI.actArmInAPIPort
            to clawGripperHardware.armImpulseActuatorClawGripper.actArmInPort;
        connect clawGripperAPI.impulseActuatorArmClawGripperAPI.actArmOutAPIPort
            to clawGripperHardware.armImpulseActuatorClawGripper.actArmOutPort;
        connect clawGripperHardware.armImpulseActuatorClawGripper.senseArmEndInPort
            to clawGripperAPI.impulseActuatorArmClawGripperAPI.senseArmEndInAPIPort;
        connect clawGripperAPI.impulseActuatorArmClawGripperAPI.armStateAPIPort
            to clawGripperAPI.clawGripperEngine.armStateClawGripperAPIPort;

        connect clawGripperAPI.clawGripperEngine.rotorClawGripperAPICmd
            to clawGripperAPI.rotor1DActuatorClawGripperAPI.rotorAPICmd;
        connect clawGripperAPI.rotor1DActuatorClawGripperAPI.rotorAPIState
            to clawGripperAPI.clawGripperEngine.rotorClawGripperAPIState;
        connect clawGripperAPI.rotor1DActuatorClawGripperAPI.actRotLeftPort
            to clawGripperHardware.rotor1DActuator.actRotorLeftPort;
        connect clawGripperAPI.rotor1DActuatorClawGripperAPI.actRotRightPort
            to clawGripperHardware.rotor1DActuator.actRotorRightPort;
        connect clawGripperHardware.rotor1DActuator.senseRotorEndRightPort
            to clawGripperAPI.rotor1DActuatorClawGripperAPI.senseRotEndRightPort;

        connect clawGripperAPI.clawGripperEngine.verticalClawGripperAPICmd
            to clawGripperAPI.vertical1DActuatorClawGripperAPI.verticalCmdPort;
        connect clawGripperAPI.vertical1DActuatorClawGripperAPI.verticalStatePort
            to clawGripperAPI.clawGripperEngine.verticalClawGripperAPIState;
        connect clawGripperAPI.vertical1DActuatorClawGripperAPI.actVertDownPort
            to clawGripperHardware.vertical1DActuator.actVerticalDownPort;
        connect clawGripperAPI.vertical1DActuatorClawGripperAPI.actVertUpPort
            to clawGripperHardware.vertical1DActuator.actVerticalUpPort;
        connect clawGripperHardware.vertical1DActuator.senseVerticalEndDownPort
            to clawGripperAPI.vertical1DActuatorClawGripperAPI.senseVertEndDownPort;

        connect clawGripperHardware.encoderVertical.verticalEncoderPort
            to clawGripperAPI.clawGripperEngine.verticalEncoderPort;

        connect clawGripperHardware.encoderHorizontal.horizontalEncoderPort
            to clawGripperAPI.clawGripperEngine.horizontalEncoderPort;
        connect clawGripperHardware.encoderRotate.rotaryEncoderPort
            to clawGripperAPI.clawGripperEngine.rotaryEncoderPort;
    }
}