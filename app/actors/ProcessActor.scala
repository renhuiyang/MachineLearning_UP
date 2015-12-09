package actors

import akka.actor.{ActorRef, Actor}
import services.{MachineLearning, Hdfs, RawDataTransfer}

/**
  * Created by yangrenhui on 15-12-9.
  */
class ProcessActor(statesActor:ActorRef) extends Actor{
  object messages{
    def unapplySeq(msg:String):Option[Seq[String]]={
      val msgs = msg.trim.split(" ")
      if(msgs.forall(_.isEmpty)) None else Some(msgs)
    }
  }
  def receive={
    case messages("Start",trainingfile,targetfile,_*)=>process(trainingfile,targetfile)
  }

  def process(filename:String, targetname:String): Unit ={
    statesActor ! s"Start /root/$targetname"
    RawDataTransfer.process(s"/tmp/Upload/$filename",s"/tmp/Upload/$targetname",s"/tmp/Process/$filename",s"/tmp/Process/$targetname")
    statesActor!s"Update /root/$targetname 30"
    Hdfs.put(filename,s"/tmp/Process/$filename")
    statesActor!s"Update /root/$targetname 50"
    Hdfs.put(targetname,s"/tmp/Process/$targetname")
    statesActor!s"Update /root/$targetname 60"
    MachineLearning.run(filename,targetname,"Result")
    statesActor!s"Update /root/$targetname 90"
    Hdfs.get("Result/part-00000",s"/tmp/Download/result.txt")
    statesActor!s"Update /root/$targetname 100"
  }
}
