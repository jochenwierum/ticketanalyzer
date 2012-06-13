package de.jowisoftware.mining.plugins

object PluginType extends Enumeration {
  val SCM = Value("SCM")
  val Tickets = Value("Tickets")
  val Linker = Value("Linker")

  def find(name: String) =
    try {
      this.withName(name)
    } catch {
      case e: Exception =>
        throw new RuntimeException(
          "Not a valid plugin type: "+name, e)
    }

  def types = SCM :: Tickets :: Linker :: Nil
}
