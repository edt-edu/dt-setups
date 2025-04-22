//Example of probems with package structure. Imports do not work as expected. This Fails to parse in line 17, since Integer cant be referenced.
package testPackage {
    attribute def Integer {}
    enum def Color {
        RED;
        GREEN;
        BLUE;
    }
    enum def Speed{
        SLOW;
        FAST;
    }
}
private import testPackage::*;
package partPackage {
    item def testItem {
        attribute int123 : Integer;
    }
    part def testPart {
        attribute colorTest : Color;
    }
    part test3 {
        part t1Part : testPart {
            attribute color = Color::RED;
        }
        part t2Part : testPart {
            attribute color = Color::GREEN;
        }
    }
}