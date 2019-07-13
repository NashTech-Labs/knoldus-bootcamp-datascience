package com.knoldus.training

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.Encoder

import com.knoldus.training.Transformations

object Main {


  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("RddOperations")
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    //import sqlContext.implicits._
    //import sqlContext.createSchemaRDD

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

    spark.sparkContext.setLogLevel("WARN")

    AppConfig.setSparkSession(spark)
    import spark.implicits._
    import com.knoldus.spark.UDFs.containsTulipsUDF


    val homeDir=sys.env("HOME")
    val projectDir=homeDir+"/dev/projects/TrainingSprints/TrainingSprint5/RddOperations"
    val path1=projectDir+"/data/20190325.export.CSV"
    val path2=projectDir+"/data/20190324.export.CSV"

    val checkpointDir=projectDir+"/Checkpoint"

    val rdd1=sc.textFile(path1)
    val rdd2=sc.textFile(path2)
    val rdd=rdd1.union(rdd2)

    val rddGdelt1=Transformations.getGdeltRdd(rdd1)
    val rddGdelt2=Transformations.getGdeltRdd(rdd2)

    val rddGdelt=Transformations.getGdeltRdd(rdd)
    println("Number of rows= " + rdd.count)
    val rddSplit=Transformations.splitDelimeter(rdd)
    val yearActor=Transformations.getYearActor(rddSplit)
    val actor=Transformations.getActor(rddSplit)
    val idActor=Transformations.getIdActor(rddSplit)

    println
    println("First 10 ids")
    rddSplit.take(10).foreach(x => println(x(0)))

    println
    println("First 10 year actor pairs")
    yearActor.take(10).foreach(println)

    println
    val agg=Transformations.aggregateExample(yearActor)
    println("agg= " + agg)

    println
    val aggByKey=Transformations.aggregateByKeyExample(yearActor).toList
    println("aggByKey= " + aggByKey)
    
    println
    val cart=Transformations.cartesianExample(yearActor, sc).collect.toList
    println("cart= " + cart)

    Transformations.checkpointExample(yearActor, checkpointDir, sc)
    Transformations.coalesceExample(yearActor)

    println
    val cogrouped=Transformations.cogroupExample(yearActor, sc).collect.toList
    println("cogrouped= " + cogrouped)

    println
    val collectedMap=Transformations.collectAsMapExample(yearActor)
    println("collectedMap= " + collectedMap)

    //println
    //val combined=Transformations.combineByKeyExample(yearActor).collect.toList
    //println("combined= " + combined)
    
    println
    println("context= " + rdd.context)

    println
    val approxDistinct=Transformations.countApproxDistinctExample(yearActor)
    println("approxDistinct= " + approxDistinct)

    println
    val approxDistinctByKey=Transformations.countApproxDistinctByKeyExample(yearActor)
    println("approxDistinctByKey= " + approxDistinctByKey)

    val countedByKey=Transformations.countByKeyExample(yearActor)
    println("countedByKey= " + countedByKey)

    //println
    //val countedByValue=Transformations.countByValueExample(yearActor)
    //println("countedByValue= " + countedByValue)

    println
    val dependencies=Transformations.dependenciesExample(yearActor)
    println("dependencies= " + dependencies.mkString)

    println
    println
    println("The first 10 distinct actors")
    val distinctActors=Transformations.distinctExample(actor)
    distinctActors.take(10).foreach(println)

    println
    println("firstId= " + idActor.first)

    println
    println("GDELTs with actors whose names have length 6")
    val len6Actors=Transformations.filterExample(yearActor)
    len6Actors.take(10).foreach(println)

    println
    println("First 10 GDELTs")
    rddGdelt.take(10).foreach(println)

    practiceJoins(rddGdelt1, rddGdelt2)

    val smallGdelt=sc.parallelize(rddGdelt2.take(10))

    practiceJoins(rddGdelt1, smallGdelt)

    practiceJoins(smallGdelt, rddGdelt1)

    println("DONE")

