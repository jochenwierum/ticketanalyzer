package de.jowisoftware.mining.analyzer.workflow

import java.io.File
import scala.io.Source
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.collection.mutable.Buffer
import javax.imageio.ImageIO

class DotWrapper(dotPath: File) {

  def run(input: String) = {
    val process = startProcess

    writeDot(input, process)
    val image = ImageIO.read(process.getInputStream)

    process.waitFor
    image
  }

  private def writeDot(input: String, process: Process) {
    val stdIn = process.getOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stdIn))
    writer.write(input)
    writer.close
  }

  private def startProcess: Process = {
    val builder = new ProcessBuilder
    builder.command(dotPath.getCanonicalPath, "-Tpng")
    val process = builder.start
    process
  }
}