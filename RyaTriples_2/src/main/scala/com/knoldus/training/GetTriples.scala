package com.knoldus.training

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Row, SparkSession, functions}
import org.apache.spark.{SparkConf, SparkContext}
import java.util.Properties

import com.knoldus.common.{AppConfig, KLogger}
import org.eclipse.rdf4j.repository.RepositoryConnection

import scala.annotation.tailrec
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl.{SimpleValueFactory, TreeModel}
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary._
import org.eclipse.rdf4j.query
import org.eclipse.rdf4j.rio.{RDFFormat, Rio}
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.http.HTTPRepository
import java.io._

import org.apache.accumulo.core.client.{Connector, ZooKeeperInstance}
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.apache.rya.accumulo.AccumuloRdfConfiguration
import org.apache.rya.accumulo.AccumuloRyaDAO
import org.apache.rya.rdftriplestore.{RdfCloudTripleStore, RyaSailRepository}
import org.apache.accumulo.core.client.AccumuloException
import org.apache.accumulo.core.client.AccumuloSecurityException
import org.apache.accumulo.core.client.Connector
import org.apache.accumulo.core.client.MutationsRejectedException
import org.apache.accumulo.core.client.TableExistsException
import org.apache.accumulo.core.client.TableNotFoundException
//import org.apache.accumulo.core.client.mock.MockInstance
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType}

//import org.apache.any23.rdf.RDFUtils

//import org.apache.any23.extractor.csv

object RdfModel {
  lazy val model=new impl.TreeModel()
}

object RdfValueFactory {
  val vf=SimpleValueFactory.getInstance()
}

object GetTriples {

  def main(args: Array[String]): Unit = {

    // Logging Demonstration
    val LOGGER: Logger = KLogger.getLogger(this.getClass)
    val age = 20
    LOGGER.info("Age " + age )
    LOGGER.warn("This is warning")

    val conf=new SparkConf().setMaster("local[2]").setAppName("RyaTriples")
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

    val path="/home/jouko/dev/project/TrainingSprints/TrainingSprint8/RyaTriples/src/main/resources/Legacy_Purchase_Orders.csv"

    val df = spark.read
      .format("csv")
      .option("header", "true")
      .option("inferSchema", true)
      .load(path)

    df.show(5, false)
    val newColumns=df.columns.map( x => x.replace(" ", "_"))
    val dfRenamed=df.toDF(newColumns: _*).withColumn("ROW_ID", functions.monotonically_increasing_id())
    dfRenamed.columns.foreach(println)

    val knoldus="http://www.knoldus.com/"

    val columnNames=dfRenamed.columns

    columnNames.foreach(println)

    val vf=SimpleValueFactory.getInstance()

    val columnIris=getColumnIris(knoldus, vf, columnNames)

    RdfModel.model

    addColumnsToModel(knoldus)
    //dfRenamed.foreach( x => addToModel(x, columnIris) )

    val outFile="test.ttl"

    writeModel(outFile, RdfModel.model)


    spark.stop()

  }

