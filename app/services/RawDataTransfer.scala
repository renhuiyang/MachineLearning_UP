package services

import java.io.{File, PrintWriter}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.control.Exception._

import scala.pickling.Defaults._
import scala.pickling.json._

/**
  * Created by yangrenhui on 15-12-7.
  */
object RawDataTransfer {
  private class ItemNum{
    var nums = ArrayBuffer[ArrayBuffer[String]]()

    def set(i: Int, str: String): Unit = {
      if (i+1 < nums.length) {
        if (nums(i) contains str) {
        } else {
          nums(i) += str
        }
      }else{
        while(nums.length < i+1){
          nums += ArrayBuffer[String]()
        }
        nums(nums.length-1) += str
      }
    }

    def get(i:Int, str:String): String ={
      if(isDoubleNumber(str)) {
        return str
      }
      if (i < nums.length)
        nums(i).indexOf(str).toString
      else
        "-1"
    }
  }

  private def isDoubleNumber(s:String):Boolean = (allCatch opt s.toDouble).isDefined

  def process(rawTrainingData:String,rawTargetData:String,trainingData:String,targetData:String): Unit ={
    val itemNum = new ItemNum()

    // Loop through each line in the file
    for (line <- Source.fromFile(rawTrainingData).getLines()) {
      val parts = line.split(",").map(_.trim).zipWithIndex.foreach{case(x,i)=>{
        if(!isDoubleNumber(x)){
          itemNum.set(i,x)
        }
      }}
    }

    val trainingfileoutputname = trainingData
    val trainingwriter = new PrintWriter(new File(trainingfileoutputname))

    for (line <- Source.fromFile(rawTrainingData).getLines()) {
      val parts = line.split(",").map(_.trim)
      val processedline = for{
        part <- parts
      } yield itemNum.get(parts.indexOf(part),part)
      trainingwriter.write(processedline.mkString(",")+"\n")
    }

    trainingwriter.close()

    val testfilename = rawTargetData
    val testfileoutputname = targetData
    val testWriter = new PrintWriter(new File(testfileoutputname))

    for (line <- Source.fromFile(testfilename).getLines()) {
      val parts = line.split(",").map(_.trim)
      val processedline = for {
        part <- parts
      } yield itemNum.get(parts.indexOf(part), part)
      testWriter.write(processedline.mkString(",")+"\n")
    }

    testWriter.close()
  }

  def processTrainingRaw(rawTrainingData:String,trainingData:String,metricData:String): Unit ={
    val itemNum = new ItemNum()
    // Loop through each line in the file
    for (line <- Source.fromFile(rawTrainingData).getLines()) {
      line.split(",").map(_.trim).zipWithIndex.foreach {
        case (x, i) => {
          if (!isDoubleNumber(x)) {
            itemNum.set(i, x)
          }
        }
      }
    }

    val trainingwriter = new PrintWriter(new File(trainingData))

    for (line <- Source.fromFile(rawTrainingData).getLines()) {
      val parts = line.split(",").map(_.trim)
      val processedline = for{
        part <- parts
      } yield itemNum.get(parts.indexOf(part),part)
      trainingwriter.write(processedline.mkString(",")+"\n")
    }

    trainingwriter.close
    val itemNumpicking = itemNum.pickle.value
    val writer = new PrintWriter(new File(metricData))
    writer.write(itemNumpicking)
    writer.close
  }

  def processTargetRaw(rawTargetData:String,targetData:String,metricData:String): Unit ={
    val metricfile = Source.fromFile(metricData)
    val itemNumpicking = try metricfile.mkString finally metricfile.clone
    val itemNum = itemNumpicking.unpickle[ItemNum]
    val testWriter = new PrintWriter(new File(targetData))

    for (line <- Source.fromFile(rawTargetData).getLines()) {
      val parts = line.split(",").map(_.trim)
      val processedline = for {
        part <- parts
      } yield itemNum.get(parts.indexOf(part), part)
      testWriter.write(processedline.mkString(",")+"\n")
    }
    testWriter.close
  }
}
