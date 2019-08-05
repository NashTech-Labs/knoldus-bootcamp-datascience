package com.rccl.ssh

import org.apache.spark.sql.{SaveMode, SparkSession}

object TestMinio  {
  case class customer(val name :String, val age :Int, val address :String)
  def main(args: Array[String]): Unit = {


    if (args == null || args.length < 1) throw new Exception("Not enough arguments")
    val miniopassword = args(0).toString()
    val spark = SparkSession
      .builder
      .appName("Minio Testing")
      .config("fs.s3a.access.key", "ga_read_user")
      .config("fs.s3a.secret.key", miniopassword)
      .config("fs.s3a.endpoint", "10.16.5.152:10213")
      .config("fs.s3a.path.style.access", "true")
      .config("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .config("fs.s3a.connection.ssl.enabled", "false")
      .getOrCreate()

    val numrecords = 100000
    val gen = new scala.util.Random()
    import spark.implicits._
    val df =  (0 to numrecords).map( num => new customer(gen.nextString(10),gen.nextInt(10),gen.nextString(10) )).toDF()
    df.show(10)
    try {
      df
        .coalesce(1)
        .write
        .mode(SaveMode.Append)
        .option("header", "true")
        .parquet(s"s3a://test/")
    } catch {
      case e: Exception =>       println(e.printStackTrace())
        System.exit(-1)
    }
  }



}
