import Classes.HumanInterfaced
import Classes.Driver
import kotlin.concurrent.thread

fun main() {
    val humans = listOf(
        HumanInterfaced("Alice", 0.0, 0.0, 5),
        HumanInterfaced("Bob", 10.0, 10.0, 7),
        HumanInterfaced("Charlie", 20.0, 20.0, 4),
        Driver("John Driver", 30.0, 30.0, 11, "Toyota", "right")
    )

    println("\n Starting parallel movement ")

    val threads = mutableListOf<Thread>()

    humans.forEach { human ->
        val thread = thread {
            when (human) {
                is Driver -> {
                    human.driveStraightLine()
                }
                else -> {
                    human.continuousRandomMovement()
                }
            }
        }
        threads.add(thread)
    }

    println("Simulation will run for 10 seconds")
    Thread.sleep(30000)

    println("\n=== Stopping all movements ===")
    humans.forEach {
        println("Stopping ${it.name}")
        it.stopMovement()
    }

//    threads.forEach {
//        try {
//            it.join(2000)
//            if (it.isAlive) {
//                println("Thread is still alive, interrupting...")
//                it.interrupt()
//            }
//        } catch (e: Exception) {
//            println("Error joining thread: ${e.message}")
//        }
//    }

    println("\n=== Final positions ===")
    humans.forEach { human ->
        println("${human.name} final position: (${"%.2f".format(human.x)}, ${"%.2f".format(human.y)})")
    }

    println("Simulation completed successfully!")
}