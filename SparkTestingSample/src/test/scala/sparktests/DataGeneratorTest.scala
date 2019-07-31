package sparktests

import common._

import com.holdenkarau.spark.testing.{DatasetGenerator, SharedSparkContext}
import org.apache.spark.sql.{Dataset, SQLContext, SparkSession}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

case class Car(val name: String, val speed: Int) {
  def print() = println("Name:"+name+" Speed:"+speed)
}

class DatasetGeneratorSizeSpecial extends FunSuite
  with SharedSparkContext with Checkers {

  test("test generating sized Datasets[Custom Class]") {
    val sqlContext = SparkSession.builder.getOrCreate().sqlContext
    import sqlContext.implicits._

    val carGen: Gen[Dataset[Seq[Car]]] =
    DatasetGenerator.genSizedDataset[Seq[Car]](sqlContext) { size =>
      val slowCarsTopNumber = math.ceil(size * 0.1).toInt

      def carGenerator(speed: Gen[Int]): Gen[Car] = for {
        name <- Arbitrary.arbitrary[String]
        speed <- speed
      } yield new Car(name, speed)

      val cars: Gen[List[Car]] = for {
        slowCarsNumber: Int <- Gen.choose(0, slowCarsTopNumber)
        slowCars: List[Car] <- Gen.listOfN(slowCarsNumber, carGenerator(Gen.choose(0, 20)))
        normalSpeedCars: List[Car] <- Gen.listOfN(
          size - slowCarsNumber,
          carGenerator(Gen.choose(21, 150))
        )
      } yield {
        slowCars ++ normalSpeedCars
      }
      cars
    }
    carGen.sample.foreach {
      ds  :Dataset[Seq[Car]] =>
              ds.foreach {
                cars : Seq[Car] =>
                      cars.foreach {
                        car :Car => car.print()
                      }
              }
    }
    val property =
      forAll(carGen.map(_.flatMap(identity))) {
        dataset =>
          val cars = dataset.collect()
          val dataSetSize = cars.length
          val slowCars = cars.filter(_.speed < 21)
          slowCars.length <= dataSetSize * 0.1 &&
            cars.map(_.speed).length == dataSetSize
      }

    check(property)
  }
}
