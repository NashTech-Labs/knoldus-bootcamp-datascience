package com.knoldus.training

import java.io.{File, PrintWriter}

import breeze.linalg.{max, min}
import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.common.Constants._
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.rdd.RDD
import com.knoldus.training.Forecasting._
import com.knoldus.training.Difference._

object Forecasting2 {



  def main(args: Array[String]): Unit = {

    val LOGGER: Logger = KLogger.getLogger(this.getClass)



    // Spark Demo
    val spark = SparkSession
      .builder()
      .appName("Forecasting")
      .config("spark.some.config.option", "some-value")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._
    import com.knoldus.spark.UDFs.containsTulipsUDF

    val featureFiles=args(0)
    val trainingFile=args(1)
    val testFile=args(2)
    val outputPath=args(3)

    val test = readTestFile(testFile)

    val files = spark.sparkContext.textFile(featureFiles)
    val features = files.map( file => readFeaturesFile(file))

    val paths = spark.sparkContext.textFile(trainingFile)
    val data = paths.map( x => Forecasting.readTrainingFile(x) )

    val joined = joinFeatures(features, data)
    val good = identifyGoodAndBad(joined)

    val differenced = good.map( x => getDifferences(x) )
    val yearlyDifferenced = differenced.map( x => (x._1, getYearlyDifference(x._2)) )
    val collected = yearlyDifferenced.filter( x => x._2._2.length>2 ).collect
    val modeled = collected.map( x => (x._1, x._2._1, doLinearRegression(x._2._2, spark) ) )
    val yearlyUndifferenced = modeled.map( x => (x._1, undifferenceYear((x._2, x._3))) )

    val undifferenced = yearlyUndifferenced.map( x => undifference(x) )

    val result = getPredictionsForTestDates(test, undifferenced, spark)
    val reasonable = makeReasonable(result)

    writeOutput(reasonable, outputPath)

    spark.stop()
  }

  def makeReasonable(info: Info, maxSale: Double): Info = {
    val sales = info.sales
    val reasonableSale = min( max(0.0, sales), maxSale)
    info.copy( sales= reasonableSale)
  }

  def makeReasonable(info: Array[Info]): Array[Info] = {
    for { i <- info.indices } {
      println("store= " + info(i).store + " dept= " + info(i).dept + " i= " + i + " sales= " + info(i).sales + " predicted= " + info(i).predicted)
    }
    if (info.forall( x => x.predicted )) { info }
    else {
      val maxGiven = info.filter(x => !x.predicted).maxBy(x => x.sales).sales
      println("maxGiven= " + maxGiven)
      info.map(x => makeReasonable(x, maxGiven.abs * 2.5))
    }
  }

  def makeReasonable(info: Array[Array[Info]]): Array[Array[Info]] = {
    info.map( x => makeReasonable(x) )
  }

  def identifyGoodAndBad(info: Info): Info = {
    val good = if (info.sales<=0) { false } else { true }

    val store = info.store
    val dept = info.dept
    val date = info.date
    val temperature = info.temperature
    val fuelPrice = info.fuelPrice
    val markDowns = info.markDowns
    val cpi = info.cpi
    val unemployment = info.unemployment
    val weeklySales = info.sales
    val isHoliday = info.isHoliday
    val predicted = info.predicted

    new Info(store, dept, date, temperature, fuelPrice, markDowns, cpi, unemployment, weeklySales, isHoliday, predicted, good)
  }

  def identifyGoodAndBad(info: Array[Info]): Array[Info] = {
    info.map( x => identifyGoodAndBad(x) )
  }

  def identifyGoodAndBad(rdd: RDD[Array[Info]]): RDD[Array[Info]] = {
    rdd.map( x => identifyGoodAndBad(x) )
  }

  def forecastToInfo(forecast: Forecast): Info = {
    val store = forecast.store
    val dept = forecast.dept
    val date = forecast.date
    val temperature = UNKNOWN_DOUBLE
    val fuelPrice = UNKNOWN_DOUBLE
    val markDowns: Array[Double] = Array()
    val cpi = UNKNOWN_DOUBLE
    val unemployment = UNKNOWN_DOUBLE
    val sales = 0.0
    val isHoliday = forecast.isHoliday
    val predicted = true
    val good =true

    new Info(store, dept, date, temperature, fuelPrice, markDowns, cpi, unemployment, sales, isHoliday, predicted, true)
  }

