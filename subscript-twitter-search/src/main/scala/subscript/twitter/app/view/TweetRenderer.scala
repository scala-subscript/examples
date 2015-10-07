package subscript.twitter.app.view

import subscript.twitter.api.Tweet

import scala.swing.ListView.Renderer
import scala.swing._

/**
 * Created by anatolii on 11/28/14.
 */
class TweetRenderer extends Renderer[Tweet] {
  override def componentFor(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Tweet, index: Int): Component = new BoxPanel(Orientation.Vertical) {
    val model: Tweet = list.listData(index).asInstanceOf[Tweet]

    val text   = new TextArea(s"${model.username} at ${model.date}:\n${model.text}\n") {
      editable = false
    }

    contents += text
  }
}
