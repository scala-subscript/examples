package subscript.twitter.app.view

import subscript.twitter.api.Tweet

import scala.swing.BorderPanel.Position
import scala.swing.ScrollPane.BarPolicy
import scala.swing._

class View extends SimpleSwingApplication {

  val searchField = new TextField
  val statusLabel = new Label
  val tweetList    = new ListView[Tweet] {
    renderer = new TweetRenderer
  }
  val scrollPane  = new ScrollPane(tweetList) {
    horizontalScrollBarPolicy = BarPolicy.AsNeeded
    verticalScrollBarPolicy   = BarPolicy.Always
  }

  def top = new MainFrame {

    title         = "Twitter Search"
    bounds        = new Rectangle(300, 100, 400, 300)
    preferredSize = new Dimension(bounds.width, bounds.height)

    contents = new BorderPanel {
      layout(searchField) = Position.North
      layout(scrollPane ) = Position.Center
      layout(statusLabel) = Position.South
    }
    setStatus("initialized")
  }
  def setError (s: String ): Unit = {setTweets(Nil); setStatus(s)}
  def setStatus(s: String ): Unit = statusLabel.text=s
  def setTweets(ts: Seq[Tweet]): Unit = {setStatus(s"${ts.size} tweets"); tweetList.listData = ts}

}
