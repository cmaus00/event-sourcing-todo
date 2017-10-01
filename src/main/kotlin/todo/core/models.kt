package todo.core

import io.vavr.collection.List

data class Todo(val id: Int, val content: String, val done: Boolean)
data class TodoState(val version: Int, val todos: List<Todo>)

sealed class TodoEvent {
    data class Added(val item: Todo) : TodoEvent()
    data class Removed(val item: Todo) : TodoEvent()
    data class Changed(val oldItem: Todo, val newItem: Todo) : TodoEvent()
}

class RecordedEvent(val eventData: TodoEvent, val id: Int)