package controllers

object LoginForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class Data(name: String, password: String)


  val loginForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )
}
