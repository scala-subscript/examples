package subscript.example

import subscript.language
import subscript.Predef._

import java.awt.datatransfer.{DataFlavor, StringSelection}
import java.awt.{Toolkit, Dimension, Point, Color}
import java.io.{FileOutputStream, BufferedOutputStream, BufferedInputStream, File}
import java.net.URL
import javax.tools.Tool

import scala.swing._
import scala.swing.event._
import subscript._
import subscript.DSL._
import subscript.swing._
import subscript.swing.Scripts._

object FileDownloader extends SimpleSubscriptApplication {

  // Helper function for grouping elements in a grid with one row
  def group(elts: Component*) = new GridPanel(1, elts.length) {contents ++= elts}

  // A group with a text field and a label where user can write a URL of the file
  // he wants to get
  val sourceUrlField = new TextField
  val sourceUrlLabel = new Label("Source URL:")
  val sourceGroup    = group(sourceUrlLabel, sourceUrlField, new Label)

  // A group with a label and a button that allow the user to select
  // a location on his/her computer where the file will be downloaded
  val destinationFileField   = new TextField
  val destinationFileLabel   = new Label("Save to:")
  val selectFileBtn          = new Button("Select Save Location") {enabled = false}
  val fileChooser            = new FileChooser
  val destinationGroup       = group(destinationFileLabel, destinationFileField, selectFileBtn)

  // A control panel of the program: the buttons to start and cancel the download
  val startBtn     = new Button("Start ") {enabled = false}
  val cancelBtn    = new Button("Cancel") {enabled = false}
  val controlGroup = group(startBtn, cancelBtn)

  // A progress bar will display the progress of the download
  val progressBar          = new ProgressBar {
    background = Color.WHITE;
    foreground = Color.ORANGE;
  }

  // A popup menu
  val copyBtn       = new MenuItem("Copy"      ) {enabled = false}
  val pasteBtn      = new MenuItem("Paste"     ) {enabled = false}
  val pasteAndGoBtn = new MenuItem("Paste & Go") {enabled = false}
  val popup         = new PopupMenu {
    contents ++= Seq(copyBtn, pasteBtn, pasteAndGoBtn)
  }

  // Make the fields popup-capable
  var popupInvoker: Option[TextField] = None
  def enablePopup(c: TextField, pm: PopupMenu): Unit = {
    def showPopup(p: Point): Unit = {
      popupInvoker = Some(c)
      pm.show(c, p.x, p.y)
    }

    c.reactions += {
      case MousePressed (_, p: Point, _, _, triggersPopup: Boolean) if triggersPopup => showPopup(p)
      case MouseReleased(_, p: Point, _, _, triggersPopup: Boolean) if triggersPopup => showPopup(p)
    }

    c.listenTo(c.mouse.clicks)
  }

  enablePopup(sourceUrlField      , popup)
  enablePopup(destinationFileField, popup)

  val top = new MainFrame {
    title = "SubScript File downloader"
    location = new Point(150, 150)
    preferredSize = new Dimension(500, 150)

    contents = new GridPanel(4, 1) {
      contents ++= Seq(sourceGroup, destinationGroup, controlGroup, progressBar)
    }
  }


  override def live = subscript.DSL._execute(liveScript)

  override script liveScript = workflow ...

  script..
    workflow = 
      [
        startCommand      downloads
      + selectFileCommand selectFile
      + copyCommand       copy
      + pasteCommand      paste
      + pasteAndGoCommand  paste downloads
      ] + guiUpdated

    // Listens for any events on the labels
    guiUpdated = anyEvent(sourceUrlField) || anyEvent(destinationFileField)

    // Buttons commands
    startCommand      = if !sourceUrlField.text.isEmpty && !destinationFileField.text.isEmpty then startBtn else {..}
    cancelCommand     = cancelBtn
    selectFileCommand = selectFileBtn

    // Popup commands
    copyCommand       = if popupInvoker.map(_.selected != null).getOrElse(false)                  then copyBtn       else {..}
    pasteCommand      = if Toolkit.getDefaultToolkit.getSystemClipboard.getContents(null) != null then pasteBtn      else {..}
    pasteAndGoCommand = if Toolkit.getDefaultToolkit.getSystemClipboard.getContents(null) != null then pasteAndGoBtn else {..}

    // Copy-paste actions
    copy  = val selection = new StringSelection(popupInvoker.get.selected)
            Toolkit.getDefaultToolkit.getSystemClipboard.setContents: selection, selection

    paste = val str   = Toolkit.getDefaultToolkit.getSystemClipboard.getData(DataFlavor.stringFlavor).asInstanceOf[String]
            val field = (popupInvoker.get)
            if  field.selected != null then let field.text = field.text.replace(field.selected, str)
            else let field.text = field.text.take(field.caret.position) + str + field.text.substring(field.caret.position)


    // Downloading lifecycle
    downloads = downloadSequence / cancelCommand

    // Defines how the file selection happens
    selectFile = // Request the user to select a file. Store the result in a variable.
                 // A selection can be successful, or it can fail for certain reasons.
                 var selectionResult: FileChooser.Result.Value = null
                 @gui: let selectionResult = fileChooser.showSaveDialog(destinationFileLabel)

                 // If a user failed to select the location for some reasons then break
                 // from the script
                 if selectionResult != FileChooser.Result.Approve then break

                 // Otherwise, save his or her choice to the local variable and
                 // update the GUI correspondently
                 else @gui: let destinationFileField.text = fileChooser.selectedFile.getAbsolutePath


    // Defines how the file download happens
    downloadSequence = // Convert the string for of the URL to an object
                       // and the string of the file to an object
                       val url = new URL(sourceUrlField.text)
                       val destinationFile = new File(destinationFileField.text)

                       // Open an input stream, get the size of the file
                       var is = new BufferedInputStream(url.openStream())
                       var size: Int = is.available()

                       // Open an output stream - the one we'll write to
                       val os = new BufferedOutputStream(new FileOutputStream(destinationFile))

                       // Configure the progress bar
                       @gui: {!
                         progressBar.max   = size
                         progressBar.min   = 0
                         progressBar.value = 0
                       !}

                       // We will download the file one chunk at a time
                       val chunkSize = 8 * 1024

                       // This variable keeps track of how much bytes was read from an input stream
                       // normally, a chunkSize bytes will be read at a time, but when we're close to an end
                       // (less then one chunk remains), this number will be less. When we reach the end of the file,
                       // this number will be -1.
                       var read      = -1

                       // Keep track of how much bytes was already downloaded. This is needed solely for the progress bar.
                       var processed = 0


                       // Download loop does an actual downloading work
                       [
                         // Create a buffer to read the data to
                         var buff = new Array[Byte](chunkSize)

                         // Read the data from an input stream, from a new thread
                         {* read = is read buff *}

                         // If something was read, write that something to the destination file's
                         // output stream and update the GUI
                         if read != -1 then [
                           {* os.write(buff, 0, read) *}
                           let processed += read
                         ]

                         @gui: let progressBar.value = processed

                         // Repeat these steps until we reach the end of the stream
                         while (read != -1)
                       ]

                       // Clean-up: flush and close streams
                       {!
                         is.close()
                         os.flush()
                         os.close()
                       !}


}
