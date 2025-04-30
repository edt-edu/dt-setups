private import ScalarValues::*;
//* This is an Example of how the following PDDL-Action can be represented in SysMLv2:
PDDL-Domain:
(define (domain highbay-warehouse)
  (:requirements :strips :typing :fluents :numeric-fluents)
  (:types forklift location item slot height)
  
  (:predicates 
    (at ?f - forklift ?l - location)                    ; Forklift's ground location.
  )
  ;; Action: Move the forklift between locations.
  (:action move
    :parameters (?f - forklift ?startLocation ?targetLocation - location)
    :precondition (at ?f ?startLocation) ; Nur das muss im Ausgang erfuellt sein 
    :effect (and 
              (not (at ?f ?from))
              (at ?f ?to)) : Post condition
  )
PDDL-Problem:
(define (problem warehouse-refined)
  (:domain highbay-warehouse)
  (:objects 
    forklift1 - forklift
    loc1 - location
    itemA itemB itemC - item
    slot1 slot2 slot3 - slot
    height1 height2 height3 - height
  )
   ;; Forklift initial position and height.
    (at forklift1 loc1)
    (forklift-at-height forklift1 height1) //not relevant for the curent problem
*/

attribute def Location { 
    attribute name : String; // if the location should be handled as a string
    attribute atPosition : Real; // if the location should be handled as a real number
}
part def Forklift { //maps on (at ?f - forklift ?l - location) 
    attribute currentLocation : Location; // currentLocation as a mapping to the predicate (at ?f ?l)
}
attribute startLocation : Location { // Represents concrete Object in the PDDL-Problem
    name = "Start";}
attribute targetLocation : Location { // Represents concrete Object in the PDDL-Problem
    name = "Target";}

part forklift1 : Forklift { //Represents concrete Object "forklift1" in the PDDL-Problem
    currentLocation = startLocation;
}

action def Move{ 
    in forklift : Forklift; //maps to :parameters (?f - forklift, ?startLocation(from) und ?targetLocation(to) - location)
    in startLocation : Location; // would not be necessary but is defined in the PDDL
    in targetLocation : Location;
    assign forklift.currentLocation := targetLocation;
}

state def warehouseStateDef {
    in forklift : Forklift;
    entry;
    then startLocation;
    state startLocation{}
    state moving{
        entry action move{
            perform action move : Move{} // Syntax aus der SysMLv2 Dokumentation geht nicht: action focus : Focus (in scene, out image);
        }
    }
    state targetLocation{}
    transition first startLocation if(forklift.currentLocation == startLocation) then moving; // this represents the precondition (at ?f ?from) that needs to hold true in order to perform the action ;do action resembles an effect action which is performed when the transition usage is triggered
    transition first moving if(forklift.currentLocation != startLocation and forklift.currentLocation == targetLocation) then targetLocation; // this represents the postcondition ((and (not (at ?f ?l)) (at ?f ?to)):Postcondition) that needs to hold true after the action has been performed
}

