//This is an example of a parseable SysMLv2 model that contains an action def, that is not used. 
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

item def testItem {
    attribute int123 : Integer;
}
part def testPart {
    attribute colorTest : Color;
}
part test3 {
    part t1Part : testPart {
        attribute colorTest = Color::RED;
    }
    part t2Part : testPart {
        attribute color = Color::GREEN;
    }
    action def testAction {
        assign t1Part.colorTest := Color::RED;
    }
}