  def addColumnsToModel(knoldus: String): Unit = {
    val vf=SimpleValueFactory.getInstance()

    val recordIRI = vf.createIRI(knoldus, "record")
    RdfModel.model.add(recordIRI, RDF.TYPE, OWL.CLASS)

    val recordTypeIRI = vf.createIRI(knoldus, "RECORD_TYPE")

    RdfModel.model.add(recordTypeIRI, RDF.TYPE, recordIRI)

    val h = vf.createLiteral("H")
    val d = vf.createLiteral("D")
    RdfModel.model.add(recordTypeIRI, OWL.ONEOF, h)
    RdfModel.model.add(recordTypeIRI, OWL.ONEOF, d)

    //val testIRI = vf.createIRI(knoldus, "TEST")
    //RdfModel.model.add(testIRI, OWL.DATATYPEPROPERTY, OWL.CARDINALITY)

    val requisitionNumberIRI = vf.createIRI(knoldus, "REQUISITION_NUMBER")
    RdfModel.model.add(requisitionNumberIRI, RDF.TYPE, recordIRI)

    val inputDateIRI = vf.createIRI(knoldus, "INPUT_DATE")
    RdfModel.model.add(inputDateIRI, RDF.TYPE, DC.DATE)

    val priceIRI = vf.createIRI(knoldus, "price")
    RdfModel.model.add(priceIRI, RDF.TYPE, OWL.CLASS)

    val totalAmountIRI = vf.createIRI(knoldus, "TOTAL_AMOUNT")
    RdfModel.model.add(totalAmountIRI, RDF.TYPE, priceIRI)

    val departmentNumberIRI = vf.createIRI(knoldus, "DEPARTMENT_NUMBER")
    RdfModel.model.add(departmentNumberIRI, OWL.DATATYPEPROPERTY, OWL.CARDINALITY)

    val departmentIRI = vf.createIRI(knoldus, "department")

    val departmentNameIRI = vf.createIRI(knoldus, "DEPARTMENT_NAME")
    RdfModel.model.add(departmentNameIRI, RDF.TYPE, departmentIRI)

    val costCenterIRI = vf.createIRI(knoldus, "cost_center")
    RdfModel.model.add(costCenterIRI, RDF.TYPE, OWL.CLASS)

    val costCenterNumberIRI = vf.createIRI(knoldus, "COST_CENTER")
    RdfModel.model.add(costCenterNumberIRI, OWL.DATATYPEPROPERTY, OWL.CARDINALITY)

    val costCenterNameIRI = vf.createIRI(knoldus, "COST_CENTER_NAME")
    RdfModel.model.add(costCenterIRI, RDF.PROPERTY, costCenterNameIRI)

    val inputByIRI = vf.createIRI(knoldus, "INPUT_BY")
    RdfModel.model.add(inputByIRI, RDF.TYPE, DC.CONTRIBUTOR)

    val purchasingAgentIRI = vf.createIRI(knoldus, "PURCHASING_AGENT")
    RdfModel.model.add(purchasingAgentIRI, RDF.TYPE, FOAF.AGENT)

    val poTypeIRI = vf.createIRI(knoldus, "po_type")
    RdfModel.model.add(poTypeIRI, RDF.TYPE, OWL.CLASS)

    val poTypeCodeIRI = vf.createIRI(knoldus, "PO_TYPE_CODE")
    RdfModel.model.add(poTypeIRI, RDF.PROPERTY, poTypeCodeIRI)

    val poTypeDescriptionIRI = vf.createIRI(knoldus, "PO_TYPE_DESCRIPTION")
    RdfModel.model.add(poTypeIRI, RDF.PROPERTY, poTypeDescriptionIRI)

    val poCategoryIRI = vf.createIRI(knoldus, "po_category")
    RdfModel.model.add(poCategoryIRI, RDF.TYPE, OWL.CLASS)

    val poCategoryCodeIRI = vf.createIRI(knoldus, "PO_CATEGORY_CODE")
    RdfModel.model.add(poCategoryIRI, RDF.PROPERTY, poCategoryCodeIRI)

    val poCategoryDescriptionIRI = vf.createIRI(knoldus, "PO_CATEGORY_DESCRIPTION")
    RdfModel.model.add(poCategoryIRI, RDF.PROPERTY, poCategoryDescriptionIRI)

    val poStatusIRI = vf.createIRI(knoldus, "po_status")
    RdfModel.model.add(poStatusIRI, RDF.TYPE, OWL.CLASS)

    val poStatusCodeIRI = vf.createIRI(knoldus, "PO_STATUS_CODE")
    RdfModel.model.add(poStatusIRI, RDF.PROPERTY, poStatusCodeIRI)

    val poStatusDescriptionIRI = vf.createIRI(knoldus, "PO_STATUS_DESCRIPTION")
    RdfModel.model.add(poStatusIRI, RDF.PROPERTY, poStatusIRI)

    val vouchedAmountIRI = vf.createIRI(knoldus, "VOUCHED_AMOUNT")
    RdfModel.model.add(vouchedAmountIRI, RDF.TYPE, priceIRI)

    val vendorIRI = vf.createIRI(knoldus, "vendor")
    RdfModel.model.add(vendorIRI, RDF.TYPE, OWL.CLASS)

    val vendorNumberIRI = vf.createIRI(knoldus, "VENDOR_NUMBER")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorNumberIRI)

