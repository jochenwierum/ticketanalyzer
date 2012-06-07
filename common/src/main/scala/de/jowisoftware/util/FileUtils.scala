package de.jowisoftware.util
import java.io.File

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
}