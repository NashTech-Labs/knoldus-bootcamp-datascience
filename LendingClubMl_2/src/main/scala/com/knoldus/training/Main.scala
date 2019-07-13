package com.knoldus.training

import java.io.{File, PrintWriter}

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.{Transformers, UDFs}
import org.apache.log4j.Logger
import org.apache.spark.ml.{Model, Pipeline, PipelineModel}
import org.apache.spark.ml.classification._
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.sql.{Column, DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.ml.regression.RegressionModel
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.ml.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.ml.stat.{ChiSquareTest, Correlation}
import org.apache.spark.sql.functions._
import com.ibm.aardpfark.spark.ml.SparkSupport.toPFA
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine

import ml.combust.bundle.BundleFile
import ml.combust.mleap.spark.SparkSupport._

import org.apache.spark.ml.bundle.SparkBundleContext

import scala.io.Source

object LogObject {
  val LOGGER: Logger = KLogger.getLogger(this.getClass)
}

object Main {

  def main(args: Array[String]):Unit = {

    val sparkMaster="spark://192.168.1.5:7077"

    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .config("spark.some.config.option", "some-value")
      .master(sparkMaster)
      .getOrCreate()

    AppConfig.setSparkSession(spark)
    import spark.implicits._

    spark.sparkContext.setLogLevel("WARN")

    val path=args(0)
    val lrOutPath=args(1)
    val lsvmOutPath=args(2)
    val dtOutPath=args(3)

    val timeStart=System.currentTimeMillis()

    val df=readCsvFile(path, spark).persist

    val Array(training, test) = df.randomSplit(Array(0.9, 0.1), seed = 0)
    val cleanedTraining=cleanDataCols(training, spark).persist
    val cleanedTest=cleanDataCols(test, spark).persist

    df.unpersist

    calcCorrelationMatrix(cleanedTraining)

    val (numNegativeTraining, numPositiveTraining) = countNumNegativePositive(cleanedTraining)
    val (numNegativeTest, numPositiveTest) = countNumNegativePositive(cleanedTest)

    LogObject.LOGGER.info("numNegativeTraining= " + numNegativeTraining + " numPositiveTraining= " + numPositiveTraining)
    LogObject.LOGGER.info("numNegativeTest= " + numNegativeTest + " numPositiveTest= " + numPositiveTest)

    val (lrModel, lsvmModel, dtModel)=testModels(cleanedTraining, cleanedTest)

    lrModel.save(lrOutPath)
    lsvmModel.save(lsvmOutPath)
    dtModel.save(dtOutPath)

    val timeEnd=System.currentTimeMillis()

    LogObject.LOGGER.info("Total run time: " + (timeEnd-timeStart) + " ms")

    spark.stop()

  }

  def readCsvFile(path: String, spark: SparkSession): DataFrame = {
    spark.read
      .format("csv")
      .option("header", "true")
      .option("inferSchema", true)
      .load(path)
  }

  def calcCorrelationMatrix(df: DataFrame): (Matrix, Matrix) = {
    val Row(coeff1: Matrix) = Correlation.corr(df, "features").head
    LogObject.LOGGER.info("Pearson correlation matrix:\n" + coeff1.toString)

    val Row(coeff2: Matrix) = Correlation.corr(df, "features", "spearman").head
    LogObject.LOGGER.info("Spearman correlation matrix\n" + coeff2.toString)

    (coeff1, coeff2)
  }

  def testModels(training: DataFrame, test: DataFrame):
  (LogisticRegressionModel, LinearSVCModel, DecisionTreeClassificationModel) = {
    val lrModel=logisticRegression(training)
    val testPredict=lrModel.transform(test)
    //saveLogisticRegession(cvModel, outPath)

    testPredict.show(false)
    LogObject.LOGGER.info("Metrics for logistic regression")
    calcMetrics(testPredict)

    //readLogisticRegression(outPath)

    val lsvmModel = linearSupportVectorMachine(training)
    val testPredictLsvm=lsvmModel.transform(test)
    LogObject.LOGGER.info("Metrics for LSVM")
    calcMetrics(testPredictLsvm)

    val dtModel=decisionTree(training)
    val testPredictDt=dtModel.transform(test)

    val pfa = toPFA(dtModel, true)
    println("pfa= " + pfa)

    testPredictDt.show(20)

    LogObject.LOGGER.info("Metrics for decision tree")
    calcMetrics(testPredictDt)

    (lrModel, lsvmModel, dtModel)
  }

  def countNumNegativePositive(df: DataFrame): (Long, Long) = {
    val numPositive=df.filter("label==1.0").count
    val numNegative=df.count-numPositive
    (numNegative, numPositive)
  }

  def readLogisticRegression(path: String): Unit = {
    //val pfaJson=Source.fromFile(path)
    val pfaJson = Source.fromFile(path).getLines.mkString
    println(pfaJson)
    val engine=PFAEngine.fromJson(pfaJson, multiplicity = 1).head
    val input=
      """
        |{"features":[1000.0,36.0,12.98,33.69,1.0,5.0,2.0,55000.0]}
      """.stripMargin
    val result = engine.action(engine.jsonInput(input))
    println("engine= " + result)
  }

  def saveLogisticRegession(model: LogisticRegressionModel, path: String): Unit = {
    val pfa = toPFA(model, true)

    println(pfa)
    val engine=PFAEngine.fromJson(pfa, multiplicity = 1).head
    val input=
      """
        |{"features":[1000.0,36.0,12.98,33.69,1.0,5.0,2.0,55000.0]}
      """.stripMargin
    val result = engine.action(engine.jsonInput(input))
    println("engine1= " + result)

    val pw=new PrintWriter(new File(path))
    pw.write("" + pfa)
    pw.close()
  }

  /*
  def saveMLeapModel(model: LogisticRegressionModel, df: DataFrame, path: String): Unit = {
    val sbc = SparkBundleContext().withDataset(model.transform(df))
    for {bf <- managed(BundleFile(path))} {
      model.writeBundle.save(bf)(sbc).get
    }
  }
  */

  def calcMetrics(df: DataFrame): Unit = {
    val predictionAndLabel=df.select("prediction", "label").rdd
      .map( x => (x.getDouble(0), x.getDouble(1)))

    val metrics=new MulticlassMetrics(predictionAndLabel)

    for {label <- metrics.labels} {
      LogObject.LOGGER.info("Precision= " + metrics.precision(label))
      LogObject.LOGGER.info("Recall= " + metrics.recall(label))
      LogObject.LOGGER.info("F-Measure= " + metrics.fMeasure(label))
    }
    LogObject.LOGGER.info("Confusion matrix: \n" + metrics.confusionMatrix)

  }

  //TODO These functions should be combined into one
  def logisticRegression(df: DataFrame): LogisticRegressionModel = {
    val lr=new LogisticRegression()

    val lrModel=lr.fit(df)
    LogObject.LOGGER.info("Coefficients= " + lrModel.coefficients)
    LogObject.LOGGER.info("Intercept= " + lrModel.intercept)

    val pipeline = new Pipeline().setStages(Array(lr))
    val paramGrid = new ParamGridBuilder().addGrid(lr.regParam, Array(0.1, 0.01)).build
    val cv = new CrossValidator().setEstimator(pipeline).setEvaluator(new BinaryClassificationEvaluator())
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3)

    val cvModel=cv.fit(df)


    val bestPipelineModel = cvModel.bestModel.asInstanceOf[PipelineModel]
    val stages = bestPipelineModel.stages
    val bestModel=stages(0).asInstanceOf[LogisticRegressionModel]
    bestModel
  }

  def decisionTree(df: DataFrame): DecisionTreeClassificationModel = {
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setMaxDepth(30)

    val pipeline = new Pipeline().setStages(Array(dt))

    val paramGrid = new ParamGridBuilder().addGrid(dt.maxDepth, Array(1, 2, 5, 10, 20, 30)).build

    val cv = new CrossValidator().setEstimator(pipeline).setEvaluator(new BinaryClassificationEvaluator())
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3)

    val cvModel=cv.fit(df)
    val bestPipelineModel = cvModel.bestModel.asInstanceOf[PipelineModel]
    val bestModel=bestPipelineModel.stages(0).asInstanceOf[DecisionTreeClassificationModel]

    val model = pipeline.fit(df)
    val predictions=model.transform(df)

    predictions.show

    bestModel
  }

  def linearSupportVectorMachine(df: DataFrame): LinearSVCModel = {
    val lsvc = new LinearSVC().setRegParam(0.1)
    val lsvcModel = lsvc.fit(df)

    val pipeline = new Pipeline().setStages(Array(lsvc))

    val paramGrid = new ParamGridBuilder().addGrid(lsvc.regParam, Array(0.01, 0.1, 1.0)).build

    val cv = new CrossValidator().setEstimator(pipeline).setEvaluator(new BinaryClassificationEvaluator())
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3)

    val cvModel=cv.fit(df)
    val bestPipelineModel = cvModel.bestModel.asInstanceOf[PipelineModel]
    val bestModel=bestPipelineModel.stages(0).asInstanceOf[LinearSVCModel]

    val model = pipeline.fit(df)
    val predictions=model.transform(df)

    predictions.show

    bestModel
  }

  def cleanCols(df: DataFrame): Array[Column] = {
    val transformations=List(Transformers.getColTermInt()(_), Transformers.getColGradeInt()(_),
      Transformers.getColSubgradeInt()(_), Transformers.getColLabel()(_), Transformers.getColEmpLengthInt()(_),
      Transformers.getColAnnualInc()(_), Transformers.getColIssueDateInt()(_),// Transformers.getColHomeOwnership()(_),
      Transformers.getColVerificationStatusDouble()(_), Transformers.getColPymntPlanDouble()(_),
      Transformers.getColDtiDouble()(_), Transformers.getColDelinq2YrsInt()(_), Transformers.getColZipcodeInt()(_),
      Transformers.getColEarliestCrLineInt()(_), Transformers.getColInqLast6MthsInt()(_),
      //Transformers.getColMthsSinceLastDelinqInt()(_), gfg
      Transformers.getColOpenInt()(_),
      Transformers.getColPubRecInt()(_), Transformers.getColRevolBalDouble()(_),
      Transformers.getColRevolUtilDouble()(_), Transformers.getColTotalAccInt()(_),

      Transformers.getColOptionDouble()(_:DataFrame)("out_prncp", "out_prncp_double"),
      //Transformers.getColOutPrncpDouble()(_),

      Transformers.getColOptionDouble()(_:DataFrame)("total_pymnt", "total_pymnt_double"),
      Transformers.getColOptionDouble()(_:DataFrame)("total_rec_prncp", "total_rec_prncp_double"),
      Transformers.getColOptionDouble()(_:DataFrame)("total_rec_int", "total_rec_int_double"),
      Transformers.getColOptionDouble()(_:DataFrame)("total_rec_late_fee", "total_rec_late_fee_double"),
      Transformers.getColOptionDouble()(_:DataFrame)("recoveries", "recoveries_double"),
      Transformers.getColOptionDouble()(_:DataFrame)("collection_recovery_fee", "collection_recovery_fee_double"),
      Transformers.getColOptionInt()(_:DataFrame)("policy_code", "policy_code_int"),
      Transformers.getColApplicationTypeInt()(_)
    )

    transformations.map( func => func(df) ).toArray
  }

  def cleanDataCols(df: DataFrame, spark: SparkSession): DataFrame = {
    val cols=cleanCols(df)
    //val cols=transformations.map( func => func(df) ).toArray

    val df2=df.select(cols: _*)

    df2.show

    val goodFeatures=Array("issue_dInt", "out_prncp_double", "dti_double", "total_rec_prncp_double", "annual_incInt")
    //val goodFeatures=Array("out_prncp_double")

    val assembler = new VectorAssembler()
      .setInputCols(df2.columns.filter( x => x!="label" ))
      //.setInputCols(goodFeatures)
      .setOutputCol("features")

    val dfCleaned=assembler.transform(df2)

    dfCleaned.show(false)

    dfCleaned
  }

  def cleanDataFoldLeft(df: DataFrame, spark: SparkSession): DataFrame = {
    val transformations=List(Transformers.withTermInt()(_),
      Transformers.withGradeInt()(_),
      Transformers.withSubgradeInt()(_),
      Transformers.withEmpLengthInt()(_),
      Transformers.withLabel()(_),
      Transformers.withAnnualInc()(_),
      Transformers.withIssueDateInt()(_),
      //Transformers.withHomeOwnership()(_),
      Transformers.withVerificationStatusDouble()(_),
      Transformers.withPymntPlanDouble()(_)
    )

    val df2=transformations.foldLeft(df)((acc, func) => acc.transform(func))

    val features = Array("loan_amnt", "termInt", "int_rate", "installment",
      "gradeInt", "subgradeInt", "emp_length_int", "annual_incInt",// "home_ownership_indexed",
      "verification_status_double", "pymnt_plan_double")

    val assembler = new VectorAssembler().setInputCols(features)
      .setOutputCol("features")

    assembler.transform(df2)
  }

  def cleanDataTransforms(df: DataFrame, spark: SparkSession): DataFrame = {
    import spark.implicits._

    val df2=df.transform(Transformers.withTermInt())
      .transform(Transformers.withGradeInt())
      .transform(Transformers.withSubgradeInt())
      .transform(Transformers.withLabel())


    val assembler = new VectorAssembler().setInputCols(Array("loan_amnt", "termInt", "gradeInt", "subgradeInt"))
      .setOutputCol("features")

    assembler.transform(df2)

  }
  /*
  def cleanDataMessy(df: DataFrame, spark: SparkSession): DataFrame = {
    import spark.implicits._

    val termDf=df.withColumn("termInt", UDFs.termToIntUDF($"term"))
    val gradeDf=termDf.withColumn("gradeInt", UDFs.gradeToIntUDF($"grade"))
    val subgradeDf=gradeDf.withColumn("subgradeInt", UDFs.subgradeToIntUDF($"sub_grade"))
    val emp_length_intDf=subgradeDf.withColumn("emp_length_int", UDFs.empLengthToIntUDF($"emp_length"))
    val issueDateInt=emp_length_intDf.withColumn("issue_dInt", UDFs.issueDateToIntUDF($"issue_d"))
    val loanStatusDf=issueDateInt.withColumn("label", UDFs.loanStatusToDoubleUDF($"loan_status"))
    val annualIncDf=loanStatusDf.withColumn("annual_incInt", UDFs.annualIncToDoubleUDF($"annual_inc"))
    val labelIndexer = new StringIndexer().setInputCol("home_ownership").setOutputCol("home_ownership_indexed")
    val homeOwnershipDf=labelIndexer.fit(annualIncDf).transform(annualIncDf)

    val verificationStatusDf=homeOwnershipDf
      .withColumn("verification_status_double", UDFs.verificationStatusToDoubleUDF($"verification_status"))

    val pymntPlanDf=verificationStatusDf.withColumn("pymnt_plan_double", UDFs.pymntPlanToDoubleUDF($"pymnt_plan"))

    val labelIndexer2 = new StringIndexer()
      .setInputCol("purpose")
      .setOutputCol("purpose_indexed")
      .setHandleInvalid("skip")

    //val purposeDf=labelIndexer2.fit(pymntPlanDf).transform(pymntPlanDf)

    val dtiNullDf=fillNull(pymntPlanDf, "dti_null", "dti", "10.0")
    val dtiDf=dtiNullDf.withColumn("dti_double", UDFs.stringToDoubleUDF($"dti_null"))
    val delinq2yrsDf=dtiDf.withColumn("delinq_2yrs_int", UDFs.stringToIntUDF($"delinq_2yrs"))

    val zipcodeNullDf=delinq2yrsDf
      .withColumn("zip_code_not_null", when($"zip_code".isNull, "0").otherwise($"zip_code"))
    val zipcodeDf=zipcodeNullDf.withColumn("zip_code_int", UDFs.zipcodeToIntUDF($"zip_code_not_null"))

    val labelIndexer3 = new StringIndexer()
      .setInputCol("addr_state")
      .setOutputCol("addr_state_index")

    val addrStateDf=labelIndexer3.fit(zipcodeDf).transform(zipcodeDf)

    val earliestCrLineNullDf=fillNull(zipcodeDf, "earliest_cr_line_null", "earliest_cr_line", "DEC-1995")
    val earliestCrLineDf=earliestCrLineNullDf.withColumn("earliest_cr_line_int", UDFs.issueDateToIntUDF($"earliest_cr_line_null"))
    val inqLast6MthsDf=earliestCrLineDf.withColumn("inq_last_6mths_int",UDFs.stringToIntUDF($"inq_last_6mths"))
    val mthsSinceLastDelinqDf=fillNull(inqLast6MthsDf, "mths_since_last_delinq_null", "mths_since_last_delinq", "1000")
    val mthsSinceLastDelinqDf2=mthsSinceLastDelinqDf.withColumn("mths_since_last_delinq_int", UDFs.stringToIntUDF($"mths_since_last_delinq_null"))
    val openAccDf=mthsSinceLastDelinqDf2.withColumn("open_acc_int", UDFs.optionToIntUDF($"open_acc"))
    val pubRecDf=openAccDf.withColumn("pub_rec_int", UDFs.optionToIntUDF($"pub_rec"))
    val revolBalDf=pubRecDf.withColumn("revol_bal_double", UDFs.optionToIntUDF($"revol_bal"))
    val revolUtilDf=revolBalDf.withColumn("revol_util_double", UDFs.optionToDoubleUDF($"revol_util"))
    val totalAccDf=revolUtilDf.withColumn("total_acc_int", UDFs.optionToIntUDF($"total_acc"))
    val outPrncpDf=totalAccDf.withColumn("out_prncp_double", UDFs.optionToDoubleUDF($"out_prncp"))
    val totalPymntDf=outPrncpDf.withColumn("total_pymnt_double", UDFs.optionToDoubleUDF($"total_pymnt"))
    val totalRecPrncpDf=totalPymntDf.withColumn("total_rec_prncp_double", UDFs.optionToDoubleUDF($"total_rec_prncp"))
    val totalRecIntDf=totalRecPrncpDf.withColumn("total_rec_int_double", UDFs.optionToDoubleUDF($"total_rec_int"))
    val totalRecLateFeeDf=totalRecIntDf.withColumn("total_rec_late_fee_double", UDFs.optionToDoubleUDF($"total_rec_late_fee"))
    val recoveriesDf=totalRecLateFeeDf.withColumn("recoveries_double", UDFs.optionToDoubleUDF($"recoveries"))
    val collectionRecoveryFeeDf=recoveriesDf.withColumn("collection_recovery_fee_double", UDFs.optionToDoubleUDF($"collection_recovery_fee"))
    val policyCodeDf=collectionRecoveryFeeDf.withColumn("policy_code_int", UDFs.optionToIntUDF($"policy_code"))
    val applicationTypeDf=policyCodeDf.withColumn("application_type_int", UDFs.applicationTypeToIntUDF($"application_type"))

    //val selectedDf=annualIncDf.select("loan_amnt", "termInt", "int_rate", "installment", "gradeInt",
    //"subgradeInt", "emp_length_int", "annual_incInt", "label")


    val assembler = new VectorAssembler().setInputCols(Array("loan_amnt", "termInt", "int_rate", "installment",
      "gradeInt", "subgradeInt", "emp_length_int", "annual_incInt", "home_ownership_indexed",
      "verification_status_double", "pymnt_plan_double", "zip_code_int",
      "dti_double", "delinq_2yrs_int", "earliest_cr_line_int", "inq_last_6mths_int", "mths_since_last_delinq_int",
      "open_acc_int", "pub_rec_int", "revol_bal_double", "revol_util_double", "total_acc_int", "out_prncp_double",
      "total_pymnt_double", "total_rec_prncp_double", "total_rec_int_double", "total_rec_late_fee_double",
      "recoveries_double", "collection_recovery_fee_double", "policy_code_int", "application_type_int"))
      .setOutputCol("features")

    assembler.transform(applicationTypeDf)
  }
  */
}
