package com.knoldus.training

import com.knoldus.common.AppConfig
import org.apache.spark.sql.SparkSession

import org.apache.spark.ml.classification._
import org.apache.spark.sql.SparkSession


object RunSavedModel {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Run saved model")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._

    spark.sparkContext.setLogLevel("WARN")

    val inputDataPath = args(0)
    val inputModelPath = args(1)
    val predictionPath = args(2)

    val model = DecisionTreeClassificationModel.load(inputModelPath)
    val df=Main.readCsvFile(inputDataPath, spark)
    val cleanedDf=Main.cleanDataCols(df, spark)

    val predictedDf=model.transform(cleanedDf)

    predictedDf
      .select("prediction")
      .repartition(1)
      .write.format("com.databricks.spark.csv")
      .option("header", "true")
      .save(predictionPath)

    spark.stop()
  }
}
