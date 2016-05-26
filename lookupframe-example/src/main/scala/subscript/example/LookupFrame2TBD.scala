package subscript.example

import subscript.language

import scala.swing._
import scala.swing.event._

import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._

// Subscript assignment: enrich the LookupFrame application to become LookupFrame2.
// LookupFrame is about a text entry field with a search button, that simulates the invocation of a background search.
//
// The following 5 steps should be applied to SubScript code of the controller:
//
// 1. The user may start the search by clicking the `searchButton` or pressing the Enter key.
// 2. It should be possible to cancel ongoing searches.
// 3. The user should be able to exit, using the exit button or the window closing event, and a confirmation dialog.
// 4. A search may only start when there the search text field contains some text (not empty or just spaces).
// 5. When a search is ongoing show progress by appending a sequence number in the output text field, every 250 ms.
//
// The code for all GUI widgets is already in place.

object LookupFrame2TBD extends LookupFrame2TBDApplication

class LookupFrame2TBDApplication extends SimpleSubscriptApplication {
  import scala.language.implicitConversions

  val outputTA     = new TextArea         {editable      = false}
  val searchButton = new Button("Go"    ) {enabled       = false; focusable = false}
  val cancelButton = new Button("Cancel") {enabled       = false; focusable = false}
  val   exitButton = new Button("Exit"  ) {enabled       = false; focusable = false}
  val searchLabel  = new Label("Search")  {preferredSize = new Dimension(45,26)}
  val searchTF     = new TextField        {preferredSize = new Dimension(100, 26)}
  
  val top          = new MainFrame {
    title          = "LookupFrame - Subscript"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(500,300)
    contents       = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton, cancelButton, exitButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
  }
  
  top.listenTo (searchTF.keys)
  val f = top.peer.getRootPane().getParent().asInstanceOf[javax.swing.JFrame]
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) // TBD: does not seem to work on MacOS
  
  def sleep(duration_ms: Long) = try {Thread.sleep(duration_ms)}
                                 catch {case e: InterruptedException => /*println("sleep interrupted")*/}
  def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
  
  override def  live = subscript.DSL._execute(liveScript)

  implicit script vkey(??k: Key.Value) = vkey2: top, ??k


  script..

    TBD = println: "This is TBD"

    liveScript        = ... searchSequence // TBD

    // 1. The user may start the search by clicking the `searchButton` or pressing the Enter key.
    //    In the body of `searchCommand`, add `Key.Enter` as an alternative.
    searchCommand     = searchButton

    // 2. It should be possible to cancel ongoing searches.
    //    Make a `cancelSearch` script, that starts with a `cancelCommand` and then shows text "Searching Canceled".
    //    The command is either clicking the `cancelButton` or pressing the Escape key
    //    Make sure the `cancelSearch` script is called when a search starts (i.e. after the search command)
    //    in a disruption composition with the search. E.g. `x/y` means `do x, possibly disrupted by y`.
    cancelCommand     = TBD
    cancelSearch      = TBD

    showSearchingText = @gui: let outputTA.text = "Searching: "+searchTF.text
    showSearchResults = @gui: let outputTA.text = "Results: 1, 2, 3"
    showCanceledText  = TBD

    searchSequence    = searchCommand
                        showSearchingText searchInDatabase showSearchResults

    // 3. A search may only start when there the search text field contains some text (not empty or just spaces).
    //    To ensure this, insert a call to the script `guard`, just before the call to `searchCommand`.
    //    As parameters `guard` expects the GUI control (`searchTF`), and the by-name condition to be evaluated
    //    each time an event happens in the GUI control: `!searchTF.text.trim.isEmpty`.

    // 4. The user should be able to exit, using the exit button or the window closing event, and a confirmation dialog.
    //    The exit command is clicking the exit button or the window closing event (`windowClosing: top`).
    //    After that command has been issued a confirmation dialog should be run, using a call to the method `confirmExit`.
    //    For this purpose make a script `doExit`. This is a sequential loop that runs while the confirmation
    //    dialog returns false. For this purpose, let the result data of `confirmExit` flow via an arrow `~~(b:Boolean)~~>`
    //    to the `while` iterator.
    //    To activate the `doExit` script, put a call to it in an or-parallel composition (`||`)
    //    at the top level script (`live`).
    exitCommand       = TBD
    doExit            = TBD

    // 5. When a search is ongoing show progress by appending a sequence number in the output text field, every 250 ms.
    //    Create a script `progressMonitor` which is an eternal loop that appends the loop counter (`here.pass`) as text
    //    to `outputTA.text` and then calls `sleep(200)`. The latter must be done in a new thread, inside `{*` and `*}`
    //    or after `do*`. Like with `exit`, call `progressMonitor` from an or-parallel composition.
    searchInDatabase  = {*sleep(5000)*} // TBD
    progressMonitor   = TBD
    
}
