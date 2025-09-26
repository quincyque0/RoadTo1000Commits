package Interfaces

public interface Movable {
    var x: Double
    var y: Double
    var currentSpeed: Int
    fun moveTo(targetX: Double, targetY: Double) {
        println("go to")
    }
}