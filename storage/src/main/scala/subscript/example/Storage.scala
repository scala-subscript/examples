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

  /** Contains data in a map */
  class Store extends SubScriptActor {
    // Each entry is composed of a `data` part and `details` part (see its definition below).
    private var data = Map[String, Entry]()

    // Accpets requests for 1) writing an entry, 2) reading the data of an entry and 3) reading the details of an entry
    script..
      live =
        var a: ActorRef = null
        ?a ~~(WriteReq  (k, v))~~> do! data += (k -> v)
          +~~(DataReq   (k   ))~~> do! a ! Data(data(k).data)
          +~~(DetailsReq(k   ))~~> do! a ! Details(data(k).details)
        ...
  }

  /** An interface to be used when interacting with the data store. */
  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~> {store ? req}   // Data request will first request the `data` part of the given entry, then its `details`, then compose them in a typle
               ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
               ~~(details: Details )~~> do! anActor ! (data, details)
      ...
  }

  /** Simulates a user interacting with the data store. */
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
