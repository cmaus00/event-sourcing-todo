package todo.helper

import kotlin.system.measureTimeMillis


fun timed(title: String, n:Int, f: () -> Unit) {
    println(title)

    println("warmup phase")
    f()
    f()

    val t_avg = (1..n).map {
        measureTimeMillis {
            print(".")
            f()
        }
    }.average()

    println("\navg(t, $n): $t_avg\n")
}