package de.jowisoftware.mining.model.nodes.helper

import de.jowisoftware.neo4j.content.Node

trait MiningNode extends Node {
  final def id = content.getId
}
