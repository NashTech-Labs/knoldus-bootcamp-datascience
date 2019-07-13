package com.knoldus.training

import breeze.linalg.min
import breeze.numerics.{exp, log}
import com.knoldus.training.Forecasting2.{Info, infoToString}
import com.knoldus.common.Constants._

object Difference {

  def getDifference(info1: Info, info2: Info): Info = {

    val sales1 = info1.sales
    val sales2 = info2.sales
    //val sales1 = if (info1.sales>0 || info1.sales==UNKNOWN_DOUBLE) { info1.sales } else { 0.01 }
    //val sales2 = if (info2.sales>0 || info2.sales==UNKNOWN_DOUBLE) { info2.sales } else { 0.01 }
    //val diffSale = if (sales1!=UNKNOWN_DOUBLE) { log(sales1)-log(sales2) }
    val diffSale = if (sales1!=UNKNOWN_DOUBLE && sales2!=UNKNOWN_DOUBLE) { sales1-sales2 }
    else { UNKNOWN_DOUBLE }

    val tempDiff = info1.temperature-info2.temperature

    info1.copy(sales = diffSale)
  }

  def getDifferences(info: Array[Info]): (Info, Array[Info]) = {
    val first = info(0)

    val diffSales = for { i <- 1 until info.length } yield {

      getDifference(info(i), info(i-1))
    }

    (first, diffSales.toArray)
  }

  def undifference(prev: Info, info: Array[Info], n: Int, result: Array[Info]): Array[Info] = {
    //val maxSale = 100000.0
    if (n==info.length) { result }
    else {
      //val sale = min(exp( log(prev.sales) + info(n).sales ), maxSale)
      //val sale = min( prev.sales + info(n).sales , maxSale)
      val sale = prev.sales + info(n).sales
      //val sale = exp( log(prev.sales) + info(n).sales )
      val sale2 = if (sale.isNaN) { 0 } else { sale }
      val newSale = info(n).copy(sales = sale2)
      undifference(newSale, info, n + 1, result :+ newSale )
    }
  }

  def undifference(differenced: (Info, Array[Info])): Array[Info] = {
    val result: Array[Info] = Array()
    val ans = differenced._1 +: undifference(differenced._1, differenced._2, 0, result)
    if (ans(0).store==10 && ans(0).dept==18) {
      println("store= " + ans(0).store + " dept= " + ans(0).dept + " daily diff")
      for { i <- ans.indices } {
        println{infoToString(ans(i))}
      }
    }
    ans
  }

  def getYearlyDifference(info: Array[Info]): (Array[Info], Array[Info]) = {
    val diffSales = for { i <- 52 until info.length } yield {
      //val diffSale = if (info(i).sales!=UNKNOWN_DOUBLE && info(i-52).sales!=UNKNOWN_DOUBLE) {
      //  info(i).sales-info(i-52).sales
      //}
      //else { UNKNOWN_DOUBLE }
      //changeSale(info(i), diffSale)
      getDifference(info(i), info(i-52))
    }
    (info.slice(0, 52), diffSales.toArray)
  }

  def undifferenceYear(info: Array[Info], n: Int, result: Array[Info]): Array[Info] = {
    if (n-52==info.length) { result }
    else {
      val sale = result(n-52).sales + info(n-52).sales
      val newSale = info(n-52).copy( sales = sale )
      undifferenceYear(info, n + 1, result :+ newSale )
    }
  }

  def undifferenceYear(differenced: (Array[Info], Array[Info])): Array[Info] = {
    val firstYear = differenced._1
    val result = undifferenceYear(differenced._2, 52, firstYear)
    if (result(0).store==10 && result(0).dept==18) {
      println("store= " + result(0).store + " dept= " + result(0).dept + " yearly diff")
      for { i <- result.indices } {
        println{infoToString(result(i))}
      }
    }
    result
  }

}
