package de.jowisoftware.mining.importer.mantis

object Main {
  def main(args: Array[String]) = {
    org.apache.log4j.BasicConfigurator.configure()
    new MantisImporter().importAll(
      Map(
        //"url" -> "http://www.mantisbt.org/bugs/api/soap/mantisconnect.php",
        "url" -> "http://192.168.0.1/mantis/api/soap/mantisconnect.php",
        "username" -> "administrator",
        "password" -> "test",
        "project" -> "1"),
      null)
  }
}