package de.jowisoftware.mining.plugins

object PluginType extends Enumeration {
  val SCM = Value("SCM")
  val ITS = Value("ITS")
  val Linker = Value("Linker")
  val Analyzer = Value("Analyzer")

  def find(name: String) =
    try {
      this.withName(name)
    } catch {
      case e: Exception =>
        throw new RuntimeException(
          "Not a valid plugin type: "+name, e)
    }
}
