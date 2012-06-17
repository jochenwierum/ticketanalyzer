package de.jowisoftware.mining.importer.git.walker

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.AbstractTreeIterator

object DiffWalker {
  def createList(walk: TreeWalk): Map[String, String] = {
    if (!walk.next) {
      Map()
    } else {
      val entry = (walk.getPathString -> identifyChange(walk))
      createList(walk) + entry
    }
  }

  private def identifyChange(walk: TreeWalk): String = {
    val currentTreeHasId = walk.getTree(0, classOf[AbstractTreeIterator]) != null
    val otherTrees = for (i <- 1 until walk.getTreeCount) yield walk.getTree(i, classOf[AbstractTreeIterator]) != null
    val oneOtherHasId = otherTrees.exists(identity)

    if (!currentTreeHasId && oneOtherHasId) {
      "D"
    } else if (currentTreeHasId && oneOtherHasId) {
      "M"
    } else {
      "A"
    }
  }
}