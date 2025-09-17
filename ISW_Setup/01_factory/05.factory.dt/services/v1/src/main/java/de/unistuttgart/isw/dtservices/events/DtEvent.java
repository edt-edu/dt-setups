package de.unistuttgart.isw.dtservices.events;

public class DtEvent {

    private String name;
    private String queueName;
    private String replyTo;
    private String correlationId;
    // serialised as json
    private String data;

    public DtEvent(String name, String queueName, String data, String replyTo, String correlationId) {
        this.name = name;
        this.queueName = queueName;
        this.data = data;
        this.replyTo = replyTo;
        this.correlationId = correlationId;
    }

    // used to deserialize the event
    protected DtEvent() {
    }

    public String getName() {
        return name;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getData() {
        return data;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getCorrelationId() {
        return correlationId;
    }
    
}
