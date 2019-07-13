package example

import example.Direction.Direction


object Direction extends Enumeration {
  type Direction = Value
  val NORTH, SOUTH, EAST, WEST = Value
}

trait Vehicle {

  def setSpeed(speed :Int)

  def makeSound(sound :String)

  def move(x :Int, y:Int , direction :Direction) :Position

  def getPosition() :Position


}
