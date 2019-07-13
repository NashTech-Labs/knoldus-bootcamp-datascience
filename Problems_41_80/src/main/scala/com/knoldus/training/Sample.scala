package com.knoldus.training

import com.knoldus.common.{AppConfig, KLogger}
import com.knoldus.spark.Transformers
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random
import scala.annotation.tailrec

object Sample {


  def main(args: Array[String]):Unit = {

    // Logging Demonstration
    val LOGGER: Logger = KLogger.getLogger(this.getClass)
    val demo = "demo"
    LOGGER.info("This is a " + demo )
    LOGGER.warn("This is warning")

    val freqs=List(("a", 45), ("b", 13), ("c", 12), ("d", 16), ("e", 9), ("f", 5))
    val huffmanResult=Huffman.huffman(freqs)
    println(huffmanResult)

    val l1=List(3, 2, 5, 7, 1)
    val l2=List(3, 2, 5, 7, 4)

    /*
    println(TreeProblems.fromList(l1))
    println(TreeProblems.isSymmetric(TreeProblems.fromList(l1)))
    println(TreeProblems.isSymmetric(TreeProblems.fromList(l2)))
    println(TreeProblems.minHbalNodes(3))
    println(TreeProblems.maxHbalHeight(4))
    */

   val tree=TreeProblems2.Node('x', TreeProblems2.Node('x', TreeProblems2.End, TreeProblems2.End), TreeProblems2.End)

   val count=tree.leafCount
   println("count= " + count)

  TreeProblems2.testLeafList()

  val str="a(b(d,e),c(,f(g,)))"
  val (leftString, rightString)=TreeProblems2.getLeftRight(str)
  println("leftString= " + leftString)
  println("rightString= " + rightString)

  val (leftLeftString, rightLeftString)=TreeProblems2.getLeftRight(leftString)
  println("leftLeftString= " + leftLeftString)
  println("rightLeftString= " + rightLeftString)

  val res=TreeProblems2.fromString(str).toString

  println("res= " + res)
}

    def length[A](l: List[A]): Int = {
      if (l.isEmpty) { 0 }
      else { 1 + length(l.tail) }
    }

    def compress[A](l: List[A]): List[A] = {
      if (l.isEmpty) { l }
      else if (l.tail.isEmpty) { l }
      else if ( l.head==l.tail.head ) { compress(l.tail) }
      else { l.head::compress(l.tail) }
    }

    def combineFirstTwoDirect[A](l: List[(Int, A)]): List[(Int, A)] = {
      if (l.length>1) {
        (l.tail.head._1 + 1, l.tail.head._2)::l.tail.tail
      }
      else { l }
    }

    def encodeDirect[A](l: List[A]): List[(Int, A)] = {
      if (l.isEmpty) { List() }
      else if (l.tail.isEmpty) { List((1, l.head)) }
      else if ( l.head==l.tail.head ) { val temp=(1, l.head)::encodeDirect(l.tail) ; combineFirstTwoDirect(temp) }
      else { (1, l.head)::encodeDirect(l.tail) }
    }

    def removeAt[A](n: Int, l: List[A]): (List[A], A) = {
      if (n==0) { (l.tail, l.head) }
      else { val removed=removeAt(n-1, l.tail); (l.head::removed._1, removed._2) }
    }

}

object HelperMethods {

  @tailrec
  def rangeRecursive(result: List[Int], idx1: Int, idx2: Int): List[Int] = {
    if (idx1>idx2) { result }
    else if (idx1==idx2) { idx1::result }
    else { rangeRecursive( idx1::result, idx1 + 1, idx2) }
  }

  def getFreqs[A](l: List[List[A]]): List[(List[A], Int)] = {
    l.map( x => (x, l.count( y => Sample.length(y)==Sample.length(x)) ) )
  }

  @tailrec
  def findFactorRecursive(i: Int, n: Int): Int = {
    if (n%i==0) { i }
    else { findFactorRecursive(i + 1, n) }
  }
}

object Problems_21_40 {

