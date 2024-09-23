package REN_1V_01_VGRSystemModel {
	
	import ScalarValues::*;
	package ConceptualLevel {
	
		package SystemStructure {
			
			package PartDefinitions{
				import PortDefinitions::*;
				import SignalDefinitions::*;
				part REN_1V_01_VGRSystem : VGRSystem {
					part controller : Controller;
					part robot : GripperRobot;
				}
				
				
				// TODO discuss how to model interaction at the abtraction level:
				// simple actions
				// or action mapped to port (cf. asynchronous messaging through ports)
				
				part def VGRSystem {
					
					// attributes
					// ----------
					//attribute gripperPosition:>ISQSpaceTime::universalCartesianSpatial3dCoordinateFrame;
					attribute gripperPosition : SignalDefinitions::GripperStatus;
					
					attribute currentCommandKind : AttributeDefinitions::CommandKind;
					attribute currentCommandFinished : Boolean;
					attribute currentCommandConditionReached : Boolean;
					
					
					// ports
					// -----
					port actionCmdPort:ActionCmdPort;
					
					// perform actions
					// ---------------
					perform action provideLogService;
					perform action applyCurrentCmdTarget : ApplyCurrentCmdTarget;
					perform action changeCurrentCommandToMove {
						assign currentCommandKind :=  AttributeDefinitions::CommandKind::Move;
					} 
					//perform action performTokenMove : MoveToken;
					// perform action performStopAction : StopAction;
					
					// exhibit states
					// --------------
					exhibit state controllerStates  {
	                    entry action initial; 
		                state stopped {
		                }
		                state in_action {
		                	entry;
		                	then
		                	state applyingCurrentCmdTarget {
		                		state ff;	
		                	}
		                	// force evaluation of the state/allow interruption 
		                	transition 'continue'
		                		first applyingCurrentCmdTarget
		                		accept when not VGRSystem::currentCommandFinished
		                		do applyCurrentCmdTarget
		                		then applyingCurrentCmdTarget;
		                	transition 'finished'
		                		first applyingCurrentCmdTarget
		                		accept when VGRSystem::currentCommandFinished
		                		then done;
		                }
		                transition initial then stopped;
		                transition 'stopped-in_action'
		                    first stopped
		                    accept moveCmdMsg:MoveCmdMsg via actionCmdPort
		                    then in_action;
		                transition 'stopped-stopped'
		                    first stopped
		                    accept stopCmdMsg:StopCmdMsg via actionCmdPort
		                    then stopped;
		                transition 'in_action-interrupted-stopped'
		                    first in_action
		                    accept stopCmdMsg:StopCmdMsg via actionCmdPort
		                    then stopped;
		                
		                transition 'in_action-finished-stopped'
		                    first in_action
		                    accept when currentCommandFinished
		                    then stopped;
		                transition 'in_action-in_action'
		                    first in_action
		                    accept moveCmdMsg:MoveCmdMsg via actionCmdPort
		                    then in_action;
                    }
                
 				} 
				
				part def Controller {
					// attributes
					
				}
				 
				part def GripperRobot; 
				
				// TODO discuss the position coordinate at this level of abstraction: 
				// relative to machine (ie. rot + x + y) 
				// or relative to the factory
				// or as expected by the UI
				// or should remain abstract at this level
				//part def TokenPosition;  
				
				action def ApplyCurrentCmdTarget {
					
				}
				
				/*action def MoveToken {
					in moveCmdParam : SignalDefinitions::MoveCmdMsg;
					//in dropPosition : TokenPosition;
				}
				action def StopAction {
					in stopCmdParam : SignalDefinitions::StopCmdMsg;
				}*/
			}
			package PortDefinitions{
				port def PowerSwitchCmdPort;
				port def ActionCmdPort{
                	in item actionCmdMsg:SignalDefinitions::CmdMsg;
            	}
			}
			package SignalDefinitions{
	            item def CmdMsg{
	            }
	            item def MoveCmdMsg:>CmdMsg{
	            	attribute pickupPosition : GripperRelativePosition;
	            	attribute dropPosition : GripperRelativePosition;
	            }
	            
	            item def StopCmdMsg:>CmdMsg{
	            }
	            item def EngineStatus;
	            
	            attribute def GripperRelativePosition {
	            	comment /* 
	            	 * discussion: should we use a standard coordinate system such as ISQSpaceTime::universalCartesianSpatial3dCoordinateFrame
	            	 * or use robot relative coordinate (angles, etc) or encoder based (as sent in the current command  )
	            	 */
	            }
	            attribute def GripperStatus :> GripperRelativePosition {
	            	doc /* Position and status (succion active/inactive) of the gripper
	            	    */
	            }
	          
	        }
	        package AttributeDefinitions{
	        	enum def CommandKind {
	        		Move;
	        		Pick;
	        		Place;
	        	}
	        }
		}
	}	
}