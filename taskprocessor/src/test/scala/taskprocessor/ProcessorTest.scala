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

class ProcessorTest(val actorFamily: ActorFamily) extends TestKit(SSARunnerV1Scheduler.system) with TestBase with Commons {
  
  s"Processor from $actorFamily" should "confirm that it has received task " in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    processor ! Task(randomData, 1)
    control.expectMsg(ReceiptConfirmation(1))
  }
  
  it should "return task result to requester" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val data = randomData
    val result = join(data)
    
    processor ! Task(data, 1)
    
    control.receiveOne(3 seconds)
    control.expectMsg(Success(1, Some(result)))
  }
  
}

class SSProcessorTest extends ProcessorTest(SSActorFamily)

class PAProcessorTest extends ProcessorTest(PAActorFamily)