  def insertAt[A](el: A, idx: Int, l: List[A]): List[A] = {
    if (idx==0) { el::l }
    else { l.head::insertAt(el, idx-1, l.tail) }
  }

  def range(idx1: Int, idx2: Int): List[Int] = {
    if (idx1>idx2) { List() }
    else if (idx1==idx2) { List(idx1) }
    else { idx1::range(idx1 + 1, idx2) }
  }

  def randomSelect[A](n: Int, l: List[A]): List[A] = {
    if (n<=0) { List() }
    else { val temp=Sample.removeAt(Random.nextInt(l.length), l); temp._2::randomSelect(n-1, temp._1) }
  }

  def lotto(n: Int, max: Int): List[Int] = {
    randomSelect(n, range(1, max))
  }

  def randomPermutation[A](l: List[A]): List[A] = {
    randomSelect(Sample.length(l), l)
  }

  def combinations[A](n: Int, l: List[A]): List[List[A]] = {
    if (n==0 || Sample.length(l)==0) { List(List()) }
    else if (n==Sample.length(l)) { List(l) }
    else {
      combinations(n-1, l.tail).map( x => l.head::x ):::combinations(n, l.tail)
    }
  }

  def groupCombinations[A](n: Int, l: List[A]): List[(List[A], List[A])] = {
    combinations(n, l).map( x => (x, l.filter( y => !x.contains(y))))
  }

  def group3[A](l: List[A]): List[List[List[A]]] = {
    group(List(2, 3, 4), l)
  }

  def group[A](groupSizes: List[Int], l: List[A]): List[List[List[A]]] = {
    if (groupSizes.isEmpty) { List(List()) }
    else {
      val temp = groupCombinations(groupSizes.head, l)
      temp.flatMap(y => group(groupSizes.tail, y._2).map(x => y._1 :: x))
    }
  }

  def insertToSort[A](x: List[A], l: List[List[A]]): List[List[A]] = {
    if (l.isEmpty) { List(x) }
    else if (Sample.length(x)<=Sample.length(l.head)) { x::l }
    else { l.head::insertToSort(x, l.tail) }
  }

  def lsort[A](l: List[List[A]]): List[List[A]] = {
    if (l.isEmpty) { l }
    else { insertToSort(l.head, lsort(l.tail)) }
  }

  def insertToSortFreq[A](x: (List[A], Int), l: List[(List[A], Int)]): List[(List[A], Int)] = {
    if (l.isEmpty) { List(x) }
    else if (x._2<=l.head._2) { x::l }
    else { l.head::insertToSortFreq(x, l.tail) }
  }

  def lsortFreqHelper[A](l: List[(List[A], Int)]): List[(List[A], Int)] = {
    if (l.isEmpty) { l }
    else { insertToSortFreq(l.head, lsortFreqHelper(l.tail)) }
  }

  def lsortFreq[A](l: List[List[A]]): List[List[A]] = {
    val lFreqs=HelperMethods.getFreqs(l)
    val sorted=lsortFreqHelper(lFreqs)
    sorted.map( x => x._1 )
  }

  def isPrime(n: Int): Boolean = {
    //for {i <- range(2, n-1)} { if (n%i==0) { return false } }
    //true
    val p=(2 until n).toList.map( x => n%x ).product
    if (p==0) { false } else { true }
  }

  def gcd(a: Int, b: Int): Int = {
    if (b==0) { a } else gcd(b, a % b)
  }

  def isCoprime(a: Int, b: Int): Boolean = {
    gcd(a, b)==1
  }

  @tailrec
  def coprimesInRangeRecursive(count: Int, a: Int, b: Int, c: Int): Int = {
    if (a>b) { count }
    else {
      val coprime=if (isCoprime(a, b)) { 1 } else { 0 }
      coprimesInRangeRecursive(count + coprime, a + 1, b, c)
    }
  }

  def coprimesInRange(a: Int, b: Int, c: Int): Int = {
    if (a>b) { 0 }
    else {
      val coprime: Int=if (isCoprime(a, b)) { 1 } else { 0 }
      coprime + coprimesInRange(a + 1, b, c)
    }
  }