    spark.stop()

  }

  def practiceJoins(rddGdelt1: RDD[GDELT], rddGdelt2: RDD[GDELT]): Unit = {
    val zipped1=Transformations.zipAndSwitchKeyValue(rddGdelt1)
    val zipped2=Transformations.zipAndSwitchKeyValue(rddGdelt2)
    val count1=zipped1.count
    val count2=zipped2.count

    println
    println("Joining an RDD with " + count1 + " rows with an RDD of " + count2 + " rows")
    practiceInnerJoin(zipped1, zipped2)
    println("Persisting zipped1")
    zipped1.persist
    practiceInnerJoin(zipped1, zipped2)
    println("Persisting zipped2")
    zipped2.persist
    practiceInnerJoin(zipped1, zipped2)
    println("Unpersisting zipped1 and zipped2")
    zipped1.unpersist(true)
    zipped2.unpersist(true)
    practiceInnerJoin(zipped1, zipped2)
   
    practiceInnerJoinWithPartitioning(zipped1, zipped2)
    practiceInnerJoinWithPartitioningAndSorting(zipped1, zipped2)
    practiceLeftOuterJoin(zipped1, zipped2)
    practiceRightOuterJoin(zipped1, zipped2)
    practiceGenericJoin(zipped1, zipped2, Transformations.joinExample, "Inner join")

    println
    println
    println("-----------------------------------------------------------------")
    println
    println
  }

  def practiceInnerJoin(zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)]): Unit = {
    println
    println("Inner join test")
    val timeStart=System.currentTimeMillis
    val testJoin=Transformations.joinExample(zipped1, zipped2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println("Inner join test took " + (timeEnd-timeStart) + " ms")
    println
  }

  def practiceInnerJoinWithPartitioning(zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)]): Unit = {
    println
    println("Inner join with partitioning by range test")
    val timeStart1=System.currentTimeMillis
    val (partitioned1, partitioned2)=Transformations.partition2ByExample(zipped1, zipped2)
    val timeStart2=System.currentTimeMillis
    val testJoin=Transformations.joinExample(partitioned1, partitioned2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println("Inner join with partitioning by range test just joining took " + (timeEnd-timeStart2) + " ms")
    println("Inner join with partitioning by range test took " + (timeEnd-timeStart1) + " ms")
    println
  }

  def practiceInnerJoinWithPartitioningAndSorting(zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)]): Unit = {
    println
    println("Inner join with partitioning by range and sorting test")
    val timeStart1=System.currentTimeMillis
    val (partitioned1, partitioned2)=Transformations.repartition2AndSortWithinPartitionsExample(zipped1, zipped2)
    val timeStart2=System.currentTimeMillis
    val testJoin=Transformations.joinExample(partitioned1, partitioned2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println("Inner join with partitioning by range and sorting test just joining took " + (timeEnd-timeStart2) + " ms")
    println("Inner join with partitioning by range and sorting test took " + (timeEnd-timeStart1) + " ms")
    println
  }

  def practiceLeftOuterJoin(zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)]): Unit = {
    println
    println("Left outer join test")
    val timeStart=System.currentTimeMillis
    val testJoin=Transformations.leftOuterJoinExample(zipped1, zipped2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println("Left outer join test took " + (timeEnd-timeStart) + " ms")
    println
  }

  def practiceRightOuterJoin(zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)]): Unit = {
    println
    println("Right outer join test")
    val timeStart=System.currentTimeMillis
    val testJoin=Transformations.rightOuterJoinExample(zipped1, zipped2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println("Right outer join test took " + (timeEnd-timeStart) + " ms")
    println
  }

  def practiceGenericJoin[T](zipped1: RDD[(Int, GDELT)], zipped2: RDD[(Int, GDELT)], joinFunc: (RDD[(Int, GDELT)], RDD[(Int, GDELT)]) => RDD[(Int, T)], joinType: String): Unit = {
    println
    println(joinType + " test")
    val timeStart=System.currentTimeMillis
    val testJoin=joinFunc(zipped1, zipped2)
    testJoin.take(5).foreach(println)
    val timeEnd=System.currentTimeMillis
    println(joinType + " test took " + (timeEnd-timeStart) + " ms")
    println
  }
}
