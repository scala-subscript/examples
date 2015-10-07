package subscript.example.taskprocessor.blur

import akka.actor._
import akka.util.Timeout
import akka.util.Timeout._
import akka.pattern.ask

import scala.util.Random
import scala.concurrent.duration._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import subscript.akka._

import subscript.example.taskprocessor.Protocol._
import subscript.example.taskprocessor.ssactors.SSActorFamily
import subscript.example.taskprocessor.plain.PAActorFamily
import subscript.example.taskprocessor.ActorFamily

object Blur {
  def main(args: Array[String]) {
    // Selecting ActorFamily
    val familyName = args(2)
    val actorFamily: ActorFamily =
      if (familyName.toLowerCase().startsWith("s")) SSActorFamily else PAActorFamily
    import actorFamily._

    // Initialization
    preStart()
    val as = SSARunnerV1Scheduler.system

    // Set some variables
    val numProcessors: Int = args(0).toInt
    val dimension    : Int = args(1).toInt
    implicit val ourTimeout: Timeout = 1 minute
    
    // Spawn actors
    val fp = as actorOf Props(frontProcessor[Seq[Int], Seq[(Seq[Int], Int)], Seq[Int], Seq[(Seq[Int], Int)]], fork, join)
    val processors = for (_ <- 1 to numProcessors) yield as actorOf Props(processor[(Seq[Int], Int), (Seq[Int], Int)], process)
        
    // Configure FrontProcessor
    Await.ready(fp ? Configuration(processors), ourTimeout.duration)
    
    // Generate random image
    val img = rndImage(dimension * dimension)
    
    // Do computation and measurements
    val ts = System.currentTimeMillis
    val Success(_, Some(data: Seq[Int])) = Await.result(fp ? Task(img, 1), ourTimeout.duration)
    val total = System.currentTimeMillis() - ts
    
    // Report observation
    val report = s"""
      |Integers processed    : ${data.size}
      |Operation duration, ms: $total
      |Processors            : $numProcessors
      |Actor family          : $actorFamily""".stripMargin
    
    println(report)
  }
  
  // TaskProcessor part
  val fork: (Seq[Int], Int) => Seq[(Seq[Int], Int)] = {(img, n) =>
    val step = (img.size - 1) / n + 1
    img.sliding(step, step).toList.zipWithIndex
  }
  
  val join: Seq[(Seq[Int], Int)] => Seq[Int] = imgs =>
    imgs.sortBy {case (_, index) => index}.map {case (l, _) => l}.flatten
    
  val process: ((Seq[Int], Int)) => (Seq[Int], Int) =
    {case (data, index) => (blur(data, 100), index)}
  
    
  // Technical part
  def blur(image: Seq[Int], width: Int): Seq[Int] =
    for (i <- 0 until image.length) yield {
      var r = 0
      var g = 0
      var b = 0

      val start = math.max(0               , i - width)
      val end   = math.min(image.length - 1, i + width)
      val realWidth = end - start

      for (j <- start to end) {
        val px = image(j)
        r += red  (px) / realWidth
        g += green(px) / realWidth
        b += blue (px) / realWidth
      }
      pixel(r, g, b)
    }

  def byte(n: Int, pos: Int) = {
    val d = pos * 8
    (n & (0xff << d)) >> d
  }

  def red  (px: Int) = byte(px, 2)
  def green(px: Int) = byte(px, 1)
  def blue (px: Int) = byte(px, 0)

  def pixel(r: Int, g: Int, b: Int) =
    0xff000000 |
    r << 16    |
    g << 8     |
    b

  def rndPixel = pixel(Random.nextInt(0xff), Random.nextInt(0xff), Random.nextInt(0xff))

  def rndImage(n: Int) = for (_ <- 1 to n) yield rndPixel
  
}
