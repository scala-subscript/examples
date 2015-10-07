package subscript.example.taskprocessor

import akka.actor._
import scala.concurrent.duration._

object Protocol {
  type Proxy = ActorRef
  type Processor = ActorRef
  
  case class Configuration(processors: Seq[Processor])
  case class Task[D]      (data: D, id: Long         )
  
  case class Success[R]         (id: Long, data: Option[R] = None)
  case class Failure            (id: Long                        )
  case class ReceiptConfirmation(id: Long                        )
  
  case class Timeout   (id: Long)
  
  case object Ready
  
  
  // Some configuration
  var maxRetries    = 3
  var retryInterval = 100 milliseconds
  var timeout       = 1 minute
}