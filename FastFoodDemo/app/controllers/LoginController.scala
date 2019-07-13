package controllers

import javax.inject.Inject
import models.{UserRepository}
import play.api.data._
import play.api.i18n._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class LoginController @Inject()(userService: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import LoginForm._

  private val postUrl = routes.AuthController.auth()

  def index = Action {
    Ok(views.html.index())
  }

  def login = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.login(postUrl, loginForm))
  }

}

