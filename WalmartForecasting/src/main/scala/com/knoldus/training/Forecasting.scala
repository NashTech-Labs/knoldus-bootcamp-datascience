package com.knoldus.training

import java.io.PrintWriter

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.Model
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.rdd.RDD
import com.cloudera.sparkts._
import com.cloudera.sparkts.models.{ARIMA, ARIMAModel}

import java.io._

object Forecasting {


  def main(args: Array[String]):Unit = {

    // Logging Demonstration
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

    val pathFile=args(0)
    val outputPath=args(1)

    val paths=spark.sparkContext.textFile(pathFile)

    val data=paths.map( x => readTrainingAndTestFiles(x) )
    val rawPredictions=data.map( x => doArima(x) )

    writeOutputCsv(rawPredictions, outputPath)

    spark.stop()

  }

  def strToSales(str: String): Sales = {
    val parts = str.split(",")

    val store = parts(0).toInt
    val dept = parts(1).toInt
    val date = toDate(parts(2))
    val sales = parts(3).toDouble
    val isHoliday = boolToInt(parts(4))

    new Sales(store, dept, date, sales, isHoliday)
  }

  def strToForecast(str: String): Forecast = {
    val parts = str.split(",")

    val store = parts(0).toInt
    val dept = parts(1).toInt
    val date = toDate(parts(2))
    val isHoliday = boolToInt(parts(3))

    new Forecast(store, dept, date, isHoliday)
  }

  def readTrainingFile(file: String): Array[Sales] = {
    val lines=scala.io.Source.fromFile(file).getLines()
    lines.map( line => strToSales(line) ).toArray
  }

  def readTestFile(file: String): Array[Forecast] = {
    val lines=scala.io.Source.fromFile(file).getLines()
    lines.map( line => strToForecast(line) ).toArray
  }

  def readTrainingAndTestFiles(line: String): (Array[Sales], Array[Forecast]) = {
    val parts=line.split(" ")
    (readTrainingFile(parts(0)), readTestFile(parts(1)))
  }

  def toDate(str: String): ForecastDate = {
    val parts=str.split("-")
    new ForecastDate(parts(0).toInt, parts(1).toInt, parts(2).toInt)
  }

  def dateToInt(date: String): Int = {
    val parts=date.split("-")
    (365*parts(0).toInt + 30*parts(1).toInt + parts(2).toInt)/7
  }

  def boolToInt(str: String): Int = {
    if (str=="TRUE") { 1 } else { 0 }
  }

  def doArima(data: (Array[Sales], Array[Forecast])): Array[Sales] = {
    val values=data._1.map( x => x.sales )
    if (values.length>10) {
      val ts = Vectors.dense(values)
      val model = ARIMA.autoFit(ts)
      val forecast = model.forecast(ts, data._2.length).toArray
      val result = for {i <- 0 until data._2.length} yield {
        new Sales(data._2(i).store, data._2(i).dept, data._2(i).date, forecast(i), data._2(i).isHoliday)
      }
      result.toArray
    }
    else {
      val result = for {i <- 0 until data._2.length} yield {
        new Sales(data._2(i).store, data._2(i).dept, data._2(i).date, 0.0, data._2(i).isHoliday)
      }
      result.toArray
    }
  }

  def pad(i: Int): String = {
    if (i<10) { "0" + i }
    else { i.toString }
  }

  def dateToString(date: ForecastDate): String = {
    date.year + "-" + pad(date.month) + "-" + pad(date.day)
  }

  def writeLine(sales: Sales, pw: PrintWriter): Unit = {
    pw.write(sales.store + "_" + sales.dept + "_" +  dateToString(sales.date) + "," + sales.sales + "\n")
  }

  def writeOutputCsv(prediction: RDD[Array[Sales]], path: String): Unit =
  {
    val pw = new PrintWriter(new File(path))
    pw.write("Id,Weekly_Sales\n")
    val results=prediction.collect.flatten
    results.foreach( x => writeLine(x, pw) )
    pw.close()
  }

  case class Forecast(store: Int, dept: Int, date: ForecastDate, isHoliday: Int)
  case class Sales(store: Int, dept: Int, date: ForecastDate, sales: Double, isHoliday: Int)
  case class ForecastDate(year: Int, month: Int, day: Int)
}
