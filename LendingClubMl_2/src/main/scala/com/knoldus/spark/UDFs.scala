package com.knoldus.spark
import org.apache.spark.sql.functions.udf
import com.knoldus.common.AppConfig
import com.knoldus.training.LogObject
object UDFs {

  val spark = AppConfig.sparkSession
  import spark.implicits._


  def safeToInt(str: String, default: Int): Int = {
    try { str.toInt }
    catch { case e: NumberFormatException => LogObject.LOGGER.warn("Unable to convert " + str + " to int"); default }
  }

  def safeToDouble(str: String, default: Double): Double = {
    try { str.toDouble }
    catch { case e: NumberFormatException => LogObject.LOGGER.warn("Unable to convert " + str + " to double"); default }
  }

  val termToInt: String => Int = x => x.trim.split(" ")(0).toInt

  val termToIntUDF = udf(termToInt)

  val gradeToInt: String => Int = x => x.charAt(0).toInt-'A'.toInt

  val gradeToIntUDF = udf(gradeToInt)

  val subgradeToInt: String => Int = x => safeToInt(x.substring(1, 2), 0)

  val subgradeToIntUDF = udf(subgradeToInt)

  val empLengthToInt: String => Int = x => if (x=="10+ years") { 11 } else if (x=="< 1 year") { 1 }
    else if (x=="n/a") { 0 } else if (x.trim.split(" ").length==2) { safeToInt(x.trim.split(" ")(0), 0) }
    else { 0 }

  val empLengthToIntUDF = udf(empLengthToInt)

  def stringToYear(str: String): Int = {
    if (str.split("-").length == 2) {
      safeToInt(str.split("-")(1), 2015)
    }
    else { 2015 }
  }

  val issueDateToInt: AnyRef => Int = {
    x => {
      x match {
        case str: String => stringToYear(str)
        case _ => 2015
      }
    }
  }

  val issueDateToIntUDF = udf(issueDateToInt)

  val loanStatusToDouble: String => Double = x => if (x=="Current" || x=="Fully Paid") { 1.0 } else { 0 }

  val loanStatusToDoubleUDF = udf(loanStatusToDouble)

  val annualIncToDouble: String => Double = x => try { x.toDouble }
  catch { case e: NumberFormatException => 0; case _: Throwable => 0 }

  val annualIncToDoubleUDF = udf(annualIncToDouble)

  val verificationStatusToDouble: String => Double = x => if (x=="Not Verified") { 1.0 } else { 0 }

  val verificationStatusToDoubleUDF = udf(verificationStatusToDouble)

  val pymntPlanToDouble: String => Double = x => if (x=="y") { 1.0 } else { 0 }

  val pymntPlanToDoubleUDF = udf(pymntPlanToDouble)

  val zipcodeToInt: AnyRef => Int = {
    x =>
      x match {
        case str: String => safeToInt(str.replaceAll("x", ""), 0)
        case _ => LogObject.LOGGER.warn("Zipcode is null"); 0
      }
  }

  val zipcodeToIntUDF = udf(zipcodeToInt)

  val stringToDouble: String => Double = { x => safeToDouble(x, 0) }

  val stringToDoubleUDF = udf(stringToDouble)

  val stringToInt: String => Double = { x => safeToInt(x, 0) }

  val stringToIntUDF = udf(stringToInt)

  val optionToInt: AnyRef => Int = {
    x =>
      x match {
        case str: String => safeToInt(str, 0)
        case _ => 0
      }
  }

  val optionToIntUDF = udf(optionToInt)

  val optionToDouble: AnyRef => Double = {
    x =>
      x match {
        case str: String => safeToDouble(str, 0)
        case _ => LogObject.LOGGER.warn("option null"); 0
      }
  }

  val optionToDoubleUDF = udf(optionToDouble)

  val applicationTypeToInt: AnyRef => Int = {
    x =>
      x match {
        case str: String => if (str=="Individual") { 1 } else { 0 }
        case _ => 0
      }
  }

  val applicationTypeToIntUDF = udf(applicationTypeToInt)

  val outPrncpToDouble: String => Double = x => safeToDouble(x, 3.0)

  val outPrncpToDoubleUDF = udf(outPrncpToDouble)

}
