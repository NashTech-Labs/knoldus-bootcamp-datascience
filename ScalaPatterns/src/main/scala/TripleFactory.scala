object TripleFactory {
  def main(args: Array[String]): Unit = {
    val trip1=("Person", "Clinton", "hasTitle", "president")
    val trip2=("Person", "George III", "livesIn", "England")
    val trip3=("Person", "Clinton", "livesIn", "America")
    val trip4=("Person", "George III", "hasTitle", "King")
    val trip5=("Location", "America", "isPartOf", "Earth")

    val trips=List(trip1, trip2, trip3, trip4, trip5)

    val entities=trips.map( x => Triple(x._1, x._2, x._3, x._4) )
    val combinedEntities=combineEntities(entities)

    combinedEntities.foreach(println)
  }

  def combineEntity(triples: List[Triple]): Triple = {
    if (triples.length==2) {
      val trip1 = triples.head
      val trip2 = triples.tail.head
      (trip1, trip2) match {
        case (Person(name1, title1, location1), Person(name2, title2, location2)) => {
          val title = if (title1 != "") { title1 } else { title2 }
          val location = if (location1 != "") { location1 } else { location2 }
          new Person(name1, title, location)
        }
      }
    }
    else { triples.head }
  }

  def combineEntities(triples: List[Triple]): List[Triple] = {
    if (triples.isEmpty) { List() }
    else {
      combineEntity(triples.filter(x => x.name == triples.head.name)) ::
        combineEntities(triples.filter(x => x.name != triples.head.name))
    }

  }
}

object Triple {
  def apply(kind: String, uri: String, predicate: String, objectWord: String): Triple = kind match {
    case "Person" =>
      if (predicate=="hasTitle") { new Person(name=uri, title=objectWord) }
      else { new Person(name=uri, location=objectWord) }
    case "Location" => new Location(name=uri, partOf=objectWord)
  }
}

trait Triple {
  val name=""
}

case class Person(override val name: String="", title: String="", location: String="") extends Triple {
  override def toString(): String = { name + " has title " +  title + " and lives in " + location }
}

case class Location(override val name: String="", partOf: String="") extends Triple {
  override def toString(): String = { name + " is a part of " + partOf }
}