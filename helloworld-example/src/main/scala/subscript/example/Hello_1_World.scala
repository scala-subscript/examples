package subscript.example
import subscript.language

import subscript.DSL._

// Subscript sample application: Hello+(+); World
//
object Hello_1_World {
  // def main(args: Array[String]) {_execute(live)}
   
  script live = {!println("Hello,")!} + [+]; 
                {!println("world!")!}
}

