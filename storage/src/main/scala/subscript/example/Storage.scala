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

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future

 
object Storage {
  import Protocol._

  implicit val timeout = Timeout(5 seconds)
  implicit script f2s(f: Future[_]): Any =
    @{f.onComplete {case aTry => there.executeForTry(aTry); `script`.$ = aTry}}: {.  .}

  class Store extends SubScriptActor {
    private var data = Map[String, Entry]()

    script..
      live =
        var a: ActorRef = null
        ?a ~~(WriteReq  (k, v))~~> do! data += (k -> v)
          +~~(DataReq   (k   ))~~> do! a ! Data(data(k).data)
          +~~(DetailsReq(k   ))~~> do! a ! Details(data(k).details)
        ...
  }

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr
              +~~(req    : DataReq )~~> {store ? req} 
               ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
               ~~(details: Details )~~> do! anActor ! (data, details)
      ...
  }

  class Probe(proxy: ActorRef) extends SubScriptActor {
    script live =
      do! proxy ! WriteReq("k", Entry("data", "details"))
      sleep: 1000
      do! proxy ! DataReq("k")
      proxy ~~(respnse)~~> println: respnse
  }
 
 
  def main(args: Array[String]) {  
    val store = SSARunnerV1Scheduler.system actorOf Props[Store]
    val proxy = SSARunnerV1Scheduler.system actorOf Props(classOf[Proxy], store)
    val probe = SSARunnerV1Scheduler.system actorOf Props(classOf[Probe], proxy)
    SSARunnerV1Scheduler.execute(null)
  }
}

object Protocol {
  case class Entry(data: String, details: String)
  
  case class WriteReq(key: String, value: Entry)

  case class DataReq(key: String)
  case class Data(value: String)

  case class DetailsReq(key: String)
  case class Details(value: String)
}
