package subscript.example.taskprocessor.plain

import subscript.akka._
import scala.concurrent.duration._
import akka.actor._
import subscript.example.taskprocessor.ActorFamily

object PAActorFamily extends ActorFamily {
  
  def preStart() {}
  
  def frontProcessor[Di, Df, Ri, Rf] = classOf[FrontProcessor[Di, Df, Ri, Rf]]
  
  def processor[Df, Rf]              = classOf[Processor[Df, Rf]             ]
  
  def proxy                          = classOf[Proxy                         ]
  
  override def toString() = "Plain Actors"
  
}