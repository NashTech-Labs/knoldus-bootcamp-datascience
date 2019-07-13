package controllers

import javax.inject.Inject
import models._
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


class BodyParserController @Inject()(ws: WSClient, foodService: FoodRepository,
                                   cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index())
  }

  def bodyParser = Action { request: Request[AnyContent] =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      val foodItemName=(json \ "foodItem").as[String]
      val food=foodService.getFoodItem(foodItemName)
      if (food.isEmpty) {
        Ok("No menu item with that name")
      }
      else {
        Ok("The price of " + foodItemName + " is " + "$" + food.head.price)
      }
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
  }


}




