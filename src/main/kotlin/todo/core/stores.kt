package todo.core

import io.vavr.control.Option


class EventStore(private val events: io.vavr.collection.List<RecordedEvent>) {
    fun add(event: TodoEvent) = EventStore(events.prepend(RecordedEvent(event, 1 + (events.peekOption().flatMap({ Option.of(it.id) }).orElse(Option.of(0))).get())))
    fun laterThan(id: Int) = EventStore(events.takeWhile({ e -> e.id > id }))
    fun <S> foldLeft(acc: S, f: (acc: S, e: RecordedEvent) -> S) = events.reverse().foldLeft(acc, f)
    fun peek() = events.head()
}

class StateStore(val state: TodoState, val reducers: List<Reducer>) {
    private fun dispatch(event: RecordedEvent): StateStore =
            StateStore(
                    TodoState(
                            event.id,
                            reducers.fold(state.todos, { s, f -> f(s, event.eventData) })
                    ),
                    reducers
            )
    fun processEvents(eventStore: EventStore) = eventStore
            .laterThan(state.version)
            .foldLeft(this, { s, e -> s.dispatch(e) })
}