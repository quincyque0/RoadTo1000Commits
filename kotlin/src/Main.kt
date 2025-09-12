import kotlin.random.Random
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

class Human {
    var x: Double = 0.0
    var y: Double = 0.0
    var name: String = ""
    var subname: String = ""
    var surname: String = ""
    var age: Int = -1
    var curspeed: Int = -1

    constructor(_name: String, _subname: String, _surname: String,_age: Int, _curspeed: Int){
        name = _name
        subname = _subname
        surname = _surname
        age = _age
        curspeed = _curspeed
        println("We created class with name $name")

    }
    fun move(){
        println("Human is moved")
    }
    fun moveto(_toX: Double, _toY: Double){
        x = _toX
        y = _toY
        println("moved to :($x,$y)")
    }
    fun randomMove() {
        x += (Math.random() * 10 - 5)
        y += (Math.random() * 10 - 5)

        performRandomMove()

        val distance = sqrt(x.pow(2) + y.pow(2))

        val timeMs = (distance / curspeed * 1000).toLong()

        val timer = Executors.newScheduledThreadPool(1)

        timer.schedule({
            performRandomMove()
            timer.shutdown()
        }, timeMs, TimeUnit.MILLISECONDS)
        println("move to $x $y")
    }

    private fun performRandomMove() {
        x += (Math.random() * 100 - 5)
        y += (Math.random() * 100 - 5)
    }
}

fun main() {
    val petya: Human = Human("Alice","razehard","viktorovna", 25, 10)
//    petya.move()
//    petya.moveto(4.0,9.0)
//
//    var counter: Int = 10;
//    val name : String = "";
//    print(name)
//    println("Hello world");
    petya.randomMove()

}
