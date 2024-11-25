package REN_1V_01_VGRSystemModel {
	
	import ScalarValues::*;
	package LogicalLevel {
		package Definitions {
			import SignalDefinitions::*;
			import PortDefinitions::*;
			part def VGRSystem {
				state vgrSystemStates : VGRSystemStates {
					
				}
				state def VGRSystemStates{
					entry; then iddle;
					state iddle;
					
					state setup;
					
					state move_step_1;
					state move_step_2;
					state move_step_3;
					state move_step_4;
										
					state stop;
					
					transition 'setupCmd'
		                    first iddle
		                    accept setupCmdMsg:SetupCmdMsg // via actionCmdPort
		                    then setup;
				}
			}
			part def Compressor {
				
			}
			part def Valve {
				
			}
			part def EncoderEngineWithSwitch {
				
				port gotoposCmdPort:GotoposEnginePort;
				
				out port forwardActuator : ActuatorOutPort;
				out port backwardActuator : ActuatorOutPort;
				
				attribute encoderValue : Positive;
				attribute referenceSwitch : Boolean;
				
				attribute currentTargetPos : Positive;
				
				
				state engineStates : EngineStates { in selfEncoderEngineWithSwitch = EncoderEngineWithSwitch::self;  }	
				state def EngineStates {
					in selfEncoderEngineWithSwitch : EncoderEngineWithSwitch;
					
					entry; then idle;
					state idle;
					
					state forward_until_ref {
						do assign selfEncoderEngineWithSwitch.referenceSwitch := true;
					}
					state forward_until_target_pos {
						do assign selfEncoderEngineWithSwitch.encoderValue := selfEncoderEngineWithSwitch.currentTargetPos; 
					}
					state backward_until_target_pos {
						do assign selfEncoderEngineWithSwitch.encoderValue := selfEncoderEngineWithSwitch.currentTargetPos; 
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
							action assign selfEncoderEngineWithSwitch.currentTargetPos := msg.targetPos; 
						}
						then forward_until_target_pos;
					transition  forward_until_ref_to_idle
						first forward_until_ref
						accept when selfEncoderEngineWithSwitch.referenceSwitch == true
						then idle;
					transition  forward_until_target_pos_to_idle
						first forward_until_ref
						if selfEncoderEngineWithSwitch.encoderValue == selfEncoderEngineWithSwitch.currentTargetPos
						then idle;
					transition  backward_until_target_pos_to_idle
						first forward_until_ref
						if selfEncoderEngineWithSwitch.encoderValue == selfEncoderEngineWithSwitch.currentTargetPos
						then idle;
				}
				
			}
			//part def PLC {
			//}
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
            
            port def ActuatorOutPort {
            	attribute value : Boolean;
            }
	    }
		package SystemModel {
			import Definitions::*;
			
			part REN_1V_01_VGRSystem : VGRSystem {
				part compressor : Compressor;
				part valve : Valve;
				part rotationEngine : EncoderEngineWithSwitch;
				//state rotationengineSTates : EncoderEngineWithSwitch::EngineStates { in selfEncoderEngineWithSwitch = rotationEngine; }	
				part verticalEngine : EncoderEngineWithSwitch;
				part horizontalEngine : EncoderEngineWithSwitch;
				// TODO PLC ?
				//part controller : PLC;
			}
		}
	}
	
}