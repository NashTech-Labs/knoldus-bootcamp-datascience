package com.knoldus.training

import java.io.{File, PrintWriter}

import com.cloudera.sparkts.models.ARIMA
import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.common.Constants._
import com.knoldus.training.Forecasting.{ForecastDate, readTestFile}
import com.knoldus.training.Forecasting2._
import org.apache.log4j.Logger
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{DecisionTreeRegressor, LinearRegression}
import org.apache.spark.mllib.regression.LassoWithSGD
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}

object Forecasting3 {
  def main(args: Array[String]): Unit = {
    val LOGGER: Logger = KLogger.getLogger(this.getClass)

    val master="mesos://192.168.1.5:5050"

    val sparkUri="http://localhost/spark-2.4.0-bin-hadoop2.7.tgz"

    // Spark Demo
    val spark = SparkSession
      .builder()
      .appName("Forecasting")
      .config("spark.executor.uri", sparkUri)
      .master(master)
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._
    import com.knoldus.spark.UDFs.containsTulipsUDF

    if (args.length!=8) {
      val errorMessage = "USAGE: featureFiles trainingFile testFile modelType elasticNetParam regParam modelOutputDir" +
      " outputPath"
      LOGGER.error(errorMessage)
    }

    val featureFiles = args(0)
    val trainingFile = args(1)
    val testFile = args(2)
    val modelType = args(3)
    val elasticNetParam = args(4).toDouble
    val regParam = args(5).toDouble
    val modelOutputDir = args(6)
    val outputPath = args(7)

    val params = new Params(modelType, elasticNetParam, regParam)

    val startTime = System.currentTimeMillis()

    makeForecast(featureFiles, trainingFile, testFile, params, modelOutputDir, outputPath, spark)

    val endTime = System.currentTimeMillis()

    LOGGER.info("Total run time: " + (endTime-startTime)/1000.0)

    spark.stop()
  }

  def makeForecast(featureFiles: String, trainingFile: String, testFile: String, params: Params,
                   modelOutputDir: String, outputPath: String, spark: SparkSession): Unit = {

    val test = readTestFile(testFile)

    val files = spark.sparkContext.textFile(featureFiles)
    val features = files.map( file => readFeaturesFile(file) )
    features.persist
    val paths = spark.sparkContext.textFile(trainingFile)
    val data = paths.map( x => Forecasting.readTrainingFile(x) )
    data.persist
    val joined = joinFeatures(features, data)
    joined.persist
    val filledIn = joined.map( x => fillMissingData(x) )
    filledIn.persist
    val collected = filledIn.collect

    val sqlContext = new org.apache.spark.sql.SQLContext(spark.sparkContext)

    val modeled = if (params.modelType=="LinearRegression") {
      collected.map( x => doLinearRegression(x, params, modelOutputDir, sqlContext) )
    }
    else {
      collected.map(x => doDecisionTree(x, params, modelOutputDir, sqlContext))
    }

    val reasonable = makeReasonable(modeled)

    val result = getPredictionsForTestDates(test, reasonable, spark)

    writeOutput(result, outputPath)
  }

  def fillMissingCpis(info: Array[Info]): Array[Info] = {
    val cpi = info.map( x => x.cpi ).filter( x => x!=UNKNOWN_DOUBLE )
    val ts = org.apache.spark.mllib.linalg.Vectors.dense(cpi)
    val model = ARIMA.autoFit(ts)
    val forecast = model.forecast(ts, info.length-cpi.length).toArray
    val newInfo = for { i <- cpi.length until info.length } yield {
      info(i).copy( cpi = forecast(i) )
    }
    val a = newInfo.toArray
    info.slice(0, cpi.length) ++ a
  }

  def fillMissingUnemployment(info: Array[Info]): Array[Info] = {
    val unemployment = info.map( x => x.unemployment ).filter( x => x!=UNKNOWN_DOUBLE )
    val ts = org.apache.spark.mllib.linalg.Vectors.dense(unemployment)
    val model = ARIMA.autoFit(ts)
    val forecast = model.forecast(ts, info.length-unemployment.length).toArray
    val newInfo = for { i <- unemployment.length until info.length } yield {
      info(i).copy( unemployment = forecast(i) )
    }
    val a = newInfo.toArray
    info.slice(0, unemployment.length) ++ a
  }

