package todo.asynchronous

import io.vavr.kotlin.list
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import todo.core.*
import todo.helper.events
import kotlin.coroutines.experimental.CoroutineContext

inline suspend fun <T, R> ReceiveChannel<T>.fold(initial: R, operation: (acc: R, T) -> R): R {
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element)
    return accumulator
}

private suspend fun processEventCommand(cmd: EventCommand, store: EventStore, broadcast: SendChannel<EventNotifications>): EventStore {
    return when (cmd) {
        is EventCommand.Add -> {
            val newStore = store.add(cmd.event)
            broadcast.send(EventNotifications.NewEvent(newStore))
            newStore
        }
        is EventCommand.GetStore -> {
            cmd.response.complete(store)
            store
        }
    }
}

private suspend fun processStateCommand(cmd: StateCommand, store: StateStore): StateStore {
    return when (cmd) {
        is StateCommand.Update -> store.processEvents(cmd.events)
        is StateCommand.GetState -> {
            cmd.response.complete(store.state)
            store
        }
    }
}

fun eventActor(initState: EventStore, broadcast: SendChannel<EventNotifications>, context: CoroutineContext) = actor<EventCommand>(context) {
    channel.fold(initState, { acc, cmd -> processEventCommand(cmd, acc, broadcast) })
}

fun stateActor(store: StateStore, context: CoroutineContext) = actor<StateCommand>(context) {
    channel.fold(store, { acc, cmd -> processStateCommand(cmd, acc) })
}

sealed class StateCommand {
    class Update(val events: EventStore) : StateCommand()
    class GetState(val response: CompletableDeferred<TodoState>) : StateCommand()
}

sealed class EventNotifications {
    class NewEvent(val events: EventStore) : EventNotifications()
}

sealed class EventCommand {
    class Add(val event: TodoEvent) : EventCommand()
    class GetStore(val response: CompletableDeferred<EventStore>) : EventCommand()
}

fun processEventsWithActors(numEvents: Int,
                            producerContext: CoroutineContext,
                            eventConnectingContext: CoroutineContext,
                            eventContext: CoroutineContext,
                            stateContext: CoroutineContext,
                            stateConnectingContext: CoroutineContext) {

    val broadcast = BroadcastChannel<EventNotifications>(Channel.CONFLATED)
    val eventStoreActor = eventActor(EventStore(list()), broadcast, eventContext)
    val stateStoreActor = stateActor(
            StateStore(
                    TodoState(0, list()),
                    listOf(::addEventReducer)
            ),
            stateContext
    )

    val producer = produce(producerContext) {
        events(numEvents).forEach {
            send(it)
        }
    }


    launch(eventConnectingContext) {
        for (ev in producer) {
            eventStoreActor.send(EventCommand.Add(ev))
        }
    }

    val stateStoreJob = launch(stateConnectingContext) {
        val openSubscription = broadcast.openSubscription()
        openSubscription.consumeEach {
            when (it) {
                is EventNotifications.NewEvent -> {
                    stateStoreActor.send(StateCommand.Update(it.events))

                    if (it.events.peek().id == numEvents) {
                        return@launch
                    }
                }
            }
        }
    }


    runBlocking {
        stateStoreJob.join()
        stateStoreActor.close()
        eventStoreActor.close()
    }
}
