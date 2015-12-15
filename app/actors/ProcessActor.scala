package actors

import akka.actor.{ActorRef, Actor}
import com.typesafe.config.ConfigFactory
import services.{MachineLearning, Hdfs, RawDataTransfer}

/**
  * Created by yangrenhui on 15-12-9.
  */
class ProcessActor(statesActor:ActorRef) extends Actor{
  val sysconf = ConfigFactory.load()
  // hdfs://10.45.79.217:9000/user/root/bankResult
  val hdfsMasterUrl="hdfs://"+sysconf.getString("hadoop.master.uri")+":9000/user/root"
  object messages{
    def unapplySeq(msg:String):Option[Seq[String]]={
      val msgs = msg.trim.split(" ")
      if(msgs.forall(_.isEmpty)) None else Some(msgs)
    }
  }
  def receive={
    case messages("Start",trainingfile,targetfile,_*)=>process(trainingfile,targetfile)
    case messages("Create",localfile,numberIteration,modelName,_*)=>create(localfile,numberIteration,modelName)
    case messages("Predict",localfile,metric,_*)=>predict(localfile,metric)
  }

  def process(filename:String, targetname:String): Unit ={
    statesActor ! s"Start /root/$targetname"
    RawDataTransfer.process(s"/tmp/Upload/$filename",s"/tmp/Upload/$targetname",s"/tmp/Process/$filename",s"/tmp/Process/$targetname")
    statesActor!s"Update /root/$targetname 30"
    Hdfs.put(filename,s"/tmp/Process/$filename")
    statesActor!s"Update /root/$targetname 50"
    Hdfs.put(targetname,s"/tmp/Process/$targetname")
    statesActor!s"Update /root/$targetname 60"
   // MachineLearning.run(filename,targetname,"Result")
    statesActor!s"Update /root/$targetname 90"
    Hdfs.get("Result/part-00000",s"/tmp/Download/result.txt")
    statesActor!s"Update /root/$targetname 100"
    Hdfs.del("Result")
    Hdfs.del(targetname)
    Hdfs.del(filename)
  }

  def create(filename:String, numberIteration:String,modelName:String): Unit ={
    statesActor ! s"Start $filename"
    RawDataTransfer.processTrainingRaw(s"/tmp/Upload/$filename",s"/tmp/Process/$filename",s"/tmp/Metric/$modelName")
    statesActor!s"Update $filename 30"
    Hdfs.put(filename,s"/tmp/Process/$filename")
    statesActor!s"Update $filename 50"
    // hdfs://10.45.79.217:9000/user/root/bankResult
    val hdfsTrainingData:String = s"$hdfsMasterUrl/$filename"
    val numIteration = numberIteration.toInt
    val hdfsmodelName:String =  s"$hdfsMasterUrl/model/$modelName"
    MachineLearning.createModel(hdfsTrainingData,numIteration,hdfsmodelName)
    statesActor!s"Update $filename 90"
    Hdfs.del(filename)
    statesActor!s"Update $filename 100"
  }

  def predict(filename:String, metric:String):Unit={
    statesActor ! s"Start $filename"
    RawDataTransfer.processTargetRaw(s"/tmp/Upload/$filename",s"/tmp/Process/$filename",s"/tmp/Metric/$metric")
    statesActor!s"Update $filename 30"
    //upload processed file
    Hdfs.put(filename,s"/tmp/Process/$filename")
    statesActor!s"Update $filename 50"
    val modelName:String = s"$hdfsMasterUrl/model/$metric"
    val hdfsTargetData:String = s"$hdfsMasterUrl/$filename"
    val hdfsresultPath:String = s"$hdfsMasterUrl/result/$filename"
    MachineLearning.predict(modelName,hdfsTargetData,hdfsresultPath)
    statesActor!s"Update $filename 80"
    Hdfs.del(filename)
    Hdfs.del(s"processed_$filename")
    statesActor!s"Update $filename 100"
  }
}
