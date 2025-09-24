import kotlin.random.Random
import kotlin.math.sqrt
import kotlin.math.pow
import java.util.Timer
import kotlin.concurrent.thread

open class Human {
    var x: Double = 0.0
    var y: Double = 0.0
    var name: String = ""
    var currentSpeed: Int = 1
    private var movementTimer: Timer? = null
     var movementThread: Thread? = null
     var isMoving = false

    constructor(_name: String, _startX: Double, _startY: Double, _speed: Int) {
        name = _name
        x = _startX
        y = _startY
        currentSpeed = _speed
        println("Created human: $name at position (${"%.2f".format(x)}, ${"%.2f".format(y)})")
    }

    open fun move() {
        val targetX = x + (Random.nextDouble() * 100 - 50)
        val targetY = y + (Random.nextDouble() * 100 - 50)
        moveTo(targetX, targetY)
    }

    fun moveTo(targetX: Double, targetY: Double) {
        stopMovement()

        val startX = x
        val startY = y
        val distance = calculateDistance(startX, startY, targetX, targetY)
        val totalTime = (distance / currentSpeed * 1000).toLong()

        println("$name starts moving from (${"%.2f".format(startX)}, ${"%.2f".format(startY)}) to (${"%.2f".format(targetX)}, ${"%.2f".format(targetY)})")
        println("Distance: ${"%.2f".format(distance)} units, Time: ${totalTime/1000.0} seconds")

        isMoving = true
        val startTime = System.currentTimeMillis()
        var updateCount = 0

        movementThread = thread {
            while (isMoving) {
                try {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = minOf(elapsed.toDouble() / totalTime, 1.0)

                    x = startX + (targetX - startX) * progress
                    y = startY + (targetY - startY) * progress

                    if (updateCount % 10 == 0) {
                        println("$name moving... Position: (${"%.2f".format(x)}, ${"%.2f".format(y)}), Progress: ${(progress * 100).toInt()}%")
                    }
                    updateCount++

                    if (progress >= 1.0) {
                        println("$name arrived at destination!")
                        isMoving = false
                        break
                    }

                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    fun continuousRandomMovement() {
        stopMovement()
        isMoving = true

        movementThread = thread {
            while (isMoving) {
                try {
                    if (Random.nextDouble() < 0.3) {
                        move()
                    }
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    break
                }
            }
            println("$name movement thread finished")
        }
    }

    fun stopMovement() {
        isMoving = false
        movementThread?.interrupt()
        movementThread = null
        movementTimer?.cancel()
        movementTimer = null
        println("$name stopped movement")
    }

    private fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}

class Driver : Human {
    var carModel: String = ""
    var direction: String = ""

    constructor(
        _name: String,
        _startX: Double,
        _startY: Double,
        _speed: Int,
        _carModel: String,
        _direction: String
    ) : super(_name, _startX, _startY, _speed) {
        carModel = _carModel
        direction = _direction
        println("Created driver: $name driving $carModel")
    }

    override fun move() {
        var targetX = x
        var targetY = y

        when (direction.lowercase()) {
            "up" -> targetY -= 100.0
            "down" -> targetY += 100.0
            "left" -> targetX -= 100.0
            "right" -> targetX += 100.0
            else -> targetX += 100.0
        }

        moveTo(targetX, targetY)


        direction = getOppositeDirection(direction)
    }

    private fun getOppositeDirection(dir: String): String {
        return when (dir.lowercase()) {
            "up" -> "down"
            "down" -> "up"
            "left" -> "right"
            "right" -> "left"
            else -> "right"
        }
    }

    fun driveStraightLine() {
        stopMovement()
        isMoving = true

        movementThread = thread {
            while (isMoving) {
                try {
                    move()
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {

                    break
                }
            }
            println("$name driving thread finished")
        }
    }
}

fun main() {
    val humans = listOf(
        Human("Alice", 0.0, 0.0, 5),
        Human("Bob", 10.0, 10.0, 7),
        Human("Charlie", 20.0, 20.0, 4),
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
    Thread.sleep(10000)

    println("\n=== Stopping all movements ===")
    humans.forEach {
        println("Stopping ${it.name}")
        it.stopMovement()
    }

    threads.forEach {
        try {
            it.join(2000)
            if (it.isAlive) {
                println("Thread is still alive, interrupting...")
                it.interrupt()
            }
        } catch (e: Exception) {
            println("Error joining thread: ${e.message}")
        }
    }

    println("\n=== Final positions ===")
    humans.forEach { human ->
        println("${human.name} final position: (${"%.2f".format(human.x)}, ${"%.2f".format(human.y)})")
    }

    println("Simulation completed successfully!")
}