package REN_1V_01_VGRSystemModel {
	
	import ScalarValues::*;
	package ConceptualLevel {
	
		package SystemStructure {
			
			package PartDefinitions{
				import PortDefinitions::*;
				import SignalDefinitions::*;
				import CalcDefinitions::*;
				import ActionDefinitions::*;
				
				
				// TODO discuss how to model interaction at the abtraction level:
				// simple actions
				// or action mapped to port (cf. asynchronous messaging through ports)
				
				part def VGRSystem {
					
					// attributes
					// ----------
					attribute moveTolerance : Positive;
					attribute gripperStatus : SignalDefinitions::GripperStatus;
					
					
					//  introduced for implementing behavior (TODO move to private or add tag or move to a runtime object?)
					
					attribute isInitalized : Boolean := false {
						doc /* false until the system has performed a setup at least once */
					}
					attribute actionCommandDone : Boolean := false {
						doc /* used to know when an complex action command has been finished */
						comment /* maybe use a signal instead ?*/
					}
					attribute isOnReferencePosition : Boolean := false {
						doc /* true if the system detect that it is on its reference position */
					}

					comment about currentMoveCommandPickupPosition, currentMoveCommandDropPosition /* can both be replaced by a single attribute MoveCmdMsg ? */
					attribute currentMoveCommandPickupPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states	*/

						// can be replaced by a 
					}
					attribute currentMoveCommandDropPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states	*/
					}
					attribute currentGotoTargetPosition : GripperRelativePosition {
						doc /* target value for intermediate target in the move command	*/
					}
					
					// ports
					// -----
					port cmdPort:CmdPort;
					
					// perform actions
					// ---------------
					perform action provideLogService;
					//perform action performTokenMove : MoveToken;
					// perform action performStopAction : StopAction;
					perform action gotoReferencePos : GotoReferencePos{}
					perform action gotoPosition : GotoPosition{ 
						in targetPosition = currentGotoTargetPosition;
						in moveTolerance = moveTolerance;
					}
					perform action activateGrip : ActivateGrip{}
					perform action deactivateGrip : DeactivateGrip{}
					
					// exhibit states
					// --------------
										// behavior
					state vgrSystemStates : VGRSystemStates {
						in selfVGRSystem = VGRSystem::self;
					}
					state def VGRSystemStates{
						in selfVGRSystem : VGRSystem;
						//in this : VGRSystem;

						entry; then idle;
						state idle;
						
						state setup {
							//entry selfVGRSystemPLC.setupEngines;
							do action gotoReferencePos : GotoReferencePos{}
						}
						
						state move {
							doc /* simplified move sequence */
							entry  action assign selfVGRSystem.actionCommandDone  := false;
							then prepare_move;
						
							state prepare_move {
							}

							state setup_before_move {
								do selfVGRSystem.gotoReferencePos ;
							}
							state moveStep01_gotoPickPosition {
								entry action assignTargetPositionForMoveStep01 {
									assign selfVGRSystem.currentGotoTargetPosition  := selfVGRSystem.currentMoveCommandPickupPosition;
								}
								do selfVGRSystem.gotoPosition;
							}
							state moveStep02_pickToken {
								do selfVGRSystem.activateGrip;
							}
							state moveStep03_gotoDropPos {
								entry action assignTargetPositionForMoveStep03 {
									assign selfVGRSystem.currentGotoTargetPosition  := selfVGRSystem.currentMoveCommandPickupPosition;
								}
								do selfVGRSystem.gotoPosition;
							}
							state moveStep04_dropToken{
								do selfVGRSystem.deactivateGrip;
							}
							
							state move_done {
								entry action signalEndOfActioncommand {
									assign selfVGRSystem.actionCommandDone  := true;
								}
							}
						transition 'prep_move_to_setup' 
							first prepare_move
							if not selfVGRSystem.isInitalized
							//do selfVGRSystemPLC.setupEngines
							then setup_before_move;
						
						transition 'setup_before_move_to_step_1'
							first setup_before_move 
							if selfVGRSystem.isOnReferencePosition == true 
							// do selfVGRSystemPLC.resetDecoderCounters
							then moveStep01_gotoPickPosition;

						transition 'prep_move_to_step1'
							first prepare_move
							if selfVGRSystem.isInitalized
							then moveStep01_gotoPickPosition;

						transition 'move_step_1_to_move_step_2' 
							first moveStep01_gotoPickPosition
							// if has reached step_1 target
							if IsInTolerance(value = selfVGRSystem.gripperStatus.gripperPosition.abstractPos, expectedValue = selfVGRSystem.currentMoveCommandPickupPosition.abstractPos, tolerance = selfVGRSystem.moveTolerance)
							then moveStep02_pickToken;
						
						transition 'move_step_2_to_move_step_3' 
							first moveStep02_pickToken
							// if has reached step_2 target
							if selfVGRSystem.gripperStatus.isGripping
							then moveStep03_gotoDropPos; 
						
						transition 'move_step_3_to_move_step_4' 
							first moveStep03_gotoDropPos
							// if has reached step_3 target
							if IsInTolerance(value = selfVGRSystem.gripperStatus.gripperPosition.abstractPos, expectedValue = selfVGRSystem.currentMoveCommandDropPosition.abstractPos, tolerance = selfVGRSystem.moveTolerance)			
							then moveStep04_dropToken;
						
						transition 'move_step_4_to_move_done' 
							first moveStep04_dropToken
							// if has reached step_4 target
							if not selfVGRSystem.gripperStatus.isGripping		
							then move_done;
						}					
						state stop {
							//entry selfVGRSystemPLC.stopAll;
						}
						
						transition 'idle_to_setup'
			                    first idle
			                    accept setupCmdMsg:SetupCmdMsg via selfVGRSystem.cmdPort
			                    then setup;
						transition 'move_to_setup'
			                    first move
			                    accept setupCmdMsg:SetupCmdMsg via selfVGRSystem.cmdPort
			                    then setup;
						transition 'setup_to_idle'
			                    first setup
								if selfVGRSystem.isOnReferencePosition == true 
								// do selfVGRSystemPLC.resetDecoderCounters
			                    then idle;
						
						transition 'idle_to_move'
			                    first idle
			                    accept msg:MoveCmdMsg via selfVGRSystem.cmdPort
								do action storeMoveParams {
									action assign selfVGRSystem.currentMoveCommandPickupPosition := msg.pickupPosition; 
									action assign selfVGRSystem.currentMoveCommandDropPosition := msg.dropPosition; 
								} 
			                    then move;
						transition 'setup_to_move'
			                    first setup
			                    accept msg:MoveCmdMsg via selfVGRSystem.cmdPort
								do action storeMoveParams {
									action assign selfVGRSystem.currentMoveCommandPickupPosition := msg.pickupPosition; 
									action assign selfVGRSystem.currentMoveCommandDropPosition := msg.dropPosition; 
								} 
			                    then move;
						transition 'move_to_idle'
			                    first move
								if selfVGRSystem.actionCommandDone == true 
			                    then idle;

						transition 'idle_to_stop'
							first idle
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystem.cmdPort
							then stop;
						
						transition 'setup_to_stop'
							first setup
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystem.cmdPort
							then stop;
						transition 'move_to_stop'
							first move
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystem.cmdPort
							then stop;
						transition 'stop_to_idle'
							first stop
							then idle;
						
					}
 				} 
			}
			package ActionDefinitions {
				import ContraintDefinitions::*;
				import SystemConstants::*;
				action def GotoReferencePos {
					in moveTolerance : Natural;
					out reachedPosition : SignalDefinitions::GripperRelativePosition; // used to send result for assertion/control	

					
					constraint reachedPositionIsAccetable : IsCloseToPosition {
						in gripperPosition = reachedPosition;
						in expectedPosition = referencePos_value;
						in tolerance = moveTolerance;
					}
				}
				action def GotoPosition {
					in targetPosition : SignalDefinitions::GripperRelativePosition;
					in moveTolerance : Natural;
					out reachedPosition : SignalDefinitions::GripperRelativePosition; // used to send result for assertion/control	

					constraint reachedPositionIsAccetable : IsCloseToPosition {
						in gripperPosition = reachedPosition;
						in expectedPosition = targetPosition;
						in tolerance = moveTolerance;
					}
				}
				action def ActivateGrip {
				}
				action def DeactivateGrip {
				}
			}
			package ContraintDefinitions {
				import CalcDefinitions::*;
				constraint def IsCloseToPosition {
					in gripperPosition : SignalDefinitions::GripperRelativePosition;
					in expectedPosition : SignalDefinitions::GripperRelativePosition;
					in tolerance : Natural;

					IsInTolerance(gripperPosition.abstractPos, expectedPosition.abstractPos, tolerance )
				}
			}
			package CalcDefinitions {
				
				calc def IsInTolerance { in value : Natural; in expectedValue : Natural; in tolerance : Positive;
					return : Boolean = value  < expectedValue + tolerance and value  > expectedValue - tolerance;
				}

				
			}
			package PortDefinitions{
				port def PowerSwitchCmdPort;
				/*port def ActionCmdPort{
                	in item actionCmdMsg:SignalDefinitions::CmdMsg;
            	}*/
				port def CmdPort{
					doc /* 
					* port allowing to send commands to the system
					*/
                	in item cmdMsg:SignalDefinitions::CmdMsg;
            	}
			}
			package SignalDefinitions{

				item def CmdMsg{
	            }
	            item def MoveCmdMsg:>CmdMsg{
	            	attribute pickupPosition : GripperRelativePosition;
	            	attribute dropPosition : GripperRelativePosition;
	            }        
	            item def SetupCmdMsg:>CmdMsg{
	            }
	            item def StopCmdMsg:>CmdMsg{
	            }
				
	            attribute def GripperRelativePosition {
	            	comment /* 
	            	 * discussion: should we use a standard coordinate system such as ISQSpaceTime::universalCartesianSpatial3dCoordinateFrame
	            	 * or use robot relative coordinate (angles, etc) or encoder based (as sent in the current command  )
	            	 */
	            	 attribute abstractPos : Natural;
	            }
	            attribute def GripperStatus  {
	            	doc /* Position and status (succion active/inactive) of the gripper
	            	    */
					attribute gripperPosition : SignalDefinitions::GripperRelativePosition;
					attribute isGripping : Boolean; 
	            }
	        }
			package SystemConstants {
				attribute referencePos_value : SignalDefinitions::GripperRelativePosition {
					doc /* value of the reference position */
					attribute redefines abstractPos = 0;
				}
			}
		}
	
		package SystemConfigurations {
			import SystemStructure::PartDefinitions::*;
			package REN_1V_01_VGRSystem_DT_Monitoring_configuration_a {
				doc /* configuration for Digital Twin based monitoring of REN_1V_01_VGRSystem */
				package PartsTree {
					part REN_1V_01_VGRSystem : VGRSystem {
						attribute moveTolerance redefines moveTolerance=20;

						perform ActionTree::provideLogService redefines provideLogService;
						perform ActionTree::gotoReferencePos redefines gotoReferencePos;
						perform ActionTree::gotoPosition redefines gotoPosition;
						perform ActionTree::activateGrip redefines activateGrip;
						perform ActionTree::deactivateGrip redefines deactivateGrip;
					}
				}
				package ActionTree{
					comment /* in this package, the actions are empty and only supports monitoring */
					action provideLogService;
					action gotoReferencePos : GotoReferencePos{}
					action gotoPosition : GotoPosition{}
					action activateGrip : ActivateGrip{}
					action deactivateGrip : DeactivateGrip{}
				}
			}
			package REN_1V_01_VGRSystem_DT_Simulation_configuration_a {
				doc /* configuration for Digital Twin based simulation */
				package PartsTree {
					comment /* TODO */
					part REN_1V_01_VGRSystem : VGRSystem {
						attribute moveTolerance redefines moveTolerance=20;

						perform ActionTree::provideLogService redefines provideLogService;
						perform ActionTree::gotoReferencePos redefines gotoReferencePos;
						perform ActionTree::gotoPosition redefines gotoPosition;
						perform ActionTree::activateGrip redefines activateGrip;
						perform ActionTree::deactivateGrip redefines deactivateGrip;
					}
				}
				package ActionTree{
					import SystemStructure::*;
					comment /* TODO */
					comment /* in this package, the actions simulates the system behavior */
					comment about gotoReferencePos::simulateGotoReferencePos::vgrSystem, gotoPosition::simulateGotoPosition::vgrSystem, activateGrip::simulateActivateGrip::vgrSystem, deactivateGrip::simulateDeactivateGrip::vgrSystem 
						/* TODO 
						   Q1 how to bind it ? 
						   Q2 used to assign: is it a in or a out ? */
					comment about gotoReferencePos, gotoPosition 
						/* Q1: should we simulate some time spent to move depending on initial status ?  
						   Q2: should we simulate some intermediate reached positions ?
						*/
					action provideLogService;
					action gotoReferencePos : GotoReferencePos{
						comment /* Simulate move to reference position */
						//in vgrSystem : VGRSystem;

						in moveTolerance : Natural;
						out reachedPosition : SignalDefinitions::GripperRelativePosition;

						first start;

						then action simulateGotoReferencePos {
							in vgrSystem : VGRSystem;
							action assign vgrSystem.gripperStatus.gripperPosition := 0; 
							action assign vgrSystem.isOnReferencePosition := true;
							action assign reachedPosition := vgrSystem.gripperStatus.gripperPosition;
						}

						//then action assign reachedPosition := vgrSystem.gripperStatus.gripperPosition;
						then done;
					}
					action gotoPosition : GotoPosition{
						comment /* Simulate move to target position */
						in targetPosition : SignalDefinitions::GripperRelativePosition;
						in moveTolerance : Natural;
						out reachedPosition : SignalDefinitions::GripperRelativePosition;
 
						first start;
						then action simulateGotoPosition {
							in vgrSystem : VGRSystem;
							assign vgrSystem.gripperStatus.gripperPosition := targetPosition; 
							then assign vgrSystem.isOnReferencePosition :=  SimulatedIsOnReferencePosition(pos = targetPosition);
							then assign reachedPosition := vgrSystem.gripperStatus.gripperPosition; 
						}
						then done;
					}
					action activateGrip : ActivateGrip{

						comment /* Simulate activation of the gripper */
						first start; 
						then action simulateActivateGrip {
							in vgrSystem : VGRSystem;
							assign vgrSystem.gripperStatus.isGripping := true;
						}
						then done;

					}
					action deactivateGrip : DeactivateGrip{

						comment /* Simulate deactivation of the gripper */
						first start; 
						then action simulateDeactivateGrip {
							in vgrSystem : VGRSystem;
							assign vgrSystem.gripperStatus.isGripping := false;
						}
						then done;
					}
					calc def SimulatedIsOnReferencePosition { in pos : SignalDefinitions::GripperRelativePosition;
						return : Boolean = pos.abstractPos == 0;
					}
				}
			}
		}
	}	
}
