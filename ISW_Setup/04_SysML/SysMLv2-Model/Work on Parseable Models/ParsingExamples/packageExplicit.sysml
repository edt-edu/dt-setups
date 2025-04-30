// This now works compared to actionWithPackageFail.sysml, since the the Integer is now explicitly referenced with the package name.
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
        attribute int123 : testPackage::Integer;
    }
    part def testPart {
        attribute colorTest : testPackage::Color;
    }
    part test3 {
        part t1Part : testPart {
            attribute color = testPackage::Color::RED;
        }
        part t2Part : testPart {
            attribute color = testPackage::Color::GREEN;
        }
    }
}