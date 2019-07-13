package models


import java.util.Date

import javax.inject.Inject
import anorm.SqlParser.{get, scalar}
import anorm._
import controllers.MenuForm.{MenuData}
import play.api.db.DBApi

import scala.concurrent.Future

//create table food_items (
//id                        bigint not null auto_increment,
//food_item                 varchar(255) not null,
//price                     bigint not null,
//constraint pk_food_items primary key(id)
//);

case class Food(id: Long, food_item: String, price: Long)

object Food {
  implicit def toParameters: ToParameterList[Food] =
    Macro.toParameters[Food]
}

@javax.inject.Singleton
class FoodRepository @Inject()(dbapi:DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  private val simple = {
    get[Long]("food_items.id") ~
      get[String]("food_items.food_item") ~
      get[Long]("food_items.price") map {
      case id ~ food_item ~ price =>
        Food(id, food_item, price)
    }
  }

  def getFoodItemsFuture(): Future[List[Food]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from food_items".as(simple.*)
    }
  }(ec)

  def getFoodItems(): List[Food] = {
    db.withConnection { implicit connection =>
      SQL"select * from food_items".as(simple.*)
    }
  }

  def getFoodItem(foodItem: String): List[Food] = {
    db.withConnection { implicit connection =>
      SQL"select * from food_items where food_item=$foodItem".as(simple.*)
    }
  }

}

