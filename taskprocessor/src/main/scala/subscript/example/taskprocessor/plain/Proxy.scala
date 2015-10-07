package subscript.example.taskprocessor.plain

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import subscript.example.taskprocessor.Protocol
import Protocol._

/**
 * The job of this actor is to provide improved guarantees on message
 * delivery.
 * It will make sure that there were at least maxRetries attempts to deliver message,
 * then it will wait for successful response for `timeout`. If Success is received,
 * it will be redirected to parent, otherwise Failure will be sent to parent.
 */
class Proxy(
    target       : ActorRef,
    maxRetries   : Int,
    retryInterval: FiniteDuration,
    timeout      : FiniteDuration
) extends Actor {
  val scheduler = context.system.scheduler
  
  var taskId       : Long             = -1
  var isBusy       : Boolean          = false
  var spammer      : Option[ActorRef] = None
  var taskRequester: ActorRef         = null
  
  def receive = {
    
    case t @ Task(data, id) =>
      reset(Some(id))  // Prepare for new processing
      spam(t)          // Spawn spammer
      
      // Estimate when kill/stop what
      val error            = Duration.Zero
      val maxRetryTime     = retryInterval * maxRetries + error
      val maxOperationTime = maxRetryTime + timeout + error
      
      // Kill spammer in some time
      spammer.foreach {s => scheduler.scheduleOnce(maxRetryTime, s, PoisonPill)}
      
      // Timeout operation in some time
      scheduler.scheduleOnce(maxOperationTime, self, Timeout(taskId))
    
    
    // When the target reports success, stop operation and send that success to the parent
    case s @ Success(id: Long, Some(data)) if id == taskId =>
      taskRequester ! s
      reset(None)
    
    // Confirms that the message is received by the target
    case s @ ReceiptConfirmation(id) if id == taskId && sender == target =>
      spammer.foreach {context stop _}
    
      
    // When timeout is received and it is relevant (ID of the task
    // that timed out must be equal to current id) and this proxy is busy
    // terminate all the processing with reset(None) and report failure to parent
    case Timeout(id) if id == taskId && isBusy =>
      taskRequester ! Failure(id)
      reset(None)
      
  }
  
  
  /**
   * In its first half, this method resets the state of the actor to
   * its primeval condition - stop all processing.
   * If the argument passed is not None, we start new processing (at least
   * reflect that it started in the internal state).
   */
  def reset(tid: Option[Long]) {
    // Stop all processing
    taskId = -1
    isBusy = false
    spammer.foreach {context stop _}
    spammer = None
    taskRequester = null
    
    // Start new processing (at least document it)
    tid.foreach {id =>
      taskId = id
      isBusy = true
      taskRequester = sender
    }
  }
  
  /**
   * This method spawns a spammer.
   */
  def spam(msg: Any) {
    val props = Props(classOf[Spammer], target, msg, retryInterval)
    spammer = Some(context actorOf props)
  }
  
}

/**
 * The main job of this actor is to fire given message into given actor with given
 * interval all its life until killed.
 */
class Spammer(target: ActorRef, msg: Any, interval: FiniteDuration) extends Actor {
  context.system.scheduler.schedule(Duration.Zero, interval, self, "fire")
  def receive = {case "fire" => target.!(msg)(context.parent)}
}