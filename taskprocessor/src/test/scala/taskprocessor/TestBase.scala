package taskprocessor

import org.scalatest._
import akka.testkit._
import subscript.akka._
import subscript.example.taskprocessor.Protocol._
import scala.concurrent.duration._

trait TestBase extends
    FlatSpecLike      with
    Matchers          with
    BeforeAndAfterAll with
    Commons
{this: TestKit =>
   
  override def beforeAll {
    actorFamily.preStart()
    timeout = 3 seconds
  }
  
  override def afterAll {/*TestKit.shutdownActorSystem(system)*/}
  
}
  