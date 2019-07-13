package com.knoldus.training

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Sample {


  def main(args: Array[String]):Unit = {

    // Logging Demonstration
    val LOGGER: Logger = KLogger.getLogger(this.getClass)
    val age = 20
    LOGGER.info("Age " + age )
    LOGGER.warn("This is warning")


    // Spark Demo
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .config("spark.some.config.option", "some-value")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._
    import com.knoldus.spark.UDFs.containsTulipsUDF

    // Transform Example
    val sourceDF :DataFrame = spark.sparkContext.makeRDD(Seq((2), (10))).toDF("wave_height")
    val funDF = sourceDF.transform(Transformers.withStokeLevel())
    funDF.show()

    // UDF Example
    val anotherDF = spark.sparkContext.makeRDD(Seq(("tulips are pretty"), ("apples are yummy"))).toDF("joys")
    val happyDF = anotherDF.withColumn("joys_contains_tulips", containsTulipsUDF($"joys"))
    happyDF.show()


    // Unit test Demo
    val a = calculate401k(age) _

    // Configuration Demo
    println(AppConfig.port)
    println(a(monkey401Kprocess))
    println(a(human401))
    spark.stop()

  }

  def calculate401k(age: Int) ( func: (Int) => String): String = {

    func(age)

  }


  def monkey401Kprocess(i: Int) :String = {

    i.toString + "I am a monkey"
  }

  def human401(i :Int) :String = {
    i + " I am a man"
  }
}
