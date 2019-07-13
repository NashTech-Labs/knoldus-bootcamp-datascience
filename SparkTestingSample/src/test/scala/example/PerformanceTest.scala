package example

import java.nio.file.Files

import com.holdenkarau.spark.testing.{PerTestSparkContext, PerfListener}
import org.apache.spark._
import org.scalatest.FunSuite

/**
  * Illustrate using per-test sample test. This is the one to use
  * when your tests may be destructive to the Spark context
  * (e.g. stopping it)
  */
class PerTestSparkContextTest extends FunSuite with PerTestSparkContext {

  val tempPath = Files.createTempDirectory(null).toString()

  //tag::samplePerfTest[]
  test("wordcount perf") {
    val listener = new PerfListener()
    sc.addSparkListener(listener)
    doWork(sc)
    println(listener)
    assert(listener.totalExecutorRunTime > 0)
    assert(listener.totalExecutorRunTime < 10000)
    sc.stop()
  }
  //end::samplePerfTest[]

  test("spark context still exists") {
    assert(sc.parallelize(List(1,2)).reduce(_ + _) === 3)
  }

  def doWork(sc: SparkContext): Unit = {
    val data = sc.textFile("../README.md")
    val words = data.flatMap(_.split(" "))
    words.map((_, 1)).reduceByKey(_ + _).saveAsTextFile(tempPath + "/magic")
  }
}
