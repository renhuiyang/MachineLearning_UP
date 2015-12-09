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

  def put(input:String,filePath:String): Unit ={
    val cmd = s"curl -X PUT -L http://$hdfsMasterUrl/user/root/$input?op=CREATE&data=true&$userInfo --header Content-Type:application/octet-stream --header Transfer-Encoding:chunked -T $filePath"
    cmd.!
  }

  def get(filePath:String,result:String): Unit ={
    val cmd = s"curl -o $result -X GET http://$hdfsMasterUrl/user/root/$filePath?op=OPEN&$userInfo"
    cmd.!
  }
}
