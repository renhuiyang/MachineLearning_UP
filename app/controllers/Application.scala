package controllers

import java.util.concurrent.TimeUnit

import actors.{StatesActor, ProcessActor}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
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

  new java.io.File("/tmp/Process").mkdirs
  new java.io.File("/tmp/Download").mkdirs
  new java.io.File("/tmp/Upload").mkdirs
  new java.io.File("/tmp/Metric").mkdirs

  Hdfs.mkdir("model")
  Hdfs.mkdir("result")

  def index = Action {
    Ok(views.html.index())
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

  def query(id:String) = Action.async{
    implicit val _timeout = Timeout(3,TimeUnit.SECONDS)
    (statesActor?s"Query $id").mapTo[String].flatMap{percentage=>
      if(percentage=="100"){
        Future{Hdfs.list("model")}.map{arrays=>
        {
          Ok(Json.obj("percentage"->percentage,"models"->Json.toJson(arrays)))
        }
        }
      }else{
        Future{Ok(Json.obj("percentage"->percentage))}
      }
    }
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

  def readExitsModel = Action.async{
    val result = Future{Hdfs.list("model")}
    result.map{
      case array:Array[String] =>Ok(views.html.savedModel(array))
      case _ => Ok(views.html.savedModel(Array[String]()))
    }
  }

  def createModel = Action{
    Ok(views.html.createModel())
  }

  def create = Action(parse.multipartFormData) { request =>
    val picture = request.body.file("TrainingData").get
    //import java.io.File
    val filename = picture.filename
    picture.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
    processActor ! s"Create $filename"
    Ok(views.html.createwaiting(filename))
  }

  def predict = Action{request=>
    val tmpForm = Form(
        "model"->text
    )
    val model = tmpForm.bindFromRequest()(request).get
    val file = request.body.asMultipartFormData.get.file("TragetData").get

    val filename = file.filename
    file.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
    processActor!s"Predict $filename $model"
    Ok(views.html.predictwaiting(filename))
  }
}
