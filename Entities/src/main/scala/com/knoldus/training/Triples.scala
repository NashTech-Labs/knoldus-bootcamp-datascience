package com.knoldus.training

import java.util
import java.util.Properties

import com.datastax.driver.core.Cluster
import com.knoldus.common.{AppConfig, KLogger}

import scala.annotation.tailrec
//import com.knoldus.training.Entities.{addEntitiesToDatabase, createTables, getEntities, getSentences}
import com.knoldus.training.Entities._
import edu.stanford.nlp.ie.util.RelationTriple
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object Triples {

  def main(args: Array[String]):Unit = {
    val cluster = Cluster.builder.addContactPoints("127.0.0.1").build
    val session = cluster.newSession()

    // Logging Demonstration
    val LOGGER: Logger = KLogger.getLogger(this.getClass)

    val conf=new SparkConf().setMaster("local[2]").setAppName("Enteties")
    val sc=new SparkContext(conf)

    // Spark Demo
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .config("spark.some.config.option", "some-value")
      .master("local[*]")
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._
    import com.knoldus.spark.UDFs.containsTulipsUDF

    val dfPrefixes=spark
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "prefixes", "keyspace" -> "knowledge_graph"))
      .load()

    val prefixes=dfPrefixes.select("prefix").rdd.map( r => r(0).toString ).collect()

    val homeDir=sys.env("HOME")
    val projectDir=homeDir + "/dev/project/TrainingSprints/TrainingSprint3/Entities"
    //val path=projectDir + "/data"
    //val path=projectDir+"/SmallData"
    val path=projectDir+"/SmallerData"

    val rdd=sc.wholeTextFiles(path)
    println(rdd.count)
    //rdd.take(1).foreach(println)
    val rddSentences=rdd.flatMap( x => getSentences(x._2) )
    println(rddSentences.count)
    val triples=rddSentences.flatMap( x => getTriples(x) )
    triples.take(1000).foreach(println)
    println("ntriple= "+triples.count())
    //triples.take(1000).foreach(println)

    addTriplesToDatabase(triples, prefixes)

    session.closeAsync()
  }

  def addTripleToDatabase(triple: Array[String], prefixes: Array[String]): Unit = {


    val url="http://knoldus.com/"
    val subjectPrefix=triple(3)
    val subjectUri=url+subjectPrefix+"/"+triple(0)
    val predicatePrefix=triple(1)
    val predicateUri=triple(1)
    val objectValue=triple(2)
    val objectPrefix=triple(4)
    val objectUri=url+objectPrefix+"/"+triple(2)

    val insertTriple="insert into knowledge_graph.triples "+
    "(Subject_prefix, subject_uri, predicate_prefix, predicate_uri, object_value, object_prefix, object_uri, last_updated) values "+
    "('"+subjectPrefix+"', '"+subjectUri+"', '"+predicatePrefix+"', '"+predicateUri+"', '"+objectValue+"', '"+objectPrefix+"', '"+objectUri+"', toTimestamp(now()));"

    //println(insertTriple)
    if (prefixes.contains(subjectPrefix) && prefixes.contains(objectPrefix)) {
      val cluster = Cluster.builder.addContactPoints("127.0.0.1").build
      val session = cluster.newSession()
      session.execute(insertTriple)
      session.closeAsync()
    }
  }

  def addTriplesToDatabase(triples: RDD[Array[String]], prefixes: Array[String]): Unit = {
    triples.foreach( x => addTripleToDatabase(x, prefixes) )
  }
/*
  Now as you read triples, you need to check if there is an entity already existing and link it before storing in database.

    the other add on technique is to use word2vec to find similar entities.
*/
  def getSubjectPrefix(triple: RelationTriple): String = {
    val tokens=triple.canonicalSubject
    val result: Array[(String, String)]=Array()
    tokensToEntities(result, tokens, 0).map( x => x._2 ).mkString("_")
  }

  def getObjectPrefix(triple: RelationTriple): String = {
    val tokens=triple.canonicalObject
    val result: Array[(String, String)]=Array()
    tokensToEntities(result, tokens, 0).map( x => x._2 ).mkString("_")
  }

  @tailrec
  def triplesToArray(result: Array[Array[String]], it: util.Iterator[RelationTriple]): Array[Array[String]] = {
    if (it.hasNext) {
      val tempTriple=it.next()
      val subjectPrefix=getSubjectPrefix(tempTriple)
      val objectPrefix=getObjectPrefix(tempTriple)
      val tripleList=Array(tempTriple.subjectLemmaGloss(), tempTriple.relationLemmaGloss(), tempTriple.objectLemmaGloss(), subjectPrefix, objectPrefix)
      triplesToArray(result:+tripleList, it)
    }
    else { result }
  }

  def getTriples(sentence: String): Array[Array[String]] = {
    val props = new Properties()
    props.setProperty("ner.useSUTime", "false")
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse,natlog,openie")
    val pipeline=new StanfordCoreNLP(props)
    val doc=new Annotation(sentence.toString)
    pipeline.annotate(doc)
    val sentences=doc.get(classOf[CoreAnnotations.SentencesAnnotation])
    val triple=sentences.get(0).get(classOf[NaturalLogicAnnotations.RelationTriplesAnnotation])
    val result: Array[Array[String]]=Array()
    val it: util.Iterator[RelationTriple]=triple.iterator()
    triplesToArray(result, it)
  }


}

class TripleInfo(s: String, sp: String, r: String, rp: String, o: String, op: String) {
  val subjectStr: String=s
  val subjectPrefix: String=sp
  val relationStr: String=r
  val relationPrefix: String=rp
  val objectStr: String=o
  val objectPrefix: String=op

}
