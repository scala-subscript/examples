package subscript.example
import subscript.language

import subscript.DSL._

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments

object Hello {
  // bridge method:
  def main( args: Array[String]): Unit = _execute(live)
  
  script live = {!println("Hello")!}
 
}
