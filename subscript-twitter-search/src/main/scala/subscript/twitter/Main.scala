package subscript.twitter

import subscript.twitter.app.controller.{PureController, SubScriptController, SubScriptFuturesController}
import subscript.twitter.app.view.View

/**
 * Created by anatolii on 11/27/14.
 */
object Main {

  def main(args: Array[String]): Unit = {
    val view = new View()
    new SubScriptController(view).start()
    // new SubScriptController(view).start()
    //new PureController(view).start()
  }

}
