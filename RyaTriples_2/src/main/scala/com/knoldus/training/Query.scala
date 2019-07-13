package com.knoldus.training

import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.eclipse.rdf4j.model.{Model, Statement, ValueFactory}
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


object Query {
  def main(args: Array[String]): Unit = {
    exampleQuery()
  }

  def exampleQuery(): Unit = {
    val db=new SailRepository(new MemoryStore)
    db.init()

    val conn=db.getConnection


    val filename="/test.ttl"
    val input=getClass.getResourceAsStream(filename)
    conn.add(input, "", RDFFormat.TURTLE)

    val queryString="PREFIX kn: <http://www.knoldus.com/> \n"+
      "SELECT ?PURCHASE_ORDER_NUMBER ?TOTAL_AMOUNT\n"+
      "WHERE { \n"+
      "    ?s kn:PURCHASE_ORDER_NUMBER ?PURCHASE_ORDER_NUMBER .\n"+
      "    ?s kn:TOTAL_AMOUNT ?TOTAL_AMOUNT. \n"+
      "    ?s kn:VENDOR_CITY 'MUSCATINE'. \n"+
      "    ?s kn:DEPARTMENT_NAME 'HUMAN DEVELOPMENT AND SERVICES';"+
      "}"


    val query=conn.prepareTupleQuery(queryString)

    val result=query.evaluate()

    while (result.hasNext) {
      val solution=result.next()
      println("Order= "+solution.getValue("PURCHASE_ORDER_NUMBER") + " " + solution.getValue("TOTAL_AMOUNT"))
    }

    println("DONE")
  }

}
