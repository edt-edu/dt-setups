package REN_1V_01_VGRSystemModel {
	
	package ConceptualLevel {
	
		package SystemStructure {
			
			package PartDefinitions{
				import PortDefinitions::*;
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
					
					// ports
					// -----
					
					// perform actions
					
					perform action provideLogService;
					perform action performTokenMove : MoveToken;
					
					// exhibit states
					
	               // port powerSwitchCmdPort:PowerSwitchCmdPort;
	                //port performTokenMoveCmdPort:MoveTokenCmdPort; // ie. the user of the factory has a way to ask for a TokenMove
	                //port stopMoveCmdPort:StopMoveCmdPort;			// ie. the user of the factory has a way to ask for a StopMove
					//perform action performTokenMove;
					//perform action performEmptyMove;
					//perform action control; 
					//perform action acquireData;
 				} 
				
				part def Controller;
				
				part def GripperRobot; 
				
				// TODO discuss the position coordinate at this level of abstraction: 
				// relative to machine (ie. rot + x + y) 
				// or relative to the factory
				// or as expected by the UI
				// or should remain abstract at this level
				//part def TokenPosition;  
				
				action def MoveToken {
					in moveCmdParam : SignalDefinitions::MoveCmdMsg;
					//in dropPosition : TokenPosition;
				}
				
			}
			package PortDefinitions{
				port def PowerSwitchCmdPort;
				port def MoveTokenCmdPort;
				port def StopMoveCmdPort;
			}
			package SignalDefinitions{
	            item def CmdMsg{
	            }
	            item def MoveCmdMsg:>CmdMsg{
	            //    attribute ignitionOnOff:IgnitionOnOff;
	            	attribute pickupPosition : GripperRelativePosition;
	            	attribute dropPosition : GripperRelativePosition;
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
		}	
		
	}	
}