  def fillMissingMarkDowns(info: Array[Info]): Array[Info] = {
    val ave = for { i <- 0 until 5 } yield {
      val notNull = info.map( x => x.markDowns(i) ).filter( x => x!=UNKNOWN_DOUBLE )
      notNull.sum/notNull.length.toDouble
    }
    info.map(x => if (x.markDowns(0)==UNKNOWN_DOUBLE) { x.copy(markDowns = ave.toArray ) } else x)
  }

  def fillMissingData(info: Array[Info]): Array[Info] = {
    val markDowns = fillMissingMarkDowns(info)
    val cpi = fillMissingCpis(markDowns)
    fillMissingUnemployment(cpi)
  }

  def applyModel(xy: Array[(Double, Vector)], info: Array[Info], intercept: Double, coefficients: Vector):
  Array[Info] = {
    val predicted = for { i <- info.indices } yield {
      if (info(i).sales!=UNKNOWN_DOUBLE) { info(i) }
      else {
        val dotProduct = for { j <- 0 until coefficients.size } yield {
          coefficients(j)*xy(i)._2(j)
        }
        val newSale = intercept + dotProduct.toArray.sum
        info(i).copy(sales = newSale, predicted = true)
      }
    }
    predicted.toArray
  }

  def getWeek(date: ForecastDate): Int = {
    (date.year*365 + date.month*31 + date.day)/7
  }

  def getWeekOfYear(date: ForecastDate): Int = {
    ((date.month-1)*31 + date.day)/7
  }

  def infoToDataPoint(info: Array[Info], aveSale: Double, weeklyAve: Array[Double],
                      weeklyAveTemp: Array[Double], n: Int): (Double, Vector) = {
    val week = getWeekOfYear(info(n).date)
    val ave = if (week<weeklyAve.length) { weeklyAve(week) } else 0
    val aveTemp = if (week<weeklyAveTemp.length) { weeklyAveTemp(week)-info(n).temperature } else 0
    (info(n).sales/aveSale,
      Vectors.dense(info(n).temperature, info(n).isHoliday, getWeek(info(n).date), info(n).fuelPrice,
      ave,
      aveTemp,
      info(n).markDowns(0), info(n).markDowns(1), info(n).markDowns(2), info(n).markDowns(3), info(n).markDowns(4),
      info(n).cpi, info(n).unemployment
    ) )
  }

  def infoToDataPoint(info: Array[Info], aveSale: Double, n: Int): (Double, Vector) = {
    val week = getWeekOfYear(info(n).date)
    val weekVector: Array[Double] = Array.fill(week){0.0} ++ Array(1.0) ++ Array.fill(54-week-1){0.0} ++
    Array(info(n).temperature, info(n).isHoliday, getWeek(info(n).date), info(n).fuelPrice) ++ info(n).markDowns ++
    Array(info(n).cpi, info(n).unemployment)
    (info(n).sales/aveSale, Vectors.dense(weekVector))
  }

  def infoToDataPointDecisionTree(info: Array[Info], n: Int): (Double, Vector) = {
    val week = getWeekOfYear(info(n).date)
    val features = Array(week, info(n).temperature, info(n).isHoliday, info(n).fuelPrice, info(n).cpi) ++
    Array(info(n).unemployment) ++ info(n).markDowns
    (info(n).sales, Vectors.dense(features) )
  }

  def getWeeklyAverage(info: Array[Info]): Array[Double] = {
    val filtered = info.filter( x => x.sales>0 )
    val indexed = filtered.map( x => ( getWeekOfYear(x.date), x.sales ) )
    val grouped = indexed.groupBy( x => x._1 )
    val averaged = grouped.map( x => (x._1, x._2.map( y => y._2 ).sum/x._2.length ) )
    val result = averaged.toArray.sortBy( x => x._1 ).map( x => x._2 )

    result
  }

