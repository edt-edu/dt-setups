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
					part rotationImpulseCounter : TwoWaysImpulseCounter;
					part rotationEngineController : EncoderEngineWithSwitchSystemPLCModule;
					//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
					
					part verticalImpulseCounter : TwoWaysImpulseCounter;
					part verticalEngineController : EncoderEngineWithSwitchSystemPLCModule;
					
					part horizontalImpulseCounter : TwoWaysImpulseCounter;
					part horizontalEngineController : EncoderEngineWithSwitchSystemPLCModule;

					// port binding
					bind rotationImpulseCounter.encoderCount = rotationEngineController.encoder;
					bind verticalImpulseCounter.encoderCount = verticalEngineController.encoder;
					bind horizontalImpulseCounter.encoderCount = horizontalEngineController.encoder;


					port plcCmdPort:PLCCmdPort;

					attribute currentMoveCommandPickupPosition : GripperRelativePosition {
						doc /* target value for the move, used by some states	*/
					}
					attribute currentMoveCommandDropPosition : GripperRelativePosition {
						doc /* target value for the move, used by some states	*/
					}

					// behavior
					state vgrSystemStates : VGRSystemPLCStates {
						in selfVGRSystemPLC = VGRSystemPLC::self;
					}
					state def VGRSystemPLCStates{
						in selfVGRSystemPLC : VGRSystemPLC;

						entry; then idle;
						state idle;
						
						state setup;
						
						state setup_before_move {
						}
						state move_step_1 {
							do selfVGRSystemPLC.gotoPickupPos;
						}
						state move_step_2;
						state move_step_3;
						state move_step_4;
											
						state stop;
						
						transition 'setupCmd'
			                    first idle
			                    accept setupCmdMsg:SetupCmdMsg via plcCmdPort
			                    then setup;
						transition 'idle_to_move_on_Cmd'
			                    first idle
			                    accept msg:MoveCmdMsg via plcCmdPort
								do action storeMoveParams {
									action assign selfVGRSystemPLC.currentMoveCommandPickupPosition := msg.pickupPosition; 
									action assign selfVGRSystemPLC.currentMoveCommandDropPosition := msg.dropPosition; 
								} 
			                    then move_step_1;
						transition 'move_step_1_to_move_step_2' 
							first move_step_1
							// if all controllers have reached their target
							// todo: store current target in each controller for simpler code with multiple steps
							if selfVGRSystemPLC.rotationEngineController.encoder.value == currentMoveCommandPickupPosition.rotationPos
								and selfVGRSystemPLC.verticalEngineController.encoder.value == currentMoveCommandPickupPosition.verticalPos
								and selfVGRSystemPLC.horizontalEngineController.encoder.value == currentMoveCommandPickupPosition.horizontalPos							
							then move_step_2;
					}

					perform action gotoPickupPos {
						assign rotationEngineController.gotoposCmdPort :=  currentMoveCommandPickupPosition.rotationPos;
						assign verticalEngineController.gotoposCmdPort :=  currentMoveCommandPickupPosition.verticalPos;
						assign horizontalEngineController.gotoposCmdPort :=  currentMoveCommandPickupPosition.horizontalPos;						
					}
				}
				part def CompressorPLCModule {
					out port compressorActuator : ActuatorPort;
				}
				part def ValvePLCModule {
					out port valveActuator : ActuatorPort;
				}
				
				part def TwoWaysImpulseCounter {
					
					in port incrementSignal : SensorPort;
					in port decrementSignal : SensorPort;
					
					out port encoderCount : EncoderPort;
					
					action def Reset {
						action assign encoderCount := 0;
					}
					action reset : Reset;
					state encoderCounterStates : EncoderCounterStates{ in selfTwoWaysImpulseCounter = TwoWaysImpulseCounter::self;  }
					state def EncoderCounterStates {
						in selfTwoWaysImpulseCounter : TwoWaysImpulseCounter;
						entry; then dd;
						state uu {
							doc /* 	incrementSignal is Up
									and decrementSignal isUp
	            	    		*/
						}
						state ud {
							doc /* 	incrementSignal is Up
									and decrementSignal is Down
	            	    		*/}
						state du {
							doc /* 	incrementSignal is Down
									and decrementSignal is Up
	            	    		*/}
						state dd {
							doc /* 	incrementSignal is Down
									and decrementSignal is Down
	            	    		*/}
						transition dd_To_ud 
							first dd
							if selfTwoWaysImpulseCounter.incrementSignal.value == true
							do assign selfTwoWaysImpulseCounter.encoderCount.value := selfTwoWaysImpulseCounter.encoderCount.value +1 
							then ud; 
						transition ud_To_dd 
							first ud
							if selfTwoWaysImpulseCounter.incrementSignal.value == false
							then dd; 
						
						transition dd_To_du 
							first dd
							if selfTwoWaysImpulseCounter.decrementSignal.value == true
							do assign selfTwoWaysImpulseCounter.encoderCount.value := selfTwoWaysImpulseCounter.encoderCount.value -1 
							then du;
						transition ud_To_uu 
							first ud
							if selfTwoWaysImpulseCounter.decrementSignal.value == true
							do assign selfTwoWaysImpulseCounter.encoderCount := selfTwoWaysImpulseCounter.encoderCount.value -1
							then uu;
						transition du_To_dd 
							first du
							if selfTwoWaysImpulseCounter.decrementSignal.value == false
							then dd; 
						transition uu_To_ud 
							first uu
							if selfTwoWaysImpulseCounter.decrementSignal.value == false
							then ud;  
						transition uu_To_du 
							first uu
							if selfTwoWaysImpulseCounter.incrementSignal.value == false
							then du;
					}
				}
				part def EncoderEngineWithSwitchSystemPLCModule {
					
					port gotoposCmdPort:GotoposEnginePort;

					
					in port encoder : EncoderPort;
					in port referenceSwitch : SensorPort;

					out port forwardActuator : ActuatorPort;
					out port backwardActuator : ActuatorPort;
					
					
					attribute currentTargetEncoderValue : Positive {
						doc /* target value for the encoder, used by some states	*/
					}
					
					
					state engineStates : EngineStates { in selfEncoderEngineWithSwitch = EncoderEngineWithSwitchSystemPLCModule::self;  }	
					state def EngineStates {
						in selfEncoderEngineWithSwitch : EncoderEngineWithSwitchSystemPLCModule;
						
						entry; then idle;
						state idle;
						
						state forward_until_ref {
							do assign selfEncoderEngineWithSwitch.referenceSwitch := true;
						}
						state forward_until_target_pos {
							do assign selfEncoderEngineWithSwitch.encoder.value := selfEncoderEngineWithSwitch.currentTargetEncoderValue; 
						}
						state backward_until_target_pos {
							do assign selfEncoderEngineWithSwitch.encoder.value := selfEncoderEngineWithSwitch.currentTargetEncoderValue; 
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
							accept msg : GotoposEngineCmdMsg via selfEncoderEngineWithSwitch.gotoposCmdPort
								if selfEncoderEngineWithSwitch.encoder.value <  msg.targetPos
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := false;
								action assign selfEncoderEngineWithSwitch.currentTargetEncoderValue := msg.targetPos; 
							}
							then forward_until_target_pos;
						transition idle_to_backward_until_target_pos
							first idle
							accept msg : GotoposEngineCmdMsg via selfEncoderEngineWithSwitch.gotoposCmdPort
								if selfEncoderEngineWithSwitch.encoder.value >  msg.targetPos
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator.value := false;
								action assign selfEncoderEngineWithSwitch.backwardActuator.value := true;
								action assign selfEncoderEngineWithSwitch.currentTargetEncoderValue := msg.targetPos; 
							}
							then forward_until_target_pos;
						transition  forward_until_ref_to_idle
							first forward_until_ref
							accept when selfEncoderEngineWithSwitch.referenceSwitch.value == true
							then idle;
						transition  forward_until_target_pos_to_idle
							first forward_until_ref
							if selfEncoderEngineWithSwitch.encoder.value == selfEncoderEngineWithSwitch.currentTargetEncoderValue
							then idle;
						transition  backward_until_target_pos_to_idle
							first backward_until_target_pos
							if selfEncoderEngineWithSwitch.encoder.value == selfEncoderEngineWithSwitch.currentTargetEncoderValue
							then idle;
						
						transition  backward_until_target_pos_stop
							first forward_until_ref
							if selfEncoderEngineWithSwitch.encoder.value == selfEncoderEngineWithSwitch.currentTargetEncoderValue
							then idle;
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
	            item def GotoposEngineCmdMsg {
	            	attribute targetPos : Natural;
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
			port def EncoderPort {
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
				bind vgrMachine.rotationEngine.incrementSignal = vgrPLC.rotationImpulseCounter.incrementSignal;
				bind vgrMachine.rotationEngine.decrementSignal = vgrPLC.rotationImpulseCounter.decrementSignal;
				bind vgrMachine.rotationEngine.forwardActuator = vgrPLC.rotationEngineController.forwardActuator;
				bind vgrMachine.rotationEngine.backwardActuator = vgrPLC.rotationEngineController.backwardActuator;
				bind vgrMachine.rotationReferenceSwitch.switchSensor = vgrPLC.rotationEngineController.referenceSwitch;

				bind vgrMachine.verticalEngine.incrementSignal = vgrPLC.verticalImpulseCounter.incrementSignal;
				bind vgrMachine.verticalEngine.decrementSignal = vgrPLC.verticalImpulseCounter.decrementSignal;
				bind vgrMachine.verticalEngine.forwardActuator = vgrPLC.verticalEngineController.forwardActuator;
				bind vgrMachine.verticalEngine.backwardActuator = vgrPLC.verticalEngineController.backwardActuator;
				bind vgrMachine.verticalReferenceSwitch.switchSensor = vgrPLC.verticalEngineController.referenceSwitch;
				
				bind vgrMachine.horizonalEngine.incrementSignal = vgrPLC.horizontalImpulseCounter.incrementSignal;
				bind vgrMachine.horizonalEngine.decrementSignal = vgrPLC.horizontalImpulseCounter.decrementSignal;
				bind vgrMachine.horizonalEngine.forwardActuator = vgrPLC.horizontalEngineController.forwardActuator;
				bind vgrMachine.horizonalEngine.backwardActuator = vgrPLC.horizontalEngineController.backwardActuator;
				bind vgrMachine.horizontalReferenceSwitch.switchSensor = vgrPLC.horizontalEngineController.referenceSwitch;
				
				bind vgrMachine.compressor.compressorActuator = vgrPLC.compressorController.compressorActuator;
				bind vgrMachine.valve.valveActuator = vgrPLC.valveController.valveActuator;
				
			}
		}
	}
}