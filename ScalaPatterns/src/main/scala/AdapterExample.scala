object AdapterExample {

  val MILES_TO_KILOMETERS=1.60934

  case class KilometersPerHour(speed: Double)
  case class MilesPerHour(speed: Double)

  class Car {
    def setSpeed(kph: KilometersPerHour) = println("The car is traveling at " + kph.speed + " kph.")
  }

  implicit def mphToKph(mph: MilesPerHour): KilometersPerHour = new KilometersPerHour(mph.speed*MILES_TO_KILOMETERS)

  def main(args: Array[String]): Unit = {
    new Car().setSpeed(new MilesPerHour(60.0))
  }
}
