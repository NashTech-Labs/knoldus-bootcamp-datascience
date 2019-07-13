import java.io.File

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IOUtils

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.highgui.Highgui
import org.opencv.objdetect.CascadeClassifier
import org.apache.spark.{SparkConf, SparkContext, SparkFiles}

object LibraryLoader {
  //lazy val load = System.load(SparkFiles.get("libopencv_java2413.so"))
  //lazy val load = System.load("/home/jouko/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark/lib/libopencv_java2413.so")
  lazy val load = System.load("/home/jouko/dev/projects/TrainingSprints/TrainingSprint7/opencv/build/lib/libopencv_java2413.so")
}

object FaceDetector {
  val homeDir: String=sys.env("HOME")
  val projectDir: String=homeDir + "/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark"
  val resourcesDir: String=projectDir + "/src/main/resources"

  val modelFile: String=resourcesDir + "/haarcascade_frontalface_alt.xml"
  lazy val faceDetector = new CascadeClassifier(modelFile)
}

object CountFaces {
  def main(args: Array[String]): Unit = {

    val sparkMaster="spark://192.168.1.5:7077"
    //val sparkMaster="local[2]"

    val conf = new SparkConf().setMaster(sparkMaster).setAppName("OpenCVSpark")
    val sc = new SparkContext(conf)
    /*
    val spark = SparkSession
        .builder()
        .appName("OpenCVSpark")
        //.master("local[2")
        .master(sparkMaster)
        .getOrCreate()

    spark.sparkContext.setLogLevel("DEBUG")
    */

    val homeDir=sys.env("HOME")
    val projectDir=homeDir + "/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark"
    val resourcesDir=projectDir + "/src/main/resources"

    val modelFile1=resourcesDir + "/haarcascade_frontalface_alt.xml"
    val modelFile2=resourcesDir + "/haarcascade_frontalface_default.xml"
    val modelFile3=resourcesDir + "/lbpcascade_frontalface.xml"

    val modelFiles=List(modelFile1, modelFile2, modelFile3)

    val inputPaths=resourcesDir + "/images_with_counts_2.txt"

    //FaceDetector.faceDetector

    for (modelFile <- modelFiles) {
      //val localModelFile=downloadToCluster(modelFile, sc)
      //runModel(inputPaths, localModelFile, sc)
      runModel(inputPaths, modelFile, sc)
    }
  }

  def runModel(inputPaths: String, modelFile: String, sc: SparkContext): Unit = {
    println()
    println("modelFile= " + modelFile)
    val rdd=sc.textFile(inputPaths).map( x => x.split(" ") )
    rdd.collect.foreach( x => println(x(0)))
    //val rddTest=rdd.map( path => testFunc(path(0)))
    //rddTest.collect.foreach(println)
    val rddFaces=rdd.map( path => (path(0), countFaces(path(0), modelFile), path(1).toInt) )
    rddFaces.cache
    val loss=calcError(rddFaces)
    rddFaces.collect.foreach( x => println(x))
    println("loss= " + loss)
    println()
  }

  def testFunc(path: String): String = {
    val homeDir: String=sys.env("HOME")
    val projectDir: String=homeDir + "/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark"
    val resourcesDir: String=projectDir + "/src/main/resources"

    val modelFile: String=resourcesDir + "/haarcascade_frontalface_alt.xml"
    println(modelFile)
    val faceDetector = new CascadeClassifier(modelFile)
    //LibraryLoader.load
    val image = Highgui.imread(path)
    val faceDetections = new MatOfRect()
    faceDetector.detectMultiScale(image, faceDetections)
    path + "asdf"
  }

  def countFaces(imagePath: String, modelFile: String): Int = {
    //println("In countFaces")
    LibraryLoader.load
    //println("Loaded library")
    //val faceDetector = new CascadeClassifier(SparkFiles.get(modelFile))
    val faceDetector = new CascadeClassifier(modelFile)

    val image = Highgui.imread(imagePath)
    println("width= " + image.width)

    val faceDetections = new MatOfRect()
    //FaceDetector.faceDetector.detectMultiScale(image, faceDetections)
    faceDetector.detectMultiScale(image, faceDetections)

    faceDetections.toArray.length
  }

  def calcError(rddFaces: RDD[(String, Int, Int)]): Double = {
    val comparison=rddFaces.map( x => (x._2.toDouble, x._3.toDouble) )
    comparison.collect.foreach(println)
    val scores=comparison.map( x => math.abs(x._2-x._1)/(x._1 + x._2) )
    scores.sum/scores.count.toDouble
  }

  def copyFile(from: String, to: String): Unit = {
    val conf = new Configuration()
    val fromPath = new Path(from)
    val toPath=new Path(to)
    val is = fromPath.getFileSystem(conf).open(fromPath)
    val os = toPath.getFileSystem(conf).create(toPath)
    IOUtils.copyBytes(is, os, conf)
    is.close()
    os.close()
  }

  def downloadToCluster(path: String, sc: SparkContext): String = {
    val suffix = path.substring(path.lastIndexOf("."))
    val file = File.createTempFile("file_", suffix, new File("/tmp/"))
    copyFile(path, "file://" + file.getPath())
    sc.addFile("file://" + file.getPath())
    file.getPath()
  }
}

