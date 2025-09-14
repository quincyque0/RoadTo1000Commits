import kotlin.random.Random
import kotlin.math.sqrt
import kotlin.math.pow
import java.util.Timer
import java.util.TimerTask

class Human {
    var x: Double = 0.0
    var y: Double = 0.0
    var name: String = ""
    var subname: String = ""
    var surname: String = ""
    var age: Int = -1
    var curspeed: Int = -1
    private var timer: Timer? = null

    constructor(_name: String, _subname: String, _surname: String, _age: Int, _curspeed: Int) {
        name = _name
        subname = _subname
        surname = _surname
        age = _age
        curspeed = _curspeed
        println("We created class with name $name")
    }

    fun move() {
        println("Human is moving from current position ($x, $y)")
    }

    fun stopMovement() {
        timer?.cancel()
        timer = null
        println("$name stopped movement")
    }

    fun moveto(_toX: Double, _toY: Double) {
        stopMovement()

        val startX = x
        val startY = y
        val totalDistance = calculateDistance(startX, startY, _toX, _toY)
        val totalTime = (totalDistance / curspeed * 1000).toLong()

        println("$name starts moving from (${"%.2f".format(startX)}, ${"%.2f".format(startY)}) to (${"%.2f".format(_toX)}, ${"%.2f".format(_toY)})")
        println("Distance: ${"%.2f".format(totalDistance)} units, Time: ${totalTime/1000.0} seconds")

        timer = Timer()
        val startTime = System.currentTimeMillis()

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = minOf(elapsed.toDouble() / totalTime, 1.0)

                x = startX + (_toX - startX) * progress
                y = startY + (_toY - startY) * progress

                val currentDistance = calculateDistance(startX, startY, x, y)
                println("${name} moving... Position: (${"%.2f".format(x)}, ${"%.2f".format(y)}), Progress: ${(progress * 100).toInt()}%")

                if (progress >= 1.0) {
                    println("${name} arrived at destination!")
                    cancel()
                }
            }
        }, 0, 100)
    }

    fun randomMove() {
        val targetX = x + (Random.nextDouble() * 100 - 50)
        val targetY = y + (Random.nextDouble() * 100 - 50)
        moveto(targetX, targetY)
    }

    fun continuousRandomMovement() {
        stopMovement()

        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (Random.nextDouble() < 0.3) {
                    val targetX = x + (Random.nextDouble() * 50 - 25)
                    val targetY = y + (Random.nextDouble() * 50 - 25)
                    stopMovement()
                    moveto(targetX, targetY)
                }
            }
        }, 0, 2000)
    }

    private fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}

fun main() {
    val humans = arrayOf(
        Human("Alice", "Marie", "Johnson", 25, 5),
        Human("Bob", "James", "Smith", 30, 7),
        Human("Charlie", "David", "Brown", 22, 4),
        Human("Diana", "Rose", "Wilson", 28, 6),
        Human("Eve", "Grace", "Davis", 26, 8),
        Human("Frank", "William", "Miller", 32, 9),
        Human("Grace", "Elizabeth", "Moore", 24, 5),
        Human("Henry", "Charles", "Taylor", 29, 7),
        Human("Ivy", "Sophia", "Anderson", 27, 6),
        Human("Jack", "Robert", "Thomas", 31, 8),
        Human("Kate", "Ann", "Jackson", 23, 4),
        Human("Leo", "Michael", "White", 26, 7),
        Human("Mia", "Olivia", "Harris", 25, 5),
        Human("Noah", "Benjamin", "Martin", 28, 6),
        Human("Olivia", "Emma", "Thompson", 24, 4),
        Human("Paul", "George", "Garcia", 30, 8),
        Human("Quinn", "Alex", "Martinez", 27, 7),
        Human("Ryan", "Daniel", "Robinson", 29, 9),
        Human("Sophia", "Isabella", "Clark", 26, 5),
        Human("Thomas", "Edward", "Rodriguez", 33, 10),
        Human("Uma", "Lisa", "Lewis", 25, 6),
        Human("Victor", "Alexander", "Lee", 28, 7),
        Human("Wendy", "Nicole", "Walker", 24, 5)
    )

    humans.forEachIndexed { index, human ->
        val row = index / 5
        val col = index % 5
        human.x = col * 30.0
        human.y = row * 25.0
        println("${human.name} ${human.surname} - start pos: (${"%.2f".format(human.x)}, ${"%.2f".format(human.y)})")
    }

    val simulationTimeSeconds = 45
    println("\nstarts on  $simulationTimeSeconds seconds")
    println("number of members: ${humans.size}")

    humans.forEachIndexed { index, human ->
        Thread.sleep(100)
        human.continuousRandomMovement()
        println("${human.name} start")
    }

    Thread.sleep(simulationTimeSeconds * 1000L)


    humans.forEach { it.stopMovement() }
}