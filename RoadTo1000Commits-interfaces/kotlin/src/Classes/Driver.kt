package Classes

import kotlin.concurrent.thread

class Driver : HumanInterfaced {
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