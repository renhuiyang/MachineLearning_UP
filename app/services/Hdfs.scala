package services

import java.io._

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import play.api.libs.Files

import sys.process._

import scala.util.{Failure, Success}

/**
  * Created by yangrenhui on 15-12-4.
  */
object Hdfs{
  val sysconf = ConfigFactory.load()
  System.setProperty("HADOOP_USER_NAME",sysconf.getString("hadoop.user.name"))
  val conf = new Configuration()
  conf.set("fs.defaultFS","hdfs://"+sysconf.getString("hadoop.master.uri"+":9000"))
  val fileSystem = FileSystem.get(conf)
/*  val hdfsMasterUrl=conf.getString("hadoop.master.uri")+":50070/webhdfs/v1"
  val userInfo="user.name="+conf.getString("hadoop.user.name")

  def put(input:String,filePath:String): Unit ={
    val cmd = s"curl -X PUT -L http://$hdfsMasterUrl/user/root/$input?op=CREATE&data=true&$userInfo --header Content-Type:application/octet-stream --header Transfer-Encoding:chunked -T $filePath"
    cmd.!
  }

  def get(filePath:String,result:String): Unit ={
    val cmd = s"curl -o $result -X GET http://$hdfsMasterUrl/user/root/$filePath?op=OPEN&$userInfo"
    cmd.!
  }*/

  def put(filePath: String, input: String): Unit = {
    import java.io.File
    val src = new File(input)
    val reader = new BufferedInputStream(new FileInputStream(src))
    val path = new Path(filePath)
    val os = fileSystem.create(path)

    val b = new Array[Byte](1024)
    var numBytes = reader.read(b)
    while (numBytes > 0) {
      os.write(b, 0, numBytes)
      numBytes = reader.read(b)
    }
    os.close()
    reader.close()
  }

  def del(filePath:String):Boolean={
    val path = new Path(filePath)
    fileSystem.deleteOnExit(path)
  }

  private def read(filePath:String):InputStream={
    val path = new Path(filePath)
    fileSystem.open(path)
  }

  def get(filePath:String,output:String): Unit ={
    val target = new File(output)
    val targetWriter = new BufferedOutputStream(new FileOutputStream(target))
    val reader = new BufferedInputStream(read(filePath))

    val b = new Array[Byte](1024)
    var numBytes = reader.read(b)
    while(numBytes > 0){
      targetWriter.write(b,0,numBytes)
      numBytes = reader.read(b)
    }

    targetWriter.close()
    reader.close()
  }

  def mkdir(folderPath: String): Unit = {
    val path = new Path(folderPath)
    if (!fileSystem.exists(path)) {
      fileSystem.mkdirs(path)
    }
  }

  def list(folderPath:String): Array[String] ={
    val path = new Path(folderPath)
    fileSystem.listStatus(path).map{status=>status.getPath.toUri.toString}
  }
}
