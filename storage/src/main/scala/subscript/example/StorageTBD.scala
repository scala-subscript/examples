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

// val r = new scala.util.Random
// r.nextInt(1000)

/*
 * Trainging assignment for enriching a simple SubScript Actor application.
 *
 * The starting situation is a two-actor system:
 * - a Probe
 * - a Proxy
 *
 * The probe sends either a WriteRequest or a DataRequest to the Proxy.
 * The proxy receives the WriteRequest but it does not take further action;
 * the DataRequest results in a reply message from the Proxy to the Probe
 *
 * The task is to include a Store actor in the system.
 * Class Proxy should be extended so that it communicates with the Store,
 * that way Proxy would really act as a proxy.
 * This goes in 6 assigment steps.
 *
 * The class for Store is already given; like the Probe class it does not require modifications.
 *
 * The steps to be implemented in class Proxy are:
 *
 * 1. Pass a received WriteReq on to the store.
 *    The Scala code for sending a write request `wr` to `store` is `store ! wr`
 *
 * 2. Forward the request to the store, await the answer (which is of type Data),
 *    and send that back to the original requester.
 *    Use here the syntax `store ? req`. This returns a future that will later receive the answer back from the store.
 *    That answer should be replied back.
 *    This is possible using an implicit conversion of the future into a script, and the dataflow arrow.
 *    `{store ? req} ~~(d:Data)~~>` (add the appropriate code at the right of the arrow)
 *
 * 3. Now instead of sending the Data back to the original requester, create a DetailsRequest,
 *    using the constructor call DetailsReq(req.key), and send that to the `store`,
 *    again using the `?` method that results in a Future. This future will result in
 *    an answer of type `Details`; when that happens send that back to the original requester,
 *    in a tuple together with the `Data`.
 *
 * 4. Suppose that receiving both answers back from the `store` might take too long, say more than 1 second.
 *    So add a TimeOut mechanism, using the parallel-or operator `||`.
 *    A 1-second sleep should run in a background thread, or-parallel to the
 *    sequence of both forwardings to the store and the sending back to the original actor.
 *    After 1 second of sleeping the slow message forwarding will be canceled.
 *
 * 5. When a timeout occurs a message "timeout" should be sent back to the original requester.
 *    This may be done just after the call to `sleep`.
 *    There is now a race condition between that message and the other one for the original requester.
 *
 *    Note:
 *    This could in principle be resolved by using the disrupt operator `/` rather than `||`.
 *    But then the RHS of `/` should only perform an action after the second has passed.
 *    This is well possible to do; for an idea lookup the 'triggers' in the eye-test example.
 *
 * 6. The sequence of communications with the `store` and with the original requester,
 *    does not leave receiving other messages in between, since everything is done in a sequential loop.
 *    Depending on how SubScriptActors are implemented, this may result in newle arrived messages to be thrown away.
 *    It may therefore be useful to do the communications sequence in parallel with handling newly arrived messages.
 *    That may be done in 2 ways:
 *
 *    a. By putting code between `[*` and `*]`. This "launches" a process,
 *       well comparable with a trailing ampersand (`&`) in a Unix shell.
 *
 *    b. By doing the communication sequence in a parallel loop, rather than a sequential one.
 *       So instead of `...` write `& ..?`. The two dots denote some sparsity: a new parallel iteration
 *       only starts as soon as for the first time in the previous iteration an action has happened.
 *       That action is the message reception of the original requester.
 *       Note: this `&..` functionality of SubScript has not well been tested yet at the time of writing
 *
 * Note: the current SubScript preprocessor version does not support the syntax `?a:A`.
 * Therefore you will see the pattern `var a:A = null; ?a`
 *
 * Spoilers are at the bottom.
 */

object StorageTBD {
  import ProtocolTBD._

  implicit val timeout = Timeout(5 seconds)
  implicit script f2s(f: Future[_]): Any =
    @{f.onComplete {case aTry => there.executeForTry(aTry); `script`.$ = aTry}}: {.  .}

  /** An interface to be used when interacting with the data store. */
  class Proxy(store: ActorRef) extends SubScriptActor {

    script live =

      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> []  // TBD: forward operation to the store
              +~~(req    : DataReq )~~> do! anActor ! "Request received" // TBD: replace by forwarding and getting the anser back
      ...
  }


  /** Contains data in a map */
  class Store extends SubScriptActor {
    // Each entry is composed of a `data` part and `details` part (see its definition below).
    private var data = Map[String, Entry]()

    // Accepts requests for 1) writing an entry, 2) reading the data of an entry and 3) reading the details of an entry
    script..
      live =
        var a: ActorRef = null
        ?a ~~(WriteReq  (k, v))~~> do! data += (k -> v)
          +~~(DataReq   (k   ))~~> do! a ! Data(data(k).data)
          +~~(DetailsReq(k   ))~~> do! a ! Details(data(k).details)
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

object ProtocolTBD {
  case class Entry(data: String, details: String)
  
  case class WriteReq(key: String, value: Entry)

  case class DataReq(key: String)
  case class Data(value: String)

  case class DetailsReq(key: String)
  case class Details(value: String)
}












/* SPOILER ALERT: solutions are given here:

  0. Begin situation:

  class Proxy(store: ActorRef) extends SubScriptActor {

    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~> do! anActor ! "Request received"
      ...
  }

  1. Write Request

  class Proxy(store: ActorRef) extends SubScriptActor {

    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~> do! anActor ! "Request received"
      ...
  }

  2. Data Request

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~> {store ? req}   // Data request will first request the `data` part of the given entry, then its `details`, then compose them in a typle
               ~~(data   : Data    )~~> do! anActor ! data
      ...
  }

  3. Details Request

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~> {store ? req}
               ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
               ~~(details: Details )~~> do! anActor ! (data, details)
      ...
  }

  4. Timeout

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~>

                    [ {store ? req}
                       ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
                       ~~(details: Details )~~> do! anActor ! (data, details)
                    || sleep: 1000
                    ]
      ...
  }

  5. Timeout + message to original requester

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~>

                    [ {store ? req}
                       ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
                       ~~(details: Details )~~> do! anActor ! (data, details)
                    || sleep: 1000 let anActor ! "timeout"
                    ]
      ...
  }

  6a. Process launching

  class Proxy(store: ActorRef) extends SubScriptActor {
    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> [* do! store ! wr *]
              +~~(req    : DataReq )~~>

                    [* {store ? req}
                       ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
                       ~~(details: Details )~~> do! anActor ! (data, details)
                    || sleep: 1000 let anActor ! "timeout"
                    *]
      ...


  6b. Parallel loop with `..?`

  class Proxy(store: ActorRef) extends SubScriptActor {

    script live =
      var anActor: ActorRef = null
      ?anActor ~~(wr     : WriteReq)~~> do! store ! wr  // Write operation is redirected to the store as is
              +~~(req    : DataReq )~~>

                    [ {store ? req}
                       ~~(data   : Data    )~~> {store ? DetailsReq(req.key)}
                       ~~(details: Details )~~> do! anActor ! (data, details)
                    || sleep: 1000 let anActor ! "timeout"
                    ]
      & ..?



 */