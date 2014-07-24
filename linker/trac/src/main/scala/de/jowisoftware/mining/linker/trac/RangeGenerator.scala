package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.model.nodes.Commit
import org.neo4j.graphdb.{Node => NeoNode}

trait RangeGenerator {
  def findRange(c1: Commit, c2: Commit): Set[Commit]
}
