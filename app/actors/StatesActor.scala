package actors

import akka.actor.Actor

/**
  * Created by yangrenhui on 15-12-9.
  */
class StatesActor extends Actor{
  object messages{
    def unapplySeq(msg:String):Option[Seq[String]]={
      val msgs = msg.trim.split(" ")
      if(msgs.forall(_.isEmpty)) None else Some(msgs)
    }
  }

  var states = scala.collection.mutable.Map[String,String]()

   def receive={
     case messages("Start",str,_*)=>states+=(str->"0")
     case messages("Query",str,_*)=>{
       sender!states(str)
       if(states(str) == "100")
         states -= str
     }
     case messages("Update",str,percentage)=>{
       println(s"Update $str $percentage")
       states(str)=percentage
     }
   }
}
