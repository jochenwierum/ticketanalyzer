package de.jowisoftware.util
import java.io.File
import java.util.regex.Pattern
import java.io.FilenameFilter

object FileUtils {
  def delTree(dir: File) = {
    if (dir.exists()) {
      require(dir.isDirectory())

      def delTree(dir: File) {
        for (file <- dir.listFiles())
          if (file.isDirectory())
            delTree(file)
          else
            file.delete()
        dir.delete()
      }
      delTree(dir)
    }
  }

  def expandPath(base: File, path: String): List[File] = {
    val segments = path.split("""[\\/]+""").toList

    def appendMatchingPathes(base: File, segments: List[String]): List[File] =
      segments match {
        case Nil => base :: Nil
        case head :: tail =>
          if (head.equals("..")) {
            appendMatchingPathes(base.getParentFile(), tail)
          } else if (!head.contains('*')) {
            val file = new File(base, head).getCanonicalFile
            if (file.exists)
              appendMatchingPathes(file, tail)
            else
              Nil
          } else {
            val pattern = Pattern.quote(head)
              .replaceAll("\\?", "\\\\E.\\\\Q")
              .replaceAll("\\*", "\\\\E.*\\\\Q")
              .replaceAll("\\\\Q\\\\E", "")
            val regex = Pattern.compile("^"+pattern+"$", Pattern.CASE_INSENSITIVE)
            val files = base.listFiles(new FilenameFilter() {
              def accept(dir: File, name: String) = {
                regex.matcher(name).find
              }
            })
            if (files != null) {
              files.flatMap { f =>
                appendMatchingPathes(f, tail)
              }.toList
            } else {
              Nil
            }
          }
      }

    appendMatchingPathes(base, segments)
  }
}