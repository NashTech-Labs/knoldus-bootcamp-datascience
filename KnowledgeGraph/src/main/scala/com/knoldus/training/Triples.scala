package com.knoldus.training

import java.util
import java.util.Properties

import com.datastax.driver.core.Cluster
import com.knoldus.common.{AppConfig, KLogger}
import org.eclipse.rdf4j.repository.RepositoryConnection

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

import org.eclipse.rdf4j.model.{Model, ValueFactory, Statement}
import org.eclipse.rdf4j.model.impl.{SimpleValueFactory, TreeModel}
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.{FOAF, RDF, DC, SKOS, DCAT, OWL}
import org.eclipse.rdf4j.query
import org.eclipse.rdf4j.rio.{RDFFormat, Rio}
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.http.HTTPRepository
import java.io._

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore

object Triples {

  def main(args: Array[String]):Unit = {
    //val cluster = Cluster.builder.addContactPoints("127.0.0.1").build
    //val session = cluster.newSession()

    // Logging Demonstration
    val LOGGER: Logger = KLogger.getLogger(this.getClass)

    val conf = new SparkConf().setMaster("local[2]").setAppName("Enteties")
    val sc = new SparkContext(conf)

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

    /*
    val dfPrefixes=spark
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "prefixes", "keyspace" -> "knowledge_graph"))
      .load()

    val prefixes=dfPrefixes.select("prefix").rdd.map( r => r(0).toString ).collect()
*/
    val prefixes = Array("LOCATION", "CAUSE_OF_DEATH", "CITY", "COUNTRY", "CRIMINAL_CHARGE", "EMAIL", "IDEOLOGY", "NATIONALITY", "RELIGION",
      "STATE_OR_PROVINCE", "TITLE", "URL", "ORGANIZATION", "MISC", "PERSON", "NUMBER")


    val homeDir = sys.env("HOME")
    val projectDir = homeDir + "/dev/project/TrainingSprints/TrainingSprint3/KnowledgeGraph"
    //val path=projectDir + "/data"
    //val path = projectDir + "/SmallData"
    //val path=projectDir+"/SmallerData"
    //val path=projectDir+"/VerySmallData"

    for (i <- 0 until 995) {
      val path=projectDir+"/SplitData/"+i
      val outfile=projectDir+"/src/main/resources/out_"+i+".ttl"
      makeModelForDir(sc, projectDir, path, prefixes, outfile)
    }

  }

  def makeModelForDir(sc: SparkContext, projectDir: String, path: String, prefixes: Array[String], outfile: String): Unit = {
    val rdd=sc.wholeTextFiles(path)
    println(rdd.count)
    //rdd.take(1).foreach(println)
    val rddSentences=rdd.flatMap( x => getSentencesCoreMap(x._2) )
    println(rddSentences.count)
    val triples=rddSentences.flatMap( x => getTriplesFromCoreMap(x) )
    //triples.take(1000).foreach(println)
    println("ntriple= "+triples.count())
    //triples.take(1000).foreach(println)

    addTriplesToDatabase(triples, prefixes)
    val collectedTriples=triples.collect()

    val model=new TreeModel()
    collectedTriples.foreach( x => addToModel(x, model) )

    writeModel(outfile, model)
    //session.closeAsync()

  }

  def writeModel(filename: String, model: Model): Unit = {
    val pw=new PrintWriter(new File(filename))
    Rio.write(model, pw, RDFFormat.TURTLE)
  }

  def isLocationPhrase(phrase: String): Boolean = {
    phrase=="be in" || phrase==" be of"
  }

  def isLocationPrefix(prefix: String): Boolean = {
    prefix=="LOCATION" || prefix=="CITY" || prefix=="COUNTRY" || prefix=="REGION" || prefix=="STATE_OR_PROVINCE"
  }

  def addToModel(triple: Array[String], model: Model): Unit = {
    val url="http://knoldus.com/"

    val subjectPrefix=triple(3)
    val subjectUri=url+subjectPrefix+"/"+triple(0)
    val predicatePrefix=triple(1)
    //val predicateUri=triple(1)
    val predicateUri=triple(5)
    val objectValue=triple(2).replaceAll(" ", "_")
    val objectPrefix=triple(4)
    val objectUri=url+objectPrefix+"/"+triple(2)

    val subject=triple(0).replaceAll(" ", "_")
    val predicate=triple(1)

    println(subject+" "+subjectPrefix+" | "+predicate+" "+" "+predicatePrefix+" | "+objectValue+" "+objectPrefix)

    val knoldus="http://www.knoldus.com/"

    val vf=SimpleValueFactory.getInstance()

    if (subjectPrefix=="PERSON" && predicate=="be" && objectPrefix=="TITLE") {

      val person=vf.createIRI(knoldus, subject)
      val title=vf.createIRI(knoldus, objectValue)


      model.add(person, RDF.TYPE, title)
    }
    else if (subjectPrefix=="PERSON" && isLocationPhrase(predicate) && isLocationPrefix(objectPrefix)) {
      val person=vf.createIRI(knoldus, subject)
      val location=vf.createIRI(knoldus, objectValue)

      model.add(person, FOAF.BASED_NEAR, location)
    }
    else if (isLocationPrefix(subjectPrefix) && isLocationPhrase(predicate) && isLocationPrefix(objectPrefix)) {
      val location1=vf.createIRI(knoldus, subject)
      val location2=vf.createIRI(knoldus, objectValue)

      model.add(location1, FOAF.BASED_NEAR, location2)
    }
    else if (subjectPrefix=="PERSON" && predicate=="be" && objectPrefix=="NUMBER") {
      val person=vf.createIRI(knoldus, subject)
      val age=vf.createIRI(knoldus, objectValue)

      model.add(person, FOAF.AGE, age)
    }


  }

