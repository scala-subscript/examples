package subscript.example.taskprocessor

import akka.actor._

trait ActorFamily {

  def preStart(): Unit
  
  def frontProcessor[Di, Df, Ri, Rf]: Class[_ <: Actor]
  
  def processor[Df, Rf]             : Class[_ <: Actor]
  
  def proxy                         : Class[_ <: Actor]
  
}