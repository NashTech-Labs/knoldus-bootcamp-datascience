
object Factory {

  def main(args: Array[String]): Unit = {
    val dog=Animal("dog")
    val cat=Animal("cat")

    println(dog)
    println(cat)
  }

}


object Animal {
  def apply(kind: String) = kind match {
    case "dog" => new Dog()
    case "cat" => new Cat()
  }
}

trait Animal {
  val numLegs=4
}

class Dog extends Animal {
  override def toString(): String = { "I chase cats" }
}

class Cat extends Animal {
  override def toString(): String = { "I chase mice" }
}

