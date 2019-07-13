package models


import java.util.Date

import javax.inject.Inject
import anorm.SqlParser.{get, scalar}
import anorm._
import controllers.CommentForm.CommentData
import controllers.MenuForm.MenuData
import play.api.db.DBApi

import scala.concurrent.Future

case class Comment(id: Long, comment_type: String, comment_str: String)

object Comment {
  implicit def toParameters: ToParameterList[Comment] =
    Macro.toParameters[Comment]
}

@javax.inject.Singleton
class CommentRepository @Inject()(dbapi:DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  private val simple = {
    get[Long]("comments.id") ~
      get[String]("comments.comment_type") ~
      get[String]("comments.comment_str") map {
      case id ~ comment_type ~ comment_str =>
        Comment(id, comment_type, comment_str)
    }
  }

  def insert(commentData: CommentData): Future[Option[Long]] = Future {
    println("Inserting comment " + commentData.comment)
    db.withConnection { implicit connection =>
        SQL"insert into comments (comment_type, comment_str) values (${commentData.commentType}, ${commentData.comment}) ".executeInsert()
    }
  }(ec)


  def getComments(): Future[List[Comment]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from comments".as(simple.*)
    }
  }(ec)

  /*
  def getOrders(userId: Option[String], foodItem: String, minQuantity: Int): Future[List[Order]] = Future {
    val name= userId match { case Some(userIdStr) => userIdStr; case _ => ""}
    //val foodItemStr=foodItem match { case Some(foodItemStr) => foodItemStr; case _ => " " }
    val queryFood=if (foodItem!="") { " and food_item='" + foodItem + "'" } else { "" }
    val queryQuantity=" and quantity>=" + minQuantity
    val query="select * from food_orders where user_id='" + name + "'" + queryFood + queryQuantity
    println(query)
    db.withConnection { implicit connection =>
      //SQL"select * from food_orders where user_id=$name and food_item=$foodItemStr".as(simple.*)
      SQL(query).as(simple.*)
    }
  }(ec)
  */

}

