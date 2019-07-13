package com.knoldus.training

import java.io.{File, PrintWriter}

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.{Transformers, UDFs}
import org.apache.log4j.Logger
import org.apache.spark.ml.{Model, Pipeline, PipelineModel}
import org.apache.spark.ml.classification._
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.sql.{Column, DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.ml.regression.RegressionModel
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.ml.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.ml.stat.{ChiSquareTest, Correlation}
import org.apache.spark.sql.functions._
import com.ibm.aardpfark.spark.ml.SparkSupport.toPFA
import com.knoldus.training.Main.{cleanDataCols, readCsvFile}
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine

import scala.io.Source

object GetCorrelations {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .config("spark.some.config.option", "some-value")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._

    spark.sparkContext.setLogLevel("WARN")

    val path=args(0)

    val df=readCsvFile(path, spark).persist

    val Array(training, test) = df.randomSplit(Array(0.9, 0.1), seed = 0)
    val cleanedCols=Main.cleanCols(training)

    val df2=df.select(cleanedCols: _*)

    df2.show

    val assembler = new VectorAssembler().setInputCols(df2.columns)
      .setOutputCol("features")

    val df3=assembler.transform(df2)

    val (pearsonCorr, spearmanCorr)=Main.calcCorrelationMatrix(df3)
    val goodFeatures=getGoodFeatures(spearmanCorr, df2.columns)

    LogObject.LOGGER.info("GoodFeatures: [" + goodFeatures.mkString(", ") + "]")

    spark.stop()
  }

  def isRedundant(result: Array[Int], idx: Int, corr: Matrix): Boolean = {
    result.exists{ x => math.abs(corr(x, idx))>0.8 }
  }

  def addHighCorrelations(sorted: IndexedSeq[(Double, Int)], result: Array[Int], n: Int, corr: Matrix): Array[Int] = {
    if (n==sorted.length || sorted(n)._1<0.1) { result }
    else if (!isRedundant(result, sorted(n)._2, corr)) { addHighCorrelations(sorted, result :+ sorted(n)._2, n + 1, corr) }
    else { addHighCorrelations(sorted, result, n + 1, corr) }
  }

  def getGoodFeatures(corr: Matrix, cols: Array[String]): Array[String] = {
    val mat=corr.toDense
    val high=for {i <- 0 to mat.numCols-2 } yield {
      (math.abs(mat(i, mat.numCols-1)), i)
    }

    val sorted=high.sortBy( x => -x._1 )

    val init: Array[Int] = Array()
    val result=addHighCorrelations(sorted, init, 0, corr)

    result.map{ x => cols(x) }
  }


}
