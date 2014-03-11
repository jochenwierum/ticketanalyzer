package de.jowisoftware.util

object HTMLUtil {
  def mask(s: String) =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")

  def stripHTML(s: String) =
    s.replaceAll("<[^>]+>", "")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .replace("&amp;", "&")
}