package todo.helper

import todo.core.Todo
import todo.core.TodoEvent

fun events(numEvents: Int): Sequence<TodoEvent> {
    var i = 0
    return generateSequence(
            fun(): TodoEvent? {
                if (i < numEvents) {
                    i += 1
                    return TodoEvent.Added(Todo(i, "event $i", false))
                }
                return null
            }
    )
}