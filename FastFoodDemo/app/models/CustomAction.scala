package models

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext

import scala.concurrent.Future

class CustomAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    println("Calling action")
    block(request)
  }
}
