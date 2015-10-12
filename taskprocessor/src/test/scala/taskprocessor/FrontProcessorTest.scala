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

class FrontProcessorTest(val actorFamily: ActorFamily) extends TestKit(SSARunnerV1Scheduler.system) with TestBase with Commons {
  
  s"FrontProcessor from $actorFamily" should "report readiness when it processes configuration" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val fp = frontProcessor
    val (p1, p2) = (TestProbe(), TestProbe())
    fp ! Configuration(Seq(p1.ref, p2.ref))
    
    control.expectMsg(Ready)
  }
  
  it should "fork tasks and propagate parts to processors" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val fp = frontProcessor
    val (p1, p2) = (TestProbe(), TestProbe())
    fp ! Configuration(Seq(p1.ref, p2.ref))
    control.expectMsg(Ready)

    val data = randomData
    val forked = fork(data, 2)
    
    fp ! Task(data, 1)
    val t1 = p1.expectMsgAnyOf(Task(forked(0), 1), Task(forked(1), 1))
    val t2 = p2.expectMsgAnyOf(Task(forked(0), 1), Task(forked(1), 1))
    
    t1 should not be t2
  }
  
  it should "fail if at least one of its processes stays silent for more then 3 seconds after receiving the task" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val fp = frontProcessor
    val (p1, p2) = (TestProbe(), TestProbe())
    fp ! Configuration(Seq(p1.ref, p2.ref))
    control.expectMsg(Ready)
    
    fp ! Task(randomData, 1)
    
    p1 expectMsgClass classOf[Task[Seq[Int]]]
    p1 reply ReceiptConfirmation(1)
    
    p2 expectMsgClass classOf[Task[Seq[Int]]]
    p2 reply ReceiptConfirmation(1)
    
    control.expectNoMsg(2 seconds)
    control.expectMsg(Failure(1))
  }
  
  it should "report success if all its processors reported success" in {
    val control = TestProbe()
    implicit val cref = control.ref
    
    val fp = frontProcessor
    val (p1, p2) = (TestProbe(), TestProbe())
    fp ! Configuration(Seq(p1.ref, p2.ref))
    control.expectMsg(Ready)
    
    val data: Seq[Int]        = randomData
    val forked: Seq[Seq[Int]] = fork(data, 2)
    val joined: Int           = join(data)
    
    fp ! Task(data, 1)
    
    p1 expectMsgClass classOf[Task[Seq[Int]]]
    p1 reply ReceiptConfirmation(1)
    p1 reply Success(1, Some(join(forked(0))))
    
    p2 expectMsgClass classOf[Task[Seq[Int]]]
    p2 reply ReceiptConfirmation(1)
    p2 reply Success(1, Some(join(forked(1))))
    
    control.expectMsg(Success(1, Some(joined)))
  }

}

class SSFrontProcessorTest extends FrontProcessorTest(SSActorFamily)

class PAFrontProcessorTest extends FrontProcessorTest(PAActorFamily)
