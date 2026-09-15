private import DataTypes::*;
private import Factory::*;
private import Factory::HardwarePackage::*;
private import ScalarValues::*;
package ModuleHardware {
    part def Actuator1D :> Hardware {
        in port forwardActuatorPort {
            in moveForward1D : Boolean;
        }
        out port backwardLimitSwitchPort {
            out backwardLimitSwitch1D : Boolean;
        }
        in port backwardActuatorPort {
            in moveBackward : Boolean;
        }
    }
    part def CantileverMotor :> Actuator1D{
        out port forwardLimitSwitchPort{
            out forwardLimitSwitch : Boolean;
        }
    }
    part def VerticalActuator1D :> Actuator1D {
        in port actVerticalDownPort :>> backwardActuatorPort {
            in actVerticalDown : Boolean;
        }
        in port actVerticalUpPort :>> forwardActuatorPort {
            in actVerticalUp : Boolean;
        }
        out port senseVerticalEndDownPort :>> backwardLimitSwitchPort {
            out senseVerticalEndDown : Boolean;
        }
    }
    part def ArmActuator1D :> Actuator1D {
        in port actArmOutPort :>> forwardActuatorPort {
            in actArmOut : Boolean;
        }
        in port actArmInPort :>> backwardActuatorPort {
            in actArmInClawGripperAP : Boolean;
        }
        out port senseArmEndInPort :> backwardLimitSwitchPort {
            out senseArmEndIn : Boolean;
        }
    }
    part def RotorActuator1D :> Actuator1D {
        in port actRotorLeftPort :>> forwardActuatorPort {
            in actRotorLeft : Boolean;
        }
        in port actRotorRightPort :>> backwardActuatorPort {
            in actRotorRight : Boolean;
        }
        out port senseRotorEndRightPort :>> backwardLimitSwitchPort {
            out senseRotorEndRight : Boolean;
        }
    }

    part def HorizontalActuator1D :> Actuator1D {
        in port actHorRightPort :>> forwardActuatorPort {
            in actHorRight : Boolean;
        }
        in port actHorLeftPort :>> backwardActuatorPort {
            in actHorLeft : Boolean;
        }
        out port senseHorEndLeftPort :>> backwardLimitSwitchPort {
            out senseHorEndLeft : Boolean;
        }
    }



    part def ImpulseActuator :> Hardware {
        in port forwardActuatorPort {
            in moveForwardImpulse : Boolean;
        }
        in port backwardActuatorPort {
            in moveBackwardImpulse : Boolean;
        }
        out port backwardLimitSwitchPort {
            out backwardLimitSwitchImpulse : Boolean;
        }
    }
    part def ArmImpulseActuator :> ImpulseActuator {
        in port actArmOutPort :>> forwardActuatorPort {
            in actArmOut : Boolean;
        }
        in port actArmInPort :>> backwardActuatorPort {
            in actArmIn : Boolean;
        }
        out port senseArmEndInPort :>> backwardLimitSwitchPort {
            out senseArmEndIn : Boolean;
        }
    }
    part def ClawImpulseActuator :> ImpulseActuator{
        in port actClawClosePort :>> forwardActuatorPort {
            in actClawClose : Boolean;
        }
        in port actClawOpenPort :>> backwardActuatorPort {
            in actClawOpen : Boolean;
        }
        out port senseClawOpenPort :>> backwardLimitSwitchPort {
            out senseClawOpen : Boolean;
        } 
    }

    part def VacuumModule :> Hardware {
        in port actCompressorPort {
            in actCompressorVacuum : Boolean;
        }
        in port openValvePort {
            in openValveVacuum : Boolean;
        }
    }
    part def Mill :> Hardware {
        port actMillPort {
            actMill : Boolean;
        }
    }
    part def ConveyorSlider :> Hardware {
        in port actMotorSliderForwardPort {
            in actMotorSliderForwardConveyor : Boolean;
        }
        in port actMotorSliderBackwardPort {
            in actMotorSliderBackwardConveyor : Boolean;
        }
        out port senseButtonPushForwardPort {
            out senseButtonForwardConveyor : Boolean;
        }
        out port senseButtonPushBackwardPort {
            out senseButtonBackwardConveyor : Boolean;
        }
    }
    part def ConveyorBeltSimple :> Hardware {
        in port actConveyorBeltForwardPort {
            in actForwardSimpleConveyorBelt : Boolean;
        }
    }
    part def Drill :> Hardware {
        in port actDrillPort {
            in actDrill : Boolean;
        }
    }
    part def LightBarrier :> Hardware {
        out port lightBarrierPort {
            out LightBarrier : Boolean;
        }
    }
    part def ColorSensor :> Hardware {
        out port colorSensorPort {
            out colorSensor : Boolean;
        }
    }
    part def Compressor :> Hardware {
        in port actCompressorOnPort {
            in actCompressorOn : Boolean;
        }
    }
    part def Valve :> Hardware {
        in port openValvePort {
            in openValve : Boolean;
        }
    }
    part def Encoder :> Hardware {
        out port encoderSignalPort {
            out impulse1 : Boolean;
            out impulse2 : Boolean;
        }
    }
    part def RotaryEncoder :> Encoder{
         out port rotaryEncoderPort :>> encoderSignalPort {
            out impulseClockwise : Boolean;
             out impulseCounterClockwise : Boolean;
        }
    }
    part def HorizontalEncoder :> Encoder{
         out port horizontalEncoderPort :>> encoderSignalPort {
            impulseLeft : Boolean;
            impulseRight : Boolean;
        }
    }
    part def VerticalEncoder :> Encoder{
         out port verticalEncoderPort :>> encoderSignalPort {
            out impulseUp : Boolean;
            out impulseDown : Boolean;
        }
    }
    part def Phototransistor :> Hardware {
        out port phototransistorPort {
            out Phototransistor : Boolean;
        }
    }
    part def TrailSensor :> Hardware {
        out port trailSensorPort {
            out trailSensor : Boolean;
        }
    }
    part def ConveyorBelt :> ConveyorBeltSimple {
        in port actConveyorBeltBackwardPort {
            in actConveyorBeltBackward : Boolean;
        }
    }
}