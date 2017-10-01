package todo

import io.vavr.kotlin.list
import todo.core.*
import todo.helper.events


internal data class EventsAndState(val events: EventStore, val state: StateStore)

fun processEventsSynchronous(numEvents: Int) {
    val s = EventsAndState(
            EventStore(list()),
            StateStore(
                    TodoState(0, list()),
                    listOf(::addEventReducer)
            )
    )
    val endState = events(numEvents).fold(s) { eventsAndState, event ->
        val (events, state) = eventsAndState
        val nextEvents = events.add(event)
        EventsAndState(nextEvents, state.processEvents(nextEvents))
    }
    if (endState.state.state.version != numEvents) {
        throw Exception("not all events incorporated")
    }
    if (endState.events.peek().id != numEvents) {
        throw Exception("not all events recorded")
    }
}