  def totient(a: Int): Int = {
    //coprimesInRange(1, a, a)
    coprimesInRangeRecursive(0, 1, a, a)
  }

  def findFactor(n: Int): Int = {
    //for (i <- 2 to n ) { if (n%i==0) { return i } }
    //n
    HelperMethods.findFactorRecursive(2, n)
  }

  def primeFactors(n: Int): List[Int] = {
    val m=findFactor(n)
    if (m==n) { List(n) }
    else { m::primeFactors(n/m) }
  }

  def primeFactorMultiplicity(n: Int): List[(Int, Int)] = {
    Sample.encodeDirect(primeFactors(n)).map( x => (x._2, x._1) )
  }

  def factorsToPhi(factors: List[(Int, Int)]): Int = {
    if (factors.isEmpty) { 1 }
    else { scala.math.pow(factors.head._1, factors.head._2-1).toInt*(factors.head._1-1)*factorsToPhi(factors.tail) }
  }

  def phi(m: Int): Int = {
    val factors=primeFactorMultiplicity(m)
    factorsToPhi(factors)
  }

  def comparePhiAndTotient(m: Int): Boolean = {
    val p=phi(m)
    val t=totient(m)
    println("phi(" + m + ")= " + p + " totient(" + m + ")= " + t)
    p==t
  }

  def listPrimesInRange(r: Range): List[Int] = {
    r.filter( x => isPrime(x) ).toList
  }

  def goldbachRecursive(i: Int, n: Int): (Int, Int) = {
    if (isPrime(i) && isPrime(n-i)) {  (i, n-i) }
    else { goldbachRecursive(i + 1, n) }
  }

  def goldbach(n: Int): (Int, Int) = {
    //for (i <- 2 until n) { if (isPrime(i) && isPrime(n-i)) { return (i, n-i)} }
    //(-1, -1)
    goldbachRecursive(2, n)
  }

}

object Huffman {

  abstract class Tree[+T]

  case class Node[+T](v: T, l: Tree[T], r: Tree[T]) extends Tree[T] {
    val value: T=v
    val left: Tree[T]=l
    val right: Tree[T]=r
  }

  case object End extends Tree[Nothing]

  def toTree(freq: (String, Int)): Tree[(List[String], Int)] = {
    val tree: Tree[(List[String], Int)]=Node((List(freq._1), freq._2), End, End)
    tree
  }

  def insertFork(f: Tree[(List[String], Int)], n: List[Tree[(List[String], Int)]]): List[Tree[(List[String], Int)]] = {
    if (n.isEmpty) { List(f) }
    else {
      val weight1=f match { case Node(value, left, right) => { value._2 } }
      val weight2=n.head match { case Node(value, left, right) => { value._2 } }
      if (weight1<weight2) { f::n }
      else { n.head::insertFork(f, n.tail) }
    }
  }

  def combine(nodes: List[Tree[(List[String], Int)]]): List[Tree[(List[String], Int)]] = {
    if (nodes.isEmpty || nodes.tail.isEmpty) { nodes }
    else {
      val node1=nodes.head match { case Node(value, left, right ) => { Node(value, left, right) } }
      val node2=nodes.tail.head match { case Node(value, left, right ) => { Node(value, left, right) } }
      val strings1=nodes.head match { case Node(value, left, right) => { value._1 } }
      val strings2=nodes.tail.head match { case Node(value, left, right) => { value._1} }
      val weight1=nodes.head match { case Node(value, left, right) => { value._2 } }
      val weight2=nodes.tail.head match { case Node(value, left, right) => { value._2} }

      val value=(strings1:::strings2, weight1 + weight2)
      val fork: Tree[(List[String], Int)]=Node(value, node1, node2)
      insertFork(fork, nodes.tail.tail)
    }
  }

  def combineUntilDone(nodes: List[Tree[(List[String], Int)]]): Tree[(List[String], Int)] = {
    if (nodes.tail.isEmpty) { nodes.head }
    else { combineUntilDone(combine(nodes)) }
  }

