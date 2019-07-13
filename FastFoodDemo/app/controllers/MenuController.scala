package controllers

import javax.inject.Inject
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import models.{FoodRepository, SecurityAction, UserRepository}
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class MenuController @Inject()(messagesApi: MessagesApi, securityAction: SecurityAction, userService: UserRepository, foodService: FoodRepository, securityService: Security, cc: MessagesControllerComponents)
                              (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with I18nSupport {
  import MenuForm._
  import LoginForm._

  private val postUrl = routes.OrderController.order()


  def index = Action {
    Ok(views.html.index())
  }

  def menu = securityAction.async { implicit request =>
    val foodItems=foodService.getFoodItems().toArray
    Future(Ok(views.html.menu(postUrl, menuForm, foodItems)))
  }

}
