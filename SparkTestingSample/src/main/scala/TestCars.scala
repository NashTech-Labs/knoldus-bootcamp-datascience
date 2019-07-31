
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.{col, udf}
object Main extends  App {
  val spark = SparkSession.
    builder.
    appName("Spark Testing Example").master("spark://localhost:7077").
    getOrCreate()
  val rawCars = spark.sqlContext
    .read.format("csv").option("header", "true")
    .option("inferSchema", "true")
    .load("file:///Users/iramaraju/dev/projects/knoldus/knoldus-bootcamp-datascience/SparkTestingSample/data/test.txt")

  rawCars.createOrReplaceTempView("Cars")
  spark.sqlContext.udf.register("cen",century)

  def century = (year: Int) => year / 100

  spark.sql("select cen(year), make, model from Cars ").show()
  spark.stop()


}