  def treeToHuffmanCodes(node: Tree[(List[String], Int)], code: String): List[(String, String)] = {
    val result=node match {
      case Node(value, End, End) => List((value._1.head, code))
      case Node(value, left, right) => treeToHuffmanCodes(left, code + "0"):::treeToHuffmanCodes(right, code + "1")
      case _ => List( ("x", "x") )
    }
    result
  }

  def huffman(freqs: List[(String, Int)]): List[(String, String)] = {
    val nodes=freqs.sortBy( x => x._2 ).map( x => toTree(x) )
    val tree=combineUntilDone(nodes)
    treeToHuffmanCodes(tree, "").sortBy( x => x._1 )
  }
}


object HelperMethods_41_60 {
  def getGoldbachList(r: Range): List[(Int, Int)] = {
    r.toList.map( x => Problems_21_40.goldbach(x) )
  }
}

object Problems_41_60 {
  def printGoldbachList(r: Range): Unit = {
    HelperMethods_41_60.getGoldbachList(r).foreach(println)
  }

  def printGoldbachListLimited(r: Range, limit: Int): Unit = {
    HelperMethods_41_60.getGoldbachList(r).filter( x => x._1>=limit ).foreach(println)
  }

  def and(b1: Boolean, b2: Boolean): Boolean = {
    if (b1) { b2 } else { false }
  }

  def or(b1: Boolean, b2: Boolean): Boolean = {
    if (b1) { true } else { b2 }
  }

  def nand(b1: Boolean, b2: Boolean): Boolean = {
    if (b1) { if (b2) { false } else { true } } else { true }
  }

  def nor(b1: Boolean, b2: Boolean): Boolean = {
    if (b1) { false } else { if (b2) { false } else { true } }
  }

  def xor(b1: Boolean, b2: Boolean): Boolean = {
    //if (or(and(b1, b2), nor(b1, b2))) { false }
    //else { true }
    if (b1) { if (b2) { false } else { true } }
    else { if (b2) { true } else { false } }
  }

  def impl(b1: Boolean, b2: Boolean): Boolean = {
    if (b1) { b2 }
    else { true }
  }

  def equ(b1: Boolean, b2: Boolean): Boolean = {
    if (or(and(b1, b2), nor(b1, b2))) { true }
    else { false }
  }

  def tabel2(func: (Boolean, Boolean) => Boolean ): Unit = {
    println("A\tB\tresult")
    println("true\ttrue\t" + func(true, true))
    println("true\tfalse\t" + func(true, false))
    println("false\ttrue\t" + func(false, true))
    println("false\tfalse\t" + func(false, false))
  }

  def gray(n: Int): List[String] = {
    if (n==1) { List("0", "1") }
    else {
      val prev=gray(n-1)
      prev.map( x => "0" + x ):::prev.reverse.map( x => "1" + x )
    }
  }

}

object TreeProblems {
  abstract class Tree[+T] {
    def isSymmetric(): Boolean
  }

  case class Node[+T](v: T, l: Tree[T], r: Tree[T]) extends Tree[T] {
    val value: T=v
    val left: Tree[T]=l
    val right: Tree[T]=r

    override def toString(): String = { "T( " + value + " " + left + ", " + right + " )"}
    def isSymmetric(): Boolean = {
      isMirror(left, right)
    }
  }

  case object End extends Tree[Nothing] {
    override def toString(): String = { "." }
    def isSymmetric(): Boolean = { true }
  }

  def getTreeCombinations[T](tree: TreeProblems.Tree[T], trees: List[TreeProblems.Tree[T]], value: T): List[Tree[T]] = {
    trees.map( x => Node(value, tree, x) )
  }

  def cBalanced[T](nnode: Int, value: T): List[Tree[T]] = {
    if (nnode==0) { List() }
    else if (nnode==1) { List(Node(value, End, End)) }
    else if (nnode==2) { List(Node(value, Node(value, End, End), End), Node(value, End, Node(value, End, End)))}
    else if (nnode%2==1) {
      val temps = cBalanced(nnode / 2, value)
      temps.flatMap(x => getTreeCombinations(x, temps, value))
    }
    else {
      val temps1 = cBalanced(nnode / 2 - 1, value)
      val temps2 = cBalanced(nnode / 2, value)
      temps1.flatMap(x => getTreeCombinations(x, temps2, value)):::temps2.flatMap(x => getTreeCombinations(x, temps1, value))
    }
  }

