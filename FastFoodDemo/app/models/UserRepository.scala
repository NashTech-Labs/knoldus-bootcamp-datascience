package models


import java.util.Date

import akka.stream.actor.ActorPublisherMessage.Request
import javax.inject.Inject
import anorm.SqlParser.{get, scalar}
import anorm._
import controllers.LoginForm.Data
import play.api.db.DBApi
import play.api.mvc.RequestHeader

import scala.concurrent.Future

case class User(id: Long, name: String, pass: String)

object User {
  implicit def toParameters: ToParameterList[User] =
    Macro.toParameters[User]
}

@javax.inject.Singleton
class UserRepository @Inject()(dbapi:DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  private val simple = {
    get[Long]("users.id") ~
    get[String]("users.name") ~
      get[String] ("users.pass") map {
      case id ~ name ~ pass =>
        User(id, name, pass)
    }
  }

  private val simpleInt = {
    get[Int]("auth_count") map { case x => x }
  }

  def findByName(name: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from users where name = $name".as(simple.singleOpt)
    }
  }(ec)

  def checkUser(name: String, pass: String): Future[Option[Int]] = Future {
    db.withConnection { implicit connection =>
      SQL"select count(*) as auth_count from users where name = $name and pass = $pass".as(simpleInt.singleOpt)
    }
  }(ec)

  def checkUser2(name: String, pass: String): Option[Int] = {
    db.withConnection { implicit connection =>
      SQL"select count(*) as auth_count from users where name = $name and pass = $pass".as(simpleInt.singleOpt)
    }
  }

  def checkUser(request: RequestHeader): Option[Int] = {
    val name=request.session.get("USERNAME")
    val pass=request.session.get("PASS")
    println("name= " + name)
    println("pass= " + pass)
    val check = (name, pass) match {
      case (Some(nameStr), Some(passStr))  => checkUser2(nameStr, passStr)
      case _ => Some(0)
    }
    check
  }
}
