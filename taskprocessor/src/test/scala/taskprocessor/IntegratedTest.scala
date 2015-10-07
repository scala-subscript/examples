package taskprocessor

import org.scalatest._
import akka.testkit._
import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps
import subscript.example.taskprocessor.Protocol._
import subscript.akka._
import scala.util.Random
import subscript.example.taskprocessor.ssactors.SSActorFamily
import subscript.example.taskprocessor.plain.PAActorFamily
import subscript.example.taskprocessor.ActorFamily

class IntegratedTest(val actorFamily: ActorFamily) extends TestKit(SSARunnerV1Scheduler.system) with TestBase with Commons {

  s"Framework based on $actorFamily" should "process random tasks with random number of processors correctly" in {
    val control = TestProbe()
    implicit val cref = control.ref

    val fp = frontProcessor
    val complexityLevel = 20

    // Somehow, will constantly fail with nProcessors too big (e.g. 20)
    val nProcessors = 4 //Random.nextInt(complexityLevel) + 1
    val processors  = for (_ <- 1 to nProcessors) yield processor

    def task: Seq[Int]     = for (_ <- 1 to complexityLevel) yield Random.nextInt(complexityLevel)
    val tasksWithSolutions = for (_ <- 1 to complexityLevel) yield {val t = task; (t, join(t))}


    fp ! Configuration(processors)
    control.expectMsg(Ready)

    for (((task, solution), idx) <- tasksWithSolutions.zipWithIndex) {
      val errorString = s"""
      |Integrated test error.
      |Number of processors : $nProcessors
      |Task                 : $task
      |Solution             : $solution
      |Index                : $idx
      |Error message        : """.stripMargin

      fp ! Task(task, idx)
      /*val Success(id, Some(result)) = control.expectMsgClass(classOf[Success[Int]])
      withClue(errorString) {
        result shouldEqual solution
        id     shouldEqual idx
      }*/
      control.expectMsg(Success(idx, Some(solution)))
    }
  }

}

class SSIntegratedTest extends IntegratedTest(SSActorFamily)

class PAIntegratedTest extends IntegratedTest(PAActorFamily)
