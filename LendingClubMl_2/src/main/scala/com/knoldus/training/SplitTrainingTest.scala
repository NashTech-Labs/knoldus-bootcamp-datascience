package com.knoldus.training

import java.io.{File, PrintWriter}

import com.knoldus.common.AppConfig
import org.apache.spark.sql.SparkSession


object SplitTrainingTest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SplitTrainingTest")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._

    spark.sparkContext.setLogLevel("WARN")

    val inputPath = args(0)
    val testSetSize = args(1).toDouble
    val trainingOutPath = args(2)
    val testOutPath = args(3)

    val df = spark.read
      .format("csv")
      .option("header", "true")
      .option("inferSchema", true)
      .load(inputPath)

    val count=df.count()
    val fraction=testSetSize/count.toDouble

    LogObject.LOGGER.info("trainingOutPath= " + trainingOutPath)
    LogObject.LOGGER.info("testOutPath= " + testOutPath)

    val Array(trainingDf, testDf) = df.randomSplit(Array(1.0-fraction, fraction))

    trainingDf.repartition(1).write.format("com.databricks.spark.csv").option("header", "true").save(trainingOutPath)
    testDf.repartition(1).write.format("com.databricks.spark.csv").option("header", "true").save(testOutPath)
  }
}
