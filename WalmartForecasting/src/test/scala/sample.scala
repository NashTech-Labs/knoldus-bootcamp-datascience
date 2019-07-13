
import com.knoldus.common.Constants._
import com.knoldus.training.Forecasting.{Forecast, ForecastDate, Sales}
import com.knoldus.training.Forecasting2.Info
import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test
import com.knoldus.training.Difference
import com.knoldus.training.Forecasting
import com.knoldus.training.Forecasting2
import com.knoldus.training.GenFakeData
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

class ExampleSuite extends TestCase {

  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _
  var sc: SparkContext = _

  override def setUp() :Unit = {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]

    sc = {
      val sparkConfig = new SparkConf()
      sparkConfig.set("spark.broadcast.compress", "false")
      sparkConfig.set("spark.shuffle.compress", "false")
      sparkConfig.set("spark.shuffle.spill.comress", "false")
      sparkConfig.set("spark.driver.allowMultipleContexts", "true")
      new SparkContext("local[2]", "RddOperations", sparkConfig)
    }
  }

  //case class Info(store: Int, dept: Int, date: ForecastDate, temperature: Double, fuelPrice: Double, markDowns: Array[Double], cpi: Double, unemployment: Double, sales: Double, isHoliday: Int, predicted: Boolean)

  val store=1
  val dept=1

  val date1=new ForecastDate(2013, 3, 12)
  val temperature1=43.1
  val fuelPrice1=2.67
  val markDowns1=Array(56.0, 45.2, 90.4, 126.9, 90.3)
  val cpi1=125.9
  val unemployment1=4.5
  val sales1=98765.43
  val isHoliday1=0
  val predicted1=false
  val good1=false

  val info1=new Info(store, dept, date1, temperature1, fuelPrice1, markDowns1, cpi1, unemployment1, sales1, isHoliday1, predicted1, good2)

  val date2=new ForecastDate(2013, 3, 19)
  val temperature2=54.1
  val fuelPrice2=2.69
  val markDowns2=Array(56.1, 45.3, 90.4, 126.9, 90.3)
  val cpi2=126.1
  val unemployment2=4.4
  val sales2=98789.03
  val isHoliday2=0
  val predicted2=false
  val good2=false

  val info2=new Info(store, dept, date2, temperature2, fuelPrice2, markDowns2, cpi2, unemployment2, sales2, isHoliday2, predicted2, good2)

  val infos=Array(info1, info2)

  val size = 100
  val randomInfo = GenFakeData.genFakeInfo(size)
  val randomSales = GenFakeData.genFakeSales(size)

  val forecast = new Forecast(store, dept, date1, isHoliday1)

  @Test
  def testIdentifyGoodAndBad: Unit = {
    val info = Forecasting2.identifyGoodAndBad(info2)
    assertEquals(info.good, true)
  }

  @Test
  def testIdentifyGoodAndBadArray: Unit = {
    val info = Forecasting2.identifyGoodAndBad(randomInfo)

    for { i <- randomInfo.indices } {
      assertEquals(true, info(i).good)
    }
  }

  @Test
  def testForecastToInfo: Unit = {
    val info = new Info(store, dept, date1, UNKNOWN_DOUBLE, UNKNOWN_DOUBLE, Array(), UNKNOWN_DOUBLE, UNKNOWN_DOUBLE, 0.0, isHoliday1, false, false)
    val expInfo = Forecasting2.forecastToInfo(forecast)

    assertEquals(Forecasting2.infoToString(expInfo), Forecasting2.infoToString(info))
  }

  @Test
  def testInfoToString: Unit = {
    val str = Forecasting2.infoToString(info1)
    val expected = "1_1_2013-03-12,98765.43"

    assertEquals(expected, str)
  }

  @Test
  def testMyToDouble: Unit = {
    assertEquals(34.2, Forecasting2.myToDouble("34.2"), 0.0001)
    assertEquals(UNKNOWN_DOUBLE, Forecasting2.myToDouble("NA"), 0.0001)
  }

  @Test
  def testStrToFeatures: Unit = {
    val str = "1,2013-3-12,43.1,2.67,56.0,45.2,90.4,126.9,90.3,125.9,4.5,98765.43,FALSE"
    val exp = Forecasting2.strToFeatures(str)

    assertEquals(exp.store, 1)
    assertEquals(exp.dept, UNKNOWN_INT)
    assertEquals(exp.date.year, 2013)
  }

  @Test
  def testjoinSalesData: Unit = {
    val newInfos = Forecasting2.joinSalesData(randomInfo, randomSales)

    for { i <- randomInfo.indices } {
      assertEquals(randomSales(i).sales, newInfos(i).sales, 0.00001)
    }
  }

  @Test
  def testStrToSales: Unit = {
    val str = "1,1,2013-3-9,845.2,FALSE"
    val sales = Forecasting.strToSales(str)
    val exp = new Sales(1, 1, new ForecastDate(2013, 3, 9), 845.2, 0)

    assertEquals(exp, sales)
  }

  @Test
  def testStrToForecast: Unit = {
    val str = "1,1,2012-11-05,TRUE"
    val forecast = Forecasting.strToForecast(str)
    val exp = new Forecast(1, 1, new ForecastDate(2012, 11, 5), 1)

    assertEquals(exp, forecast)
  }

  @Test
  def testToDate: Unit = {
    val str = "2012-11-05"
    val date = Forecasting.toDate(str)
    val exp = new ForecastDate(2012, 11, 5)

    assertEquals(exp, date)
  }

  @Test
  def testBoolToInt: Unit = {
    assertEquals(1, Forecasting.boolToInt("TRUE"))
    assertEquals(0, Forecasting.boolToInt("FALSE"))
  }

  @Test
  def testPad: Unit = {
    assertEquals("01", Forecasting.pad(1))
    assertEquals("10", Forecasting.pad(10))
  }

  @Test
  def testDifference: Unit = {
    val diff=Difference.getDifferences(infos)
    val back=Difference.undifference(diff)

    assertEquals(infos(0).sales, back(0).sales, 0.00001)
    assertEquals(infos(1).sales, back(1).sales, 0.00001)
  }

  @Test
  def testYearlyDifference: Unit = {
    val diff = Difference.getYearlyDifference(randomInfo)
    val back = Difference.undifferenceYear(diff)

    for { i <- back.indices } {
      assertEquals(back(i).sales, randomInfo(i).sales, 0.00001)
      assertEquals(back(i).date, randomInfo(i).date)
    }
  }

  override def tearDown(): Unit = {
    if (sc != null) { sc.stop() }
    super.tearDown()
  }
}