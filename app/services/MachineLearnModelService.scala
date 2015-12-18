package services

import models.{MachineLearnModels, MachineLearnModel}
import scala.concurrent.Future
/**
  * Created by yangrenhui on 15-12-18.
  */
object MachineLearnModelService {
  def add(m:MachineLearnModel):Future[String]={
    MachineLearnModels.add(m)
  }

  def delete(name:String):Future[Int]={
    MachineLearnModels.delete(name)
  }

  def get(name:String):Future[Option[MachineLearnModel]]={
    MachineLearnModels.get(name)
  }

  def listAll():Future[Seq[MachineLearnModel]]={
    MachineLearnModels.listAll
  }
}
