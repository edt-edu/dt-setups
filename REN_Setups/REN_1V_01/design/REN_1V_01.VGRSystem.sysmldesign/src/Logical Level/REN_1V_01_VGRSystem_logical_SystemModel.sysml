package REN_1V_01_VGRSystemPLCModel {
	
	import ScalarValues::*;
	package LogicalLevel {
		package SoftwareDomain {
			package Definitions {
				import SignalDefinitions::*;
				import PortDefinitions::*;
				import REN_1V_01_VGRSystemPLCModel::LogicalLevel::PortDefinitions::*;
				part def VGRSystemPLC {
				
					part compressorController : CompressorPLCModule;
					part valveController : ValvePLCModule;
					part rotationImpulseCounter : QuadratureDecoder;
					part rotationEngineController : EncoderEngineWithSwitchSystemPLCModule;
					//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
					
					part verticalImpulseCounter : QuadratureDecoder;
					part verticalEngineController : EncoderEngineWithSwitchSystemPLCModule;
					
					part horizontalImpulseCounter : QuadratureDecoder;
					part horizontalEngineController : EncoderEngineWithSwitchSystemPLCModule;

					// port binding
					bind rotationImpulseCounter.decoderCount = rotationEngineController.decoderCount;
					bind verticalImpulseCounter.decoderCount = verticalEngineController.decoderCount;
					bind horizontalImpulseCounter.decoderCount = horizontalEngineController.decoderCount;


					port plcCmdPort:PLCCmdPort;

					comment about currentMoveCommandPickupPosition, currentMoveCommandDropPosition /* can both be replaced by a single attribute MoveCmdMsg ? */
					attribute currentMoveCommandPickupPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states	*/

						// can be replaced by a 
					}
					attribute currentMoveCommandDropPosition : GripperRelativePosition {
						doc /* target value for the move, used to remember the current command in several states	*/
					}

					attribute currentCommandTargetPosition : GripperRelativePosition {
						doc /* target value for the move, used by some states	
								this is a value used to simplify the code in the transitions, by putting all the info in the action
						*/
					}

					attribute isInitalized : Boolean := false {
						doc /* false until all EncoderEngineWithSwitchSystemPLCModule have been placed on the reference switch at least once and reset the associated counter */
					}
					attribute actionCommandDone : Boolean := false {
						doc /* used to know when an complex action command has been finished */
						comment /* maybe use a signal instead ?*/
					}

					attribute verticalOffset : Natural := 250 {
						doc /* value used by move, pick, place command to grip the token by placing the gripper above the token */
					}

					//attribute 

					// behavior
					state vgrSystemStates : VGRSystemPLCStates {
						in selfVGRSystemPLC = VGRSystemPLC::self;
					}
					state def VGRSystemPLCStates{
						in selfVGRSystemPLC : VGRSystemPLC;
						//in this : VGRSystemPLC;

						entry; then idle;
						state idle;
						
						state setup {
							entry selfVGRSystemPLC.setupEngines;
						}
						
						state move {
							entry  action assign selfVGRSystemPLC.actionCommandDone  := false;
							then prepare_move;
						
							state prepare_move {
							}

							state setup_before_move {
								entry selfVGRSystemPLC.setupEngines;
							}
							state move_step_1_retractArm {
								entry selfVGRSystemPLC.retractArm;
							}
							state move_step_2_rotateToPickupPos {
								entry selfVGRSystemPLC.rotateToPickupPos;
							}
							state move_step_3_gotoAbovePickupPos {
								entry selfVGRSystemPLC.gotoAbovePickupPos;
							}
							state move_step_4_gotoPickupPos{
								entry selfVGRSystemPLC.gotoPickupPos;
							}
							state move_step_5_activateGrip {
								entry selfVGRSystemPLC.activateGrip;
							}
							state move_step_6_raiseArm {
								entry selfVGRSystemPLC.raiseArm;
							}
							state move_step_7_retractArm {
								entry selfVGRSystemPLC.retractArm;
							}
							state move_step_8_rotateToDropPos {
								entry selfVGRSystemPLC.rotateToDropPos;
							}
							state move_step_9_gotoDropPos {
								entry selfVGRSystemPLC.gotoDropPos;
							}
							state move_step_10_releaseGrip {
								entry selfVGRSystemPLC.releaseGrip;
							}
							state move_step_11_raiseArm {
								entry selfVGRSystemPLC.raiseArm;
							}

							state move_done {
								entry action signalEndOfActioncommand {
									assign selfVGRSystemPLC.actionCommandDone  := true;
								}
							}
						transition 'prep_move_to_setup' 
							first prepare_move
							if not selfVGRSystemPLC.isInitalized
							do selfVGRSystemPLC.setupEngines
							then setup_before_move;
						
						transition 'setup_before_move_to_step_1'
							first setup_before_move
							if selfVGRSystemPLC.rotationEngineController.referenceSwitch == true 
								and selfVGRSystemPLC.verticalEngineController.referenceSwitch == true
								and selfVGRSystemPLC.horizontalEngineController.referenceSwitch == true
							do selfVGRSystemPLC.resetDecoderCounters
							then move_step_1_retractArm;

						transition 'prep_move_to_step1'
							first prepare_move
							if selfVGRSystemPLC.isInitalized
							then move_step_1_retractArm;

						transition 'move_step_1_to_move_step_2' 
							first move_step_1_retractArm
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_2_rotateToPickupPos;
						
						transition 'move_step_2_to_move_step_3' 
							first move_step_2_rotateToPickupPos
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_3_gotoAbovePickupPos;
						
						transition 'move_step_3_to_move_step_4' 
							first move_step_3_gotoAbovePickupPos
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_4_gotoPickupPos;
						transition 'move_step_4_to_move_step_5' 
							first move_step_4_gotoPickupPos
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_5_activateGrip;
						transition 'move_step_5_to_move_step_6' 
							first move_step_5_activateGrip			
							then move_step_6_raiseArm;
						transition 'move_step_6_to_move_step_7' 
							first move_step_6_raiseArm
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_7_retractArm;
						transition 'move_step_7_to_move_step_8' 
							first move_step_7_retractArm
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_8_rotateToDropPos;
						transition 'move_step_8_to_move_step_9' 
							first move_step_8_rotateToDropPos
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_9_gotoDropPos;
						transition 'move_step_9_to_move_step_10' 
							first move_step_9_gotoDropPos
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_step_10_releaseGrip;
						transition 'move_step_10_to_move_step_11' 
							first move_step_10_releaseGrip
							then move_step_11_raiseArm;
						transition 'move_step_11_to_move_done' 
							first move_step_11_raiseArm
							// if all controllers have reached their target
							if selfVGRSystemPLC.rotationEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.decoderCount.value == selfVGRSystemPLC.currentCommandTargetPosition.horizontalPos			
							then move_done;
						}					
						state stop {
							entry selfVGRSystemPLC.stopAll;
						}
						
						transition 'idle_to_setup'
			                    first idle
			                    accept setupCmdMsg:SetupCmdMsg via selfVGRSystemPLC.plcCmdPort
			                    then setup;
						transition 'move_to_setup'
			                    first move
			                    accept setupCmdMsg:SetupCmdMsg via selfVGRSystemPLC.plcCmdPort
			                    then setup;
						transition 'setup_to_idle'
			                    first setup
								if selfVGRSystemPLC.rotationEngineController.referenceSwitch == true 
									and selfVGRSystemPLC.verticalEngineController.referenceSwitch == true
									and selfVGRSystemPLC.horizontalEngineController.referenceSwitch == true
								do selfVGRSystemPLC.resetDecoderCounters
			                    then idle;
						
						transition 'idle_to_move'
			                    first idle
			                    accept msg:MoveCmdMsg via selfVGRSystemPLC.plcCmdPort
								do action storeMoveParams {
									action assign selfVGRSystemPLC.currentMoveCommandPickupPosition := msg.pickupPosition; 
									action assign selfVGRSystemPLC.currentMoveCommandDropPosition := msg.dropPosition; 
								} 
			                    then move;
						transition 'setup_to_move'
			                    first setup
			                    accept msg:MoveCmdMsg via selfVGRSystemPLC.plcCmdPort
								do action storeMoveParams {
									action assign selfVGRSystemPLC.currentMoveCommandPickupPosition := msg.pickupPosition; 
									action assign selfVGRSystemPLC.currentMoveCommandDropPosition := msg.dropPosition; 
								} 
			                    then move;
						transition 'move_to_idle'
			                    first move
								if selfVGRSystemPLC.actionCommandDone == true 
			                    then idle;

						transition 'idle_to_stop'
							first idle
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystemPLC.plcCmdPort
							then stop;
						
						transition 'setup_to_stop'
							first setup
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystemPLC.plcCmdPort
							then stop;
						transition 'move_to_stop'
							first move
			                accept stopCmdMsg:StopCmdMsg via selfVGRSystemPLC.plcCmdPort
							then stop;
						transition 'stop_to_idle'
							first stop
							then idle;
						
					}
					comment about retractArm, gotoAbovePickupPos, gotoPickupPos, activateGrip, releaseGrip 
						/* not sure about the rigth way to perform a "Call" on the port in sysml syntax 
						   just an assignment ? (on the port ? on the attribute(s) of the port ?)
						   a "send" ?
						*/
					perform action retractArm {
						send GotoposEngineCmdMsg(targetPos = 0) to horizontalEngineController;
					}
					perform action raiseArm {
						// TODO use previous value instead of pickuptarget
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.verticalPos - verticalOffset) to verticalEngineController;
					}
					perform action rotateToPickupPos {
						/* performs only the rotation */
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.rotationPos) to rotationEngineController;
						// for convenience 
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandPickupPosition.rotationPos,
							verticalPos = currentCommandTargetPosition.verticalPos, 		// unchanged
							horizontalPos = currentCommandTargetPosition.horizontalPos);	// unchanged					
					}
					perform action gotoAbovePickupPos {
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.rotationPos) to rotationEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.verticalPos - verticalOffset) to verticalEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.horizontalPos) to horizontalEngineController;
						// for convenience 
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandPickupPosition.rotationPos,
							verticalPos = currentMoveCommandPickupPosition.verticalPos - verticalOffset,
							horizontalPos = currentMoveCommandPickupPosition.horizontalPos);			
					}
					perform action gotoPickupPos {
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.rotationPos) to rotationEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.verticalPos) to verticalEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandPickupPosition.horizontalPos) to horizontalEngineController;
						// for convenience 
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandPickupPosition.rotationPos,
							verticalPos = currentMoveCommandPickupPosition.verticalPos, 
							horizontalPos = currentMoveCommandPickupPosition.horizontalPos);					
					}
					perform action activateGrip {
						assign compressorController.compressorActuator.value := true;
						assign valveController.valveActuator.value := true;
					}
					perform action releaseGrip {
						assign compressorController.compressorActuator.value := false;
						assign valveController.valveActuator.value := false;
					}
					perform action rotateToDropPos {
						/* performs only the rotation */
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.rotationPos) to rotationEngineController;
						// for convenience 	
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandDropPosition.rotationPos,
							verticalPos = currentCommandTargetPosition.verticalPos,			// unchanged
							horizontalPos = currentCommandTargetPosition.horizontalPos);	// unchanged				
					}
					perform action gotoAboveDropPos {
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.rotationPos) to rotationEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.verticalPos - verticalOffset) to verticalEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.horizontalPos) to horizontalEngineController;	
						// for convenience 					
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandDropPosition.rotationPos,
							verticalPos = currentMoveCommandDropPosition.verticalPos - verticalOffset, 
							horizontalPos = currentMoveCommandDropPosition.horizontalPos);
					}
					perform action gotoDropPos {
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.rotationPos) to rotationEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.verticalPos) to verticalEngineController;
						send GotoposEngineCmdMsg(targetPos = currentMoveCommandDropPosition.horizontalPos) to horizontalEngineController;	
						// for convenience 					
						assign currentCommandTargetPosition := 	GripperRelativePosition(
							rotationPos = currentMoveCommandDropPosition.rotationPos,
							verticalPos = currentMoveCommandDropPosition.verticalPos, 
							horizontalPos = currentMoveCommandDropPosition.horizontalPos);
					}
					action setupEngines {
						send SetupEngineCmdMsg() to rotationEngineController;
						send SetupEngineCmdMsg() to verticalEngineController;
						send SetupEngineCmdMsg() to horizontalEngineController;
					}
					action stopAll {
						action releaseGrip;
						send StopEngineCmdMsg() to rotationEngineController;
						send StopEngineCmdMsg() to verticalEngineController;
						send StopEngineCmdMsg() to horizontalEngineController;
					}
					action resetDecoderCounters  {
						action resetRotationImpulseCounter : QuadratureDecoder::Reset{ in selfDecoder = rotationImpulseCounter;}
						action resetHorizontalImpulseCounter : QuadratureDecoder::Reset{ in selfDecoder = horizontalImpulseCounter;}
						action resetVerticalImpulseCounter : QuadratureDecoder::Reset{ in selfDecoder = verticalImpulseCounter;}
						assign isInitalized :=  true;
					}
					
				}
				part def CompressorPLCModule {
					out port compressorActuator : ActuatorPort;
				}
				part def ValvePLCModule {
					out port valveActuator : ActuatorPort;
				}
				
				part def QuadratureDecoder {
					doc /*
						 * The quadrature decoder is in charge of decoding the impulse signals coming from a quadrature encoder  typically associated to a Motor
						 * It increments the counter when the motor is rotating forward, and decrement the counter when rotating backward
						 */
					in port signalA : SensorPort;
					in port signalB : SensorPort;
					
					out port decoderCount : CounterPort;
					

					action reset : Reset {in selfDecoder = QuadratureDecoder::self; }
					action def Reset {
						in selfDecoder : QuadratureDecoder;

						action assign selfDecoder.decoderCount := 0;
					}
					state decoderStates : DecoderCounterStates{ in selfDecoder = QuadratureDecoder::self;  }
					state def DecoderCounterStates {
						in selfDecoder : QuadratureDecoder;
						entry; then dd;
						state uu {
							doc /* 	signalA is Up and signalB is Up */
						}
						state ud {
							doc /* 	signalA is Up and signalB is Down */
						}
						state du {
							doc /* 	signalA is Down	and signalB is Up */
						}
						state dd {
							doc /* 	signalA is Down	and signalB is Down */
						}
						transition dd_To_ud 
							first dd
							if selfDecoder.signalA.value == true and  selfDecoder.signalB.value == false
							do selfDecoder.increment
							then ud; 
						transition ud_To_dd 
							first ud
							if selfDecoder.signalA.value == false and  selfDecoder.signalB.value == false
							do selfDecoder.decrement
							then dd; 
						
						transition dd_To_du 
							first dd
							if selfDecoder.signalA.value == false and  selfDecoder.signalB.value == true
							do selfDecoder.decrement 
							then du;
						transition ud_To_uu 
							first ud
							if selfDecoder.signalA.value == true and  selfDecoder.signalB.value == true
							do selfDecoder.increment
							then uu;
						transition du_To_dd 
							first du
							if selfDecoder.signalA.value == false and  selfDecoder.signalB.value == false
							do selfDecoder.increment
							then dd; 
						transition uu_To_ud 
							first uu
							if selfDecoder.signalA.value == true and  selfDecoder.signalB.value == false
							do selfDecoder.decrement 
							then ud;  
						transition uu_To_du 
							first uu
							if selfDecoder.signalA.value == false and  selfDecoder.signalB.value == true
							do selfDecoder.increment
							then du;
					}

					action increment {
						decoderCount := decoderCount + 1;
					}
					
					action decrement {
						decoderCount := decoderCount - 1;
					}
				}
				part def EncoderEngineWithSwitchSystemPLCModule {

					in port decoderCount : CounterPort;
					in port referenceSwitch : SensorPort;

					out port forwardActuator : ActuatorPort;
					out port backwardActuator : ActuatorPort;

					attribute tolerance : Natural := 20;
					
					attribute currentTargetDecoderValue : Positive {
						doc /* target value for the encoder, used by some states	*/
					}
					
					
					state engineStates : EngineStates { in selfEncoderEngineWithSwitch = EncoderEngineWithSwitchSystemPLCModule::self;  }	
					state def EngineStates {
						in selfEncoderEngineWithSwitch : EncoderEngineWithSwitchSystemPLCModule;
						
						entry; then idle;
						state idle {
							entry action stopEngine {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := false;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := false;
							}
						}
						
						state forward_until_ref {
							// at some point in the future / this is simulation if not connected to real ...
							//do assign selfEncoderEngineWithSwitch.referenceSwitch := true;
						}
						state forward_until_target_pos {
							// at some point in the future / this is simulation if not connected to real ...
							//do assign selfEncoderEngineWithSwitch.decoderCount.value := selfEncoderEngineWithSwitch.currentTargetDecoderValue; 
						}
						state backward_until_target_pos {
							// at some point in the future / this is simulation if not connected to real ...
							//do assign selfEncoderEngineWithSwitch.decoderCount.value := selfEncoderEngineWithSwitch.currentTargetDecoderValue; 
						}
						
						transition idle_to_forward_until_ref
							first idle
							accept SetupCmdMsg
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := false;
							}
							then forward_until_ref;
							
						transition idle_to_forward_until_target_pos
							first idle
							accept msg : GotoposEngineCmdMsg 
								if selfEncoderEngineWithSwitch.decoderCount.value <  msg.targetPos - selfEncoderEngineWithSwitch.tolerance
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := false;
								// at some point in the future / this is simulation if not connected to real ...
								//action assign selfEncoderEngineWithSwitch.currentTargetDecoderValue := msg.targetPos; 
							}
							then forward_until_target_pos;
						transition idle_to_backward_until_target_pos
							first idle
							accept msg : GotoposEngineCmdMsg 
								if selfEncoderEngineWithSwitch.decoderCount.value >  msg.targetPos + selfEncoderEngineWithSwitch.tolerance
							do action goBackward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := false;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := true;
								// at some point in the future / this is simulation if not connected to real ...
								//action assign selfEncoderEngineWithSwitch.currentTargetDecoderValue := msg.targetPos; 
							}
							then backward_until_target_pos;
						transition  forward_until_ref_to_idle
							first forward_until_ref
							accept when selfEncoderEngineWithSwitch.referenceSwitch.value == true
							then idle;
						transition  forward_until_ref_to_idle_on_stop
							first forward_until_ref
							accept msg : StopEngineCmdMsg
							then idle;
						transition  forward_until_target_pos_to_idle
							first forward_until_target_pos
							if selfEncoderEngineWithSwitch.decoderCount.value >= selfEncoderEngineWithSwitch.currentTargetDecoderValue - selfEncoderEngineWithSwitch.tolerance and
							   selfEncoderEngineWithSwitch.decoderCount.value <= selfEncoderEngineWithSwitch.currentTargetDecoderValue + selfEncoderEngineWithSwitch.tolerance 
							then idle;
						
						transition  forward_until_target_pos_to_idle_on_stop
							first forward_until_target_pos
							accept msg : StopEngineCmdMsg
							then idle;
						transition  backward_until_target_pos_to_idle
							first backward_until_target_pos
							if selfEncoderEngineWithSwitch.decoderCount.value >= selfEncoderEngineWithSwitch.currentTargetDecoderValue - selfEncoderEngineWithSwitch.tolerance and
							   selfEncoderEngineWithSwitch.decoderCount.value <= selfEncoderEngineWithSwitch.currentTargetDecoderValue + selfEncoderEngineWithSwitch.tolerance 
							then idle;
						
						transition  backward_until_target_pos_on_stop
							first backward_until_target_pos
							accept msg : StopEngineCmdMsg
							then idle;

						transition forward_until_target_pos_to_forward_until_ref
							first forward_until_target_pos
							accept SetupCmdMsg
							then forward_until_ref;
						transition backward_until_target_pos_to_forward_until_ref
							first backward_until_target_pos
							accept SetupCmdMsg
							then forward_until_ref;
						transition backward_until_target_pos_to_forward_until_target_pos
							first backward_until_target_pos
							accept msg : GotoposEngineCmdMsg 
								if selfEncoderEngineWithSwitch.decoderCount.value <  msg.targetPos - selfEncoderEngineWithSwitch.tolerance
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := false;
							}
							then forward_until_target_pos;
						transition forward_until_target_pos_to_backward_until_target_pos
							first forward_until_target_pos
							accept msg : GotoposEngineCmdMsg 
								if selfEncoderEngineWithSwitch.decoderCount.value >  msg.targetPos + selfEncoderEngineWithSwitch.tolerance
							do action goBackward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := false;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := true;
							}
							then backward_until_target_pos;
					}
					
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
				item def EngineCmdMsg{
	            }
				item def GotoposEngineCmdMsg :> EngineCmdMsg{
	            	attribute targetPos : Natural;
	            }	        
	            item def SetupEngineCmdMsg:>EngineCmdMsg{
	            }
	            item def StopEngineCmdMsg:>EngineCmdMsg{
	            }

		        attribute def GripperRelativePosition {
	            	comment /* 
	            	 * discussion: should we use a standard coordinate system such as ISQSpaceTime::universalCartesianSpatial3dCoordinateFrame
	            	 * or use robot relative coordinate (angles, etc) or encoder based (as sent in the current command  )
	            	 */
	            	 attribute rotationPos : Natural;
	            	 attribute verticalPos : Natural;
	            	 attribute horizontalPos : Natural;
	            }   
		    }
		    package PortDefinitions {
				import SignalDefinitions::*;
		    	port def GotoposEnginePort{
	                in item gotoposEngineCmdMsg:GotoposEngineCmdMsg;
	            }
	            

				port def PLCCmdPort{
					doc /* 
					* port between PLC and orchestrator/user application
					*/
                	in item plcCmdMsg:SignalDefinitions::CmdMsg;
            	}
	            
		    }
			package System {
				import Definitions::*;
				
				// part vgrSystemPLC : VGRSystemPLC {
				// 	part compressorController : CompressorPLCModule;
				// 	part valveController : ValvePLCModule;
				// 	part rotationImpulseCounter : TwoWaysImpulseCounter;
				// 	part rotationEngineController : EncoderEngineWithSwitchSystemPLCModule;
				// 	//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
					
				// 	part verticalImpulseCounter : TwoWaysImpulseCounter;
				// 	part verticalEngineController : EncoderEngineWithSwitchSystemPLCModule;
					
				// 	part horizontalImpulseCounter : TwoWaysImpulseCounter;
				// 	part horizontalEngineController : EncoderEngineWithSwitchSystemPLCModule;

				// 	// port binding
				// 	bind rotationImpulseCounter.encoderCount = rotationEngineController.encoder;
				// 	bind verticalImpulseCounter.encoderCount = verticalEngineController.encoder;
				// 	bind horizontalImpulseCounter.encoderCount = horizontalEngineController.encoder;
				// 	// todo missing binding: rotationImpulseCounter in port
				// }
			}
		}
		package HardwareDomain {
			package Definitions {
				import PortDefinitions::*;
				import REN_1V_01_VGRSystemPLCModel::LogicalLevel::PortDefinitions::*;
				part def VGRMachine {

					part compressor : Compressor;
					part valve : Valve;
					part rotationEngine : EncoderMotor;
					part verticalEngine : EncoderMotor;
					part horizonalEngine : EncoderMotor;
					part rotationReferenceSwitch : ElectricalSwitch;
					part verticalReferenceSwitch : ElectricalSwitch;
					part horizontalReferenceSwitch : ElectricalSwitch;
				}
				part def Compressor {
					in port compressorActuator : ActuatorPort;
				}
				part def Valve {
					in port valveActuator : ActuatorPort;}
				part def EncoderMotor { 
					in port forwardActuator : ActuatorPort;
					in port backwardActuator : ActuatorPort;
					out port incrementSignal : SensorPort;
					out port decrementSignal : SensorPort;
				}
				part def ElectricalSwitch {
					out port switchSensor : SensorPort;
				}
			}
			package SignalDefinitions {}
			package PortDefinitions {}
			package System {
				import Definitions::*;
				// part vgrSystem_vgrMachine : VGRMachine {
				// 	part compressor : Compressor;
				// 	part valve : Valve;
				// 	part rotationEngine : EncoderMotor;
				// 	part verticalEngine : EncoderMotor;
				// 	part horizonalEngine : EncoderMotor;
				// 	part rotationReferenceSwitch : ElectricalSwitch;
				// 	part verticalReferenceSwitch : ElectricalSwitch;
				// 	part horizontalReferenceSwitch : ElectricalSwitch;
				// }
			}
		}
		package ElectricalDomain {
			package Definitions {}
			package SignalDefinitions {}
			package PortDefinitions {}
			package System {}
		}
		package MechanicalDomain {
			package Definitions {}
			package SignalDefinitions {}
			package PortDefinitions {}
			package System {}
		}
		package Definitions {
			part def VGRSystem {}
		}
		package PortDefinitions {

			port def ActuatorPort {
				attribute value : Boolean;
			}
			port def SensorPort {
				attribute value : Boolean;
			}
			port def CounterPort {
				attribute value : Integer;
			}
		}
		package System {
			import Definitions::*;
			import HardwareDomain::Definitions::*;
			import SoftwareDomain::Definitions::*;
			part REN_1V_01_VGRSystem : VGRSystem { 

				part vgrMachine : VGRMachine {
				}
				part vgrPLC : VGRSystemPLC {
				} 

				// plc to machine binding
				bind vgrMachine.rotationEngine.incrementSignal = vgrPLC.rotationImpulseCounter.signalA;
				bind vgrMachine.rotationEngine.decrementSignal = vgrPLC.rotationImpulseCounter.signalB;
				bind vgrMachine.rotationEngine.forwardActuator = vgrPLC.rotationEngineController.forwardActuator;
				bind vgrMachine.rotationEngine.backwardActuator = vgrPLC.rotationEngineController.backwardActuator;
				bind vgrMachine.rotationReferenceSwitch.switchSensor = vgrPLC.rotationEngineController.referenceSwitch;

				bind vgrMachine.verticalEngine.incrementSignal = vgrPLC.verticalImpulseCounter.signalA;
				bind vgrMachine.verticalEngine.decrementSignal = vgrPLC.verticalImpulseCounter.signalB;
				bind vgrMachine.verticalEngine.forwardActuator = vgrPLC.verticalEngineController.forwardActuator;
				bind vgrMachine.verticalEngine.backwardActuator = vgrPLC.verticalEngineController.backwardActuator;
				bind vgrMachine.verticalReferenceSwitch.switchSensor = vgrPLC.verticalEngineController.referenceSwitch;
				
				bind vgrMachine.horizonalEngine.incrementSignal = vgrPLC.horizontalImpulseCounter.signalA;
				bind vgrMachine.horizonalEngine.decrementSignal = vgrPLC.horizontalImpulseCounter.signalB;
				bind vgrMachine.horizonalEngine.forwardActuator = vgrPLC.horizontalEngineController.forwardActuator;
				bind vgrMachine.horizonalEngine.backwardActuator = vgrPLC.horizontalEngineController.backwardActuator;
				bind vgrMachine.horizontalReferenceSwitch.switchSensor = vgrPLC.horizontalEngineController.referenceSwitch;
				
				bind vgrMachine.compressor.compressorActuator = vgrPLC.compressorController.compressorActuator;
				bind vgrMachine.valve.valveActuator = vgrPLC.valveController.valveActuator;
				
			}
		}
	}
}