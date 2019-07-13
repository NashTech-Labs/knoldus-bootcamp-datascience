package controllers

object FilterForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class FilterData(foodItem: String, minQuantity: String)


  val filterForm = Form(
    mapping(
      "Menu Item" -> text,
      "password" -> text
    )(FilterData.apply)(FilterData.unapply)
  )
}

