package services

import com.typesafe.config.ConfigFactory
/*import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkContext, SparkConf}*/
import sys.process._

/**
  * Created by yangrenhui on 15-12-7.
  */
object MachineLearning {
/*  val sysconf = ConfigFactory.load()
  val masterurl = sysconf.getString("hadoop.master.uri")
  val conf = new SparkConf().setAppName("Spark MLib Exercise:SVMWithSGD").setMaster(s"spark://$masterurl:7077")
  val sc = new SparkContext(conf)*/
  val conf = ConfigFactory.load()
  val sparkMaster = conf.getString("hadoop.master.uri")
  val cmd:String=s"spark-submit --class main.scala.SVMWithSGD_Main --master spark://$sparkMaster:7077  SVMWithSGD.jar "

  def createModel(hdfsTrainingData:String,numIteration:Int,hdfsmodelName:String): Unit ={
    val runCmd = cmd + s"Create $hdfsTrainingData $hdfsmodelName $numIteration"
    runCmd.!
    println(runCmd)
  }

  def predict(modelName:String,hdfsTargetData:String,hdfsresultPath:String): Unit ={
    val runCmd = cmd +s"Predict $modelName $hdfsTargetData $hdfsresultPath"
    runCmd.!
  }
}
