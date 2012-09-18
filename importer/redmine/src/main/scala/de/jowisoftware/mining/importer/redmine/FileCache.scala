package de.jowisoftware.mining.importer.redmine

import java.io.{ DataInputStream, DataOutputStream, EOFException, File, FileInputStream, FileOutputStream }

import scala.xml._

class FileCache {
  private val tmpFile = File.createTempFile("ta-redmine-import", "tmp")

  private val outputStream = new FileOutputStream(tmpFile)
  private val cacheFile = new DataOutputStream(outputStream)

  def addChunk(xml: Elem) {
    val xmlAsString = Utility.toXML(xml,
      stripComments = false,
      decodeEntities = true,
      preserveWhitespace = false,
      minimizeTags = false).toString
    cacheFile.writeUTF(xmlAsString)
  }

  def readChunks(): Stream[Elem] = {
    cacheFile.close()

    val inputStream = new FileInputStream(tmpFile)
    val stream = new DataInputStream(inputStream)

    def nextElement(): Stream[Elem] =
      try {
        val text = stream.readUTF()
        val doc = XML.load(Source.fromString(text))
        doc #:: nextElement()
      } catch {
        case e: EOFException =>
          stream.close()
          tmpFile.delete()
          Stream.empty
      }

    nextElement
  }
}