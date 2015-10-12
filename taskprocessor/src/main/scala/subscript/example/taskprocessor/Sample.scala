package subscript.example.taskprocessor

import akka.actor._
import akka.util.Timeout
import akka.util.Timeout._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.Future
import subscript.example.taskprocessor.ssactors._
import scala.concurrent.ExecutionContext.Implicits.global
import subscript.example.taskprocessor.Protocol.{Configuration, Task, Success}

object Sample {
  
  /**
   * This will compute and return sum of given numbers
   */
  def summate(nums: Seq[Int], as: ActorSystem = ActorSystem()): Future[Int] = {
    implicit val ourTimeout: Timeout = 3 seconds
    
    val fork: (Seq[Int], Int) => Seq[Seq[Int]] = (seq, n) => {
      val d = math.ceil(seq.size.toDouble / n).toInt
      val res = seq.sliding(d, d).toList
      if (res.size < n) res ++ (1 to n - res.size).map {_ => List[Int]()} else res
    }
    
    val join: Seq[Int] => Int = (seq) => seq.sum
    
    
    val fp = as actorOf Props(classOf[FrontProcessor[Seq[Int], Seq[Int], Int, Int]], fork, join)
    
    val processors = for (_ <- 1 to 4) yield as actorOf Props(classOf[Processor[Seq[Int], Int]], join)
    
    fp ! Configuration(processors)
    (fp ? Task(nums, 1)).map {case Success(_, Some(result: Int)) => result}
  }
  

  
}
