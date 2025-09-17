package de.unistuttgart.isw.dtservices.events;

public class Event {
    // used by the cache to delete old events
    private final long timestamp;    
    private final DtEvent event;

    public Event(long timestamp, DtEvent event) {
        this.timestamp = timestamp;
        this.event = event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DtEvent getEvent() {
        return event;
    }
}
