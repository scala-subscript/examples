package subscript.twitter.util

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Created by anatolii on 11/27/14.
 */
@RunWith(classOf[JUnitRunner])
class WebUtilsSpec extends FlatSpec with Matchers {

  "WebUtils" should "url-encode strings" in new DummyCredentials {
    WebUtils.urlEncode(key)    shouldBe keyUrlEnc
    WebUtils.urlEncode(secret) shouldBe secretUrlEnc
  }

  it should "encode strings in Base64" in new DummyCredentials {
    WebUtils.base64Encode(credentials) shouldBe credentialsBase64
  }

  it should "issue https post requests" in new RealCredentials {
    val encoded = WebUtils base64Encode s"${WebUtils urlEncode key}:${WebUtils urlEncode secret}"
    val resp = WebUtils.post("https://api.twitter.com/oauth2/token")(
      "Authorization" -> s"Basic $encoded"
    ) {
      "grant_type=client_credentials".getBytes
    }

    new String(resp, "utf8") should fullyMatch regex """^\{"token_type":"bearer","access_token":".+"\}$"""
  }

  it should "issue https get requests" in {
    val url = "https://api.audioboo.fm/users/12/audio_clips/followed"
    val resp = WebUtils.get(url)()
    val sResp = new String(resp, "utf8")

    sResp should startWith regex """^\{"window":"""
    sResp should endWith   regex """"image_attachment":\d+\}\]\}\}$"""
  }


  trait DummyCredentials {
    val key    = "xvz1evFS4wEEPTGEFPHBog"
    val secret = "L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg"

    val keyUrlEnc    = key
    val secretUrlEnc = secret

    val credentials        = "xvz1evFS4wEEPTGEFPHBog:L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg"
    val credentialsBase64 = "eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw=="
  }

  trait RealCredentials {
    val key         = "v7WlYvTfqBhk5LjQOWJuNuusK"
    val secret      = "zJCZ5ZMXuEmtoDh6dIZFp0MWgzden0Tnm5LMOowSrKFgd1izIJ"
    val credentials = s"$key:$secret"
  }

}
