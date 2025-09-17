package de.unistuttgart.isw.util;

public class InboundMessage {
    final private String msg;

    public InboundMessage(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }

    public String getTask(){
        return "DSL for Task";
    }

}
