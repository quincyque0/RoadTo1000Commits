package Classes

import Interfaces.Movable
import kotlin.random.Random
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.concurrent.thread
import java.util.Timer


open class HumanInterfaced : Movable{
    override var x: Double = 0.0
    override var y: Double = 0.0
    var name: String = ""
    override var currentSpeed: Int = 1
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

    override fun moveTo(targetX: Double, targetY: Double) {
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