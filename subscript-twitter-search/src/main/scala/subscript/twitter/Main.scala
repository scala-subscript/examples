package subscript.twitter

import subscript.twitter.app.controller.{PureController, SubScriptController, SubScriptFuturesController, SubScriptControllerTBD_Futures}
import subscript.twitter.app.view.View

/**
 * Created by anatolii on 11/27/14.
 */
object Main {

  def main(args: Array[String]): Unit = {
    val view = new View()
    //new PureController(view).start()
    new SubScriptController(view).start()
    //new SubScriptFuturesController(view).start()
    //new SubScriptControllerTBD_Futures(view).start()
  }

}
