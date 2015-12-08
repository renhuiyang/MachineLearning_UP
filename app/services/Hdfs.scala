package services

/*import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem,Path}
import scala.io.Source*/

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigFactory
import play.api.Play.current

import play.api.mvc._
import play.api.libs.ws._
import sys.process._

import scala.util.{Failure, Success}

/**
  * Created by yangrenhui on 15-12-4.
  */
object Hdfs{
  val ws = WS.client
  val conf = ConfigFactory.load()
  //curl -i -X PUT"http://hadoop-master:50070/webhdfs/v1/tmp/webhdfs/webhdfs-test.txt?user.name=app&op=CREATE"
  val hdfsMasterUrl=conf.getString("hadoop.master.uri")+":50070/webhdfs/v1"
  val userInfo="user.name="+conf.getString("hadoop.user.name")

/*  def putStep1(input:String):Future[WSResponse]={
    //setp1:curl -i -X PUT "http://184.73.79.160:50070/webhdfs/v1/user/root/test1.txt?user.name=root&op=CREATE"
    val step1url = hdfsMasterUrl+"user/root/"+input+"?"+userInfo+"&op=CREATE"
    ws.url(step1url).put("")
  }

  def putStep2(input:String,url:String):Future[WSResponse]={
    //setp2:curl -i -X PUT -T webhdfs-test.txt"http://54.167.174.187:50075/webhdfs/v1/tmp/webhdfs/webhdfs-test.txt?op=CREATE&user.name=app&namenoderpcaddress=hadoop-master:9000&overwrite=false"
    ws.url(url).put("111111")
  }*/

  def put(input:String,filePath:String): Unit ={
    val cmd = s"curl -X PUT -L http://$hdfsMasterUrl/user/root/$input?op=CREATE&data=true&$userInfo --header Content-Type:application/octet-stream --header Transfer-Encoding:chunked -T $filePath"
    cmd.!
  }

  def get(filePath:String,result:String): Unit ={
    val cmd = s"curl -o $result -X GET http://$hdfsMasterUrl/user/root/$filePath?op=OPEN&$userInfo"
    cmd.!
  }
}
