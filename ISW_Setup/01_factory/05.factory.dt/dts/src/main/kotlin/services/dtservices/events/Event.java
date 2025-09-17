package services.dtservices.events;

import dtengine.eventsystem.abstractevents.DTEvent;

public class Event {
    // used by the cache to delete old events
    private final long timestamp;    
    private final DTEvent event;

    public Event(long timestamp, DTEvent event) {
        this.timestamp = timestamp;
        this.event = event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DTEvent getEvent() {
        return event;
    }
}
