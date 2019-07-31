package example

import common.Direction.Direction
import common.{Direction, Vehicle}


class Position(x:Int, y:Int, direction: Direction.Direction) {


  def print = {
    println(x)
    println(this.y)
    println(this.direction)
  }

}

class Car(val make :String, val model :String, year :Int) extends Vehicle {
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

object Car {
  def Car( make :String,  model :String, year :Int) = new Car(make,model,year)
}
