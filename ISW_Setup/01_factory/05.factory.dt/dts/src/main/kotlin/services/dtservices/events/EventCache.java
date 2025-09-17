package services.dtservices.events;

import dtengine.eventsystem.abstractevents.DTEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventCache<EventType extends DTEvent>  {
    private Map<String, List<Event>> cache = new HashMap<>();
    // ms
    private final int maxCacheTime;

    public EventCache(int maxCacheTime) {
        this.maxCacheTime = maxCacheTime; 
    }

    public void cleanUp() {
        long currentTime = System.currentTimeMillis();
        // iterate over all events and remove the ones that are older than maxCacheTime
        cache.forEach((k, v) -> {
            v.removeIf(e -> currentTime - e.getTimestamp() > maxCacheTime);
        });
    }

    /**
     * adds an event to the cache
     */
    public void addEvent(final String name, DTEvent event){
        List<Event> events = cache.get(name);
        if (events == null) {
            events = new LinkedList<>();
            cache.put(name, events);
        }
        events.add(new Event(System.currentTimeMillis(), event));
    }

    /**
     * Get the last count events for the given name that are not older than maxAge
     */
    public List<DTEvent> getEvent(String name, int count, int maxAge){
        List<DTEvent> events = new LinkedList<>();
        List<Event> eventList = cache.get(name);
        if (eventList == null) {
            return events;
        }
        long currentTime = System.currentTimeMillis();
        for (Event event : eventList) {
            if (events.size() >= count) {
                break;
            }
            if (currentTime - event.getTimestamp() < maxAge) {
                events.add(event.getEvent());
            }
        }
        return events;
    }
}
