package VGRDefinitions {
	doc
	/*
	 * Vacuum Gripper Robot definitions model.
	 */

	import ScalarValues::*;
	//import Quantities::*;
	//import MeasurementReferences::*;
	//import ISQ::*;
	//import SI::*;
	
	//import SignalDefinitions::*;
	
	/* PART DEFINITIONS */
	package PartDefinitions {
		part def VacuumGripperRobot {
			attribute mass : Real;
			attribute status : MachineStatus;
			
			part slideX : EncoderBasedMovementAxis ; // X tranlstation; forward/backward movement; ie. horizontal move / Arm Extension (prismatic)
			part slideZ : EncoderBasedMovementAxis; // Z translation; up/down movement, ie. vertical move / shoulder elevation (prismatic)
			part spinZ : EncoderBasedMovementAxis; // rotation around the Z-axis. ie. base rotation
			
			
			port vgrPort : PortDefinitions::VGRPort {
				
			}
			//ref part contoller : ProgrammableLogicController;
		}
		abstract part def MovementAxis;
		part def EncoderBasedMovementAxis :> MovementAxis {
			part motor : EncoderMotor;
			part referenceSwitch : Button;
			port forwardCmdPort : PortDefinitions::MotorCmdPort;
		}
		
		part def Motor {
			port forwardCmdPort : PortDefinitions::MotorCmdPort;
		}
		part def BiDirectionnalMotor  :> Motor {
			port backwardCmdPort : PortDefinitions::MotorCmdPort;
		}
		part def EncoderMotor :> BiDirectionnalMotor {
			port encoderPort : PortDefinitions::MotorEncoderSignalPort;
		}
		part def Button {
			attribute sensorValue : Boolean;
		}
		part def Compressor;
		part def Cylinder;
		part def SolenoidValve;
			
		part def ProgrammableLogicController {
			//ref part orchestrator : Orchestrator;
		}
		
		part def Orchestrator;
	}
	attribute def MachineStatus {
		// attribute gearSetting : Integer;
		// attribute acceleratorPosition : Real;
	}
	
		
	/* PORT DEFINITIONS */
	package PortDefinitions{
		
		port def VGRPort {
			port slideXMotorCmdPort : MotorCmdPort;
			port slideXEncoderPort : MotorEncoderSignalPort;
			port slideZMotorCmdPort : MotorCmdPort;
			port slideZEncoderPort : MotorEncoderSignalPort;
			port spinZMotorCmdPort : MotorCmdPort;
			port spinZEncoderPort : MotorEncoderSignalPort;
		}
		
		port def MotorCmdPort{
		    in item motorCmd : SignalDefinitions::MotorCmd;
		}
		port def MotorEncoderSignalPort {
			out item forwardImpulse : SignalDefinitions::encoderImpulse;
			out item backardImpulse : SignalDefinitions::encoderImpulse;
		}
	}
	
	
	/* INTERFACE DEFINITIONS */	
    package IterfaceDefinitions{
    	interface def VGRInternalInterface {
			end supplierPort : PortDefinitions::VGRPort;
			end consumerPort : PortDefinitions::VGRPort;
		} 
    }
	
	/* ITEM DEFINITIONS	*/
    package ItemDefinitions{
    }
	
	/* SIGNAL DEFINITIONS */
    package SignalDefinitions{
    	item def MotorCmd;
    	
    	item def encoderImpulse;
    }
	
}