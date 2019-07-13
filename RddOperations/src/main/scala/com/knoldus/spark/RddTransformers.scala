package com.knoldus.training

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
//import org.apache.spark.sql.{DataFrame, SparkSession}
//import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark._



object Transformations {

  val spark = AppConfig.sparkSession
  import spark.implicits._

  def splitDelimeter(rdd: RDD[String]): RDD[Array[String]] = {
    rdd.map( line => line.split("\t") )
  }

  def getYearActor(rdd: RDD[Array[String]]): RDD[(Int, String)] = {
    rdd.map( x => (x(3).toInt, x(5)) )
  }

  def getActor(rdd: RDD[Array[String]]): RDD[String] = {
    rdd.map( x => x(5) )
  }

  def getIdActor(rdd: RDD[Array[String]]): RDD[(Int, String)] = {
    rdd.map( x => (x(0).toInt, x(5)) )
  }

  def getGdeltRdd(rdd: RDD[String]): RDD[GDELT] = {
    val rddSplit=splitDelimeter(rdd)
    rddSplit.map( x => Info.stringArrayToGdelt(x) )
  }

  def aggregateExample(rdd: RDD[(Int, String)]): Double = {
    //Result depends upon partitioning
    val rdd2=rdd.map( x => x._1.toDouble )
    rdd2.aggregate(0.0)(_ + _, _ + math.log(_))
  }

  def aggregateByKeyExample(rdd: RDD[(Int, String)]): Array[(Int, Int)] = {
    val rdd2=rdd.map( x => (x._1, 1) )
    val concats=rdd2.aggregateByKey(0)(_ + _, _ + _).collect()
    concats
  }


  def cartesianExample(rdd: RDD[(Int, String)], sc: SparkContext): RDD[((Int, String), (Int, String))] = {
    val rdd2=sc.parallelize(rdd.take(10))
    val car=rdd2.cartesian(rdd2)
    car
  }

  def checkpointExample(rdd: RDD[(Int, String)], checkpointDir: String, sc: SparkContext): Unit = {
    sc.setCheckpointDir(checkpointDir)
    rdd.checkpoint
  }

