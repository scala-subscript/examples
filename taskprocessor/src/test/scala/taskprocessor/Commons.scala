package taskprocessor

import org.scalatest._
import akka.testkit._
import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps
import subscript.example.taskprocessor.Protocol._
import subscript.example.taskprocessor.ActorFamily

trait Commons {this: TestBase with TestKit =>
  val actorFamily: ActorFamily
  
  val fork: (Seq[Int], Int) => Seq[Seq[Int]] = (seq, n) => {
    val d = math.ceil(seq.size.toDouble / n).toInt
    val res = seq.sliding(d, d).toList
    if (res.size < n) res ++ (1 to n - res.size).map {_ => List[Int]()} else res
  }
  
  val join: Seq[Int] => Int = (seq) => seq.sum
  
  
  def randomData = for (i <- 1 to 10) yield util.Random.nextInt(10)
    
  
  def frontProcessor =
    system actorOf Props(actorFamily.frontProcessor[Seq[Int], Seq[Int], Int, Int], fork, join)
  
  def processor =
    system actorOf Props(actorFamily.processor[Seq[Int], Int], join)
  
  def proxy(target: ActorRef, maxRetries: Int, retryInterval: FiniteDuration, timeout: FiniteDuration) =
    system actorOf Props(actorFamily.proxy, target, maxRetries, retryInterval, timeout)
  
}