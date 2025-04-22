package PortDefs {
    port def machineActionPort{
        in item machineAction : CmdTypes ::Cmd;
    }
    port def machineStatePort{
        out item machineState :StateTypes::State;
    }
}