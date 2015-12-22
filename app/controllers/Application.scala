package controllers

import java.util.concurrent.TimeUnit

import actors.{StatesActor, ProcessActor}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import models.MachineLearnModels
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

  def query(id: String, qtype: String) = Action.async {
    implicit val _timeout = Timeout(3, TimeUnit.SECONDS)
    (statesActor ? s"Query $id").mapTo[String].flatMap { percentage =>
      if (percentage == "100") {
        MachineLearnModels.listAll.map { arrays => {
          Ok(Json.obj("percentage" -> percentage,"modals"->Json.toJson(arrays)))
        }
        }
      } else {
        Future {
          Ok(Json.obj("percentage" -> percentage))
        }
      }
    }
  }

  def download(result:String)=Action{
    if(! new java.io.File(s"/tmp/Download/$result").exists){
      Hdfs.get(s"result/$result/part-00000",s"/tmp/Download/raw_$result")
      Hdfs.del(s"result/$result")
      RawDataTransfer.zipResult(s"/tmp/Upload/$result",s"/tmp/Download/raw_$result",s"/tmp/Download/$result")
      new java.io.File(s"/tmp/Download/raw_$result").delete
    }
    Ok.sendFile(
      content = new java.io.File(s"/tmp/Download/$result"),
      fileName = _ => result
    )
  }

  def showModels = Action.async {
      //Hdfs.list("model")
      MachineLearnModels.listAll.map { arrays => {
      Ok(Json.toJson(arrays))
    }
    }
  }

  def showResults = Action.async {
    Future {
      Hdfs.list("result")
    }.map { arrays => {
      Ok(Json.obj("models" -> Json.toJson(arrays)))
    }
    }
  }

  def readExitsModel = Action{
    Ok(views.html.savedModel())
  }

  def createModel = Action{
    Ok(views.html.createModel())
  }

  def create = Action(parse.multipartFormData) { implicit request =>
    val numIteration = request.body.dataParts.get("numIteration").getOrElse(Seq.empty[String]).lift(0).getOrElse("None")
    val modelName = request.body.dataParts.get("model").getOrElse(Seq.empty[String]).lift(0).getOrElse("None")
    //val description = request.body.dataParts.get("dscription").getOrElse(Seq.empty[String]).lift(0).getOrElse("None")
    val picture = request.body.file("TrainingData").get
    //import java.io.File
    val filename = picture.filename
    picture.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
    processActor ! s"Create $filename $numIteration $modelName"
    Ok(views.html.createwaiting(filename))
  }

  def predict = Action(parse.multipartFormData){request=>
    val model = request.body.dataParts.get("model").getOrElse(Seq.empty[String]).lift(0).getOrElse("None")
    val file = request.body.file("TargetData").get

    val filename = file.filename
    file.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
    processActor!s"Predict $filename $model"
    Ok(views.html.predictwaiting(filename))
  }

  def uploadTarget(model:String) = Action{
    Ok(views.html.uploadTarget(model))
  }

  def removeModel(model:String) = Action{
    Hdfs.del(s"model/$model")
    Hdfs.del("model/"+model+"_description")
    Ok(views.html.savedModel())
  }
}
