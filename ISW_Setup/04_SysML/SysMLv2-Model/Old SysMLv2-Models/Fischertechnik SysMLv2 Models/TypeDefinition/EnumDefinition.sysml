package ActionEnumerationDefinition {
enum def SortingLineActionDef {
			MOVE_TO_EJECTORS {
				doc /* Moves the current package to the middle light barrier. */
			}
			SORT {
				doc /* Sorts the package waiting at the middle light barrier in the storage container specified by `"color"`. */
			}
			RESOLVE_FAILURE {
				doc /* Manually resolves critical failures. Calling this action needs to ensure that the issue is resolved */
			}
		}
		enum def IndexedLineActionDef {
			START_MILL {
				doc /* Activates the mill for two seconds if a package is available at the milling gate. */
			}
			START_DRILL {
				doc /* Activates the drill for two seconds if a package is available at the drilling gate. */
			}
			TRANSFER {
				doc /*  Transfers the package from start to finish specified in `"transferFromTo"`. The machine only acts if no  package is at the target or for `"MILL_TO_DRILL"` and `"DRILL_TO_END"` if a package is at the starting point.
				 */
			}
			STOP_MILL;
			STOP_DRILL;
		}
		enum def IndexedLineTransferActionDef {
			FEED_TO_MILL;
			MILL_TO_DRILL;
			DRILL_TO_END;
		}
		enum def WarehouseActionDef {
			RETRIEVE_EMPTY_PALLET_FOR_NEW_ITEM;
			RETRIEVE;
			STORE;
			IDLE;
		}
		enum def ConveyorBeltActionDef {
			STOP_MOVING;
			MOVE_FORWARD;
			MOVE_BACKWARD;
		}
		enum def Actuator1DActionDef {
			MOVE_FORWARD;
			MOVE_BACKWARD;
			TARGET;
			STOP;
			RESET;
		}
		enum def ImpulseActuatorActionDef {
			MOVE_FORWARD;
			MOVE_BACKWARD_FOR_RESET;
		}
		enum def VerticalActionDef  {
			MOVE_DOWN;
			MOVE_UP;
			TARGET;
			STOP;
			RESET;
		}
		enum def ArmActionDef {
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
			TARGET;
			STOP;
			RESET;
		}
		enum def RotorActionDef {
			ROTATE_LEFT;
			ROTATE_RIGHT;
			TARGET;
			STOP;
			RESET;
		}
		enum def ArmImpulseActionDef  {
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
		}
		enum def ClawImpulseActionDef {
			CLOSE_CLAW;
			OPEN_CLAW;
		}
		enum def MillActionDef {
			MILL;
			STOP_MILLING;
		}
		enum def DrillActionDef {
			DRILL;
			STOP_DRILLING;
		}
		enum def VacuumGripperActionDef {
			ROTATE_LEFT;
			ROTATE_RIGHT;
			ROTATE_TARGET;
			ROTATE_STOP;
			ROTATE_RESET;
			MOVE_ARM_OUT;
			MOVE_ARM_IN;
			ARM_TARGET;
			ARM_STOP;
			ARM_RESET;
			VERTICAL_DOWN;
			VERTICAL_UP;
			VERTICAL_TARGET;
			VERTICAL_STOP;
			VERTICAL_RESET;
		}
		enum def ClawGripperActionDef {
			VERTICAL_DOWN;
			VERTICAL_UP;
			VERTICAL_TARGET;
			VERTICAL_STOP;
			VERTICAL_RESET;
			ROTATE_LEFT;
			ROTATE_RIGHT;
			ROTATE_TARGET;
			ROTATE_STOP;
			ROTATE_RESET;
			ARM_OUT;
			ARM_IN;
			CLAW_CLOSE;
			CLAW_OPEN;
			ARM_TO;
			ARM_STOP;
			CLAW_TO;
			CLAW_RESET;
		}
		enum def ConveyorSliderActionDef {
			EXTEND;
			RETRACT;
			STOP;
		}
		enum def VacuumModuleActionDef {
			VALVE_OPEN;
			VALVE_CLOSED;
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
		}
		enum def ValveActionDef {
			VALVE_OPEN;
			VALVE_CLOSE;
		}
		enum def CompressorActionDef {
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
		}
		enum def ConveyorBeltSimpleActionDef {
			STOP_MOVING;
			START_MOVING;
		}
		enum def HorizontalActionDef {
			MOVE_LEFT;
			MOVE_RIGHT;
			TARGET;
			STOP;
			RESET;
		}
		enum def CantileverActionDef {
			MOVE_FORWARD;
			MOVE_BACKWARD;
			TARGET;
			STOP;
			RESET;
		}
}
package StateEnumerationDefinitionDef {
		enum def MovementState {
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def Actuator1DStateDef  {
			doc /* Typical status report. */
			MOVING_FORWARD;
			MOVING_BACKWARD;
			TARGETING;
			STOPPED;
			RESETTING;
			AT_BACKWARD_LIMIT;
		}
		enum def CantileverMotorStateDef {
			MOVING_FORWARD;
			MOVING_BACKWARD;
			TARGETING;
			STOPPED;
			RESETTING;
			AT_BACKWARD_LIMIT;
			AT_FORWARD_LIMIT;
		}
		enum def ConveyorBeltStateDef {
			MOVING_RIGHT;
			MOVING_LEFT;
			TARGETING;
			STOPPED;
			RESETTING;
		}
		enum def VerticalStateDef {
			MOVING_UP;
			MOVING_DOWN;
			STOPPED;
			TARGETING;
			RESETTING;
			AT_TOP_LIMIT_SWITCH;
			}
		enum def ImpulseArmStateDef {
			IS_READY;
			RESETTING;
			MOVING_OUT;
		}
		enum def RotorStateDef {
			ROTATING_RIGHT;
			ROTATING_LEFT;
			STOPPED;
			TARGETING;
			RESETTING;
		}
		enum def WarehouseStateDef {
			IDLE;
			STORING;
			RETRIEVE_EMPTY_PALLET_GET_PALLET_FROM_STORAGE;
			RETRIEVE_EMPTY_PALLET_HAS_PALLET_FROM_STORAGE;
			RETRIEVE_EMPTY_PALLET_DROP_AT_LOADING_POSITION;
			RETRIEVING_PICKUP_PALLET_FROM_STORAGE;
			RETRIEVING_DROPPING_OFF_PALLET_AT_LOADING_POSITION;
			ACTION_FAILED;
			MOVING_TO_START_POSITION;
			AT_START_POSITION;
			AT_LOADING_POSITION;
			EMPTY_PALLET_AT_INPUT;
			RETRIEVING_ITEM;
			RESOLVING_FAILURE;
		}
		enum def Warehouse1x3StateDef{
			IDLE;
			MOVING_TO_PICKUP_PALLET;
			PICKING_UP_PALLET;
			DROPPING_DOWN_PALLET;
			MOVING_TO_LOADING_POSITION;
			LOADING_FILLED_WITH_EMPTY_PALLET;
			MOVE_TO_STORING_POSITION;
			EMPTY_PALLET_AT_INPUT;
			A_EMPTY_B_EMPTY_C_EMPTY;
			A_FULL_B_EMPTY_C_EMPTY;
			A_EMPTY_B_FULL_C_EMPTY;
			A_EMPTY_B_EMPTY_C_FULL;
			A_FULL_B_FULL_C_EMPTY;
			A_FULL_B_EMPTY_C_FULL;
			A_EMPTY_B_FULL_C_FULL;
			A_FULL_B_FULL_C_FULL;
			ERROR;
		}
		enum def SortingLineStateDef {
			READY {
				doc /* No package available. Ready to receive package */
			}
			MOVING_TO_EJECTORS {
				doc /* Currently moving package to ejector light barrier. */
			}
			PACKAGE_WAITING_AT_EJECTORS {
				doc /* Package waiting at ejector light barrier. Ready to sort package. */
			}
			SORTING {
				doc /*  Sorts the package waiting at the middle light barrier in a storage container. */
			}
			CRITICAL_FAILURE_WHILE_SORTING {
				doc /* Occurs when an ejector misfires and timeouts. This state can only be exited by calling `"resolve failure"`. There is no internal machine resolve for this. Therefore calling this action needs to ensure that the issue is resolved otherwise (e.g. via vacuum_gripper). */
			}
		}
		enum def ReasonStateDef {
			CMD {
				doc /* The state corresponds to a command or the initial reset. */
			}
			TARGET_REACHED {
				doc /* A targeting operation succeded. State is `stopped`.
				  The `target` is given. */
			}
			POSITION_LIMIT {
				doc /* The previous action was stopped because of a position limit. */
			}
		}
		enum def ImpulseClawStateDef {
			CLOSING;
			IS_READY;
			RESETTING;
		}
		enum def MillStateDef {
			IS_MILLING;
			IS_NOT_MILLING;
		}
		enum def ClawGripperStateDef {
			VERTICAL_MOVING_UP;
			VERTICAL_TARGETING;
			VERTICAL_STOPPING;
			VERTICAL_RESETTING;
			ROTATING_LEFT;
			ROTATING_RIGHT;
			ROTATING_TARGETING;
			ROTATING_STOP;
			ROTATING_RESETTING;
			VERTICAL_MOVING_DOWN;
			ARM_MOVING_OUT;
			ARM_MOVING_IN;
			CLAW_OPENING;
			CLAW_CLOSING;
			ARM_MOVING_TO;
			ARM_STOPPING;
			CLAW_STOPPING;
			CLAW_RESETTING;
		}
		enum def ConveyorSliderStateDef {
			doc /* add doc here */
			EXTENDING;
			RETRACTING;
			STOPPING;
		}
		enum def DrillStateDef {
			IS_DRILLING;
			IS_MILLING;
		}
		enum def IndexedLineStateDef {
			READY;
			MILLING;
			DRILLING;
			TRANSFERRING;
		}
		enum def ImpulseActuatorStateDef {
			MOVING_FORWARD;
			MOVING_BACKWARD_FOR_RESET;
			IS_READY;
		}
		enum def VacuumModuleStateDef {
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
			VALVE_OPEN;
			VALVE_CLOSED;
		}
		enum def ArmStateDef {
			MOVING_ARM_OUT;
			MOVING_ARM_IN;
			TARGETTING;
			STOPPING;
			RESETTING;
		}
		enum def VacuumGripperStateDef {
			ROTATING_LEFT;
			ROTATING_RIGHT;
			ROTATING_TARGETING;
			ROTATING_STOPPING;
			ROTATING_RESETTING;
			ARM_MOVING_OUT;
			ARM_MOVING_IN;
			ARM_TARGETTING;
			ARM_STOPPING;
			ARM_RESETTING;
			VERTICAL_MOVING_DOWN;
			VERTICAL_MOVING_UP;
			VERTICAL_TARGETING;
			VERTICAL_STOPPING;
			VERTICAL_RESETTING;
			COMPRESSOR_ON;
			VALVE_OPEN;
			COMPRESSOR_OFF;
			VALVE_CLOSED;
		}
		enum def MachineLifecycleDef {
			ONLINE;
			ALREADY_CREATED;
			DESTROYED;
			FAILURE;
			UNKNOWN_TYPE;
			DESTROY_ABORTED;
		}
		enum def ValveStateDef {
			VALVE_OPEN;
			VALVE_CLOSED;
		}
		enum def CompressorStateDef {
			COMPRESSOR_ON;
			COMPRESSOR_OFF;
		}
		enum def ConveyorBeltSimpleStateDef {
			MOVING;
			STOPPED;
		}
		enum def HorizontalStateDef {
			MOVING_LEFT;
			MOVING_RIGHT;
			TARGETTING;
			STOPPING;
			RESETTING;
			AT_RIGHT_LIMIT_SWITCH;
		}
	}
package Locations{
	enum def IndexedLineLocationDef {
		FEED_TO_MILL;
		MILL_TO_DRILL;
		DRILL_TO_END;
	}
}
