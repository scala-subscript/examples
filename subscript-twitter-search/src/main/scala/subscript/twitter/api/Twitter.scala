package subscript.twitter.api

import java.util.logging.Logger

import scala.util.parsing.json.JSON


/**
 * Created by anatolii on 11/27/14.
 */
object Twitter {
  import subscript.twitter.util.WebUtils._

  val defaultKey    = "v7WlYvTfqBhk5LjQOWJuNuusK"
  val defaultSecret = "zJCZ5ZMXuEmtoDh6dIZFp0MWgzden0Tnm5LMOowSrKFgd1izIJ"

  val authUrl = "https://api.twitter.com/oauth2/token"

  def apply(key: String, secret: String): Twitter = {
    // According to the doc: https://dev.twitter.com/oauth/application-only

    // Step 1: Encode the key and the secret
    // URL-Encoding the key and the secret
    val keyEnc    = urlEncode(key   )
    val secretEnc = urlEncode(secret)

    // Concatenate them into a single string
    val credentials = s"$keyEnc:$secretEnc"

    // Encode credentials in Base64
    val credentialsEnc = base64Encode(credentials)


    // Step 2: Obtain a bearer token
    val response = post(authUrl)(
      "Authorization" -> s"Basic $credentialsEnc",
      "Content-Type"  -> "application/x-www-form-urlencoded;charset=UTF-8"
    ) {"grant_type=client_credentials" getBytes "utf8"}


    // Step 3: Parse the response and obtain the token
    val jResp = JSON.parseFull(new String(response, "utf8")).get.asInstanceOf[Map[String, Any]]
    val token = jResp("access_token").asInstanceOf[String]
    Logger.getGlobal.info(s"Obtained token during Twitter creation: $token")

    new Twitter(token)
  }

  def apply(): Twitter = apply(defaultKey, defaultSecret)

}

class Twitter(val token: String) {
  import subscript.twitter.util.WebUtils._

  val defaultCount = 200

  /**
   * A generic way to call the Twitter API methods.
   * @param url a REST URL to the API method
   * @param params the URL parameters of the GET query
   * @return a JSON Object (Map[String, Any]) or a JSON Array (List[Any]) representing the server response
   */
  def method(url: String)(params: (String, String)*): Any = {
    val urlWithParams =
      params.foldLeft(s"$url?") {case (url, (key, value)) => s"$url&${urlEncode(key)}=${urlEncode(value)}"}

    val resp = get(urlWithParams)("Authorization" -> s"Bearer $token")
    val respString = new String(resp, "utf8")

    Logger.getGlobal.info(s"Queried method $urlWithParams. Got response: ${respString.takeRight(100)}")

    JSON.parseFull(respString).get
  }

  def search(query: String, count: Int = defaultCount): List[Tweet] = {
    Logger.getGlobal.info(s"search start: $query")
    var resultCount = -1
    
    try {
	    val resp = method("https://api.twitter.com/1.1/search/tweets.json")(
	      "q"     -> query,
	      "count" -> s"$count"
	    ).asInstanceOf[Map[String, Any]]
	
	    val result = resp("statuses").asInstanceOf[List[Map[String, Any]]].map {tweet =>
	      val text     = tweet("text"      ).asInstanceOf[String]
	      val username = tweet("user"      ).asInstanceOf[Map[String, Any]]("screen_name").asInstanceOf[String]
	      val date     = tweet("created_at").asInstanceOf[String]
	
	      val t = Tweet(username, text, date)
	      Logger.getGlobal.info(s"Parsed tweet: $t")
	      t
	    }
	    resultCount = result.length
	    result
    }
    //catch {
    //  case e:Throwable => e.printStackTrace
    //  Nil
    //}
    finally {
	    Logger.getGlobal.info(s"search end: $query #tweets: $resultCount")
    }
  }

  def remainingOnMethod(q: String): Int =
    method("https://api.twitter.com/1.1/application/rate_limit_status.json")("resources" -> q)
      .asInstanceOf[Map[String, Any]]("resources")
      .asInstanceOf[Map[String, Any]](q)
      .asInstanceOf[Map[String, Any]].toList.head._2
      .asInstanceOf[Map[String, Any]]("remaining")
      .asInstanceOf[Double].toInt

}
