package dts.dtengine.eventsystem.abstractevents

import dtengine.eventsystem.abstractevents.DTEvent
import java.util.concurrent.LinkedBlockingQueue

class DTEventQueue {
    val queue = LinkedBlockingQueue<DTEvent>()

    // Add an event to the queue
    fun enqueue(event: DTEvent) {
        queue.offer(event)
    }

    fun getAllEvents(): MutableList<DTEvent> {
        var out :MutableList<DTEvent> = mutableListOf()
        while (!queue.isEmpty())
            out.add(queue.take())
        return out
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

}