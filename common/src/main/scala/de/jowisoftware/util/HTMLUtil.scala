package de.jowisoftware.util

object HTMLUtil {
  def mask(s: String) =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
}