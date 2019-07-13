package controllers

import javax.inject.Inject
import models.{User, UserRepository}
import models.CustomAction
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._

import scala.concurrent.{ExecutionContext, Future}


class HomeController @Inject()(userService: UserRepository, customAction: CustomAction,
                               cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = play.api.Logger(this.getClass)

  def index: Action[AnyContent] = customAction { implicit request =>
    Ok(views.html.index())
  }
}