  def getPredictionsForTestDates(test: Array[Forecast], info: Array[Array[Info]], spark: SparkSession):
  Array[Array[Info]] = {
    val testRdd = spark.sparkContext.parallelize(test)
    val flatInfo = info.flatten
    val rddInfo = spark.sparkContext.parallelize(flatInfo)
    val testWithId = testRdd.map( x => (x.store + "_" + x.dept + "_" + dateToString(x.date), x) )
    val infoWithId = rddInfo.map( x => (x.store + "_" + x.dept + "_" + dateToString(x.date), x) )

    val leftJoined = testWithId.leftOuterJoin(infoWithId)
    val notMatching = leftJoined.filter( x => x._2._2.isEmpty )
    val missingInfo = notMatching.map( x => (x._1, forecastToInfo(x._2._1)) )
    val merged = infoWithId.union(missingInfo)

    val joined = testWithId.join(merged)
    val infoOnly = joined.map( x => (x._1, x._2._2 ) )
    val grouped = infoOnly.groupByKey.map( x => x._2.toArray )

    grouped.collect
  }

  def infoToString(info: Info): String = {
    info.store + "_" + info.dept + "_" +  dateToString(info.date) + "," + info.sales
  }

  def writeLine(info: Info, pw: PrintWriter): Unit = {
    pw.write(infoToString(info) + "\n")
  }

  def writeOutput(prediction: Array[Array[Info]], path: String): Unit = {
    val pw = new PrintWriter(new File(path))
    pw.write("Id,Weekly_Sales\n")
    val results=prediction.flatten.filter( x => x.predicted )
    results.foreach( x => writeLine(x, pw) )
    pw.close()
  }

  def myToDouble(str: String): Double = {
    if (str=="NA") { UNKNOWN_DOUBLE }
    else { str.toDouble }
  }

  def strToFeatures(str: String): Info = {
    val parts=str.split(",")

    val store=parts(0).toInt
    val dept=UNKNOWN_INT
    val date=Forecasting.toDate(parts(1))
    val temperature=parts(2).toDouble
    val fuelPrice=parts(3).toDouble
    val markDown1=myToDouble(parts(4))
    val markDown2=myToDouble(parts(5))
    val markDown3=myToDouble(parts(6))
    val markDown4=myToDouble(parts(7))
    val markDown5=myToDouble(parts(8))
    val markDown: Array[Double] = Array(markDown1, markDown2, markDown3, markDown4, markDown5)
    val cpi=myToDouble(parts(9))
    val unemployment=myToDouble(parts(10))
    val sales=UNKNOWN_DOUBLE
    val isHoliday=Forecasting.boolToInt(parts(11))
    val predicted=true
    val good=true

    new Info(store, dept, date, temperature, fuelPrice, markDown, cpi, unemployment, sales, isHoliday, predicted, true)
  }

  def readFeaturesFile(featureFile: String): Array[Info] = {
    val lines=scala.io.Source.fromFile(featureFile).getLines()
    lines.map( x => strToFeatures(x) ).toArray
  }

  def joinSalesDataRecursive(info: Array[Info], sales: Array[Sales], result: Array[Info], n: Int, m: Int):
  Array[Info] = {
    if (m==sales.length) { result ++ info.slice(n, info.length).map( x => x.copy(dept = sales(0).dept)) }
    else if ( info(n).date==sales(m).date ) {
      val sale = info(n).copy(dept = sales(0).dept, sales = sales(m).sales, predicted = false)
      joinSalesDataRecursive(info, sales, result :+ sale, n + 1, m + 1)
    }
    else {
      val sale = info(n).copy(dept = sales(0).dept, sales = UNKNOWN_DOUBLE-1.0, predicted = false)
      joinSalesDataRecursive(info, sales, result :+ sale, n + 1, m)
    }
  }

