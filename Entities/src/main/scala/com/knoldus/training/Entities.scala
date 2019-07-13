package com.knoldus.training

import com.datastax.driver.core.{Cluster, Session}
import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.ling._
import edu.stanford.nlp.pipeline._
import java.util._
import java.io.Serializable

import org.apache.spark.rdd.RDD

import scala.annotation.tailrec

object Entities {


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

    val homeDir=sys.env("HOME")
    val projectDir=homeDir + "/dev/project/TrainingSprints/TrainingSprint3/Entities"
    //val path=projectDir + "/data"
    val path=projectDir+"/SmallerData"

    val rdd=sc.wholeTextFiles(path)
    println(rdd.count)
    //rdd.take(1).foreach(println)
    val rddSentences=rdd.flatMap( x => getSentences(x._2) )
    println(rddSentences.count)
    val entities=rddSentences.flatMap( x => getEntities(x) )
    entities.take(1000).foreach(println)

    createTables(session)
    addEntitiesToDatabase(entities, session)

    session.closeAsync()
  }

  /*
  CREATE TABLE knowledge_graph.prefixes (

    prefix text PRIMARY KEY,

    last_updated timestamp

  )
  CREATE TABLE knowledge_graph.triples (

Subject_prefix text,

subject_uri text,

predicate_prefix text,

predicate_uri text,

object_value text,

last_updated timestamp,

object_prefix text,

object_uri text,

source text,

PRIMARY KEY (subject_uri, predicate_uri, object_value)

)

CREATE TABLE knowledge_graph.triples (

subject_uri text,

predicate_uri text,

object_value text,

last_updated timestamp,

object_uri text,

source text,

PRIMARY KEY (subject_uri, predicate_uri, object_value)

)
**/


  def createTables(session: Session): Unit = {
    session.execute("DROP KEYSPACE IF EXISTS knowledge_graph;")
    session.execute("CREATE KEYSPACE knowledge_graph WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 } ;")
    session.execute("CREATE TABLE knowledge_graph.prefixes (prefix text PRIMARY KEY, last_updated timestamp);")
    session.execute("CREATE TABLE knowledge_graph.uri (prefix_id text, uri text, last_updated timestamp, PRIMARY KEY (prefix_id, uri));")
    session.execute("CREATE TABLE knowledge_graph.triples (Subject_prefix text, subject_uri text, predicate_prefix text, predicate_uri text, object_value text, last_updated timestamp, object_prefix text, object_uri text, source text, PRIMARY KEY (subject_uri, predicate_uri, object_value));")
    session.execute("CREATE TABLE knowledge_graph.paragraphs (documentid uuid, paraid uuid, last_updated timestamp, sentences text, PRIMARY KEY (documentid, paraid));")
    session.execute("CREATE TABLE knowledge_graph.documents (documentid uuid PRIMARY KEY, author text, content text, filename text, last_updated timestamp, published_date timestamp, source text);")
  }

  def addEntityToDatabase(entity: (String, String)): Unit = {
    val cluster = Cluster.builder.addContactPoints("127.0.0.1").build
    val session = cluster.newSession()

    val prefix=entity._2.replaceAll("'", "")
    val entityType=entity._1.replaceAll("'", "")
    val insertPrefix="insert into knowledge_graph.prefixes (prefix, last_updated) values ('"+prefix+"', toTimestamp(now()));"
    val insertUri="insert into knowledge_graph.uri (prefix_id, uri, last_updated) values ('"+prefix+"', 'http://knoldus.com/"+prefix+"/"+entityType+"', toTimestamp(now()))"

    println(insertPrefix)

    session.execute(insertPrefix)
    session.execute(insertUri)
  }

  def addEntitiesToDatabase(entities: RDD[(String, String)], session: Session): Unit = {
    //session.execute("insert into knowledge_graph.prefixes (prefix, last_updated) values ('b', toTimestamp(now()));")
    //entities.foreach( x => session.execute("insert into knowledge_graph.prefixes (prefix, last_updated) values ("+x._2+",toTimestamp(now()));") )
    entities.foreach( x => addEntityToDatabase(x) )
  }

  @tailrec
  def tokensToEntities(result: Array[(String, String)], tokens: List[CoreLabel], n: Int): Array[(String, String)] = {
    if (n==tokens.size()) { result }
    else { tokensToEntities( result:+(tokens.get(n).toString.replaceFirst("-\\d+$", ""), tokens.get(n).get(classOf[CoreAnnotations.NamedEntityTagAnnotation])), tokens, n+1 ) }
  }

  def getEntities(text: String): Array[(String, String)] = {
    val props = new Properties()
    props.setProperty("ner.useSUTime", "false")
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner")
    val pipeline = new StanfordCoreNLP(props)
    val doc=new Annotation(text)
    pipeline.annotate(doc)
    val sentence=doc.get(classOf[CoreAnnotations.SentencesAnnotation]).get(0)
    val tokens=sentence.get(classOf[CoreAnnotations.TokensAnnotation])
    val result: Array[(String, String)]=Array()
    tokensToEntities(result, tokens, 0).filter( x => x._2!="O" )
  }

  @tailrec
  def sentencesToArray(result: Array[String], sentences: List[CoreMap], n: Int): Array[String] = {
    if (n==sentences.size()) { result }
    else { sentencesToArray(sentences.get(n).toString+:result, sentences, n+1) }
  }

  def getSentences(text: String): Array[String] = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize,ssplit")
    val pipeline = new StanfordCoreNLP(props)
    val doc=new Annotation(text)
    pipeline.annotate(doc)
    val sentences=doc.get(classOf[CoreAnnotations.SentencesAnnotation])
    val result: Array[String]=Array()
    sentencesToArray(result, sentences, 0)
  }

}
