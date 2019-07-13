package com.knoldus.training

import com.knoldus.training.Forecasting.{ForecastDate, Sales}
import com.knoldus.training.Forecasting2.Info

object GenFakeData {

  def genFakeInfo(size: Int): Array[Info] = {
    val r = scala.util.Random

    val store = 1
    val dept = 1
    val year = 2019
    val month = 5
    val day = 23
    val fakeInfo = for { i <- 0 until size } yield {
      val newDay = day + i
      val date = new ForecastDate(year, month, newDay)
      val temperature = r.nextFloat*110.0-10.0
      val fuelPrice = r.nextFloat*3.0 + 1.0
      val markDowns = Array(r.nextDouble, r.nextDouble, r.nextDouble, r.nextDouble, r.nextDouble)
      val cpi = r.nextFloat*60.0 + 60.0
      val unemployment = r.nextFloat*10.0
      val sales = r.nextFloat*10000.0
      val isHoliday = 0
      val predicted = false
      val good = true

      new Info(store, dept, date, temperature, fuelPrice, markDowns, cpi, unemployment, sales, isHoliday, predicted, good)
    }
    fakeInfo.toArray
  }

  def genFakeSales(size: Int): Array[Sales] = {
    val r = scala.util.Random

    val store = 1
    val dept = 1
    val year = 2019
    val month = 5
    val day = 23
    val fakeSales = for { i <- 0 until size } yield {
      val newDay = day + i
      val date = new ForecastDate(year, month, newDay)
      val sales = r.nextFloat*10000.0
      val isHoliday = 0

      new Sales(store, dept, date, sales, isHoliday)
    }
    fakeSales.toArray
  }

}
