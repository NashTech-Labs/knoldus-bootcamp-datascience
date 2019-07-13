
object BuilderExample {

  def main(args: Array[String]): Unit = {
    val builder1=new HouseBuilderImpl
    val builder2=new HouseBuilderImpl
    val house1=builder1.setNFloors(1).setNBedRooms(17).setNBathrooms(1).setColor("cyan").build
    val house2=builder2.setNBathrooms(8).setColor("pink").build //Not all of the fields need to be set
    println(house1)
    println(house2)

    val house3=HouseImmutable(nfloors=108, color="purple")
    println(house3)

    val gdelt1=GDELT(year=2019, actor1Code="USA")
    val gdelt2=GDELT(year=476, actionCountryCode="ROME")
    println(gdelt1)
    println(gdelt2)
  }

  //No vars here
  case class HouseImmutable(nfloors: Int=0, nbedRooms: Int=0, nbathrooms: Int=0, color: String="NONE") {
    override def toString(): String = { "A lovely " +color +" house with "+nbedRooms+" bedrooms and "+nbathrooms+" bathrooms." }
  }

  case class House(nfloors: Int, nbedRooms: Int, nbathrooms: Int, color: String) {
    override def toString(): String = { "A lovely " +color +" house with "+nbedRooms+" bedrooms and "+nbathrooms+" bathrooms." }
  }

  trait HouseBuilder {
    def setNFloors(nfloors: Int): HouseBuilder
    def setNBedRooms(nbedRooms: Int): HouseBuilder
    def setNBathrooms(nbathrooms: Int): HouseBuilder
    def setColor(color: String): HouseBuilder
    def build(): House
  }

  class HouseBuilderImpl extends HouseBuilder {
    //Having vars is a downside
    private var nfloors: Int = 0
    private var nbedRooms: Int = 0
    private var nbathrooms: Int = 0
    private var color: String = "NONE"


    override def setNFloors(nfloors: Int) = {
      this.nfloors = nfloors
      this
    }

    override def setNBedRooms(nbedRooms: Int) = {
      this.nbedRooms = nbedRooms
      this
    }

    override def setNBathrooms(nbathrooms: Int) = {
      this.nbathrooms=nbathrooms
      this
    }

    override def setColor(color: String) = {
      this.color = color
      this
    }

    override def build=House(nfloors, nbedRooms, nbathrooms, color)
  }

  case class GDELT(id: Int=0, day: Int=0, monthYear: Int=0, year: Int=0, fractionDate: Double=0.0,
                   actor1Code: String="", actor1Name: String="", actor1CountryCode: String="",
                   actor1KnownGroupCode: String="", actor1EthnicCode: String="", actor1Religion1Code: String="",
                   actor1Religion2Code: String="", actor1Type1Code: String="", actor1Type2Code: String="",
                   actor1Type3Code: String="", actor2Code: String="", actor2Name: String="",
                   actor2CountryCode: String="", actor2KnownGroupCode: String="", actor2EthnicCode: String="",
                   actor2Religion1Code: String="", actor2Religion2Code: String="", actor2Type1Code: String="",
                   actor2Type2Code: String="", actor2Type3Code: String="", isRootEvent: Int=0, eventCode: String="",
                   eventBaseCode: String="", eventRootCode: String="", quadClass: String="", goldsteinScale: Double=0.0,
                   numMentions: Int=0, numSources: Int=0, numArticles: Int=0, avgTone: Double=0.0,
                   actor1Geo_Type: Int=0, actor1Geo_Fullname: String="", actor1Geo_CountryCode: String="",
                   actor1Geo_ADM1Code: String="", actor1Geo_Lat: Double=0.0, actor1Geo_Long: Double=0.0,
                   actor1Geo_FeatureID: Int=0, actor2Geo_Type: Int=0, actor2Geo_Fullname: String="",
                   actor2Geo_CountryCode: String="", actor2Geo_ADM1Code: String="", actor2Geo_Lat: Double=0.0,
                   actor2Geo_Long: Double=0.0, actor2Geo_FeatureID: Int=0, actionGeo_Type: Int=0,
                   actionGeo_Fullname: String="", actionCountryCode: String="", action_ADM1Code: String="",
                   actionGeo_Lat: Double=0.0, actionGeo_Long: Double=0.0, actionGeo_FeatureID: Int=0,
                   dateAdded: Int=0, sourceUrl: String="") {

    override def toString(): String = { "year: "+ year + " actor: " + actor1Code }

  }

}
