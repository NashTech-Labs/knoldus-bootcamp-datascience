
import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test
import com.knoldus.training.{Problems_21_40, Problems_41_60, Huffman, TreeProblems, TreeProblems2, Sample, MTree, MTreeTest}

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
  def testInsertAt :Unit = {
    val newStr: String="new"
    val abcd: List[String]=List("a", "b", "c", "d")
    val result: List[String]=List("a", "new", "b", "c", "d")
    assertEquals(Problems_21_40.insertAt(newStr, 1, abcd), result)
  }

  @Test
  def testRange: Unit = {
    assertEquals(Problems_21_40.range(4, 9), List(4, 5, 6, 7, 8, 9))
  }

  @Test
  def testRandomSelect: Unit = {
    val l=List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')
    for {i <- 0 to 100} {
      assertEquals(Problems_21_40.randomSelect(3, l).length, 3)
      assertEquals(Sample.compress(Problems_21_40.randomSelect(3, l)).length, 3)
    }
  }

  @Test
  def testLotto: Unit = {
    val n = 6
    val maxInt = 49
    for {i <- 0 to 100} {
      assertEquals(Problems_21_40.lotto(n, maxInt).length, n)
      assertEquals(Sample.compress(Problems_21_40.lotto(n, maxInt)).length, n)
      assert(Problems_21_40.lotto(n, maxInt).max <= maxInt)
      assert(Problems_21_40.lotto(n, maxInt).min >= 1)
    }
  }

  @Test
  def testRandomPermutation: Unit = {
    val l=List('a', 'b', 'c', 'd', 'e', 'f')
    for {i <- 0 to 100} {
      assertEquals(Sample.compress(Problems_21_40.randomPermutation(l)).length, l.length)
      assertEquals(Problems_21_40.randomPermutation(l).sorted, l)
    }
  }

  @Test
  def testCombinations: Unit = {
    val l=List('a', 'b', 'c', 'd', 'e')
    val result=List(List('a', 'b', 'c'), List('a', 'b', 'd'), List('a', 'b', 'e'), List('a', 'c', 'd')):::
      List(List('a', 'c', 'e'), List('a', 'd', 'e'), List('b', 'c', 'd'), List('b', 'c', 'e'), List('b', 'd', 'e')):::
      List(List('c', 'd', 'e'))
    assertEquals(Problems_21_40.combinations(3, l), result)
  }

  @Test
  def testGroup: Unit = {
    val l=List('a', 'b', 'c', 'd')
    val result=Problems_21_40.group(List(1, 1, 2), l)
    result.foreach( x => assertEquals(x.flatten.sorted, l) )
  }

  @Test
  def testlsort: Unit = {
    val l=List(List('a, 'b, 'c), List('d, 'e), List('f, 'g, 'h)):::
      List(List('d, 'e), List('i, 'j, 'k, 'l), List('m, 'n), List('o))
    val result=List(List('o), List('d, 'e), List('d, 'e), List('m, 'n), List('a, 'b, 'c)):::
      List(List('f, 'g, 'h), List('i, 'j, 'k, 'l))
    assertEquals(Problems_21_40.lsort(l), result)
  }

  @Test
  def testlsortFreq: Unit = {
    val l=List(List('a', 'b', 'c'), List('d', 'e'), List('f', 'g', 'h')):::
      List(List('d', 'e'), List('i', 'j', 'k', 'l'), List('m', 'n'), List('o'))
    val result=List(List('i', 'j', 'k', 'l'), List('o'), List('a', 'b', 'c'), List('f', 'g', 'h'), List('d', 'e')):::
      List(List('d', 'e'), List('m', 'n'))
    assertEquals(Problems_21_40.lsortFreq(l), result)
  }

  @Test
  def testIsPrime: Unit = {
    assertEquals(Problems_21_40.isPrime(2), true)
    assertEquals(Problems_21_40.isPrime(3), true)
    assertEquals(Problems_21_40.isPrime(4), false)
    assertEquals(Problems_21_40.isPrime(5), true)
    assertEquals(Problems_21_40.isPrime(6), false)
    assertEquals(Problems_21_40.isPrime(7), true)
    assertEquals(Problems_21_40.isPrime(8), false)
    assertEquals(Problems_21_40.isPrime(9), false)
    assertEquals(Problems_21_40.isPrime(10), false)
    assertEquals(Problems_21_40.isPrime(11), true)
  }

  @Test
  def testGcd: Unit = {
    assertEquals(Problems_21_40.gcd(36, 63), 9)
  }

  @Test
  def testIsCoprime: Unit = {
    assertEquals(Problems_21_40.isCoprime(35, 64), true)
  }

  @Test
  def testPrimeFactors: Unit = {
    assertEquals(Problems_21_40.primeFactors(315), List(3, 3, 5, 7))
  }

  @Test
  def testPrimeFactorMultiplicity: Unit = {
    assertEquals(Problems_21_40.primeFactorMultiplicity(315), List((3,2), (5,1), (7,1)))
  }

  @Test
  def testPhi: Unit = {
    for {i <- 2 to 1000 } {
      assertEquals(Problems_21_40.phi(i), Problems_21_40.totient(i))
    }
  }

  @Test
  def testListPrimesinRange: Unit = {
    val primes=List(7, 11, 13, 17, 19, 23, 29, 31)
    assertEquals(Problems_21_40.listPrimesInRange(7 to 31), primes)
  }

  @Test
  def testGoldbach: Unit = {
    assertEquals(Problems_21_40.goldbach(28), (5,23))
  }

  @Test
  def testAnd: Unit = {
    assertEquals(Problems_41_60.and(true, true), true)
    assertEquals(Problems_41_60.and(true, false), false)
    assertEquals(Problems_41_60.and(false, true), false)
    assertEquals(Problems_41_60.and(false, false), false)
  }

  @Test
  def testOr: Unit = {
    assertEquals(Problems_41_60.or(true, true), true)
    assertEquals(Problems_41_60.or(true, false), true)
    assertEquals(Problems_41_60.or(false, true), true)
    assertEquals(Problems_41_60.or(false, false), false)
  }

  @Test
  def testNand: Unit = {
    assertEquals(Problems_41_60.nand(true, true), false)
    assertEquals(Problems_41_60.nand(true, false), true)
    assertEquals(Problems_41_60.nand(false, true), true)
    assertEquals(Problems_41_60.nand(false, false), true)
  }

  @Test
  def testNor: Unit = {
    assertEquals(Problems_41_60.nor(true, true), false)
    assertEquals(Problems_41_60.nor(true, false), false)
    assertEquals(Problems_41_60.nor(false, true), false)
    assertEquals(Problems_41_60.nor(false, false), true)
  }

  @Test
  def testXor: Unit = {
    assertEquals(Problems_41_60.xor(true, true), false)
    assertEquals(Problems_41_60.xor(true, false), true)
    assertEquals(Problems_41_60.xor(false, true), true)
    assertEquals(Problems_41_60.xor(false, false), false)
  }

  @Test
  def testImp: Unit = {
    assertEquals(Problems_41_60.impl(true, true), true)
    assertEquals(Problems_41_60.impl(true, false), false)
    assertEquals(Problems_41_60.impl(false, true), true)
    assertEquals(Problems_41_60.impl(false, false), true)
  }

  @Test
  def testEqu: Unit = {
    assertEquals(Problems_41_60.equ(true, true), true)
    assertEquals(Problems_41_60.equ(true, false), false)
    assertEquals(Problems_41_60.equ(false, true), false)
    assertEquals(Problems_41_60.equ(false, false), true)
  }

  @Test
  def testGray: Unit = {
    val c1=List("0", "1")
    val c2=List("00", "01", "11", "10")
    val c3=List("000", "001", "011", "010", "110", "111", "101", "100")
    assertEquals(Problems_41_60.gray(1), c1)
    assertEquals(Problems_41_60.gray(2), c2)
    assertEquals(Problems_41_60.gray(3), c3)
  }

  @Test
  def testHuffman: Unit = {
    val freqs=List(("a", 45), ("b", 13), ("c", 12), ("d", 16), ("e", 9), ("f", 5))
    val result=List(("a","0"), ("b","101"), ("c","100"), ("d","111"), ("e","1101"), ("f","1100"))
    assertEquals(Huffman.huffman(freqs), result)
  }

  @Test
  def testCBlanced: Unit = {
    val result=List("T( x T( x ., . ), T( x T( x ., . ), . ) )", "T( x T( x ., . ), T( x ., T( x ., . ) ) )", "T( x T( x T( x ., . ), . ), T( x ., . ) )", "T( x T( x ., T( x ., . ) ), T( x ., . ) )")
    assertEquals(TreeProblems.cBalanced(4, "x").map( x => x.toString) , result)
  }

  @Test
  def testIsSymmetric: Unit = {
    val b=TreeProblems.Node("b", TreeProblems.End, TreeProblems.End)
    val c=TreeProblems.Node("c", TreeProblems.End, TreeProblems.End)
    val tree1=TreeProblems.Node("a", b, c)
    val tree2=TreeProblems.fromList(List(5, 3, 18, 1, 4, 12, 21))
    val tree3=TreeProblems.fromList(List(3, 2, 5, 7, 4))
    assertEquals(tree1.isSymmetric(), true)
    assertEquals(tree2.isSymmetric(), true)
    assertEquals(tree3.isSymmetric(), false)
  }

  @Test
  def testGetLeftRight(): Unit = {
    val str1="a(b(d,e),c(,f(g,)))"
    val res1=TreeProblems2.getLeftRight(str1)
    val expected1=("b(d,e)", "c(,f(g,))")
    assertEquals(expected1, res1)

    val str2="b(d,e)"
    val res2=TreeProblems2.getLeftRight(str2)
    val expected2=("d", "e")
    assertEquals(expected2, res2)

    val str3="c(,f(g,))"
    val res3=TreeProblems2.getLeftRight(str3)
    val expected3=("", "f(g,)")
    assertEquals(expected3, res3)

    val str4="f(g,)"
    val res4=TreeProblems2.getLeftRight(str4)
    val expected4=("g", "")
    assertEquals(expected4, res4)
  }

  @Test
  def testFromString(): Unit = {
    val str="a(b(d,e),c(,f(g,)))"
    val res=TreeProblems2.fromString(str).toString
    assertEquals(str, res)
  }

  @Test
  def testPreorder(): Unit = {
    val str="a(b(d,e),c(,f(g,)))"
    val res=TreeProblems2.fromString(str).preorder
    val expected=List('a', 'b', 'd', 'e', 'c', 'f', 'g')
    assertEquals(expected, res)
  }

  @Test
  def testInorder(): Unit = {
    val str="a(b(d,e),c(,f(g,)))"
    val res=TreeProblems2.fromString(str).inorder
    val expected=List('d', 'b', 'e', 'a', 'c', 'g', 'f')
    assertEquals(expected, res)

  }

  @Test
  def testPreInTree(): Unit = {
    val preo=List('a', 'b', 'd', 'e', 'c', 'f', 'g')
    val ino=List('d', 'b', 'e', 'a', 'c', 'g', 'f')
    val str=TreeProblems2.preInTree(ino, preo).toString
    val expected="a(b(d,e),c(,f(g,)))"
    assertEquals(expected, str)
  }

  @Test
  def testToDotstring(): Unit = {
    val str="a(b(d,e),c(,f(g,)))"
    val res=TreeProblems2.fromString(str).toDotstring
    val expected="abd..e..c.fg..."
    assertEquals(expected, res)
  }

  @Test
  def testGetLeftRightDotstring(): Unit = {
    val str="abd..e..c.fg..."
    val res=TreeProblems2.getLeftRightDotstring(str)
    val expected=("bd..e..", "c.fg...")
    assertEquals(expected, res)
  }

  @Test
  def testFromDotstring(): Unit = {
    val str="abd..e..c.fg..."
    val res=TreeProblems2.fromDotstring(str).toDotstring
    assertEquals(str, res)
  }

  @Test
  def testNodeCount(): Unit = {
    val n=MTree('a', List(MTree('f'))).nodeCount
    assertEquals(2, n)
  }

  @Test
  def testMTreeToString(): Unit = {
    val res=MTree('a', List(MTree('f', List(MTree('g'))), MTree('c'), MTree('b', List(MTree('d'), MTree('e'))))).toString
    val expected="afg^^c^bd^e^^^"
    assertEquals(expected, res)
  }

  @Test
  def testSplitIntoBranches(): Unit = {
    val str="fg^^c^bd^e^^"
    val branches=MTreeTest.splitIntoBranches(str, 0, 0)
    val expected=List("fg^^", "c^", "bd^e^^")
    assertEquals(expected, branches)
  }

  @Test
  def testString2MTree(): Unit = {
    val str="afg^^c^bd^e^^^"
    val expected=MTree('a', List(MTree('f', List(MTree('g'))), MTree('c'), MTree('b', List(MTree('d'), MTree('e')))))
    val res=expected.string2MTree(str)
    assertEquals(expected, res)
  }

  @Test
  def testInternalPathLength(): Unit = {
    val str="afg^^c^bd^e^^^"
    val temp=MTree('a')
    val res=temp.string2MTree(str).internalPathLength
    assertEquals(9, res)
  }

  @Test
  def testPostorder(): Unit = {
    val str="afg^^c^bd^e^^^"
    val res=MTree('a').string2MTree(str).postorder
    val expected=List('g', 'f', 'c', 'd', 'e', 'b', 'a')
    assertEquals(expected, res)
  }

  override def tearDown(): Unit = {
    println("End")
    super.tearDown()
  }
}
