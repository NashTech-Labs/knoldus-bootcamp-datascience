package controllers

import javax.inject.Inject
import models.{FoodRepository, OrderRepository, SecurityAction, UserRepository}
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class OrderController @Inject()(foodService: FoodRepository, messagesApi: MessagesApi, securityAction: SecurityAction, userService: UserRepository, orderService: OrderRepository,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with I18nSupport {
  import MenuForm._




  def index = Action {
    Ok(views.html.index())
  }


  def order = securityAction.async { implicit request =>
    val name=request.session.get("USERNAME")


    val failFunc={
      formWithErrors: Form[MenuData] => {
        val foodItems=foodService.getFoodItems().toArray
        Future(Ok(views.html.menu(routes.OrderController.order, formWithErrors, foodItems)))
      }
    }


    val foodItems=foodService.getFoodItems().toArray

    //val failFunc=null

    val successFunc={
      orderForm: MenuData => {
        val orderId=scala.util.Random.nextInt
        orderService.insert(orderForm, orderId, name, foodItems)
        Future(Ok(views.html.order(orderForm, foodItems)))
      }
    }

    menuForm.bindFromRequest.fold(failFunc, successFunc)
  }


}

