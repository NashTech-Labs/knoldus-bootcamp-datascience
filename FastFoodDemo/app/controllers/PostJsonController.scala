package controllers

import javax.inject.Inject
import models.{User, UserRepository}
import models.CustomAction
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.http.HttpEntity
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api.libs.json.JsValue
import views._

import scala.concurrent.{ExecutionContext, Future}


class PostJsonController @Inject()(ws: WSClient,
                                      cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index())
  }

  def postJson(foodItem: String) = Action.async { implicit request =>
    val url="http://localhost:9000/bodyParser"
    val data = Json.obj(
      "foodItem" -> foodItem
    )
    ws.url(url).post(data).map{ response => Ok(response.body) }
  }


}