  def isMirror[T](tree1: Tree[T], tree2: Tree[T]): Boolean = {
    tree1 match {
      case Node(value, End, End) => tree2 match { 
        case Node(value, End, End) => true
        case _ => false
      }
      case Node(value, End, right) => tree2 match { 
        case Node(value, left, End) => isMirror(left, right)
        case _ => false
      }
      case Node(value, left, End) => tree2 match {
        case Node(value, End, right) => isMirror(left, right)
        case _ => false
      }
      case Node(value, left1, right1) => tree2 match {
        case Node(value, left2, right2) => isMirror(left1, right2) && isMirror(left2, right2)
        case _ => false
      }
    }
  }

  def addValue(tree: Tree[Int], value: Int): Tree[Int] = {
    tree match {
      case End => Node(value, End, End)
      case Node(oldValue, left, right) => if (value<oldValue) {
        Node(oldValue, addValue(left, value), right)
      }
      else {
        Node(oldValue, left, addValue(right, value) )
      }
    }
  }

  def treeFromList(l: List[Int], tree: Tree[Int]): Tree[Int] = {
    if (l.isEmpty) { tree }
    else { treeFromList(l.tail, addValue(tree, l.head) ) }
  }

  def fromList(l: List[Int]): Tree[Int] = {
    val tree=Node(l.head, End, End)
    treeFromList(l.tail, tree)
  }

  def symmetricBalancedTrees[T](nnode: Int, value: T): List[Tree[T]] = {
    cBalanced(nnode, value).filter( x => x.isSymmetric() )
  }

  def hbalTrees[T](h: Int, value: T): List[Tree[T]] = {
    if (h==0) { List(Node(value, End, End)) }
    else {
      val temps1=hbalTrees(h-1, value)
      val temps2=hbalTrees(h-2, value)
      temps1.flatMap( x => getTreeCombinations(x, temps1, value) ):::temps1.flatMap( x => getTreeCombinations(x, temps2, value) ):::temps2.flatMap( x => getTreeCombinations(x, temps1, value) )
    }
  }

  def minHbalNodes(h: Int): Int = {
    if (h==1) { 1 }
    else if (h==2) { 2 }
    else { 1 + minHbalNodes(h-1) + minHbalNodes(h-2) }
  }

  def maxHbalHeight(n: Int): Int = {
    def helper(n: Int, h: Int): Int = {
      if (minHbalNodes(h)>n) { h-1 }
      else { helper(n, h+1) }
    }
    helper(n, 1)
  }

  def countNodes[T](tree: Tree[T]): Int = {
    tree match {
      case End => 0
      case Node(value, End, End) => 1
      case Node(value, left, right) => 1 + countNodes(left) + countNodes(right)
    }
  }

  def hablTreesWithNodes[T](n: Int, value: T): List[Tree[T]] = {
    val maxH=maxHbalHeight(n)
    val result=for { h <- 0 to maxH } yield {
      hbalTrees(h, value).filter( t => countNodes(t)==n )
    }
    result.toList.flatten
  }

}

object TreeProblems2 {
  sealed abstract class Tree[+T] {
    def leafCount(): Int
    def leafList(): List[T]
    def internalList(): List[T]
    def atLevel(level: Int): List[T]
    //def layoutBinaryTree(): PositionedNode[T]
    def preorder(): List[T]
    def inorder(): List[T]
    def toDotstring(): String
  }

