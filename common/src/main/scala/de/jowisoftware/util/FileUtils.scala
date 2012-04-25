package de.jowisoftware.util
import java.io.File

object FileUtils {
  def delTree(path: String) = {
    val dir = new File(path)
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
}