  def joinSalesData(info: Array[Info], sales: Array[Sales]): Array[Info] = {
    val result: Array[Info] = Array()
    joinSalesDataRecursive(info, sales, result, 0, 0)
  }

  def joinFeatures(features: RDD[Array[Info]], data: RDD[Array[Sales]]): RDD[Array[Info]] = {
    val featuresWithStore = features.map( x => (x(0).store, x) )
    val dataWithStore = data.filter( x => x.length>0 ).map( x => (x(0).store, x) )
    val joined = dataWithStore.join(featuresWithStore)
    joined.map( x => joinSalesData(x._2._2, x._2._1) )
  }

  def fillEnd(data: Array[Option[Double]]): Array[Double] = {
    ???
  }

  def fillInMissingData(data: Array[Features]): Array[Features] = {
    val cpi=data.map( x => x.cpi )
    val unemployment=data.map( x => x.unemployment )

    val filledCpi = fillEnd(cpi)
    val filledUnemployment = fillEnd(unemployment)
    ???
  }

  def makeForecastRecursive(sales: Seq[Double], features: Seq[Vector], n: Int, intercept: Double, coefficients: Vector,
                            result: Array[Double]): Array[Double] = {
    if (n==sales.length) { result }
    else {
      val sale = coefficients(0)*features(n)(0)
      makeForecastRecursive(sales, features, n + 1, intercept, coefficients, result :+ sale)
    }
  }

  def makeForecast(data: Seq[(Double, Vector)], intercept: Double, coefficients: Vector): Array[Double] = {
    val sales = data.map( x => x._1)
    val features = data.map( x => x._2 )
    val start = sales.indexOf(UNKNOWN_DOUBLE)
    val start2 = if (start>0) { start } else { sales.length }
    val result: Array[Double] = sales.toArray.slice(0, start2)
    makeForecastRecursive(sales, features, start2, intercept, coefficients, result)
  }

  def containsNan(v: Vector): Boolean = {
    v.toArray.count( x => x.isNaN )!=0
  }

  def doLinearRegression(df: DataFrame): (Double, Vector) = {
    val lr = new LinearRegression().setFitIntercept(false)
    val model = lr.fit(df)

    (model.intercept, model.coefficients)
  }

  def infoToDataPoint(info: Array[Info], n: Int): (Double, Vector) = {
    (info(n).sales, Vectors.dense(info(n).temperature) )
  }

  def getPrediction(sales: Array[Double], info: Array[Info]): Array[Info] = {
    val prediction = for { i <- sales.indices } yield {
      val newSale = if (sales(i).isNaN) { 0 } else { sales(i) }
      if (info(i).sales!=UNKNOWN_DOUBLE) { info(i).copy(sales = newSale, predicted = false) }
      else { info(i).copy(sales = newSale, predicted = true) }
    }

    prediction.toArray
  }

  def doLinearRegression(info: Array[Info], spark: SparkSession): Array[Info] = {
    val xy = for { i <- 1 until info.length if info(i).good && info(i-1).good} yield {
      infoToDataPoint(info, i)
    }

    val xy2 = for { i <- 1 until info.length } yield {
      infoToDataPoint(info, i)
    }

    val sqlContext = new org.apache.spark.sql.SQLContext(spark.sparkContext)
    import sqlContext.implicits._

    val df=xy.filter( x => x._1!=UNKNOWN_DOUBLE && !x._1.isNaN && !containsNan(x._2) ).toDF("label", "features")

    val (intercept, coefficients) = if (df.count>4) { doLinearRegression(df) } else { (0.0, Vectors.dense(0, 0)) }

    val sales = info(0).sales +: makeForecast(xy2, intercept, coefficients)

    getPrediction(sales, info)
  }

  case class Features(store: Int, date: ForecastDate, temperature: Double, fuelPrice: Double, markDowns: Array[Option[Double]], cpi: Option[Double], unemployment: Option[Double], isHoliday: Int)
  case class Info(store: Int, dept: Int, date: ForecastDate, temperature: Double, fuelPrice: Double, markDowns: Array[Double], cpi: Double, unemployment: Double, sales: Double, isHoliday: Int, predicted: Boolean, good: Boolean)
}