  case class Node[+T](v: T, l: Tree[T], r: Tree[T]) extends Tree[T] {
    val value: T=v
    val left: Tree[T]=l
    val right: Tree[T]=r

    //override def toString(): String = { "T( " + value + " " + left + ", " + right + " )"}

    def leafCount(): Int = {
     if (left==End && right==End) { 1 }
     else { left.leafCount() + right.leafCount() }
    }

    def leafList(): List[T] = {
      if (left==End && right==End) { List(value) }
      else { left.leafList():::right.leafList() }
    }

    def internalList(): List[T] = {
      if (left==End && right==End) { List() }
      else { value::left.internalList():::right.internalList() }
    }

    def atLevel(level: Int): List[T] = {
      if (level==1) { List(value) }
      else { left.atLevel(level-1):::right.atLevel(level-1) }
    }

    override def toString(): String = {
      if (left==End && right==End) { value.toString }
      else { value + "(" + left.toString + "," + right.toString + ")" }
    }

    def preorder(): List[T] = {
      value::left.preorder:::right.preorder
    }

    def inorder(): List[T] = {
      (left.inorder:+value):::right.inorder
    }

    def toDotstring(): String = {
      value + left.toDotstring + right.toDotstring
    }
  }

  case object End extends Tree[Nothing] {
    override def toString(): String = { "" }
    def leafCount(): Int = { 0 }
    def leafList(): List[Nothing] = { List() }
    def internalList(): List[Nothing] = { List() }
    def atLevel(level: Int): List[Nothing] = { List() }
    def preorder(): List[Nothing] = { List() }
    def inorder(): List[Nothing] = { List() }
    def toDotstring(): String = { "." }
  }

  def completeBinaryTreeHelper[T](nnode: Int, idx: Int, v: T): Tree[T] = {
    if (idx>nnode) { End }
    else {
      val tree1=completeBinaryTreeHelper(nnode, idx*2, v)
      val tree2=completeBinaryTreeHelper(nnode, idx*2+1, v)
      Node(v, tree1, tree2)
    }
  }

  def completeBinaryTree[T](nnode: Int, v: T): Tree[T] = {
    val tree1=completeBinaryTreeHelper(nnode, 2, v)
    val tree2=completeBinaryTreeHelper(nnode, 3, v)
    Node(v, tree1, tree2)
  }
  
/*
  case class PositionedNode[+T](override val value: T, override val left: Tree[T], override val right: Tree[T], x: Int, y: Int) extends Node[T](value, left, right) {
    override def toString = "T[" + x.toString + "," + y.toString + "](" + value.toString + " " + left.toString + " " + right.toString + ")"
  }

  def layoutBinaryTreeHelper(x: Int, y: Int, v: T, l: Tree[T], r: Tree[T]): PositionedNode[T] = {
    val toLeft=TreeProblems.count(left)
    new PositionedNode(v, l, r, x, y)
  }

  def layoutBinaryTree(): PositionedNode[T] = {
    //val leftDepths=left.getDepths(2)
    //val rightDepths=right.getDepths(2)
    layoutBinaryTreeHelper(1, 0, value, left, right)
  }
  */

  def findLeftStopIndex(str: String, idx: Int, depth: Int): Int = {
    if (depth==0 && str(idx)==',') { idx-1 }
    else if (idx==str.length) { idx-1 }
    else {
      val depth2=if (str(idx)=='(') { depth + 1 } 
      else if (str(idx)==')') { depth -1 }
      else { depth }
      if (depth2==0) { idx }
      else { findLeftStopIndex(str, idx + 1, depth2) }
    }
  }

  def getLeftRight(str: String): (String, String) = {
    val idx=if (str(2)==',') { 1 } else { findLeftStopIndex(str, 3, 0) }
    val leftString=str.slice(2, idx+1)
    val rightString=str.slice(idx+2, str.length-1)
    (leftString, rightString)
  }

  def fromString(str: String): Tree[Char] = {
    if (str.length==1) { Node(str(0), End, End) }
    else if (str.length>0 && str(0)!=',') {
      val v=str(0)
      val (leftString, rightString)=getLeftRight(str)
      val l=fromString(leftString)
      val r=fromString(rightString)
      Node(v, l, r)
    }
    else { End }
  }

