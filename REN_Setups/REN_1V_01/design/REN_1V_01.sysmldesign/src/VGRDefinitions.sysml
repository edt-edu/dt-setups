package VGRDefinitions {
	doc
	/*
	 * Vacuum Gripper Robot definitions model.
	 */

	import ScalarValues::*;
	import Quantities::*;
	import MeasurementReferences::*;
	import ISQ::*;
	import SI::*;
	
	/* PART DEFINITIONS */
	
	part def VacuumGripperRobot {
		attribute mass : Real;
		attribute status : MachineStatus;
		
		part axisX : EncoderBasedMovementAxis ; // forward/backward movement, ie. horizontal move
		part axisZ : EncoderBasedMovementAxis; // up/down movement, ie. vertical move
		part yawAxis : EncoderBasedMovementAxis; // rotation around the Z-axis. ie. rotation
		
		//ref part contoller : ProgrammableLogicController;
	}
	
	attribute def MachineStatus {
		// attribute gearSetting : Integer;
		// attribute acceleratorPosition : Real;
	}
	
	abstract part def MovementAxis;
	part def EncoderBasedMovementAxis :> MovementAxis {
		part engine : EncoderEngine;
		part referenceSwitch : Button;
	}
	
	part def EncoderEngine;
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
	
	/* PORT DEFINITIONS */
	
	
	/* INTERFACE DEFINITIONS */
	
	
}