package todo

import kotlinx.coroutines.experimental.*
import todo.asynchronous.*
import todo.helper.timed

val c1 = newSingleThreadContext("c1")
val c2 = newSingleThreadContext("c2")
val c3 = newSingleThreadContext("c3")
val c4 = newSingleThreadContext("c4")
val c5 = newSingleThreadContext("c5")
val c_all = newFixedThreadPoolContext(5, "all")

fun main(args: Array<String>) {
    val n = Regex("n=(\\d+)").find(args.joinToString(" "))?.groups?.get(1)?.value?.toInt() ?: 1
    val numEvents = Regex("e=(\\d+)").find(args.joinToString(" "))?.groups?.get(1)?.value?.toInt() ?: 100
    val callBacks = mapOf(
            "a1" to {
                timed("CommonPool", n) {
                    processEventsWithActors(numEvents, CommonPool, CommonPool, CommonPool, CommonPool, CommonPool)
                }
            },
            "a2" to {
                timed("separate single threads", n) {
                    processEventsWithActors(numEvents, c1, c2, c3, c4, c5)
                }
            },

            "a3" to {
                timed("one single thread", n) {
                    processEventsWithActors(numEvents, c1, c1, c1, c1, c1)
                }
            },

            "a4" to {
                timed("one thread pool with size 5", n) {
                    processEventsWithActors(numEvents, c_all, c_all, c_all, c_all, c_all)
                }
            },

            "a5" to {
                timed("three contexts: producer, eventstore and statestore", n) {
                    processEventsWithActors(numEvents, c1, c2, c2, c3, c3)
                }
            },

            "s" to {
                timed("synchronous", n) {
                    processEventsSynchronous(numEvents)
                }
            }
    )

    args.forEach { callBacks[it]?.invoke() }
}