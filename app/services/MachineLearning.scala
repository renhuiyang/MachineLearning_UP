package services

import com.typesafe.config.ConfigFactory
import sys.process._

/**
  * Created by yangrenhui on 15-12-7.
  */
object MachineLearning {
  val conf = ConfigFactory.load()
  val sparkMaster = conf.getString("hadoop.master.uri")
  val cmd:String=s"spark-submit --class main.scala.SVMWithSGD_Main --master spark://$sparkMaster:7077  SVMWithSGD.jar "

  def run(trainingData:String,targetData:String,resultPath:String): Unit ={
    val runCmd = cmd + s"hdfs://$sparkMaster:9000/user/root/$trainingData hdfs://$sparkMaster:9000/user/root/$targetData hdfs://$sparkMaster:9000/user/root/$resultPath 500"
    runCmd.!
  }
}
