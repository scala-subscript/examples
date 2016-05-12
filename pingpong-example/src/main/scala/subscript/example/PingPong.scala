package subscript.example
import subscript.language
import subscript.Predef._

import scala.language.postfixOps
 
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._

import subscript.akka._
import subscript.DSL._

 
object PingPong {
 
  class Pong extends SubScriptActor {

    script..
      live = receivePing ...
      receivePing = // << msg @ "ping" => println(msg); sender ! "pong" >>
        var a: ActorRef = null
        ?a ~~(msg @ "ping")~~> [println: msg do a ! "pong"]
  }
 
  class Ping(target: ActorRef) extends SubScriptActor {
 
    script..
      live = times: 3 sendPing receivePong
      sendPing = do target ! "ping" // {!target ! "ping"!}
      receivePong = target ~~(msg @ "pong")~~> println: msg // << msg @ "pong" => println(msg)>>

  }
 
 
  def main(args: Array[String]) {  
    val pong = SSARunnerV1Scheduler.system actorOf Props[Pong]
    val ping = SSARunnerV1Scheduler.system actorOf Props(classOf[Ping], pong)
    SSARunnerV1Scheduler.execute(null)
  }
}
