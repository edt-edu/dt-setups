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
				attribute referencSwitch : Boolean;
				
				
				state engineStates : EngineStates { in selfEncoderEngineWithSwitch = EncoderEngineWithSwitch::self;  }	
				state def EngineStates {
					in selfEncoderEngineWithSwitch : EncoderEngineWithSwitch;
					
					entry; then iddle;
					state iddle;
					
					state forward_until_ref;
					state forward_until_target_pos;
					state backward_until_target_pos;
					
					transition iddle_to_forward_until_ref
						first iddle
						accept SetupCmdMsg
						do action incrementCounter {
							action assign selfEncoderEngineWithSwitch.forwardActuator := true;
							
							action assign selfEncoderEngineWithSwitch.referencSwitch := true; // simulated physic at some point in the future ...
						}
						then forward_until_ref;
						
					transition iddle_to_forward_until_target_pos
						first iddle
						accept msg : GotoposEngineCmdMsg via selfEncoderEngineWithSwitch.gotoposCmdPort
							if selfEncoderEngineWithSwitch.encoderValue <  msg.targetPos
						do action incrementCounter {
							action assign selfEncoderEngineWithSwitch.forwardActuator := true;
							
							action assign selfEncoderEngineWithSwitch.encoderValue := selfEncoderEngineWithSwitch.encoderValue +1; // simulated physic
						}
						then forward_until_target_pos;
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