package REN_1V_01_VGRSystemPLCModel {
	
	import ScalarValues::*;
	package LogicalLevel {
		package SoftwareDomain {
			package Definitions {
				import SignalDefinitions::*;
				import PortDefinitions::*;
				part def VGRSystemPLC {
					state vgrSystemStates : VGRSystemPLCStates {
						
					}
					state def VGRSystemPLCStates{
						entry; then idle;
						state idle;
						
						state setup;
						
						state move_step_1;
						state move_step_2;
						state move_step_3;
						state move_step_4;
											
						state stop;
						
						transition 'setupCmd'
			                    first idle
			                    accept setupCmdMsg:SetupCmdMsg // via actionCmdPort
			                    then setup;
					}
				}
				part def CompressorPLCModule {
					
				}
				part def ValvePLCModule {
					
				}
				part def EncoderEngineWithSwitchSystemPLCModule {
					
					port gotoposCmdPort:GotoposEnginePort;
					
					comment about forwardActuator, backwardActuator, encoderValue, referenceSwitch 
					/* In this version, we consider EncoderEngineWithSwitch as a system (ie. we do not clearly identify controller vs machine ), 
					 * we do not have details of the actuator and sensors; 
					 * so they are modeled as attributes instead of port
					 */
					
					// out port forwardActuator : ActuatorOutPort;
					// out port backwardActuator : ActuatorOutPort;
					attribute forwardActuator : Boolean;
					attribute backwardActuator : Boolean;
					
					attribute encoderValue : Positive;
					attribute referenceSwitch : Boolean;
					
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
							do assign selfEncoderEngineWithSwitch.encoderValue := selfEncoderEngineWithSwitch.currentTargetEncoderValue; 
						}
						state backward_until_target_pos {
							do assign selfEncoderEngineWithSwitch.encoderValue := selfEncoderEngineWithSwitch.currentTargetEncoderValue; 
						}
						
						transition iddle_to_forward_until_ref
							first idle
							accept SetupCmdMsg
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator := false;
							}
							then forward_until_ref;
							
						transition iddle_to_forward_until_target_pos
							first idle
							accept msg : GotoposEngineCmdMsg via selfEncoderEngineWithSwitch.gotoposCmdPort
								if selfEncoderEngineWithSwitch.encoderValue <  msg.targetPos
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator := true;
								action assign selfEncoderEngineWithSwitch.backwardActuator := false;
								action assign selfEncoderEngineWithSwitch.currentTargetEncoderValue := msg.targetPos; 
							}
							then forward_until_target_pos;
						transition iddle_to_backward_until_target_pos
							first idle
							accept msg : GotoposEngineCmdMsg via selfEncoderEngineWithSwitch.gotoposCmdPort
								if selfEncoderEngineWithSwitch.encoderValue >  msg.targetPos
							do action goForward {
								action assign selfEncoderEngineWithSwitch.forwardActuator := false;
								action assign selfEncoderEngineWithSwitch.backwardActuator := true;
								action assign selfEncoderEngineWithSwitch.currentTargetEncoderValue := msg.targetPos; 
							}
							then forward_until_target_pos;
						transition  forward_until_ref_to_idle
							first forward_until_ref
							accept when selfEncoderEngineWithSwitch.referenceSwitch == true
							then idle;
						transition  forward_until_target_pos_to_idle
							first forward_until_ref
							if selfEncoderEngineWithSwitch.encoderValue == selfEncoderEngineWithSwitch.currentTargetEncoderValue
							then idle;
						transition  backward_until_target_pos_to_idle
							first backward_until_target_pos
							if selfEncoderEngineWithSwitch.encoderValue == selfEncoderEngineWithSwitch.currentTargetEncoderValue
							then idle;
						
						transition  backward_until_target_pos_stop
							first forward_until_ref
							if selfEncoderEngineWithSwitch.encoderValue == selfEncoderEngineWithSwitch.currentTargetEncoderValue
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
	            	attribute targetPos : Positive;
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
	            	 attribute rotationPos : Positive;
	            	 attribute verticalPos : Positive;
	            	 attribute horizontalPos : Positive;
	            }   
		    }
		    package PortDefinitions {
				import SignalDefinitions::*;
		    	port def GotoposEnginePort{
	                in item gotoposEngineCmdMsg:GotoposEngineCmdMsg;
	            }
	            
	            //port def ActuatorOutPort {
	            //	attribute value : Boolean;
	            //}
		    }
			package System {
				import Definitions::*;
				
				part REN_1V_01_VGRSystemPLC : VGRSystemPLC {
					part compressorController : CompressorPLCModule;
					part valveController : ValvePLCModule;
					part rotationEngineController : EncoderEngineWithSwitchSystemPLCModule;
					//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
					part verticalEngineController : EncoderEngineWithSwitchSystemPLCModule;
					part horizontalEngineController : EncoderEngineWithSwitchSystemPLCModule;
				}
			}
		}
		package HardwareDomain {
			package Definitions {
				part def VGRMachine {}
				part def Compressor {}
				part def Valve {}
				part def EncoderEngine {}
				part def ElectricalSwitch {}
			}
			package SignalDefinitions {}
			package PortDefinitions {}
			package System {
				import Definitions::*;
				part REN_1V_01_VGRSystem_vgrMachine : VGRMachine {
					part compressor : Compressor;
					part valve : Valve;
					part rotationEngine : EncoderEngine;
					part verticalEngine : EncoderEngine;
					part horizonalEngine : EncoderEngine;
					part rotationReferenceSwitch : ElectricalSwitch;
					part verticalReferenceSwitch : ElectricalSwitch;
					part horizontalReferenceSwitch : ElectricalSwitch;
				}
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
		package System {
			import Definitions::*;
			import HardwareDomain::Definitions::*;
			import SoftwareDomain::Definitions::*;
			part REN_1V_01_VGRSystem : VGRSystem { // todo consider direct reuse of REN_1V_01_VGRSystem_vgrMachine and REN_1V_01_VGRSystemPLC
				part vgrMachine : VGRMachine {
					part compressor : Compressor;
					part valve : Valve;
					part rotationEngine : EncoderEngine;
					part verticalEngine : EncoderEngine;
					part horizonalEngine : EncoderEngine;
					part rotationReferenceSwitch : ElectricalSwitch;
					part verticalReferenceSwitch : ElectricalSwitch;
					part horizontalReferenceSwitch : ElectricalSwitch;
				}
				part vgrPLC : VGRSystemPLC {
					part compressorController : CompressorPLCModule;
					part valveController : ValvePLCModule;
					part rotationEngineController : EncoderEngineWithSwitchSystemPLCModule;
					//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
					part verticalEngineController : EncoderEngineWithSwitchSystemPLCModule;
					part horizontalEngineController : EncoderEngineWithSwitchSystemPLCModule;
				} 
			}
		}
	}
}