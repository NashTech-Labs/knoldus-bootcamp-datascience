package controllers

import javax.inject.Inject
import models.{User, UserRepository}
import models.CustomAction
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api.libs.json.JsValue
import views._

import scala.concurrent.{ExecutionContext, Future}


class FakeRestApiController @Inject()(ws: WSClient,
                               cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index())
  }

  def fakeInfo(n: Int) = Action.async {
    val url="https://jsonplaceholder.typicode.com/todos/" + n
    ws.url(url).stream.map { response =>
      if (response.status == 200) { Ok("Got: " + response.json \ "title" ) }
      else { Ok("No response") }
    }
  }

  def fakeInfo2(n: Int) = Action.async {
    val url="https://jsonplaceholder.typicode.com/todos/" + n
    ws.url(url).stream.map { response =>
      if (response.status == 200) { Ok("Got: " + response.json \ "title" ) }
      else { Ok("No response") }
    }
  }
}


