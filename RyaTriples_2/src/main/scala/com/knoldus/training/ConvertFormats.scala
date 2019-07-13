package com.knoldus.training

import java.io.{File, PrintWriter, FileInputStream}

import org.eclipse.rdf4j.rio.{RDFFormat, Rio}

object ConvertFormats {

  import java.io.InputStream
  def main(args: Array[String]): Unit = {

    val filename = args(0)
    val inputFormatStr = args(1)
    val outputFormatStr = args(2)
    val outputFile = args(3)

    //val input: InputStream = getClass.getResourceAsStream("/" + filename)
    //val input: InputStream = getClass.getResourceAsStream(filename)
    val input: InputStream = new FileInputStream(filename)

    val inputFormat=stringToFormat(inputFormatStr)
    val outputFormat=stringToFormat(outputFormatStr)
    // Rio also accepts a java.io.Reader as input for the parser.

    val model = Rio.parse(input, "", inputFormat)

    val pw = new PrintWriter(new File(outputFile))
    Rio.write(model, pw, outputFormat)
  }

  def stringToFormat(formatStr: String): RDFFormat = {
    if (formatStr=="TURTLE") { RDFFormat.TURTLE }
    else if (formatStr=="NTRIPLES") { RDFFormat.NTRIPLES }
    else { RDFFormat.RDFXML }
  }
}
