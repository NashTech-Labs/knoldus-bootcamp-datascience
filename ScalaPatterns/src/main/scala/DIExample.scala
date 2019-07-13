object DIExample {
  def main(args: Array[String]): Unit = {
    val nightRider: Vehicle = Car(88.0, 2000.0)
    val dasBoot: Vehicle = Submarine(3.0, 100000.0, 40.0)
    val vehicles: Vehicles[Vehicle] = new Vehicles[Vehicle](Seq[Vehicle](nightRider, dasBoot))
    vehicles.printVehicles
  }
}

abstract class Vehicle(s: Double, w: Double) {
  val speed=s
  val weight=w

  def printVehicle(): Unit
}

case class Car(s: Double, w: Double) extends Vehicle(s: Double, w: Double) {
  def printVehicle(): Unit = { println("Vroom") }
}

case class Submarine(s: Double, w: Double, d: Double) extends Vehicle(s: Double, w: Double) {
  val depth=d
  def printVehicle(): Unit = { println("Torpedo") }
}

class Vehicles[A <: Vehicle](vehicles: Seq[A]) {
  def printVehicles: Unit = vehicles.foreach(_.printVehicle)
}