package models

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
  * Created by yangrenhui on 15-12-18.
  */

case class MachineLearnModel(id:Long,name:String,sourceData:String,description:String)
case class MachineLearnModelFormData(name:String,sourceData:String,description:String)

object MachineLearnModelForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "sourceData" -> nonEmptyText,
      "description" -> nonEmptyText
    )(MachineLearnModelFormData.apply)(MachineLearnModelFormData.unapply)
  )
}

object MachineLearnModel{
  implicit val MachineLearnModelWrites:Writes[MachineLearnModel]=(
    (JsPath \ "id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "sourcedata").write[String] and
    (JsPath \ "description").write[String])(unlift(MachineLearnModel.unapply))
}

class MachineLearnModelTableDef(tag:Tag)extends Table[MachineLearnModel](tag,"MachineLearnModel"){
  def id = column[Long]("id",O.AutoInc,O.PrimaryKey)
  def name = column[String]("name")
  def sourceData = column[String]("sourceData")
  def description = column[String]("description")

  override def * = (id,name,sourceData,description) <> (MachineLearnModel.tupled,MachineLearnModel.unapply)
  def idx =  index("idx_a", name, unique = true)
}

object MachineLearnModels{
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val ms = TableQuery[MachineLearnModelTableDef]

  def add(m:MachineLearnModel):Future[String]={
    dbConfig.db.run(ms += m).map{res=>"Model add successful!"}.recover{
      case ex:Exception=>ex.getCause.getMessage
    }
  }

  def delete(name:String):Future[Int]={
    dbConfig.db.run(ms.filter(_.name===name).delete)
  }

  def get(name:String):Future[Option[MachineLearnModel]]={
    dbConfig.db.run(ms.filter(_.name===name).result.headOption)
  }

  def listAll():Future[Seq[MachineLearnModel]]={
    dbConfig.db.run(ms.result)
  }
}








