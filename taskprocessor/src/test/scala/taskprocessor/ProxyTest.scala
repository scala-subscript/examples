package taskprocessor

import org.scalatest._
import akka.testkit._
import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps
import subscript.example.taskprocessor.Protocol._
import subscript.akka._
import subscript.example.taskprocessor.ssactors.SSActorFamily
import subscript.example.taskprocessor.plain.PAActorFamily
import subscript.example.taskprocessor.ActorFamily

class ProxyTest(val actorFamily: ActorFamily) extends TestKit(SSARunnerV1Scheduler.system) with TestBase with Commons {
  
  s"Proxy from $actorFamily" should "reroute task to its target multiple times, then report failure" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val p  = proxy(target = cref, maxRetries = 5, retryInterval = 100 milliseconds, timeout = 1 second)    
    val msg = Task(randomData, 1)
    p ! msg
    
    val spam = control.receiveWhile(800 milliseconds) {case m: Task[Seq[Int]] => m}
    spam.size should be >= (5)
    for(m <- spam) m shouldEqual msg
    
    control.expectMsg(Failure(1))
  }
  
  it should "stop spam once received confirmation" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val p = proxy(cref, 5, 100 milliseconds, 1 day)
    val msg = Task(randomData, 1)
    p ! msg
    
    control.expectMsg(msg)
    p ! ReceiptConfirmation(1)
    
    control.receiveWhile(3 seconds){case x => println(x)}
    
    control.expectNoMsg()
  }
  
  it should "fail if it doesn't receive final success in timeout" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val p = proxy(cref, 5, 100 milliseconds, 1 seconds)
    val msg = Task(randomData, 1)
    p ! msg
    
    control.expectMsg(msg)
    p ! ReceiptConfirmation(1)
    control.expectNoMsg(200 milliseconds)
    control.expectMsg(Failure(1))
  }
  
  it should "report success if received final success from the target" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val p = proxy(cref, 5, 100 milliseconds, 3 seconds)
    val msg = Task(randomData, 1)
    p ! msg
    
    control.expectMsg(msg)
    p ! ReceiptConfirmation(1)
    control.expectNoMsg(200 milliseconds)
    
    val success = Success(1, Some("success"))
    p ! success
    control.expectMsg(success)
  }
  
}

class SSProxyTest extends ProxyTest(SSActorFamily)

class PAProxyTest extends ProxyTest(PAActorFamily)