  def getWeeklyAverageTemperature(info: Array[Info]): Array[Double] = {
    val filtered = info.filter( x => x.sales>0 )
    val indexed = filtered.map( x => ( getWeekOfYear(x.date), x.temperature ) )
    val grouped = indexed.groupBy( x => x._1 )
    val averaged = grouped.map( x => (x._1, x._2.map( y => y._2 ).sum/x._2.length ) )
    val result = averaged.toArray.sortBy( x => x._1 ).map( x => x._2 )

    result
  }

  def writeComparison(info: Array[Info], newInfo: Array[Info]): Unit = {
    val projectDir = "/home/jouko/dev/projects/TrainingSprints/datascience-bootcamp/WalmartForecasting"
    val outDirectory = projectDir + "/data/comparison/"
    val outFile = outDirectory + info(0).store + "_" + info(0).dept + ".csv"
    val pw = new PrintWriter(new File(outFile))
    for { i <- info.indices } {
      pw.write(i + "," + info(i).sales + "," + newInfo(i).sales + "\n")
    }
    pw.close()
  }

  def copyPredictionToInfo(df: DataFrame, aveSale: Double, info: Array[Info]): Array[Info] = {
    val predCol = df.select("prediction").collect
    val predicted = for { i <- info.indices } yield {
      if (info(i).sales != UNKNOWN_DOUBLE) { info(i) }
      else { info(i).copy(sales = predCol(i).getDouble(0)*aveSale, predicted = true ) }
    }
    predicted.toArray
  }

  def saveModel(pipeline: Pipeline, modelDir: String, store: Int, dept: Int): Unit = {
    val modelFile = modelDir + "/" + store + "_" + dept
    pipeline.write.overwrite().save(modelFile)
  }

  def usePipeline(pipeline: Pipeline, df: DataFrame, dfAll: DataFrame, aveSale: Double, info: Array[Info],
                  modelDir: String): Array[Info] = {
    if (df.count>10) {
      val model = pipeline.fit(df)
      val prediction = model.transform(dfAll)
      val newInfo = copyPredictionToInfo(prediction, aveSale, info)

      saveModel(pipeline, modelDir, info(0).store, info(0).dept)

      //writeComparison(info, newInfo)

      newInfo
    }
    else { info }
  }

  def doDecisionTree(info: Array[Info], params: Params, modelDir: String, sqlContext: SQLContext): Array[Info] = {
    val xy = for { i <- info.indices } yield {
      infoToDataPointDecisionTree(info, i)
    }

    import sqlContext.implicits._

    val df=xy.filter( x => x._1>0 && !x._1.isNaN && !containsNan(x._2) ).toDF("label", "features")
    val dfAll=xy.toDF("label", "features")

    df.persist
    dfAll.persist

    val dt = new DecisionTreeRegressor()

    val pipeline = new Pipeline().setStages(Array(dt))

    usePipeline(pipeline, df, dfAll, 1.0, info, modelDir)
  }

  def doLinearRegression(info: Array[Info], params: Params, modelDir: String, sqlContext: SQLContext): Array[Info] = {

    val weeklyAve = getWeeklyAverage(info)
    val weeklyAveTemp = getWeeklyAverageTemperature(info)

    val filteredSales = info.map( x => x.sales ).filter( x => x>UNKNOWN_DOUBLE )
    val aveSale = filteredSales.sum/filteredSales.length.toDouble
    val xy = for { i <- info.indices } yield {
      infoToDataPoint(info, aveSale, weeklyAve, weeklyAveTemp, i)
    }

    import sqlContext.implicits._

    val df=xy.filter( x => x._1>0 && !x._1.isNaN && !containsNan(x._2) ).toDF("label", "unscaledFeatures")
    val dfAll=xy.toDF("label", "unscaledFeatures")

    val scaler = new StandardScaler()
      .setInputCol("unscaledFeatures")
      .setOutputCol("features")
      .setWithStd(true)
      .setWithMean(true)

    val lr = if (params.elasticNetParam==1.0) {
      new LinearRegression().setElasticNetParam(params.elasticNetParam).setRegParam(params.regParam)
    }
    else {
      new LinearRegression().setRegParam(params.regParam)
    }

    val pipeline = new Pipeline().setStages(Array(scaler, lr))
    usePipeline(pipeline, df, dfAll, aveSale, info, modelDir)
  }

  case class Params(modelType: String, elasticNetParam: Double, regParam: Double)

}
