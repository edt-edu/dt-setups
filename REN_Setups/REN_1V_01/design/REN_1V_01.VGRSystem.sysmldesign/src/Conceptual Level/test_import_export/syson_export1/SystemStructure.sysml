package REN_1V_01_VGRSystemModel {
	private import ScalarValues::*;
	package ConceptualLevel {
		package SystemStructure {
			package PartDefinitions {
				private import PortDefinitions::*;
				private import SignalDefinitions::*;
				private import CalcDefinitions::*;
				private import ActionDefinitions::*;
				part def VGRSystem {
					attribute moveTolerance : Positive;
					attribute gripperStatus : GripperStatus;
					attribute isInitalized : Boolean := false {
						doc /* false until the system has performed a setup at least once */
					}
					attribute actionCommandDone : Boolean := false {
						doc /* used to know when an complex action command has been finished */
						/* maybe use a signal instead ? */
					}
					attribute isOnReferencePosition : Boolean := false {
						doc /* true if the system detect that it is on its reference position */
					}
					/* can both be replaced by a single attribute MoveCmdMsg ? */
					attribute currentMoveCommandPickupPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states */
					}
					attribute currentMoveCommandDropPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states */
					}
					attribute currentGotoTargetPosition : GripperRelativePosition {
						doc /* target value for intermediate target in the move command */
					}
					port cmdPort : CmdPort;
					perform action provideLogService;
					perform action gotoReferencePos : GotoReferencePos;
					perform action gotoPosition : GotoPosition {
						in targetPosition = currentGotoTargetPosition;
						in moveTolerance = moveTolerance;
					}
					perform action activateGrip : ActivateGrip;
					perform action deactivateGrip : DeactivateGrip;
					action def VGRSystemStates {
						in selfVGRSystem : VGRSystem;
						action;
					}
				}
			}
			package ActionDefinitions {
				private import ContraintDefinitions::*;
				private import SystemConstants::*;
				action def GotoReferencePos {
					in moveTolerance : Natural;
					out reachedPosition : SignalDefinitions::GripperRelativePosition;
					constraint reachedPositionIsAccetable : IsCloseToPosition {
						in gripperPosition = reachedPosition;
						in expectedPosition = referencePos_value;
						in tolerance = moveTolerance;
					}
				}
				action def GotoPosition {
					in targetPosition : SignalDefinitions::GripperRelativePosition;
					in moveTolerance : Natural;
					out reachedPosition : SignalDefinitions::GripperRelativePosition;
					constraint reachedPositionIsAccetable : IsCloseToPosition {
						in gripperPosition = reachedPosition;
						in expectedPosition = targetPosition;
						in tolerance = moveTolerance;
					}
				}
				action def ActivateGrip;
				action def DeactivateGrip;
			}
			package ContraintDefinitions {
				private import CalcDefinitions::*;
				occurrence def IsCloseToPosition {
					in gripperPosition : SignalDefinitions::GripperRelativePosition;
					in expectedPosition : SignalDefinitions::GripperRelativePosition;
					in tolerance : Natural;
				}
			}
			package CalcDefinitions;
			package PortDefinitions {
				port def PowerSwitchCmdPort;
				/* port def ActionCmdPort{
				                	in item actionCmdMsg:SignalDefinitions::CmdMsg;
				            	} */
				port def CmdPort {
					doc /* * port allowing to send commands to the system */
					in item cmdMsg : SignalDefinitions::CmdMsg;
				}
			}
			package SignalDefinitions {
				item def CmdMsg;
				item def MoveCmdMsg :> CmdMsg {
					attribute pickupPosition : GripperRelativePosition;
					attribute dropPosition : GripperRelativePosition;
				}
				item def SetupCmdMsg :> CmdMsg;
				item def StopCmdMsg :> CmdMsg;
				attribute def GripperRelativePosition {
					/* * discussion: should we use a standard coordinate system such as ISQSpaceTime::universalCartesianSpatial3dCoordinateFrame
						            	 * or use robot relative coordinate (angles, etc) or encoder based (as sent in the current command  ) */
					attribute abstractPos : Natural;
				}
				attribute def GripperStatus {
					doc /* Position and status (succion active/inactive) of the gripper */
					attribute gripperPosition : GripperRelativePosition;
					attribute isGripping : Boolean;
				}
			}
			package SystemConstants {
				attribute referencePos_value : SignalDefinitions::GripperRelativePosition {
					doc /* value of the reference position */
					attribute :>> abstractPos = 0;
				}
			}
		}
		package SystemConfigurations {
			private import SystemStructure::PartDefinitions::*;
			package REN_1V_01_VGRSystem_DT_Monitoring_configuration_a {
				doc /* configuration for Digital Twin based monitoring of REN_1V_01_VGRSystem */
				package PartsTree {
					part REN_1V_01_VGRSystem : VGRSystem {
						attribute moveTolerance :>> moveTolerance = 20;
						perform ActionTree::provideLogService;
						perform ActionTree::gotoReferencePos;
						perform ActionTree::gotoPosition;
						perform ActionTree::activateGrip;
						perform ActionTree::deactivateGrip;
					}
				}
				package ActionTree {
					/* in this package, the actions are empty and only supports monitoring */
					action provideLogService;
					action gotoReferencePos;
					action gotoPosition;
					action activateGrip;
					action deactivateGrip;
				}
			}
			package REN_1V_01_VGRSystem_DT_Simulation_configuration_a {
				doc /* configuration for Digital Twin based simulation */
				package PartsTree {
					/* TODO */
					part REN_1V_01_VGRSystem : VGRSystem {
						attribute moveTolerance :>> moveTolerance = 20;
						perform ActionTree::provideLogService;
						perform ActionTree::gotoReferencePos;
						perform ActionTree::gotoPosition;
						perform ActionTree::activateGrip;
						perform ActionTree::deactivateGrip;
					}
				}
				package ActionTree {
					private import SystemStructure::*;
					/* TODO */
					/* in this package, the actions simulates the system behavior */
					/* TODO 
											   Q1 how to bind it ? 
											   Q2 used to assign: is it a in or a out ? */
					/* Q1: should we simulate some time spent to move depending on initial status ?  
											   Q2: should we simulate some intermediate reached positions ? */
					action provideLogService;
					action gotoReferencePos {
						/* Simulate move to reference position */
						in moveTolerance : Natural;
						out reachedPosition : SignalDefinitions::GripperRelativePosition;
						first then;
						action simulateGotoReferencePos {
							in vgrSystem : PartDefinitions::VGRSystem;
							action {
								= 0;
							}
							action {
								= true;
							}
							action {
								= .;
							}
						}
						first then done;
					}
					action gotoPosition {
						/* Simulate move to target position */
						in targetPosition : SignalDefinitions::GripperRelativePosition;
						in moveTolerance : Natural;
						out reachedPosition : SignalDefinitions::GripperRelativePosition;
						first then;
						action simulateGotoPosition {
							in vgrSystem : PartDefinitions::VGRSystem;
							action {
								= targetPosition;
							}
							first then;
							action {
								= ;
							}
							first then;
							action {
								= .;
							}
						}
						first then done;
					}
					action activateGrip {
						/* Simulate activation of the gripper */
						first then;
						action simulateActivateGrip {
							in vgrSystem : PartDefinitions::VGRSystem;
							action {
								= true;
							}
						}
						first then done;
					}
					action deactivateGrip {
						/* Simulate deactivation of the gripper */
						first then;
						action simulateDeactivateGrip {
							in vgrSystem : PartDefinitions::VGRSystem;
							action {
								= false;
							}
						}
						first then done;
					}
				}
			}
		}
	}
}