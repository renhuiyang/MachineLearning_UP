package controllers

import java.util.concurrent.TimeUnit

import actors.{StatesActor, ProcessActor}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import services.{MachineLearning, RawDataTransfer, Hdfs}

import javax.inject.Inject
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.pattern.ask

class Application extends Controller {
  val conf = ConfigFactory.load()
  val system = ActorSystem("MachineLearning")
  val statesActor = system.actorOf(Props[StatesActor],name="mystatesactor")
  val processActor = system.actorOf(Props(new ProcessActor(statesActor)),name="myprocessactor")


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action{
    Ok(views.html.login("Login"))
  }

  val taskForm = Form(
    tuple(
      "TrainingData" -> text,
      "TargetData" -> text
    )
  )

  def upload = Action{
    Ok(views.html.upload("root"))
  }

  def process = Action(parse.multipartFormData) { request =>
    request.body.file("TrainingData").map { picture =>
      //import java.io.File
      val filename = picture.filename
      picture.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
      request.body.file("TargetData").map{ target =>
        val targetname = target.filename
        target.ref.moveTo(new java.io.File(s"/tmp/Upload/$targetname"))

        processActor!s"Start $filename $targetname"

        Ok(views.html.result("wait",targetname))
      }.getOrElse{
        Redirect(routes.Application.upload).flashing{
          "error"->"Missing Target file"
        }
      }
    }.getOrElse {
      Redirect(routes.Application.upload).flashing(
        "error" -> "Missing Training file")
    }
  }

  val userForm = Form(
    tuple(
      "user" -> text,
      "pass" -> text
    )
  )

/*  def postuser=Action{ request=>
    val (user, password) = userForm.bindFromRequest.get
    Ok(views.html.upload("root")).withSession("user"->"root")
  }*/

  def query(id:String) = Action.async{
    implicit val _timeout = Timeout(3,TimeUnit.SECONDS)
    (statesActor?s"Query /root/$id").mapTo[String].map{percentage=>Ok(percentage)}
  }

  def result = Action{
    Ok(views.html.result("10","banking-batch_notital.csv"))
  }

  def download(result:String)=Action{
    Ok.sendFile(
      content = new java.io.File(s"/tmp/Download/$result.txt"),
      fileName = _ => "result.txt"
    )
  }
}
