package todo.core

import io.vavr.collection.List as VList

typealias Reducer = (VList<Todo>, TodoEvent) -> VList<Todo>

fun addTodo(state: VList<Todo>, item: Todo) = state.prepend(item)

fun removeTodo(state: VList<Todo>, item: Todo) = state.filter { it != item }

fun replaceTodo(state: VList<Todo>, item: Todo) = state.map {
    when (it.id) {
        item.id -> item
        else -> it
    }
}

fun addEventReducer(s: VList<Todo>, event: TodoEvent): VList<Todo> =
        when (event) {
            is TodoEvent.Added -> addTodo(s, event.item)
            else -> s
        }

fun removeEventReducer(s: VList<Todo>, event: TodoEvent): VList<Todo> =
        when (event) {
            is TodoEvent.Removed -> removeTodo(s, event.item)
            else -> s
        }

fun changeEventReducer(s: VList<Todo>, event: TodoEvent): VList<Todo> =
        when (event) {
            is TodoEvent.Changed -> replaceTodo(s, event.newItem)
            else -> s
        }