private import CmdTypes::*;
private import StateTypes::*;
private import Factory::SoftwarePackage::API;
private import MachineModulesAPIs::*;
private import Factory::HardwarePackage::Hardware;
private import ModuleHardware::*;
private import Factory::*;
private import ScalarValues::*;
package VaccumGripper {
    part def VacuumGripperAPI :> API { 
        doc
        /*
         * The VacuumGripper utilizes 3 1DActuator 
         */
        item vacuumGripperState :>> stateAPI : VacuumGripperState;
        in port vacuumGripperAPICmdPort :>> cmdAPIPort {
            in item vacuumGripperAPICmdItem : VacuumGripperCmd;
        }
        out port vacuumGripperStatePort :>> stateAPIPort {
            out item vacuumGripperAPIStateItem : VacuumGripperState;
        }

        part rotor1DActuatorVacuumGripperAPI : Rotor1DActuatorAPI {}
        part vertical1DActuatorVacuumGripperAPI : Vertical1DActuatorAPI {}
        part arm1DActuatorVacuumGripperAPI : Arm1DActuatorAPI {}
        part vaccumModuleAPI : VacuumModuleAPI;

        part engineVacuumGripper {
            out port valveCmdEnginePort {
                out item valveCmdEngineItem : ValveCmd;
            }
            out port compressorCmdEnginePort {
                out item compressorCmdEngineItem : CompressorCmd;
            }
            out port armCmdEnginePort {
                out item armCmdEngineItem : ArmCmd;
            }
            out port rotorCmdEnginePort {
                out item armCmdEngineItem : RotorCmd;
            }
            out port verticalCmdEnginePort {
                out item verticalCmdEngineItem : VerticalCmd;
            }
            out port vacuumGripperEngineStatePort {
                out item vacuumGripperEngineState : VacuumGripperState;
            }

            in port vacuumGripperCmdEnginePort {
                in item vacuumGripperCmdItem : VacuumGripperCmd;
            }
            in port verticalStateEnginePort {
                in item verticalStateEngineItem : VerticalState;
            }
            in port rotorStateEnginePort {
                in item rotorStateEngineItem : RotorState;
            }
            in port armStateEnginePort {
                in item armStateEngineItem : ArmState;
            }
            in port valveStateEnginePort {
                in item valveStateEngineItem : ValveState;
            }
            in port compressorStateEnginePort {
                in item compressorStateEngineItem : CompressorState;
            }
            in port rotaryEncoderPort {
                in impulseClockwise : Boolean;
                in impulseCounterClockwise : Boolean;
            }
            in port horizontalEncoderPort {
                in impulseForward : Boolean;
                in impulseBackward : Boolean;
            }
            in port verticalEncoderPort {
                in impulseUp : Boolean;
                in impulseDown : Boolean;
            }
        }
    }
    part def VacuumGripperHardware :> Hardware {
        part vertical1DActuator : VerticalActuator1D {}
        part arm1DActuator : ArmActuator1D {}
        part rotor1DActuator : Rotor1DActuatorAPI {}
        part vacuumModule : VacuumModule {}
        part encoderRotate : RotaryEncoder {}
        part encoderHorizontal : HorizontalEncoder {}
        part encoderVertical : VerticalEncoder {}
    }

    part def VacuumGripperSystem :> System {
        in port vacuumGripperCmdPort {
            in item vacuumGripperCmdItem : VacuumGripperCmd;
        }
        out port vacuumGripperStatePort {
            out item vacuumGripperStateItem : VacuumGripperState;
        }
        part vacuumGripperAPI : VacuumGripperAPI;
        part vacuumGripperHardware : VacuumGripperHardware;

        connect vacuumGripperCmdPort to vacuumGripperAPI.vacuumGripperAPICmdPort;
        connect vacuumGripperAPI.vacuumGripperAPICmdPort
            to vacuumGripperAPI.engineVacuumGripper.vacuumGripperCmdEnginePort;
        connect vacuumGripperAPI.engineVacuumGripper.vacuumGripperEngineStatePort
            to vacuumGripperAPI.vacuumGripperStatePort;
        connect vacuumGripperAPI.vacuumGripperStatePort to vacuumGripperStatePort;

        connect vacuumGripperAPI.engineVacuumGripper.rotorCmdEnginePort
            to vacuumGripperAPI.rotor1DActuatorVacuumGripperAPI.rotorAPICmd;
        connect vacuumGripperAPI.rotor1DActuatorVacuumGripperAPI.actRotLeftPort
            to vacuumGripperHardware.rotor1DActuator.actRotLeftPort;
        connect vacuumGripperAPI.rotor1DActuatorVacuumGripperAPI.actRotRightPort
            to vacuumGripperHardware.rotor1DActuator.actRotRightPort;
        connect vacuumGripperHardware.rotor1DActuator.senseRotEndRightPort
            to vacuumGripperAPI.rotor1DActuatorVacuumGripperAPI.senseRotEndRightPort;
        connect vacuumGripperAPI.rotor1DActuatorVacuumGripperAPI.rotorAPIState
            to vacuumGripperAPI.engineVacuumGripper.rotorStateEnginePort;

        connect vacuumGripperAPI.engineVacuumGripper.verticalCmdEnginePort
            to vacuumGripperAPI.vertical1DActuatorVacuumGripperAPI.verticalCmdPort;
        connect vacuumGripperAPI.vertical1DActuatorVacuumGripperAPI.actVertDownPort
            to vacuumGripperHardware.vertical1DActuator.actVerticalDownPort;
        connect vacuumGripperAPI.vertical1DActuatorVacuumGripperAPI.actVertUpPort
            to vacuumGripperHardware.vertical1DActuator.actVerticalUpPort;
        connect vacuumGripperHardware.vertical1DActuator.senseVerticalEndDownPort
            to vacuumGripperAPI.vertical1DActuatorVacuumGripperAPI.senseVertEndDownPort;
        connect vacuumGripperAPI.vertical1DActuatorVacuumGripperAPI.verticalStatePort
            to vacuumGripperAPI.engineVacuumGripper.verticalStateEnginePort;

        connect vacuumGripperAPI.engineVacuumGripper.armCmdEnginePort
            to vacuumGripperAPI.arm1DActuatorVacuumGripperAPI.armVacuumGripperAPICmd;
        connect vacuumGripperAPI.arm1DActuatorVacuumGripperAPI.actArmInVacuumGripperPort
            to vacuumGripperHardware.arm1DActuator.actArmInPort;
        connect vacuumGripperAPI.arm1DActuatorVacuumGripperAPI.actArmOutVacuumGripperPort
            to vacuumGripperHardware.arm1DActuator.actArmOutPort;
        connect vacuumGripperHardware.arm1DActuator.senseArmEndInPort
            to vacuumGripperAPI.arm1DActuatorVacuumGripperAPI.senseArmEndInVacuumGripperPort;
        connect vacuumGripperAPI.arm1DActuatorVacuumGripperAPI.armVacuumGripperAPIState
            to vacuumGripperAPI.engineVacuumGripper.armStateEnginePort;

        connect vacuumGripperAPI.engineVacuumGripper.valveCmdEnginePort
            to vacuumGripperAPI.vaccumModuleAPI.valveAPI.valveAPICmdPort;
        connect vacuumGripperAPI.vaccumModuleAPI.valveAPI.valveAPIStatePort
            to vacuumGripperAPI.engineVacuumGripper.valveStateEnginePort;
        connect vacuumGripperAPI.vaccumModuleAPI.valveAPI.actValvePort
            to vacuumGripperHardware.vacuumModule.openValvePort;

        connect vacuumGripperAPI.engineVacuumGripper.compressorCmdEnginePort
            to vacuumGripperAPI.vaccumModuleAPI.compressorAPI.compressorAPICmdPort;
        connect vacuumGripperAPI.vaccumModuleAPI.compressorAPI.compressorAPIStatePort
            to vacuumGripperAPI.engineVacuumGripper.compressorStateEnginePort;
        connect vacuumGripperAPI.vaccumModuleAPI.compressorAPI.actCompressorAPIPort
            to vacuumGripperHardware.vacuumModule.actCompressorPort;

        connect vacuumGripperHardware.encoderVertical.verticalEncoderPort
            to vacuumGripperAPI.engineVacuumGripper.verticalEncoderPort;
        connect vacuumGripperHardware.encoderHorizontal.horizontalEncoderPort
            to vacuumGripperAPI.engineVacuumGripper.horizontalEncoderPort;
        connect vacuumGripperHardware.encoderRotate.rotaryEncoderPort
            to vacuumGripperAPI.engineVacuumGripper.rotaryEncoderPort;
    }
}