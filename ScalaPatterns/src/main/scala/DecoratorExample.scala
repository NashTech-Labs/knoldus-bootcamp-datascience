import scala.io.Source

/*
trait Speak {
  def speak(): Unit = { println("I can speak") }
}

object DecoratorExample {

  def main(args: Array[String]): Unit = {
    val talkingDog=new Dog with Speak

    talkingDog.speak
    println(talkingDog.toString)

    //val regularDog=new Dog //Does not compile
    //regularDog.speak
  }

}
*/

trait ReadFromFile {
  def readFromFile (file: String): Unit = {
    for (line <- Source.fromFile(file).getLines) { println(line) }
  }
}

object DecoratorExample {

  def main(args: Array[String]): Unit = {
    val person=new Person("Obama", "president", "America") with ReadFromFile

    //Get bio and write to screen
    val file=sys.env("HOME") + "/dev/projects/TrainingSprint5/ScalaPatterns/ObamaBio.txt"
    person.readFromFile(file)

  }

}

