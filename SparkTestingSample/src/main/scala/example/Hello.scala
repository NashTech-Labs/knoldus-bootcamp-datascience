package example

import example.Direction
import example.Direction.Direction


class Position(x:Int, y:Int, direction: Direction.Direction) {


  def print = {
    println(x)
    println(this.y)
    println(this.direction)
  }

}

class Car extends Vehicle {
  private var speed :Int = 0 ;
  private var position = new Position(0,0,Direction.NORTH)

  override def setSpeed(speed: Int): Unit = {
    this.speed = speed
  }

  override def makeSound(sound: String): Unit = println(sound)

  override def move(x: Int, y:Int , direction: Direction): Position = {
    return new Position(x,y,direction)

  }

  override def getPosition(): Position = {
      return position
  }
}

object Main extends  App {

  val car = new Car
  car.move(1,1,Direction.SOUTH)
  car.makeSound("rrrrrrrrrr")
  println(car.getPosition().print)
}