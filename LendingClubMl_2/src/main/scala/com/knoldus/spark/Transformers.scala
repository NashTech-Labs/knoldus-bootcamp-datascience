package com.knoldus.spark
import com.knoldus.common.AppConfig
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

object Transformers {

  val spark = AppConfig.sparkSession
  import spark.implicits._

  def fillNull[T](df: DataFrame, outputCol: String, inputCol: String, default: T): DataFrame = {
    df.withColumn(outputCol, when(col(inputCol).isNull, default).otherwise(col(inputCol)))
  }

  def splitHashedColumn(df: DataFrame): DataFrame = {
    df.select($"hashed".getItem(1).as("emp_hash"))
  }

  def withTermInt()(df: DataFrame): DataFrame = {
    df.withColumn("termInt", UDFs.termToIntUDF($"term"))
  }

  def getColTermInt()(df: DataFrame): Column = {
    UDFs.termToIntUDF(df.col("term")).as("termInt")
  }

  def withGradeInt()(df: DataFrame): DataFrame = {
    df.withColumn("gradeInt", UDFs.gradeToIntUDF($"grade"))
  }

  def getColGradeInt()(df: DataFrame): Column = {
    UDFs.gradeToIntUDF(df.col("grade")).as("gradeInt")
  }

  def withSubgradeInt()(df: DataFrame): DataFrame = {
    df.withColumn("subgradeInt", UDFs.subgradeToIntUDF($"sub_grade"))
  }

  def getColSubgradeInt()(df: DataFrame): Column = {
    UDFs.subgradeToIntUDF(df.col("sub_grade")).as("subgradeInt")
  }

  def withEmpLengthInt()(df: DataFrame): DataFrame = {
    df.withColumn("emp_length_int", UDFs.empLengthToIntUDF($"emp_length"))
  }

  def getColEmpLengthInt()(df: DataFrame): Column = {
    UDFs.empLengthToIntUDF(df.col("emp_length")).as("emp_length_int")
  }

  def withLabel()(df: DataFrame): DataFrame = {
    df.withColumn("label", UDFs.loanStatusToDoubleUDF($"loan_status"))
  }

  def getColLabel()(df: DataFrame): Column = {
    UDFs.loanStatusToDoubleUDF(df.col("loan_status")).as("label")
  }

  def withAnnualInc()(df: DataFrame): DataFrame = {
    df.withColumn("annual_incInt", UDFs.annualIncToDoubleUDF($"annual_inc"))
  }

  def getColAnnualInc()(df: DataFrame): Column = {
    UDFs.annualIncToDoubleUDF(df.col("annual_inc")).as("annual_incInt")
  }

  def withIssueDateInt()(df: DataFrame): DataFrame = {
    df.withColumn("issue_dInt", UDFs.issueDateToIntUDF($"issue_d"))
  }

  def getColIssueDateInt()(df: DataFrame): Column = {
    UDFs.issueDateToIntUDF(df.col("issue_d")).as("issue_dInt")
  }

  def withHomeOwnership()(df: DataFrame): DataFrame = {
    val labelIndexer = new StringIndexer().setInputCol("home_ownership").setOutputCol("home_ownership_indexed")
    labelIndexer.fit(df).transform(df)
  }

  def getColHomeOwnership()(df: DataFrame): Column = {
    withHomeOwnership()(df).col("home_ownership_indexed")
  }

  def withVerificationStatusDouble()(df: DataFrame): DataFrame = {
    df.withColumn("verification_status_double", UDFs.verificationStatusToDoubleUDF($"verification_status"))
  }

  def getColVerificationStatusDouble()(df: DataFrame): Column = {
    UDFs.verificationStatusToDoubleUDF(df.col("verification_status")).as("verification_status_double")
  }

  def withPymntPlanDouble()(df: DataFrame): DataFrame = {
    df.withColumn("pymnt_plan_double", UDFs.pymntPlanToDoubleUDF($"pymnt_plan"))
  }

  def getColPymntPlanDouble()(df: DataFrame): Column = {
    UDFs.pymntPlanToDoubleUDF(df.col("pymnt_plan")).as("pymnt_plan_double")
  }

  def withDtiDouble()(df: DataFrame): DataFrame = {
    df.withColumn("dit_double", UDFs.optionToDoubleUDF($"dti"))
  }

  def getColDtiDouble()(df: DataFrame): Column = {
    UDFs.optionToDoubleUDF(df.col("dti")).as("dti_double")
  }

  def withDelinq2YrsInt()(df: DataFrame): DataFrame = {
    df.withColumn("delinq_2yrs_int", UDFs.stringToIntUDF($"delinq_2yrs"))
  }

  def getColDelinq2YrsInt()(df: DataFrame): Column = {
    UDFs.stringToIntUDF(df.col("delinq_2yrs")).as("delinq_2yrs_int")
  }

  def withZipcodeInt()(df: DataFrame): DataFrame = {
    df.withColumn("zip_code_int", UDFs.zipcodeToIntUDF($"zip_code"))
  }

