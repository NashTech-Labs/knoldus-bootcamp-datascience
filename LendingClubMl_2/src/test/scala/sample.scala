
import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test
import com.knoldus.spark.UDFs
import scala.collection.mutable.ListBuffer

class ExampleSuite extends TestCase {

  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _

  override def setUp() :Unit = {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]
    println("Setup")
  }

  @Test
  def testone: Unit = {
    assertEquals("1","1")
  }

  @Test
  def testTermToInt: Unit = {
    assertEquals(UDFs.termToInt("36 months"), 36)
    assertEquals(UDFs.termToInt("60 months"), 60)
  }

  @Test
  def testGradeToInt: Unit = {
    assertEquals(UDFs.gradeToInt("A"), 0)
    assertEquals(UDFs.gradeToInt("B"), 1)
    assertEquals(UDFs.gradeToInt("C"), 2)
    assertEquals(UDFs.gradeToInt("D"), 3)
    assertEquals(UDFs.gradeToInt("F"), 5)
  }

  @Test
  def testSubgradeToInt: Unit = {
    assertEquals(UDFs.subgradeToInt("A1"), 1)
    assertEquals(UDFs.subgradeToInt("A2"), 2)
    assertEquals(UDFs.subgradeToInt("A3"), 3)
    assertEquals(UDFs.subgradeToInt("A4"), 4)
    assertEquals(UDFs.subgradeToInt("A5"), 5)
    assertEquals(UDFs.subgradeToInt("B1"), 1)
    assertEquals(UDFs.subgradeToInt("B2"), 2)
    assertEquals(UDFs.subgradeToInt("B3"), 3)
    assertEquals(UDFs.subgradeToInt("B4"), 4)
    assertEquals(UDFs.subgradeToInt("B5"), 5)

  }

  @Test
  def testEmpLengthToInt: Unit = {
    assertEquals(UDFs.empLengthToInt("10+ years"), 11)
    assertEquals(UDFs.empLengthToInt("5 years"), 5)
    assertEquals(UDFs.empLengthToInt("< 1 year"), 1)
  }

  @Test
  def testIssueDateToInt: Unit = {
    assertEquals(UDFs.issueDateToInt("DEC-2018"), 2018)
    assertEquals(UDFs.issueDateToInt("JAN-2018"), 2018)
    assertEquals(UDFs.issueDateToInt("DEC-2017"), 2017)
  }

  @Test
  def testLoanStatusToDouble: Unit = {
    assertEquals(UDFs.loanStatusToDouble("Current"), 1.0, 0.0001)
    assertEquals(UDFs.loanStatusToDouble("Fully Paid"), 1.0, 0.0001)
    assertEquals(UDFs.loanStatusToDouble("Default"), 0.0, 0.0001)
  }

  @Test
  def testAnnualIncToDouble: Unit = {
    assertEquals(UDFs.annualIncToDouble("1000"), 1000.0, 0.0001)
  }

  @Test
  def testVerificationStatusToDouble: Unit = {
    assertEquals(UDFs.verificationStatusToDouble("Not Verified"), 1.0, 0.00001)
    assertEquals(UDFs.verificationStatusToDouble("Verified"), 0, 0.00001)
  }

  @Test
  def testPymntPlanToDouble: Unit = {
    assertEquals(UDFs.pymntPlanToDouble("y"), 1.0, 0.00001)
    assertEquals(UDFs.pymntPlanToDouble("n"), 0, 0.00001)
  }

  @Test
  def testZipcodeToInt: Unit = {
    assertEquals(UDFs.zipcodeToInt("98"), 98)
  }

  @Test
  def testStringToDouble: Unit = {
    assertEquals(UDFs.stringToDouble("fadfa"), 0, 0.00001)
    assertEquals(UDFs.stringToDouble("3.5"), 3.5, 0.00001)
  }

  @Test
  def stringToInt: Unit = {
    assertEquals(UDFs.stringToInt("fdsfafd"), 0)
    assertEquals(UDFs.stringToInt("37"), 37)
  }

  @Test
  def testOptionToInt: Unit = {
    assertEquals(UDFs.optionToInt(null), 0)
    assertEquals(UDFs.optionToInt("6"), 6)
  }

  @Test
  def testOptionToDouble: Unit = {
    assertEquals(UDFs.optionToDouble(null), 0, 0.00001)
    assertEquals(UDFs.optionToDouble("3.5"), 3.5, 0.00001)

  }

  override def tearDown(): Unit = {
    println("End")
    super.tearDown()
  }
}
