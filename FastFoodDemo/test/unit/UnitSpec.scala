package unit

import controllers.MenuForm
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n._
import play.api.mvc._
import play.api.test._
/*
def menu = securityAction.async { implicit request =>
Future(Ok(views.html.menu(postUrl, menuForm)))
}
*/
/*
"Cheese Burger $2" -> number(min = 0),
"Double Double $4" -> number(min = 0),
"Fries $1" -> number(min = 0),
"Milk Shake $3" -> number(min = 0)
)(MenuData.apply)(MenuData.unapply)
*/

//case class MenuData(cheeseBurger: Int, doubleDouble: Int, fries: Int, milkShake: Int)

class UnitSpec extends PlaySpec {

  "MenuForm" must {

    "apply successfully from request" in {
      val call = controllers.routes.MenuController.menu()
      implicit val request: Request[_] = FakeRequest(call).withFormUrlEncodedBody("Food_0" -> "2", "Food_1" -> "0", "Food_2" -> "9", "Food_3" -> "3")
      val boundForm = MenuForm.menuForm.bindFromRequest()
      val menuData = boundForm.value.get

      menuData.cheeseBurger must equal(2)
      menuData.doubleDouble must equal(0)
      menuData.fries must equal(9)
      menuData.milkShake must equal(3)
    }


    "apply successfully from map" in {
      val data = Map("Food_0" -> "2", "Food_1" -> "0", "Food_2" -> "9", "Food_3" -> "3")
      val boundForm = MenuForm.menuForm.bind(data)
      val menuData = boundForm.value.get

      menuData.cheeseBurger must equal(2)
      menuData.doubleDouble must equal(0)
      menuData.fries must equal(9)
      menuData.milkShake must equal(3)
    }


    "show errors when applied unsuccessfully" in {
      // Pass in a negative price that fails the constraints...
      val data = Map("Food_0" -> "2", "Food_1" -> "0", "Food_2" -> "9", "Food_3" -> "-3")

      val errorForm = MenuForm.menuForm.bind(data)
      val listOfErrors = errorForm.errors

      val formError: FormError = listOfErrors.head
      formError.key must equal("Food_3")

      errorForm.hasGlobalErrors mustBe false

      formError.message must equal("error.min")

      val lang: Lang = Lang.defaultLang
      val messagesApi: MessagesApi = new DefaultMessagesApi(Map(lang.code -> Map("error.min" -> "Must be greater or equal to {0}")))
      val messagesProvider: MessagesProvider = messagesApi.preferred(Seq(lang))
      val message: String = Messages(formError.message, formError.args: _*)(messagesProvider)

      message must equal("Must be greater or equal to 0")
    }

  }


}
