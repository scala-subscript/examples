package subscript.example
import subscript.language

import scala.swing._
import scala.swing.event._

import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._

// Subscript sample application: a text entry field with a search button, that simulates the invocation of a background search
//

object LookupFrame2 extends LookupFrame2Application

class LookupFrame2Application extends SimpleSubscriptApplication {
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

    liveScript        = ... searchSequence || doExit

    searchCommand     = searchButton + Key.Enter
    cancelCommand     = cancelButton + Key.Escape 
    exitCommand       =   exitButton + windowClosing: top
    
    doExit            =   exitCommand @gui: {!confirmExit!} ~~(r:Boolean)~~> while (!r)
    cancelSearch      = cancelCommand showCanceledText
    
    searchSequence    = guard: searchTF, !searchTF.text.trim.isEmpty
                        searchCommand
                        showSearchingText searchInDatabase showSearchResults / cancelSearch
    
    showSearchingText = @gui: let outputTA.text = "Searching: "+searchTF.text
    showCanceledText  = @gui: let outputTA.text = "Searching Canceled"
    showSearchResults = @gui: let outputTA.text = "Results: 1, 2, 3"

    searchInDatabase  = {* sleep(5000) *} || progressMonitor
    
    progressMonitor   = ... @gui: {outputTA.text+=here.pass} do* sleep(200)
    
}
