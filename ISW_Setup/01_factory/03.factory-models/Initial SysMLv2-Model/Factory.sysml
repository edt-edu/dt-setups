package Factory {
    private import ScalarValues::*;
    package HardwarePackage {
        part def Hardware;
    }
    package SoftwarePackage {
        part def API {
            item stateAPI : StateTypes::State;
            attribute name : String;
            in port cmdAPIPort;
            in port itemPort;
            out port stateAPIPort;
        }
    } 
    private import HardwarePackage::*;
    private import SoftwarePackage::*;
    part def System { 
        part hardware : Hardware [0..*];
        part software : API [0..*];
    }
    part def Subsystem :> System {
        part subsystems : System [0..*];
    }
    package MachineSystems {
        part def FactorySystem :> System {
            in port factoryCmd {
                in item factoryCmdItem;
            }
            port factoryState {
                out item factoryStateItem;
            }
        }
    }
}
