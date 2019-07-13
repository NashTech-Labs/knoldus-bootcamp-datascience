package controllers

import models.{FoodRepository, Order, OrderRepository}

object MenuForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class MenuData(cheeseBurger: Int, doubleDouble: Int, fries: Int, milkShake: Int)

  val foodItems=Array("Cheese Burger", "Double Double", "Fries", "Milk Shake")
  val prices=Array(2, 4, 1, 3)


  /*
  val menuForm = Form(
    mapping(
      foodItems(0).food_item + " $" + foodItems(0).price -> number(min = 0),
      foodItems(1).food_item + " $" + foodItems(1).price -> number(min = 0),
      foodItems(2).food_item + " $" + foodItems(2).price -> number(min = 0),
      foodItems(3).food_item + " $" + foodItems(3).price -> number(min = 0)
    )(MenuData.apply)(MenuData.unapply)
  )
  */
  /*
  val menuForm = Form(
    mapping(
      foodItems(0) + " $" + prices(0) -> number(min = 0),
      foodItems(1) + " $" + prices(1) -> number(min = 0),
      foodItems(2) + " $" + prices(2) -> number(min = 0),
      foodItems(3) + " $" + prices(3) -> number(min = 0)
    )(MenuData.apply)(MenuData.unapply)
  )
  */
  val menuForm = Form(
    mapping(
      "Food_0" -> number(min = 0),
      "Food_1" -> number(min = 0),
      "Food_2" -> number(min = 0),
      "Food_3" -> number(min = 0)
    )(MenuData.apply)(MenuData.unapply)
  )

}