  def coalesceExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    rdd.coalesce(2, false)
  }

  def cogroupExample(rdd: RDD[(Int, String)], sc: SparkContext): RDD[(Int, (Iterable[String], Iterable[String]))] = {
    val rdd2=sc.parallelize(rdd.take(10))
    val grouped=rdd2.cogroup(rdd2)
    grouped
  }

  def collectAsMapExample(rdd: RDD[(Int, String)]): scala.collection.Map[Int, String] = {
    rdd.collectAsMap
  }

  def combineByKeyExample(rdd: RDD[(Int, String)]): RDD[(Int, List[String])] = {
    rdd.combineByKey(List(_), (x: List[String], y: String) => y::x, (x:List[String], y: List[String]) => x:::y )
  }

  def countApproxDistinctExample[T](rdd: RDD[T]): Long = {
    rdd.countApproxDistinct(0.05)
  }

  def countApproxDistinctByKeyExample(rdd: RDD[(Int, String)]): RDD[(Int, Long)] = {
    rdd.countApproxDistinctByKey(0.05)
  }

  def countByKeyExample(rdd: RDD[(Int, String)]): scala.collection.Map[Int, Long] = {
    rdd.countByKey
  }

  def countByValueExample(rdd: RDD[(Int, String)]): scala.collection.Map[(Int, String), Long] = {
    rdd.countByValue
  }

  def dependenciesExample[T](rdd: RDD[T]): Seq[org.apache.spark.Dependency[_]] = {
    rdd.dependencies
  }

  def distinctExample(rdd: RDD[String]): RDD[String] = {
    rdd.distinct
  }

  def filterExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    rdd.filter( x => x._2.length==6 )
  }

  def filterByRangeExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    val rddSorted=sortByKeyExample(rdd)
    rddSorted.filterByRange(833246251, 833246271)
  }

  def flatMapExample(rdd: RDD[Int]): RDD[Int] = {
    rdd.flatMap( x => Array(x, x) )
  }

  def flatMapValuesExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    rdd.flatMapValues( x => Array(x, x) )
  }

  def foldExample(rdd: RDD[Int]): Int = {
    rdd.fold(0)(_ + _ )
  }

  def foldByKeyExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    rdd.foldByKey("")(_ + _)
  }

  def foreachExample(rdd: RDD[GDELT]): Unit = {
    rdd.foreach(println)
  }

  def foreachPartition(rdd: RDD[GDELT]): Unit = {
    rdd.map( x => x.year).foreachPartition( x => println(x.reduce((a, b) => a + b)))
  }

  def fullOuterJoinExample(rdd1: RDD[(Int, String)], rdd2: RDD[(Int, String)]): RDD[(Int, (Option[String], Option[String]))] = {
    rdd1.fullOuterJoin(rdd2)
  }

  def getCheckpointFileExample(rdd: RDD[GDELT]): Option[String] = {
    rdd.getCheckpointFile
  }

  def glomExample[T](rdd: RDD[T]): RDD[Array[T]] = {
    rdd.glom
  }

  def groupByExample(rdd: RDD[GDELT]): RDD[(String, Iterable[GDELT])] = {
    rdd.groupBy( x => {if (x.actor1Code=="BUS") { "BUS" } else { "NOT_BUS" }} )
  }

  def groupByKeyExample(rdd: RDD[GDELT]): RDD[(String, Iterable[GDELT])] = {
    keyByExample(rdd).groupByKey()
  }

  def histogramExample(rdd: RDD[Double]): (Array[Double], Array[Long]) = {
    rdd.histogram(5)
  }

  def intersectionExample(rdd1: RDD[GDELT], rdd2: RDD[GDELT]): RDD[GDELT] = {
    rdd1.intersection(rdd2)
  }

  def isCheckpointedExample[T](rdd: RDD[T]): Boolean = {
    rdd.isCheckpointed
  }

  def joinExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): RDD[(Int, (GDELT, GDELT))] = {
    rdd1.join(rdd2)
  }

  def keyByExample(rdd: RDD[GDELT]): RDD[(String, GDELT)] = {
    rdd.keyBy(_.actor1Code)
  }

  def keysExample(rdd: RDD[GDELT]): RDD[String] = {
    rdd.map( x => (x.actor1Code, x) ).keys
  }

  def leftOuterJoinExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): RDD[(Int, (GDELT, Option[GDELT]))] = {
    rdd1.leftOuterJoin(rdd2)
  }

  def lookupExample(rdd: RDD[GDELT]): Seq[GDELT] = {
    rdd.map( x => (x.actor2CountryCode, x) ).lookup("USA")
  }

  def mapExample(rdd: RDD[GDELT]): RDD[(String, String)] = {
    rdd.map( x => (x.eventBaseCode, x.eventRootCode) )
  }

  def mapValuesExample(rdd: RDD[(Int, GDELT)]): RDD[(Int, Double)] = {
    rdd.mapValues( x => x.avgTone )
  }

  def maxExample(rdd: RDD[(Int, String)]): (Int, String) = {
    rdd.max
  }

  def meanExample(rdd: RDD[GDELT]): Double = {
    rdd.map( x => x.actor1Geo_Lat ).mean
  }

  def minExample(rdd: RDD[(Int, String)]): (Int, String) = {
    rdd.min
  }

  def partitionByExample(rdd: RDD[(Int, GDELT)]): RDD[(Int, GDELT)] = {
    val rangePartitioner=new RangePartitioner(100, rdd)
    rdd.partitionBy(rangePartitioner)
  }

  def partition2ByExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): (RDD[(Int, GDELT)], RDD[(Int, GDELT)]) = {
    val rangePartitioner=new RangePartitioner(100, rdd1)
    val partitioned1=rdd1.partitionBy(rangePartitioner)
    val partitioned2=rdd2.partitionBy(rangePartitioner)
    (partitioned1, partitioned2)
  }

  def partitionsExample(rdd: RDD[GDELT]): Array[Partition] = {
    rdd.partitions
  }

  def pipeExample(rdd: RDD[String]): RDD[String] = {
    rdd.pipe("tail -1")
  }

  def randomSplitExample(rdd: RDD[String]): Array[RDD[String]] = {
    rdd.randomSplit(Array(0.5, 0.5), seed=11L)
  }

  def reduceExample(rdd: RDD[GDELT]): Int = {
    rdd.map( x => x.numMentions ).reduce( _ + _ )
  }

  def reduceByKeyExample(rdd: RDD[(Int, GDELT)]): RDD[(Int, Int)] = {
    rdd.map( x => (x._1, x._2.actor2Geo_Type) ).reduceByKey( _ + _ )
  }

  def repartitionExample(rdd: RDD[GDELT]): RDD[GDELT] = {
    rdd.repartition(3)
  }

  def repartitionAndSortWithinPartitionsExample(rdd: RDD[(Int, Int)]): RDD[(Int, Int)] = {
    val partitioner=new RangePartitioner(2, rdd)
    rdd.repartitionAndSortWithinPartitions(partitioner)
  }

  def repartition2AndSortWithinPartitionsExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): (RDD[(Int, GDELT)], RDD[(Int, GDELT)]) = {
    val partitioner=new RangePartitioner(100, rdd1)
    val partitioned1=rdd1.repartitionAndSortWithinPartitions(partitioner)
    val partitioned2=rdd2.repartitionAndSortWithinPartitions(partitioner)
    (partitioned1, partitioned2)
  }

  def rightOuterJoinExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): RDD[(Int, (Option[GDELT], GDELT))] = {
    rdd1.rightOuterJoin(rdd2)
  }

  def sampleExample(rdd: RDD[GDELT]): RDD[GDELT] = {
    rdd.sample(false, 0.001, 0)
  }

  def sampleByKeyExample(rdd: RDD[GDELT]): RDD[(String, GDELT)] = {
    val sampleMap=List(("GOV", 0.1), ("EDU", 0.9)).toMap
    rdd.map( x => (x.actor1Code, x) ).sampleByKey(false, sampleMap, 0)
  }

  def saveAsObjectFileExample(rdd: RDD[GDELT], path: String): Unit = {
    rdd.saveAsObjectFile(path)
  }

  def saveAsTextFileExample(rdd: RDD[GDELT], path: String): Unit = {
    rdd.saveAsTextFile(path)
  }

  def statsExample(rdd: RDD[GDELT]): org.apache.spark.util.StatCounter = {
    rdd.map( x => x.actor2Geo_Long).stats
  }

  def sortByExample(rdd: RDD[GDELT]): RDD[GDELT] = {
    rdd.sortBy( x => x.goldsteinScale, true )
  }

  def sortByKeyExample(rdd: RDD[(Int, String)]): RDD[(Int, String)] = {
    rdd.sortByKey()
  }

  def stdevExample(rdd: RDD[GDELT]): Double = {
    rdd.map( x => x.actionGeo_Lat ).stdev
  }

  def subtractExample(rdd1: RDD[GDELT], rdd2: RDD[GDELT]): RDD[GDELT] = {
    rdd1.subtract(rdd2)
  }

  def subtractByKeyExample(rdd1: RDD[(Int, GDELT)], rdd2: RDD[(Int, GDELT)]): RDD[(Int, GDELT)] = {
    rdd1.subtractByKey(rdd2)
  }

  def sumExample(rdd: RDD[GDELT]): Double = {
    rdd.map( x => x.numArticles ).sum
  }

  def switchKeyValue(rdd: RDD[(GDELT, Long)]): RDD[(Int, GDELT)] = {
    rdd.map( x => (x._2.toInt, x._1) )
  }

  def takeExample(rdd: RDD[GDELT], n: Int): Array[GDELT] = {
    rdd.take(n)
  }

  def takeOrderedExample(rdd: RDD[Int], n: Int): Array[Int] = {
    rdd.takeOrdered(n)
  }

  def topExample(rdd: RDD[Int], n: Int): Array[Int] = {
    rdd.top(n)
  }

  def treeAggregateExample(rdd: RDD[Int]): Int = {
    rdd.treeAggregate(0)(_ + _, _ + _)
  }

  def treeReduceExample(rdd: RDD[Int]): Int = {
    rdd.treeReduce(_ + _)
  }

  def unionExample(rdd1: RDD[GDELT], rdd2: RDD[GDELT]): RDD[GDELT] = {
    rdd1.union(rdd2)
  }

  def unpersistExample(rdd: RDD[GDELT]): Unit = {
    rdd.unpersist(true)
  }

  def valuesExample(rdd: RDD[GDELT]): RDD[String] = {
    rdd.map( x => (x, x.quadClass) ).values
  }

  def varianceExample(rdd: RDD[GDELT]): Double = {
    rdd.map( x => x.numSources.toDouble ).variance
  }

  def zipExample(rdd1: RDD[Int], rdd2: RDD[GDELT]): RDD[(Int, GDELT)] = {
    rdd1.zip(rdd2)
  }

  def zipAndSwitchKeyValue(rdd: RDD[GDELT]): RDD[(Int, GDELT)] = {
    val zipped=zipWithIndexExample(rdd)
    switchKeyValue(zipped)
  }

  def iterTest(aiter: Iterator[GDELT], biter: Iterator[GDELT]): Iterator[String] = {
    var res = List[String]()
    while (aiter.hasNext && biter.hasNext)
    {
     val x="("+aiter.next.actor2Name + ", " + biter.next.actor2Name + ")"
     res ::= x
    }
    res.iterator
  }

  def zipPartitionsExample(rdd1: RDD[GDELT], rdd2: RDD[GDELT]): RDD[String] = {
    rdd1.zipPartitions(rdd2)(iterTest)
  }

  def zipWithIndexExample(rdd: RDD[GDELT]): RDD[(GDELT, Long)] = {
    rdd.zipWithIndex
  }

  def zipWithUniqueIdExample(rdd: RDD[GDELT]): RDD[(GDELT, Long)] = {
    rdd.zipWithUniqueId
  }

}
