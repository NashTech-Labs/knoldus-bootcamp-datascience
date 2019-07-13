package com.knoldus.training

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

object LoadRdf {

  def main(args: Array[String]): Unit = {
      val resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ntriples.ntrips")
      val url = new URL("http://localhost/web.rya/loadrdf" +
        "?format=N-Triples" +
        "")
      val urlConnection = url.openConnection()
      urlConnection.setRequestProperty("Content-Type", "text/plain")
      urlConnection.setDoOutput(true)

      val os = urlConnection.getOutputStream

      var read: Int=0
      read=resourceAsStream.read
      while((read = resourceAsStream.read()).getClass==Unit && read >= 0) {
        os.write(read)
      }
      resourceAsStream.close()
      os.flush()

      val rd = new BufferedReader(new InputStreamReader(
        urlConnection.getInputStream()))
      var line: String= ""
      while ((line = rd.readLine())==Unit && line != null) {
        System.out.println(line)
      }
      rd.close()
      os.close()
  }
}
