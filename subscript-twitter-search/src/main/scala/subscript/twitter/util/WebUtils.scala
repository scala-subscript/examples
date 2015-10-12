package subscript.twitter.util

import java.io.{BufferedInputStream, BufferedOutputStream, InputStream}
import java.net.{HttpURLConnection, URL, URLEncoder}

import org.apache.commons.codec.binary.Base64

import scala.collection.mutable.ArrayBuffer


object WebUtils {

  def urlEncode(str: String): String = URLEncoder.encode(str, "utf8")

  def base64Encode(str: String): String = Base64.encodeBase64String(str.getBytes("utf8"))


  private def withRequest[R](url: String, method: String)(headers: (String, String)*)(job: HttpURLConnection => R): R = {
    // Create the connection
    val con = new URL(url).openConnection().asInstanceOf[HttpURLConnection]

    // Configure it
    for ((name, value) <- headers) con.setRequestProperty(name, value)
    con.setRequestMethod(method)
    con.setDoInput(true)
    con.setDoOutput(true)

    // Do the job
    job(con)
  }

  private def readStream(input: InputStream): Array[Byte] = {
    val is = new BufferedInputStream(input)

    val arr = new ArrayBuffer[Byte]()

    val buffer = new Array[Byte](8 * 1024)
    var read = -1

    do {
      read = is.read(buffer)
      if (read != -1) arr ++= buffer take read
    } while (read != -1)

    is.close()
    arr.toArray
  }


  def post(url: String)(headers: (String, String)*)(body: Array[Byte]): Array[Byte] = withRequest(url, "POST")(headers: _*) {con =>
    // Write the body
    val os = new BufferedOutputStream(con.getOutputStream)
    os write body
    os.flush()
    os.close()

    // Read the output
    readStream(con.getInputStream)
  }

  def get(url: String)(headers: (String, String)*): Array[Byte] = withRequest(url, "GET")(headers: _*) {con =>
    readStream(con.getInputStream)
  }

}
