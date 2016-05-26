package subscript.twitter.app.controller

import subscript.language
import subscript.DSL._
import subscript.swing.Scripts._
import subscript.twitter.api._
import subscript.twitter.app.view.View
import subscript.twitter.util.InterruptableFuture

/**
 * Training assignment: replace two threaded code fragments by interruptable futures.
 * See comments further below.
 *
 * This class is originally largely the same as SubScriptController
 * By several modifications the class should become equal to SubScriptFuturesController
 */
class SubScriptControllerTBD_Futures(val view: View) extends Controller {

  def start() = {
      val executor = new subscript.vm.executor.CommonScriptExecutor[Any]
      val debugger = new subscript.vm.SimpleScriptDebuggerClass
      executor.traceLevel = 2
      debugger.traceLevel = 4
      _execute(liveScript)
  }

  def sleep(t: Long) = Thread sleep t

  // Use the following two InterruptableFutures
  // and the implicit conversion script
  //
  // val fWait   = InterruptableFuture {Thread sleep keyTypeDelay}
  // val fSearch = InterruptableFuture {twitter.search(view.searchField.text, tweetsCount)}
  //
  //  implicit script f2s(intf: InterruptableFuture[_]): Any =
  //    @{var isDeactivated = false
  //      there.onDeactivate{isDeactivated = true}
  //      intf.execute().onComplete {
  //          case aTry => if (!isDeactivated) {
  //                         there.executeForTry(aTry);
  //                         `script`.$ = aTry
  //                       }
  //          }
  //      }: {.  .}

  script..

    liveScript = initialize ; [mainSequence / ..?]...

    initialize = view.main: Array()

    // use `fWait` and `fSearch` through implicit calls to script `f2s`
    // instead of `waitForDelay` and `searchTweets`:
    //
    mainSequence = anyEvent: view.searchField
                   waitForDelay
                   searchTweets ~~(ts:Seq[Tweet])~~> updateTweetsView: ts
                              +~/~(t: Throwable )~~> setErrorMsg     : t

    waitForDelay = {* view.setStatus("waiting"  ); sleep(keyTypeDelay) *}
    searchTweets = {* view.setStatus("searching"); twitter.search(view.searchField.text, tweetsCount)*}

    updateTweetsView(ts: Seq[Tweet]) = @gui: view.setTweets: ts
    setErrorMsg     (t : Throwable ) = @gui: view.setError : t.toString
}
