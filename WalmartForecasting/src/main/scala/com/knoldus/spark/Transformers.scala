package com.knoldus.spark
import com.knoldus.common.AppConfig
import com.knoldus.training.Forecasting
import com.knoldus.training.Forecasting.Sales
import com.knoldus.training.Forecasting2.{Info, readFeaturesFile}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object Transformers {

  val spark = AppConfig.sparkSession
  import spark.implicits._

  def withStokeLevel()(df: DataFrame): DataFrame = {
    df.withColumn("stoke_level",when($"wave_height" > 6, "radical").otherwise("bummer")
    )
  }

  def readAllFeatureFiles(files: RDD[String]): RDD[Array[Info]] = {
    files.map(file => readFeaturesFile(file))
  }

  def readAllTrainingFiles(paths: RDD[String]): RDD[Array[Sales]] = {
    paths.map(x => Forecasting.readTrainingFile(x))
  }


}