  def preInTreeHelper(ino: List[Char], preo: List[Char], n: Int): Tree[Char] = {
    if (ino.slice(0, n).sorted==preo.slice(0, n).sorted && n<ino.length) {
      val v=preo.head
      val l=if (ino.length>1 && n>1) { preInTree(ino.slice(0, n-1), preo.slice(1, n)) }
      else { End }
      val r=if (n<ino.length)
      { preInTree(ino.slice(n, ino.length), preo.slice(n, ino.length)) }
      else { End }
      Node(v, l, r)
    }
    else if (n>=ino.length) { Node('a', End, End) }
    else { preInTreeHelper(ino, preo, n + 1) }
  }

  def preInTree(ino: List[Char], preo: List[Char]): Tree[Char] = {
    if (ino.length==1) { Node(ino.head, End, End) }
    else if (ino==preo.reverse) {
      val v=preo.head
      val l=preInTree(ino.slice(0, ino.length-1), preo.slice(1, preo.length))
      Node(v, l, End)
    }
    else { preInTreeHelper(ino, preo, 1) }
  }

  @tailrec
  def getLeftRightDotstringRec(str: String, idx: Int, nfree: Int): Int = {
    val nfree2=if (str(idx)=='.') { nfree-1 } else { nfree + 1 }
    if (nfree2==0) { idx }
    else { getLeftRightDotstringRec(str, idx+1, nfree2) }
  }

  def getLeftRightDotstring(str: String): (String, String) = {
    val idx=getLeftRightDotstringRec(str, 1, 1)
    val leftDotstring=str.slice(1, idx+1)
    val rightDotstring=str.slice(idx+1, str.length)
    (leftDotstring, rightDotstring)
  }

  def fromDotstring(str: String): Tree[Char] = {
    if (str==".") { End }
    else {
      val v=str(0)
      val (leftDotstring, rightDotstring)=getLeftRightDotstring(str)
      val l=fromDotstring(leftDotstring)
      val r=fromDotstring(rightDotstring)
      Node(v, l, r)
    }
  }

  def testLeafList(): Unit = {
    val tree=Node("a", Node("b", End, End), Node("c", Node("d", End, End), Node("e", End, End)))
    val leafs=tree.leafList()
    println("leafs= " + leafs)
  }

}

case class MTree[+T](value: T, children: List[MTree[T]]) {
  def this(value: T) = this(value, List())
  //override def toString = "M(" + value.toString + " {" + children.map(_.toString).mkString(",") + "})"

  def nodeCount(): Int = {
    1 + children.map( m => m.nodeCount ).sum
  }
  
  def splitIntoBranches(str: String, idx: Int, depth: Int): List[String] = {
    if (str=="") { List() }
    else {
      val depth2=if (str(idx)=='^') { depth-1 } else { depth + 1 }
      if (depth2==0) { List(str.slice(0, idx+1)):::splitIntoBranches(str.slice(idx+1, str.length), 0, 0) }
      else { splitIntoBranches(str, idx+1, depth2) }
    }
  }

  def string2MTree(str: String): MTree[Char] = {
    val strs=splitIntoBranches(str.slice(1, str.length-1), 0, 0)
    MTree(str(0), strs.map( m => string2MTree(m) ))
  }

  def internalPathLengthHelper(n: Int): Int = {
    n + children.map( m => m.internalPathLengthHelper(n+1) ).sum
  }

  def internalPathLength(): Int = {
    internalPathLengthHelper(0)
  }

  def postorder(): List[T] = {
    children.flatMap( m => m.postorder):+value
  }

  override def toString(): String = {
    value.toString + children.map( m => m.toString ).mkString + "^"
  }
}

object MTree {
  def apply[T](value: T) = new MTree(value, List())
  def apply[T](value: T, children: List[MTree[T]]) = new MTree(value, children)
}

object MTreeTest {
  def splitIntoBranches(str: String, idx: Int, depth: Int): List[String] = {
    println("str= "+str+" idx= "+idx+" depth= "+depth)
    if (str=="") { List() }
    else {
      val depth2=if (str(idx)=='^') { depth-1 } else { depth + 1 }
      if (depth2==0) { List(str.slice(0, idx+1)):::splitIntoBranches(str.slice(idx+1, str.length), 0, 0) }
      else { splitIntoBranches(str, idx+1, depth2) }
    }
  }
}
