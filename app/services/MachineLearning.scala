package services

import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkContext, SparkConf}
import sys.process._

/**
  * Created by yangrenhui on 15-12-7.
  */
object MachineLearning {
  val sysconf = ConfigFactory.load()
  val masterurl = sysconf.getString("hadoop.master.uri")
  val conf = new SparkConf().setAppName("Spark MLib Exercise:SVMWithSGD").setMaster(s"spark://$masterurl:7077")
  val sc = new SparkContext(conf)
/*  val conf = ConfigFactory.load()
  val sparkMaster = conf.getString("hadoop.master.uri")
  val cmd:String=s"spark-submit --class main.scala.SVMWithSGD_Main --master spark://$sparkMaster:7077  SVMWithSGD.jar "

  def run(trainingData:String,targetData:String,resultPath:String): Unit ={
    val runCmd = cmd + s"hdfs://$sparkMaster:9000/user/root/$trainingData hdfs://$sparkMaster:9000/user/root/$targetData hdfs://$sparkMaster:9000/user/root/$resultPath 500"
    runCmd.!
  }*/

  def createModel(hdfsTrainingData:String,numIteration:Int,hdfsmodelName:String): Unit ={
    val rawTrainingData = sc.textFile(hdfsTrainingData)
    val parsedTrainingData = rawTrainingData.map { line => {
      val parts = line.split(",").map(_.trim)
      LabeledPoint(parts(parts.length - 1).toDouble, Vectors.dense(parts.slice(0, parts.length - 1).map(_.toDouble)))
    }
    }.cache()


    val model = SVMWithSGD.train(parsedTrainingData, numIteration)

    val valuesAndPreds = parsedTrainingData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val MSE = valuesAndPreds.map { case (v, p) => math.pow((v - p), 2) }.reduce(_ + _) / valuesAndPreds.count
    println("training Mean Squared Error = " + MSE)

    //save model
    model.save(sc,hdfsmodelName)
  }

  def predict(modelName:String,hdfsTargetData:String,hdfsresultPath:String): Unit ={
    val model = SVMModel.load(sc,modelName)
    val rawTestData = sc.textFile(hdfsTargetData)
    val parsedTestData = rawTestData.map { line => {
      Vectors.dense(line.split(",").map(_.trim).filter(!"".equals(_)).map(_.toDouble))
    }
    }.cache()

    val prediction = model.predict(parsedTestData)
    val predictionAndLabel = rawTestData.zip(prediction).map{ case(line,p)=>{line+","+p.toString()}}

    predictionAndLabel.coalesce(1).saveAsTextFile(hdfsresultPath)
  }


}
