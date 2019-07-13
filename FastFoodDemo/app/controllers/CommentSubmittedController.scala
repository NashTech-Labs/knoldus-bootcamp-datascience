package controllers

import javax.inject.Inject
import models.{CommentRepository, OrderRepository, SecurityAction, UserRepository}
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class CommentSubmittedController @Inject()(messagesApi: MessagesApi, securityAction: SecurityAction, commentService: CommentRepository, orderService: OrderRepository, security: Security,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with I18nSupport {

  import CommentForm._



  def index = Action {
    Ok(views.html.index())
  }
  /*
  def thanks = securityAction.async { implicit request =>
    //val name=request.session.get("USERNAME")

    Future(Ok(views.html.thanks(commentForm)))
  }
  */
  def thanks = securityAction.async { implicit request =>
    val name=request.session.get("USERNAME")


    val failFunc={
      formWithErrors: Form[CommentData] => {
        //val foodItems=foodService.getFoodItems().toArray
        Future(Ok(views.html.comments(formWithErrors)))
      }
    }


    //val foodItems=foodService.getFoodItems().toArray

    //val failFunc=null

    val successFunc={
      commentData: CommentData => {
        //val orderId=scala.util.Random.nextInt
        //orderService.insert(orderForm, orderId, name, foodItems)
        commentService.insert(commentData)
        println("After submitting comment")
        Future(Ok(views.html.thanks(commentData)))
      }
    }

    commentForm.bindFromRequest.fold(failFunc, successFunc)
  }


}



