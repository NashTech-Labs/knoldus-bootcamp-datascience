package controllers

import models.{FoodRepository, Order, OrderRepository}

object CommentForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class CommentData(commentType: String, comment: String)


  val commentForm = Form(
    mapping(
      "commentType" -> nonEmptyText,
      "comment" -> nonEmptyText
    )(CommentData.apply)(CommentData.unapply)
  )

}