  def importFoafTtl(): Model = {
    val filename="foaf.ttl"
    val input: InputStream=getClass.getResourceAsStream(filename)
    val model=Rio.parse(input, "foaf", RDFFormat.TURTLE)

    println(model)

    model
  }

  def addTripleToDatabase(triple: Array[String], prefixes: Array[String]): Unit = {


    val url="http://knoldus.com/"

    val subjectPrefix=triple(3)
    val subjectUri=url+subjectPrefix+"/"+triple(0)
    val predicatePrefix=triple(1)
    //val predicateUri=triple(1)
    val predicateUri=triple(5)
    val objectValue=triple(2)
    val objectPrefix=triple(4)
    val objectUri=url+objectPrefix+"/"+triple(2)

    val subject=triple(0)
    val predicate=triple(1)


    println(subject+" "+subjectPrefix+" | "+predicate+" "+" "+predicatePrefix+" | "+objectValue+" "+objectPrefix)

    val model=new TreeModel()
    if (subjectPrefix=="PERSON" && predicate=="be" && objectPrefix=="TITLE") {
      val knoldus="http://www.knoldus.com/"

      val vf=SimpleValueFactory.getInstance()
      val person=vf.createIRI(knoldus, subject)
      val title=vf.createIRI(knoldus, objectValue)


      model.add(person, RDF.TYPE, title)

      val filename="out.ttl"
      val pw=new PrintWriter(new File(filename))
      Rio.write(model, pw, RDFFormat.TURTLE)
    }

    /*
    val insertTriple="insert into knowledge_graph.triples "+
    "(Subject_prefix, subject_uri, predicate_prefix, predicate_uri, object_value, object_prefix, object_uri, last_updated) values "+
    "('"+subjectPrefix+"', '"+subjectUri+"', '"+predicatePrefix+"', '"+predicateUri+"', '"+objectValue+"', '"+objectPrefix+"', '"+objectUri+"', toTimestamp(now()));"

    println(insertTriple)
    if (prefixes.contains(subjectPrefix) && prefixes.contains(objectPrefix)) {
      val cluster = Cluster.builder.addContactPoints("127.0.0.1").build
      val session = cluster.newSession()
      session.execute(insertTriple)
      session.closeAsync()
    }
    */
  }

  def addTriplesToDatabase(triples: RDD[Array[String]], prefixes: Array[String]): Unit = {
    triples.foreach( x => addTripleToDatabase(x, prefixes) )
  }
/*
  Now as you read triples, you need to check if there is an entity already existing and link it before storing in database.

    the other add on technique is to use word2vec to find similar entities.
*/

  def compress[A](l: List[A]): List[A] = {
    if (l.isEmpty) { l }
    else if (l.tail.isEmpty) { l }
    else if ( l.head==l.tail.head ) { compress(l.tail) }
    else { l.head::compress(l.tail) }
  }

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

  def getPrefix(triple: RelationTriple, tripleType: String): String = {
    val tokens = if (tripleType=="subject") { triple.canonicalSubject } else { triple.canonicalObject }
    val result: Array[(String, String)]=Array()
    val entities=tokensToEntities(result, tokens, 0).map( x => x._2 )
    compress(entities.toList).mkString("_")
      .replaceAll("TITLE_PERSON_PERSON", "PERSON")
      .replaceAll("TITLE_PERSON", "PERSON")
  }

  @tailrec
  def triplesToArray(result: Array[Array[String]], it: util.Iterator[RelationTriple]): Array[Array[String]] = {
    if (it.hasNext) {
      val triple=it.next()
      val subjectPrefix=getPrefix(triple, "subject")
      val objectPrefix=getPrefix(triple, "object")
      val tripleList=Array(triple.subjectLemmaGloss(), triple.relationLemmaGloss(), triple.objectLemmaGloss(), subjectPrefix, objectPrefix, triple.isPrefixBe.toString)
      triplesToArray(result:+tripleList, it)
      //triplesToArray(result:+tripleInfo, it)
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

  def getTriplesFromCoreMap(sentence: CoreMap): Array[Array[String]] = {
    val triple=sentence.get(classOf[NaturalLogicAnnotations.RelationTriplesAnnotation])
    val result: Array[Array[String]]=Array()
    val it: util.Iterator[RelationTriple]=triple.iterator()
    triplesToArray(result, it).filter( x=> x(3)!="O" )
  }


}

class TripleInfo(s: String, sp: String, r: String, o: String, op: String, isBe: Boolean) {
  val subjectStr: String=s
  val subjectPrefix: String=sp
  val relationStr: String=r
  val objectStr: String=o
  val objectPrefix: String=op
  val isPrefixBe: Boolean=isBe
}
