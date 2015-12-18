package services
import models.{User,Users}
import scala.concurrent.Future
/**
  * Created by yangrenhui on 15-12-16.
  */
object UserService {
  def addUser(user:User):Future[String]={
    Users.add(user)
  }

  def deleteUser(id:Long):Future[Int]={
    Users.delete(id)
  }

  def getUser(id:Long):Future[Option[User]]={
    Users.get(id)
  }

  def listAllUsers:Future[Seq[User]]={
    Users.listAll
  }
}
