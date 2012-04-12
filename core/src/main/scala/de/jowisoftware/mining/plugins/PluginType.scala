package de.jowisoftware.mining.plugins

object PluginType extends Enumeration {
  type PluginType = Value
  
  val SCM = Value("SCM")
  val Tickets = Value("Tickets")
  
  def find(name: String) =
    try {
      this.withName(name)
    } catch {
      case e: Exception => throw new RuntimeException(
          "Not a valid plugin type: "+ name, e);
    }
}