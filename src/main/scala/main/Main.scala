package main

import syntax._
import parser._
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object Main extends App {

  if (args.length < 1) {
    println("Bad command, please supply a filename.")
    sys.exit(1)
  }

  val file: File = new File(args(0))

  if (!file.exists) {
    println(s"File '${file.getAbsolutePath}' does not exist.")
    sys.exit(1)
  }

  if (file.isDirectory) {
    println(s"File '${file.getAbsolutePath}' is a directory.")
    sys.exit(1)
  }

  try {

    var stream: FileInputStream = new FileInputStream(file)
    val parserResult: ParserResult = Parser.parseStream(stream)
    stream.close()
    parserResult match {
      case ParserSuccess(proc, revNames, nextName) =>
        println("Parsed:")
        println(proc pstr (revNames map (_.swap)))
      case SyntaxErrors(errors) => {
        (SyntaxErrors(errors) toStringWithText file) foreach { e => println(e) }
        sys.exit(1)
      }
    }

  } catch {

    case ioe: IOException => {
      println("IO error.")
      sys.exit(1)
    }

    case e: Exception => {
      println(s"Unknown error: ${e.getClass.getName}")
      e.printStackTrace
      sys.exit(1)
    }
  }
}
