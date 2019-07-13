package controllers

import javax.inject.Inject
import models.{CommentRepository, OrderRepository, SecurityAction, UserRepository}
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class ViewCommmentsController @Inject()(messagesApi: MessagesApi, securityAction: SecurityAction, commentService: CommentRepository, orderService: OrderRepository, security: Security,
                                        cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with I18nSupport {

  import FilterForm._



  def index = Action {
    Ok(views.html.index())
  }


  def listComments = securityAction.async { implicit request =>
    //val name=request.session.get("USERNAME")

    commentService.getComments().map{ comments => println("ncomments= " + comments.size); Ok(views.html.listComments(comments)) }
  }

}



