package controllers

import models.UserForm
import play.api.mvc._
import services.UserService
import scala.concurrent.Future
import models.{User,Users}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by yangrenhui on 15-12-17.
  */
class DbAppController extends Controller{
  def userList = Action.async { implicit request =>
    UserService.listAllUsers.map {
      users =>
        Ok(views.html.userList(UserForm.form, users))
    }
  }

  def addUser = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.userList(errorForm, Seq.empty[User]))),
      data => {
        val newUser = User(0, data.firstName, data.lastName, data.mobile, data.email)
        UserService.addUser(newUser).map { res =>
          Redirect(routes.DbAppController.userList())
        }
      }
    )
  }

  def deleteUser(id:Long) = Action.async{implicit request=>
    UserService.deleteUser(id).map{res=>
      Redirect(routes.DbAppController.userList())
    }
  }
}
