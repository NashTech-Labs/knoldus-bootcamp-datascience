package controllers


import javax.inject.Inject
import models.UserRepository
import play.api.data._
import play.api.i18n._
import play.api.mvc._

import scala.concurrent.ExecutionContext

import scala.concurrent.Future

class Security @Inject()(userService: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def security(name: Option[String], pass: Option[String], func: Result): Future[Result] = {
    val nameStr=name match {case Some(nameStr) => nameStr; case _ => "" }
    val passStr=pass match {case Some(passStr) => passStr; case _ => "" }
    userService.checkUser(nameStr, passStr).map { check =>
      println("check= " + check)
      if (check.contains(1)) {
        func
      }
      else { Ok(views.html.index()).withSession("USERNAME" -> "", "PASS" -> "")  }
    }
  }


  def security2(name: String, pass: String, func: Result): Option[Result] = {
    userService.checkUser2(name, pass).map { check =>
      println("check= " + check)
      if (check==1) {
        func
      }
      else { Ok(views.html.index()).withSession("USERNAME" -> "", "PASS" -> "") }
    }
  }
}



