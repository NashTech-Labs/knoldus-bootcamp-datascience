package com.knoldus.training

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
import org.eclipse.rdf4j.model.vocabulary.{FOAF, RDF}
import org.eclipse.rdf4j.query
import org.eclipse.rdf4j.rio.{RDFFormat, Rio}
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.http.HTTPRepository
import java.io._

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore

object QueryFromFile {

  def main(args: Array[String]): Unit = {
    getKingsOfPrussia()
  }

  def getKingsOfPrussia(): Unit = {
    val db=new SailRepository(new MemoryStore)
    db.init()

    val conn=db.getConnection


    val filename="/out.ttl"
    val input=getClass.getResourceAsStream(filename)
    conn.add(input, "", RDFFormat.TURTLE)
    val queryString="PREFIX kn: <http://www.knoldus.com/> \n"+
      "PREFIX foaf: <"+ FOAF.NAMESPACE + "> \n"+
      "SELECT ?s ?n \n"+
      "WHERE { \n"+
      "    ?s a kn:King. \n"+
      "    ?s foaf:based_near kn:Prussia;"+
      "}"

    val query=conn.prepareTupleQuery(queryString)

    val result=query.evaluate()

    while (result.hasNext) {
      val solution=result.next()
      println("King of Prussia= "+solution.getValue("s"))
    }
  }

}