    val vendorName1IRI = vf.createIRI(knoldus, "VENDOR_NAME_1")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorName1IRI)

    val vendorName2IRI = vf.createIRI(knoldus, "VENDOR_NAME_2")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorName2IRI)

    val vendorAddress1IRI = vf.createIRI(knoldus, "VENDOR_ADDRESS_1")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorAddress1IRI)

    val vendorAddress2IRI = vf.createIRI(knoldus, "VENDOR_ADDRESS_2")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorAddress2IRI)

    val vendorCityIRI = vf.createIRI(knoldus, "VENDOR_CITY")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorCityIRI)

    val vendorStateIRI = vf.createIRI(knoldus, "VENDOR_STATE")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorStateIRI)

    val vendorZipIRI = vf.createIRI(knoldus, "VENDOR_ZIP")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorZipIRI)

    val vendorContactIRI = vf.createIRI(knoldus, "vendor_contact")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorContactIRI)

    val vendorContactNameIRI = vf.createIRI(knoldus, "VENDOR_CONTACT_NAME")
    RdfModel.model.add(vendorContactIRI, RDF.PROPERTY, vendorContactNameIRI)

    val vendorContactTitleIRI = vf.createIRI(knoldus, "VENDOR_CONTACT_TITLE")
    RdfModel.model.add(vendorContactIRI, RDF.PROPERTY, vendorContactTitleIRI)

    val vendorContactPhoneIRI = vf.createIRI(knoldus, "VENDOR_CONTACT_PHONE")
    RdfModel.model.add(vendorContactIRI, RDF.PROPERTY, vendorContactIRI)

    val vendorContactExtensionIRI = vf.createIRI(knoldus, "VENDOR_CONTACT_EXTENSION")
    RdfModel.model.add(vendorContactIRI, RDF.PROPERTY, vendorContactExtensionIRI)

    val vendorMinorityCodeIRI = vf.createIRI(knoldus, "VENDOR_MINORITY_CODE")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorMinorityCodeIRI)

    val vendorMinorityDescriptionIRI = vf.createIRI(knoldus, "VENDOR_MINORITY_DESCRIPTION")
    RdfModel.model.add(vendorIRI, RDF.PROPERTY, vendorMinorityDescriptionIRI)

    val totalItemsIRI = vf.createIRI(knoldus, "TOTAL_ITEMS")
    RdfModel.model.add(totalItemsIRI, OWL.DATATYPEPROPERTY, OWL.CARDINALITY)

    val poBalanceIRI = vf.createIRI(knoldus, "PO_BALANCE")
    RdfModel.model.add(poBalanceIRI, RDF.TYPE, priceIRI)

    val itemIRI = vf.createIRI(knoldus, "item")
    RdfModel.model.add(itemIRI, RDF.TYPE, OWL.CLASS)

    val itemNumberIRI = vf.createIRI(knoldus, "ITEM_NUMBER")
    RdfModel.model.add(itemIRI, RDF.PROPERTY, itemNumberIRI)

    val itemDescriptionIRI = vf.createIRI(knoldus, "ITEM_DESCRIPTION")
    RdfModel.model.add(itemIRI, RDF.PROPERTY, itemDescriptionIRI)

    val itemQuantityOrderedIRI = vf.createIRI(knoldus, "ITEM_QUANTITY_ORDERED")
    RdfModel.model.add(itemQuantityOrderedIRI, OWL.DATATYPEPROPERTY, OWL.CARDINALITY)

    val itemUnitOfMeasureIRI = vf.createIRI(knoldus, "ITEM_UNIT_OF_MEASURE")
    RdfModel.model.add(itemUnitOfMeasureIRI, RDF.TYPE, OWL.CLASS)

    val itemUnitOfMeasureDescriptionIRI = vf.createIRI(knoldus, "ITEM_UNIT_OF_MEASURE_DESCRIPTION")
    RdfModel.model.add(itemUnitOfMeasureDescriptionIRI, RDF.TYPE, OWL.CLASS)

    val itemUnitCostIRI = vf.createIRI(knoldus, "ITEM_UNIT_COST")
    RdfModel.model.add(itemIRI, RDF.PROPERTY, itemUnitCostIRI)

    val itemTotalCostIRI = vf.createIRI(knoldus, "ITEM_TOTAL_COST")
    RdfModel.model.add(itemTotalCostIRI, RDF.TYPE, priceIRI)

    val uniqueIdIRI = vf.createIRI(knoldus, "UNIQUE_ID")
    RdfModel.model.add(uniqueIdIRI, RDF.TYPE, OWL.CLASS)







    val purchaseOrderIRI = vf.createIRI(knoldus, "purchase_order")
    RdfModel.model.add(purchaseOrderIRI, RDF.TYPE, OWL.CLASS)

    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, recordIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, inputDateIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, totalAmountIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, costCenterIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, purchasingAgentIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, poTypeIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, poCategoryIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, vouchedAmountIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, vendorIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, poBalanceIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, itemIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, itemUnitCostIRI)
    RdfModel.model.add(purchaseOrderIRI, RDF.PROPERTY, uniqueIdIRI)

  }

  def addToModel(row: Row, colNames: Array[IRI]): Unit = {


    val knoldus="http://www.knoldus.com/"


    val vf=SimpleValueFactory.getInstance()

      val rowIRI = vf.createIRI(knoldus, "row_" + row.getLong(row.size-1))
      RdfModel.model.add(rowIRI, RDF.TYPE, vf.createLiteral("order"))
      for {j <- 0 until row.size-1} {
        if (row(j)!=null) {
          row.schema.fields(j).dataType match {
            case StringType => RdfModel.model.add(rowIRI, colNames(j), vf.createLiteral(row.getString(j)))
            case LongType => RdfModel.model.add(rowIRI, colNames(j), vf.createLiteral(row.getLong(j)))
            case DoubleType => RdfModel.model.add(rowIRI, colNames(j), vf.createLiteral(row.getDouble(j)))
            case IntegerType => RdfModel.model.add(rowIRI, colNames(j), vf.createLiteral(row.getInt(j)))
          }
        }
      }
  }



  def getColumnIrisRec(knoldus: String, vf: SimpleValueFactory, cols: Array[String], n: Int, result: Array[IRI]): Array[IRI] = {
    if (n==cols.length) { result }
    else { val iri= vf.createIRI(knoldus, cols(n).replace(" ", "_")); getColumnIrisRec(knoldus, vf, cols, n+1, result :+ iri ) }
  }

  def getColumnIris(knoldus: String, vf: SimpleValueFactory, cols: Array[String]): Array[IRI] = {
    val result: Array[IRI]=Array()
    getColumnIrisRec(knoldus, vf, cols, 0, result)
  }

  def writeModel(filename: String, model: Model): Unit = {
    val pw=new PrintWriter(new File(filename))
    Rio.write(model, pw, RDFFormat.TURTLE)
  }

}
