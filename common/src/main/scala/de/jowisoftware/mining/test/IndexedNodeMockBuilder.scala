package de.jowisoftware.mining.test

import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.mining.model.nodes.helper.MiningNode

class IndexedNodeMockBuilder[A <: MiningNode] private[test] (override val companion: IndexedNodeCompanion[A], name: String = "")(implicit context: MockContext) extends NodeMockBuilder[A](companion, name)