
import com.knoldus.common.AppConfig

import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test
import com.knoldus.training.Main
import scala.collection.mutable.ListBuffer

import org.apache.spark.{SparkConf, SparkContext}

import com.knoldus.training.Transformations
import com.knoldus.training.Info

class ExampleSuite extends TestCase {


  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _
  var sc: SparkContext = _

  override def setUp() :Unit = {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]
  
  sc = {
    val sparkConfig = new SparkConf()
    sparkConfig.set("spark.broadcast.compress", "false")
    sparkConfig.set("spark.shuffle.compress", "false")
    sparkConfig.set("spark.shuffle.spill.comress", "false")
    sparkConfig.set("spark.driver.allowMultipleContexts", "true")
    new SparkContext("local[2]", "RddOperations", sparkConfig)
  }
    
    //println("Setup")
  }

  @Test
  def testSplitDelimeter(): Unit = {
    val rdd=sc.parallelize(List("Hi\tthere", "How\tare"))
    val split=Transformations.splitDelimeter(rdd).collect
    val expected=List(List("Hi", "there"), List("How", "are"))
    val splitList=split.map( x => x.toList ).toList
    assertEquals(splitList, expected)
  }

  @Test
  def testGetYearActor(): Unit = {
    val a=List(Array("7", "8", "9", "2015", "7.9", "GOV"))
    val rdd=sc.parallelize(a)
    val yearActor=Transformations.getYearActor(rdd).collect.toList
    val expected=List((2015, "GOV"))
    assertEquals(yearActor, expected)
  }
  
  @Test
  def testGetActor(): Unit = {
    val a=List(Array("7", "8", "9", "2015", "7.9", "GOV"))
    val rdd=sc.parallelize(a)
    val yearActor=Transformations.getActor(rdd).collect.toList
    val expected=List("GOV")
    assertEquals(yearActor, expected)
  }
  
  @Test
  def testGetIdActor(): Unit = {
    val a=List(Array("7", "8", "9", "2015", "7.9", "GOV"))
    val rdd=sc.parallelize(a)
    val idActor=Transformations.getIdActor(rdd).collect.toList
    val expected=List((7, "GOV"))
    assertEquals(idActor, expected)
  }

  @Test
  def testAggregateExample(): Unit = {
    val l=List((1, "GOV"), (2, "EDU"), (3, "COM"), (4, "USA"))
    val rdd=sc.parallelize(l, 2)
    val sum=Transformations.aggregateExample(rdd)
    val expected=math.log(3)+math.log(7)
    assert(math.abs(sum-expected)<0.0001)
  }

  @Test
  def testAggregateByKeyExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val concats=Transformations.aggregateByKeyExample(rdd).toList
    val expected=List((1, 2), (2, 2))
    assertEquals(concats.sortBy( x => x._1), expected)
  }

  @Test
  def testCartesianExample(): Unit = {
    val l=List((1, "GOV"), (2, "EDU"), (3, "COM"))
    val rdd=sc.parallelize(l)
    val car=Transformations.cartesianExample(rdd, sc).collect.toList
    val expected=List(
     ((1,"GOV"),(1,"GOV")), ((1,"GOV"),(2,"EDU")), ((1,"GOV"),(3,"COM")), 
     ((2,"EDU"),(1,"GOV")), ((3,"COM"),(1,"GOV")), ((2,"EDU"),(2,"EDU")), 
     ((2,"EDU"),(3,"COM")), ((3,"COM"),(2,"EDU")), ((3,"COM"),(3,"COM")))
    assertEquals(car, expected)
  }

  @Test
  def testCoalesceExample(): Unit = {
    val l=List((1, "GOV"), (2, "EDU"), (3, "COM"))
    val rdd=sc.parallelize(l)
    val rdd2=Transformations.coalesceExample(rdd)
    assertEquals(rdd2.partitions.length, 2)
  }

  @Test
  def testCogroupExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val grouped=Transformations.cogroupExample(rdd, sc).collect.toList
    val expected=List(
      (2, (List("COM", "USA"), List("COM", "USA"))), 
      (1, (List("GOV", "EDU"), List("GOV", "EDU"))) 
    )
    assertEquals(grouped, expected)
  }

  @Test
  def testCollectAsMapExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val collected=Transformations.collectAsMapExample(rdd)
    val expected=Map( 1 -> "EDU", 2 -> "USA" )
    assertEquals(collected, expected)
  }

  @Test
  def testCombineByKeyExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val combined=Transformations.combineByKeyExample(rdd).collect.toList
    val expected=List( (2, List("USA", "COM")), (1, List("EDU", "GOV")) )
    assertEquals(combined, expected)
  }

  @Test
  def countApproxDistinctExample(): Unit = {
    val l=List(1, 1, 2, 4, 8, 8)
    val rdd=sc.parallelize(l)
    val approxDistinct=Transformations.countApproxDistinctExample(rdd)
    assertEquals(approxDistinct, 4)
  }

  @Test
  def countByKeyExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val count=Transformations.countByKeyExample(rdd)
    val expected=Map( 1 -> 2, 2 -> 2)
    assertEquals(count, expected)
  }

  @Test
  def testCountByValueExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val count=Transformations.countByValueExample(rdd)
    val expected=Map( (1, "GOV") -> 1, 
      (1, "EDU") -> 1, 
      (2, "COM") -> 1,
      (2, "USA") -> 1)
    assertEquals(count, expected)
  }

  @Test
  def testDependenciesExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"), (2, "USA"))
    val rdd=sc.parallelize(l)
    val rdd2=Transformations.sortByKeyExample(rdd)
    val dependencies=Transformations.dependenciesExample(rdd2)
    assertEquals(dependencies.length, 1)
  }

  @Test
  def testDistinctExample(): Unit = {
    val l=List("a", "a", "b", "c")
    val rdd=sc.parallelize(l)
    val distinct=Transformations.distinctExample(rdd).collect.toList.sorted
    val expected=List("a", "b", "c")
    assertEquals(distinct, expected)
  }

  @Test 
  def testFilterExample(): Unit = {
    val l=List( (1, "JFK"), (2, "JFKUSA") )
    val rdd=sc.parallelize(l)
    val filtered=Transformations.filterExample(rdd).collect.toList
    val expected=List((2, "JFKUSA"))
    assertEquals(filtered, expected)
  }

  @Test
  def testFilterByRangeExample(): Unit = {
    val l=List( (833246251, "GOV"), (1, "EDU") )
    val rdd=sc.parallelize(l)
    val filtered=Transformations.filterByRangeExample(rdd).collect.toList
    val expected=List((833246251, "GOV"))
    assertEquals(filtered, expected)
  }

  @Test
  def testFlatMapExample(): Unit = {
    val l=List(1, 2, 3)
    val rdd=sc.parallelize(l)
    val duplicated=Transformations.flatMapExample(rdd).collect.toList
    val expected=List(1, 1, 2, 2, 3, 3)
    assertEquals(duplicated, expected)
  }

  @Test
  def testFlatMapValuesExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"))
    val rdd=sc.parallelize(l)
    val duplicated=Transformations.flatMapValuesExample(rdd).collect.toList
    val expected=List((1, "GOV"), (1, "GOV"), (1, "EDU"), 
      (1, "EDU"), (2, "COM"), (2, "COM"))
    assertEquals(duplicated, expected)
  }

  @Test
  def testFoldExample(): Unit = {
    val l=List(1, 2, 3, 4)
    val rdd=sc.parallelize(l)
    val sum=Transformations.foldExample(rdd)
    assertEquals(sum, 10)
  }

  @Test
  def testFoldByKeyExample(): Unit = {
    val l=List((1, "GOV"), (1, "EDU"), (2, "COM"))
    val rdd=sc.parallelize(l)
    val folded=Transformations.foldByKeyExample(rdd).collect.toList
    val expected=List((1, "GOVEDU"), (2, "COM"))
    assertEquals(folded.sortBy( x => x._1) , expected)
  }

  @Test
  def testFullOuterJoinExample(): Unit = {
    val l1=List((1, "GOV"), (2, "EDU"))
    val l2=List((1, "BUS"), (2, "COM"))
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val joined=Transformations.fullOuterJoinExample(rdd1, rdd2).collect.toList
    val expected=List(
      (2, (Some("EDU"), Some("COM"))), 
      (1, (Some("GOV"), Some("BUS")))
    )
    assertEquals(joined, expected)
  }

  @Test
  def testGlomExample(): Unit = {
    val l=List(1, 2, 3, 4, 5, 6)
    val rdd=sc.parallelize(l, 2)
    val result=Transformations.glomExample(rdd).collect.toList
      .map( x => x.toList )
    val expected=List(List(1, 2, 3), List(4, 5, 6))
    assertEquals(result, expected)
  }

  @Test
  def testGroupByExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val grouped=Transformations.groupByExample(rdd).collect.toList
      .map( x => (x._1, x._2.toList) )
    val expected=List( 
      ("BUS", List(Info.gdelt2)),
      ("NOT_BUS", List(Info.gdelt1)))
    assertEquals(grouped, expected)
  }

  @Test
  def testGroupByKeyExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val grouped=Transformations.groupByKeyExample(rdd).collect.toList
      .map( x => (x._1, x._2.toList) )
    val expected=List( 
      ("", List(Info.gdelt1)),
      ("BUS", List(Info.gdelt2)))
    assertEquals(grouped, expected)
  }

  @Test
  def testHistogramExample(): Unit = {
    val l=List(1.1, 1.2, 1.3, 2.0, 2.1, 7.4, 7.5, 7.6, 8.8, 9.0)
    val rdd=sc.parallelize(l)
    val hist=Transformations.histogramExample(rdd)
    val hist2=(hist._1.toList, hist._2.toList)
    val expected=(List(1.1, 2.68, 4.26, 5.84, 7.42, 9.0),
      List(5, 0, 0, 1, 4))
    assertEquals(hist2, expected)
  }

  @Test
  def testIntersectionExample(): Unit = {
    val l1=List(Info.gdelt1, Info.gdelt2)
    val l2=List(Info.gdelt2)
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val inter=Transformations.intersectionExample(rdd1, rdd2).collect.toList
    val expected=List(Info.gdelt2)
    assertEquals(inter, expected)
  }

  @Test
  def testIsCheckpointedExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val checkpointed=Transformations.isCheckpointedExample(rdd)
    assertEquals(checkpointed, false)
  }

  @Test
  def testJoinExample(): Unit = {
    val l1=List((1, Info.gdelt1), (2, Info.gdelt2))
    val l2=List((2, Info.gdelt1), (1, Info.gdelt2))
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val joined=Transformations.joinExample(rdd1, rdd2).collect.toList
    val expected=List(
      (2, (Info.gdelt2, Info.gdelt1)),
      (1, (Info.gdelt1, Info.gdelt2)) 
    )
    assertEquals(joined, expected)
  }

  @Test 
  def testKeyByExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val keyed=Transformations.keyByExample(rdd).collect.toList
    val expected=List( 
      ("", Info.gdelt1),
      ("BUS", Info.gdelt2))
    assertEquals(keyed, expected)
  }

  @Test
  def testKeysExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val actors=Transformations.keysExample(rdd).collect.toList
    val expected=List("", "BUS")
    assertEquals(actors, expected)
  }

  @Test
  def testLeftOuterJoinExample(): Unit = {
    val l1=List((1, Info.gdelt1), (2, Info.gdelt2))
    val l2=List((1, Info.gdelt2))
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val joined=Transformations.leftOuterJoinExample(rdd1, rdd2).collect.toList
    val expected=List(
      (2, (Info.gdelt2, None)),
      (1, (Info.gdelt1, Some(Info.gdelt2)))
    )
    assertEquals(joined, expected)
  }

  @Test 
  def testLookupExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val gdelts=Transformations.lookupExample(rdd).toList
    val expected=List(Info.gdelt1)
    assertEquals(gdelts, expected)
  }

  @Test
  def testMapExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val gdelts=Transformations.mapExample(rdd).collect.toList
    val expected=List(("036", "03"), ("020", "02"))
    assertEquals(gdelts, expected)
  }

  @Test
  def testMapValuesExample(): Unit = {
    val l=List((1, Info.gdelt1), (2, Info.gdelt2))
    val rdd=sc.parallelize(l)
    val avgTones=Transformations.mapValuesExample(rdd).collect.toList
    val expected=List((1, 0.93592512598992), (2, 0.0))
    assertEquals(avgTones, expected)
  }

  @Test
  def testMaxExample(): Unit = {
    val l=List((1, "USA"), (2, "BUS"))
    val rdd=sc.parallelize(l)
    val max=Transformations.maxExample(rdd)
    val expected=(2, "BUS")
    assertEquals(max, expected)
  }

  @Test
  def testMeanExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val mean=Transformations.meanExample(rdd)
    val expected=17.0261
    assert(math.abs(mean-expected)<0.0001)
  }

  @Test
  def testMinExample(): Unit = {
    val l=List((1, "USA"), (2, "BUS"))
    val rdd=sc.parallelize(l)
    val min=Transformations.minExample(rdd)
    val expected=(1, "USA")
    assertEquals(min, expected)
  }

  @Test
  def testPartitionsExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l, 2)
    val npartitions=Transformations.partitionsExample(rdd).size
    assertEquals(npartitions, 2)
  }

  @Test
  def testPipeExample(): Unit = {
    val l=List("Hello", "how", "are", "you", "doing", "I", "am", "fine")
    val rdd=sc.parallelize(l, 2)
    val piped=Transformations.pipeExample(rdd).collect.toList
    val expected=List("you", "fine")
    assertEquals(piped, expected)
  }

  @Test
  def testRandomSplitExample(): Unit = {
    val l=List("Hello", "how", "are", "you", "doing", "I", "am", "fine")
    val rdd=sc.parallelize(l)
    val ls=Transformations.randomSplitExample(rdd).map( x => x.collect.toList ).toList
    val expected=List(List("Hello", "how", "doing", "I", "am"),  
      List("are", "you", "fine"))
    assertEquals(ls, expected)
  }

  @Test
  def testReduceExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val sum=Transformations.reduceExample(rdd)
    assertEquals(sum, 3)
  }

  @Test
  def testReduceByKeyExample(): Unit = {
    val l=List( (1, Info.gdelt1), (1, Info.gdelt1), (2, Info.gdelt1), (2, Info.gdelt2) )
    val rdd=sc.parallelize(l)
    val reduced=Transformations.reduceByKeyExample(rdd).collect.toList
    val expected=List((2, 2), (1, 4))
    assertEquals(reduced, expected)
  }

  @Test
  def testRepartitionExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2, Info.gdelt2, Info.gdelt1)
    val rdd=sc.parallelize(l, 2)
    val npartitions1=rdd.partitions.length
    assertEquals(npartitions1, 2)
    val rdd2=Transformations.repartitionExample(rdd)
    val npartitions2=rdd2.partitions.length
    assertEquals(npartitions2, 3)
  }

  @Test
  def testRepartitionAndSortWithinPartitions(): Unit = {
    val l=List((7, 2), (1, 1), (3, 4), (9, 5), (2, 5), (4, 3), (5, 8))
    val rdd=sc.parallelize(l)
    val sorted=Transformations.repartitionAndSortWithinPartitionsExample(rdd).collect.toList
    val expected=List((1, 1), (2, 5), (3, 4), (4, 3), (5, 8), (7,2), (9,5))
    assertEquals(sorted, expected)
  }

  @Test
  def testRightOuterJoinExample(): Unit = {
    val l1=List((1, Info.gdelt2))
    val l2=List((1, Info.gdelt1), (2, Info.gdelt2))
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val joined=Transformations.rightOuterJoinExample(rdd1, rdd2).collect.toList
    val expected=List(
      (2, (None, Info.gdelt2)),
      (1, (Some(Info.gdelt2), Info.gdelt1))
    )
    assertEquals(joined, expected)
  }

  @Test
  def testStatsExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val stats=Transformations.statsExample(rdd)
    val expectedMean=0-55.694
    val expectedStdDev=55.694
    assertEquals(stats.count, 2)
    assert(math.abs(stats.mean-expectedMean)<0.001)
    assert(math.abs(stats.stdev-expectedStdDev)<0.001)
  }

  @Test
  def testSortByExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val sorted=Transformations.sortByExample(rdd).collect.toList
    val expected=List(Info.gdelt2, Info.gdelt1)
    assertEquals(sorted, expected)
  }

  @Test
  def testSortByKey(): Unit = {
    val l=List((5, "USA"), (1, "EDU"))
    val rdd=sc.parallelize(l)
    val sorted=Transformations.sortByKeyExample(rdd).collect.toList
    val expected=List((1, "EDU"), (5, "USA"))
    assertEquals(sorted, expected)
  }

  @Test
  def testStddevExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val stdDev=Transformations.stdevExample(rdd)
    val expected=4.6793
    assert(math.abs(stdDev-expected)<0.0001)
  }

  @Test
  def testSubtractExample(): Unit = {
    val l1=List(Info.gdelt1, Info.gdelt2)
    val l2=List(Info.gdelt1)
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val subtracted=Transformations.subtractExample(rdd1, rdd2).collect.toList
    val expected=List(Info.gdelt2)
    assertEquals(subtracted, expected)
  }

  @Test
  def testSubtractByKeyExample(): Unit = {
    val l1=List((1, Info.gdelt1), (2, Info.gdelt2))
    val l2=List((1, Info.gdelt2))
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val subtracted=Transformations.subtractByKeyExample(rdd1, rdd2).collect.toList
    val expected=List((2, Info.gdelt2))
    assertEquals(subtracted, expected)
  }

  @Test
  def testSumExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val sum=Transformations.sumExample(rdd)
    val expected=3.0
    val delta=0.00001
    assertEquals(sum, expected, delta)
  }

  @Test
  def testTakeExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val one=Transformations.takeExample(rdd, 1)(0)
    val expected=Info.gdelt1
    assertEquals(one, expected)
  }
  
  @Test
  def testTakeOrderExample(): Unit = {
    val l=List(9, 4, 2, 0, 5, 1)
    val rdd=sc.parallelize(l)
    val taken=Transformations.takeOrderedExample(rdd, 3).toList
    val expected=List(0, 1, 2)
    assertEquals(taken, expected)
  }

  @Test
  def testTopExample(): Unit = {
    val l=List(9, 4, 2, 0, 5, 1)
    val rdd=sc.parallelize(l)
    val top=Transformations.topExample(rdd, 3).toList
    val expected=List(9, 5, 4)
    assertEquals(top, expected)
  }

  @Test
  def testTreeAggregate(): Unit = {
    val l=List(9, 4, 2, 0, 5, 1)
    val rdd=sc.parallelize(l)
    val agg=Transformations.treeAggregateExample(rdd)
    val expected=21
    assertEquals(agg, expected)
  }

  @Test
  def testTreeReduceExample(): Unit = {
    val l=List(9, 4, 2, 0, 5, 1)
    val rdd=sc.parallelize(l)
    val sum=Transformations.treeReduceExample(rdd)
    val expected=21
    assertEquals(sum, expected)
  }

  @Test
  def testUnionExample(): Unit = {
    val l1=List(Info.gdelt1)
    val l2=List(Info.gdelt2)
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val unionized=Transformations.unionExample(rdd1, rdd2).collect.toList
    val expected=List(Info.gdelt1, Info.gdelt2)
    assertEquals(unionized, expected)
  }

  @Test
  def testValuesExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val quads=Transformations.valuesExample(rdd).collect.toList
    val expected=List("1", "1")
    assertEquals(quads, expected)
  }

  @Test
  def testVarianceExample(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val variance=Transformations.varianceExample(rdd)
    val expected=0
    assertEquals(variance, expected, 0.0001)
  }

  @Test
  def testZipExample(): Unit = {
    val l1=List(1, 2)
    val l2=List(Info.gdelt1, Info.gdelt2)
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val zipped=Transformations.zipExample(rdd1, rdd2).collect.toList
    val expected=List((1, Info.gdelt1), (2, Info.gdelt2))
    assertEquals(zipped, expected)
  }

  @Test
  def testZipPartitionsExample(): Unit = {
    val l1=List(Info.gdelt1)
    val l2=List(Info.gdelt2)
    val rdd1=sc.parallelize(l1)
    val rdd2=sc.parallelize(l2)
    val zipped=Transformations.zipPartitionsExample(rdd1, rdd2).collect.toList
    val expected=List("(UNITED STATES, )")
    assertEquals(zipped, expected)
  }

  @Test
  def testZipWithIndex(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val zipped=Transformations.zipWithIndexExample(rdd).collect.toList
    val expected=List((Info.gdelt1, 0), (Info.gdelt2, 1))
    assertEquals(zipped, expected)
  }

  @Test
  def testZipWithUniqueId(): Unit = {
    val l=List(Info.gdelt1, Info.gdelt2)
    val rdd=sc.parallelize(l)
    val zipped=Transformations.zipWithUniqueIdExample(rdd).collect.toList
    val expected=List((Info.gdelt1, 0), (Info.gdelt2, 1))
    assertEquals(zipped, expected)
  }

  override def tearDown(): Unit = {
    //println("End")
    if (sc != null) { sc.stop() }
    super.tearDown()
  }
}
