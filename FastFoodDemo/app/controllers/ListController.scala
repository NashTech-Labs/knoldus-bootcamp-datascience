package controllers

import javax.inject.Inject
import models.{OrderRepository, SecurityAction, UserRepository}
import play.api.data._
import play.api.i18n._
import play.api.mvc._

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class ListController @Inject()(messagesApi: MessagesApi, securityAction: SecurityAction, userService: UserRepository, orderService: OrderRepository, security: Security,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with I18nSupport {

  import FilterForm._



  def index = Action {
    Ok(views.html.index())
  }

  def list(foodItem: String, minQuantity: Int) = securityAction.async { implicit request =>
    val name=request.session.get("USERNAME")

    orderService.getOrders(name, foodItem, minQuantity).map{ listOrders => Ok(views.html.list(listOrders, filterForm)) }
  }

  def list = securityAction.async { implicit request =>
    val name=request.session.get("USERNAME")

    orderService.getOrders(name).map{ listOrders => Ok(views.html.list(listOrders, filterForm)) }
  }

}


