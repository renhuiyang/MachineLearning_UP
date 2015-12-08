package controllers

import com.typesafe.config.ConfigFactory
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import services.{MachineLearning, RawDataTransfer, Hdfs}

import javax.inject.Inject
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory

import play.api.mvc._
import play.api.libs.ws._


class Application extends Controller {
  val conf = ConfigFactory.load()

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val taskForm = Form(
    tuple(
      "TrainingData" -> text,
      "TargetData" -> text
    )
  )

  def upload = Action{
    Ok(views.html.upload("Please input training data and target data"))
  }

  def process = Action(parse.multipartFormData) { request =>
    request.body.file("TrainingData").map { picture =>
      //import java.io.File
      val filename = picture.filename
      picture.ref.moveTo(new java.io.File(s"/tmp/Upload/$filename"))
      request.body.file("TargetData").map{ target =>
        val targetname = target.filename
        target.ref.moveTo(new java.io.File(s"/tmp/Upload/$targetname"))

        RawDataTransfer.process(s"/tmp/Upload/$filename",s"/tmp/Upload/$targetname",s"/tmp/Process/$filename",s"/tmp/Process/$targetname")

        Hdfs.put(filename,s"/tmp/Process/$filename")
        Hdfs.put(targetname,s"/tmp/Process/$targetname")

        MachineLearning.run(filename,targetname,"Result")

        Hdfs.get("Result/part-00000",s"/tmp/Download/result.txt")

        Ok.sendFile(
          content = new java.io.File(s"/tmp/Download/result.txt"),
          fileName = _ => "result.txt"
        )
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
}
