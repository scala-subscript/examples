package subscript.twitter.api

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Created by anatolii on 11/27/14.
 */
@RunWith(classOf[JUnitRunner])
class TwitterSpec extends FlatSpec with Matchers {

  "Twitter" should "initialize without an error" in {
    val t = Twitter()
    t.token should not be 'empty
  }

  it should "implement search by tweets" in {
    val t = Twitter()
    t.search("cats") should not be 'empty
  }

  it should "query the remaining amount of method calls for the current time window" in {
    val t = Twitter()
    t.remainingOnMethod("search") should be >= 0
  }

}
