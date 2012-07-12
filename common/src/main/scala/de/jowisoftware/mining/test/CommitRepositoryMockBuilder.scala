package de.jowisoftware.mining.test

import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.mining.model.nodes.Commit

class CommitRepositoryMockBuilder private[test] (withAbbrev: Boolean, val name: String = "")(implicit context: MockContext) extends NodeMockBuilder[CommitRepository](CommitRepository, name) {
  addReadOnlyAttribute("supportsAbbrev", withAbbrev: java.lang.Boolean)

  def addCommit(id: String) = {
    val builder = new NodeMockBuilder(Commit, "commit")
    builder.addReadOnlyAttribute("id", id)
    builder.finishMockNode
  }
}