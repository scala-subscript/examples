package subscript.twitter.app.controller
import subscript.language

import subscript.DSL._
import subscript.swing.Scripts._

import subscript.twitter.api._
import subscript.twitter.app.view.View

/**
 * Created by anatolii on 11/28/14.
 */
class SubScriptController(val view: View) extends Controller {

  def start() = {
      val executor = new subscript.vm.executor.CommonScriptExecutor[Any]
      val debugger = new subscript.vm.SimpleScriptDebuggerClass
      executor.traceLevel = 2
      debugger.traceLevel = 4
      _execute(liveScript)
  }

  def sleep(t: Long) = Thread sleep t

  script..

    liveScript = initialize ; [mainSequence / ..?]...

    initialize = view.main: Array()

    mainSequence = anyEvent: view.searchField
                   waitForDelay
                   searchTweets ~~(ts:Seq[Tweet])~~> updateTweetsView: ts
                              +~/~(t: Throwable )~~> setErrorMsg     : t

    waitForDelay = {* view.setStatus("waiting"  ); sleep(keyTypeDelay) *}
    searchTweets = {* view.setStatus("searching"); twitter.search(view.searchField.text, tweetsCount)*}

    updateTweetsView(ts: Seq[Tweet]) = @gui: view.setTweets: ts
    setErrorMsg     (t : Throwable ) = @gui: view.setError : t.toString
}