  def getColZipcodeInt()(df: DataFrame): Column = {
    UDFs.zipcodeToIntUDF(df.col("zip_code")).as("zip_code_int")
  }

  def withEarliestCrLineInt()(df: DataFrame): DataFrame = {
    df.withColumn("earliest_cr_line_int", UDFs.issueDateToIntUDF($"earliest_cr_line"))
  }

  def getColEarliestCrLineInt()(df: DataFrame): Column = {
    UDFs.issueDateToIntUDF(df.col("earliest_cr_line")).as("earliest_cr_line_int")
  }

  def withInqLast6MthsInt()(df: DataFrame): DataFrame = {
    df.withColumn("inq_last_6mths_int", UDFs.stringToIntUDF($"inq_last_6mths"))
  }

  def getColInqLast6MthsInt()(df: DataFrame): Column = {
    UDFs.stringToIntUDF(df.col("inq_last_6mths")).as("inq_last_6mths_int")
  }

  def withMthsSinceLastDelinqInt()(df: DataFrame): DataFrame = {
    val df2=fillNull(df, "mths_since_last_delinq_null", "mths_since_last_delinq", "1000")
    df2.withColumn("mths_since_last_delinq_int", UDFs.stringToIntUDF($"mths_since_last_delinq"))
  }

  def getColMthsSinceLastDelinqInt()(df: DataFrame): Column = {
    withMthsSinceLastDelinqInt()(df).col("mths_since_last_delinq_int")
  }

  def withOpenAccInt()(df: DataFrame): DataFrame = {
    df.withColumn("open_acc_int", UDFs.optionToIntUDF($"open_acc"))
  }

  def getColOpenInt()(df: DataFrame): Column = {
    UDFs.optionToIntUDF(df.col("open_acc")).as("open_acc_int")
  }

  def withPubRecInt()(df: DataFrame): DataFrame = {
    df.withColumn("pub_rec_int", UDFs.optionToIntUDF($"pub_rec"))
  }

  def getColPubRecInt()(df: DataFrame): Column = {
    UDFs.optionToIntUDF(df.col("pub_rec")).as("pub_rec_int")
  }

  def withRevolBalDouble()(df: DataFrame): DataFrame = {
    df.withColumn("revol_bal_double", UDFs.optionToDoubleUDF($"revol_bal"))
  }

  def getColRevolBalDouble()(df: DataFrame): Column = {
    UDFs.optionToDoubleUDF(df.col("revol_bal")).as("revol_bal_double")
  }

  def withRevolUtilDouble()(df: DataFrame): DataFrame = {
    df.withColumn("revol_util_double", UDFs.optionToDoubleUDF($"revol_util"))
  }

  def getColRevolUtilDouble()(df: DataFrame): Column = {
    UDFs.optionToDoubleUDF(df.col("revol_util")).as("revol_util_double")
  }

  def withTotalAccInt()(df: DataFrame): DataFrame = {
    df.withColumn("total_acc_int", UDFs.optionToIntUDF($"total_acc"))
  }

  def getColTotalAccInt()(df: DataFrame): Column = {
    UDFs.optionToIntUDF(df.col("total_acc")).as("total_acc_int")
  }

  def withOptionInt()(df: DataFrame)(inputCol: String, outputCol: String): DataFrame = {
    df.withColumn(outputCol, UDFs.optionToIntUDF(df.col(inputCol)))
  }

  def getColOptionInt()(df: DataFrame)(inputCol: String, outputCol: String): Column = {
    UDFs.optionToIntUDF(df.col(inputCol)).as(outputCol)
  }

  def withOptionDouble()(df: DataFrame)(inputCol: String, outputCol: String): DataFrame = {
    df.withColumn(outputCol, UDFs.optionToDoubleUDF(df.col(inputCol)))
  }

  def getColOptionDouble()(df: DataFrame)(inputCol: String, outputCol: String): Column = {
    println("inputCol= " + inputCol + " outputCol= " + outputCol)
    UDFs.optionToDoubleUDF(df.col(inputCol)).as(outputCol)
    //UDFs.outPrncpToDoubleUDF(df.col("out_prncp")).as("out_prncp_double")
    //UDFs.outPrncpToDoubleUDF(df.col(inputCol)).as(outputCol)
  }

  def withApplicationTypeInt()(df: DataFrame): DataFrame = {
    df.withColumn("application_type_int", UDFs.applicationTypeToIntUDF(df.col("application_type")))
  }

  def getColApplicationTypeInt()(df: DataFrame): Column = {
    UDFs.applicationTypeToIntUDF(df.col("application_type")).as("application_type_int")
  }

  def getColOutPrncpDouble()(df: DataFrame): Column = {
    UDFs.outPrncpToDoubleUDF(df.col("out_prncp")).as("out_prncp_double")
  }

  //Transformers.getColOptionDouble()(_:DataFrame)("out_prncp", "out_prncp_double"),

}
