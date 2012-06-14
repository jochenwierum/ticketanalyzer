package de.jowisoftware.mining.importer.mantis

import scala.xml.Elem

abstract sealed class SoapResponse()

case class SoapResult(xml: Elem) extends SoapResponse
case class SoapError(code: String, message: String) extends SoapResponse