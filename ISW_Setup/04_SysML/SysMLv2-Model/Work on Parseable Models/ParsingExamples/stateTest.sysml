//This runs into is parseable, but runs into an error while pretty printing.
package testPackage1 {
    attribute def Integer {}
    enum def Color {
        RED;
        GREEN;
        BLUE;
    }
    enum def Speed {
        SLOW;
        FAST;
    }

    item def testItem {}
    part def testPart {}
    part test3 {
        part t1Part : testPart {
            attribute colorTest = Color::RED;
        }
        part t2Part : testPart {
            attribute color = Color::GREEN;
        }
        action def TestAction {
            assign t1Part.colorTest := Color::RED;
        }
        state entryState {}
        state def testState {
            entry;
            then state1;
            state state1 {
                entry action test;
            }
            state state2 {}
            transition state1_to_state2 first state1 then state2;
        }
    }
}
